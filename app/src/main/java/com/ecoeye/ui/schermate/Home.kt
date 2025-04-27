@file:OptIn(ExperimentalMaterial3Api::class)

package com.ecoeye.ui.schermate

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.ecoeye.caratteristiche.comunicazione.wifi.AudioRecorderManager
import com.ecoeye.caratteristiche.comunicazione.wifi.MqttViewModel
import com.ecoeye.caratteristiche.comunicazione.wifi.QuickMessage
import com.ecoeye.caratteristiche.navigazione.Schermate
import com.ecoeye.util.isInternetAvailable
import com.example.ecoeye.R

/**
 * Schermata Home in cui l'utente può comunicare con il dispositivo EcoEye tramite voce o messaggi istantanei.
 * @param navController, controller per gestire la navigazione tra la schermata Home e NearbyDevicesScreen
 * @param mqttViewModel viewModel che fornisce funzionalità per iniziare e interrompere la registrazione o
 * permettere l'invio di messaggi rapidi
 * @param audioRecorderManager classe che fornisce tutti i servizi necessari per sfruttare il microfono integrato del dispositivo
 * , registrare, salvare la registrazione e caricarla nel bucket S3
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    mqttViewModel: MqttViewModel,
    audioRecorderManager: AudioRecorderManager,
    context: Context
){
    // Variabili di stato per gestire: lo stato della registrazione del parlato,
    // la connessione al broker MQTT e ad internet
    val recording by mqttViewModel.recording.collectAsState()
    val isConnetedToAWS by mqttViewModel.isConnectedToAWS.collectAsState()

    val isConnected = remember { mutableStateOf(isInternetAvailable(context)) }

    Box(modifier = Modifier.fillMaxSize()) {
        // Immagine di sfondo
        Image(
            painter = painterResource(id = R.drawable.background_1),
            contentDescription = "Background",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = "EcoEye",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                        )
                    },
                    actions = {
                        IconButton(
                            onClick = { navController.navigate(Schermate.RicercaDispositivi.rotta) },
                            colors = IconButtonDefaults.iconButtonColors(
                                contentColor = Color.Blue
                            )
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.dispositivi_bluetooth),
                                contentDescription = "",
                                tint = Color.Black
                            )
                        }
                    },
                    colors = androidx.compose.material3.TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = Color.Black,
                        actionIconContentColor = Color.Black
                    )
                )
            }
        ) { paddingValues ->

            if(!isConnected.value || !isConnetedToAWS){
                NoInternetScreen {
                    isConnected.value = isInternetAvailable(context)
                    mqttViewModel.connectAWS()
                }
            }else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        MicButton(
                            modifier = Modifier,
                            viewModel = mqttViewModel,
                            audioRecorderManager = audioRecorderManager,
                            recording = recording,
                            isConnected = isConnetedToAWS
                        )

                        Text(
                            text = "Messaggi Rapidi",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            modifier = Modifier
                                .padding(vertical = 8.dp, horizontal = 24.dp)
                        )

                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                            thickness = 2.dp,
                            color = Color.Gray
                        )

                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            modifier = Modifier
                                .padding(12.dp),
                            contentPadding = PaddingValues(8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Sezione domande rapide
                            items(QuickMessage.entries) { quickMessage ->
                                if(quickMessage.isQuestion)RapidMessage(messaggioRapido = quickMessage, viewModel = mqttViewModel)
                            }
                            // Sezione risposte rapide
                            items(QuickMessage.entries) { quickMessage ->
                                if(!quickMessage.isQuestion)RapidMessage(messaggioRapido = quickMessage, viewModel = mqttViewModel)
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Questa funzione rappresenta un messaggio istantaneo visualizzabile nella UI come una Card.
 * Utile per facilitare la comunicazione quando non è essenziale usufruire del microfono
 * @param messaggioRapido istanza della classe QuickMessage. Rappresenta un messaggio rapido da inviare alla non udente.
 * @param viewModel rappresenta la classe contenente la logica per la comunicazione MQTT.
 */
@Composable
fun RapidMessage(
    messaggioRapido: QuickMessage,
    viewModel: MqttViewModel,
){
    val isConnected by viewModel.isConnectedToAWS.collectAsState()
    val isRecording by viewModel.isConnectedToAWS.collectAsState()

    ElevatedCard(
        modifier = Modifier.padding(4.dp),
        elevation = CardDefaults.cardElevation(6.dp),
        shape = RoundedCornerShape(12.dp),
        enabled = isConnected && isRecording,
        colors = CardDefaults.elevatedCardColors(
            contentColor = Color.Black,
            containerColor = Color.White
        ),
        onClick = {
            if(isConnected){
                viewModel.publishAWS(messaggioRapido.messaggio)
            }
        }
    ){
        Text(
            text = messaggioRapido.messaggio,
            modifier = Modifier.padding(12.dp)
        )
    }
}

/**
 * Pulsante che permette di avviare o terminare la regisgtrazione del parlato.
 * Il primo click avvia la registrazione. Il secondo la termina.
 */
@Composable
fun MicButton(
    modifier: Modifier = Modifier,
    viewModel: MqttViewModel,
    audioRecorderManager: AudioRecorderManager,
    recording: Boolean,
    isConnected: Boolean
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .clip(CircleShape)
            .size(120.dp)
            .clickable {
                if (isConnected) {
                    if (!recording) {
                        // se non stiamo registrando, allora inizia la registrazione
                        viewModel.startRecording(audioRecorderManager)
                    } else {
                        // se stiamo registrando, allora interrompe la registrazione
                        viewModel.stopRecording(audioRecorderManager)
                    }
                }
            }
            .border(4.dp, if (recording) Color.Red else Color.White, CircleShape)
            .background(
                colorResource(id = R.color.DarkGreen),
                CircleShape
            )
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_microfono), // Assicurati di avere questo drawable
            contentDescription = "Microphone Icon",
            tint = Color.Black,
            modifier = Modifier.size(60.dp)
        )
    }
}

