@file:OptIn(ExperimentalMaterial3Api::class)

package com.ecoeye.ui.schermate

import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.result.ActivityResultLauncher
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.ecoeye.caratteristiche.bluetooth.AudioRecorderManager
import com.ecoeye.caratteristiche.bluetooth.BluetoothViewModel
import com.ecoeye.caratteristiche.bluetooth.QuickMessage
import com.ecoeye.caratteristiche.comunicazione.MqttViewModel
import com.ecoeye.caratteristiche.navigazione.Schermate
import com.example.ecoeye.R
import java.util.Locale

@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: BluetoothViewModel,
    mqttViewModel: MqttViewModel,
    audioRecorderManager: AudioRecorderManager
){
    val recording by viewModel.recording.collectAsState()

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
                        viewModel = viewModel,
                        audioRecorderManager = audioRecorderManager,
                        recording
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
                        items(QuickMessage.entries) { quickMessage ->
                            RapidMessage(messaggioRapido = quickMessage, viewModel = mqttViewModel)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RapidMessage(
    messaggioRapido: QuickMessage,
    viewModel: MqttViewModel,
){
    val isConnected by viewModel.isConnected.collectAsState()

    ElevatedCard(
        modifier = Modifier.padding(4.dp),
        elevation = CardDefaults.cardElevation(6.dp),
        shape = RoundedCornerShape(12.dp),
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

@Composable
fun MicButton(
    modifier: Modifier = Modifier,
    viewModel: BluetoothViewModel,
    audioRecorderManager: AudioRecorderManager,
    recording: Boolean
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .clip(CircleShape)
            .size(120.dp)
            .clickable {
                if (!recording) {
                    // se non stiamo registrando, allora inizia la registrazione
                    viewModel.startRecording(audioRecorderManager)
                } else {
                    // se stiamo registrando, allora interrompe la registrazione
                    viewModel.stopRecording(audioRecorderManager)
                }
            }
            .border(4.dp, Color.White, CircleShape)
            .background(
                if (recording) Color.Cyan else colorResource(id = R.color.DarkGreen),
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

