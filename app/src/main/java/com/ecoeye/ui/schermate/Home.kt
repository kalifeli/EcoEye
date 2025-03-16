@file:OptIn(ExperimentalMaterial3Api::class)

package com.ecoeye.ui.schermate

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.TopAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.ecoeye.caratteristiche.bluetooth.QuickMessage
import com.ecoeye.caratteristiche.navigazione.Schermate
import com.example.ecoeye.R

@Composable
fun HomeScreen(
    navController: NavController
){
    Box(modifier = Modifier.fillMaxSize()) {
        // Immagine di sfondo
        Image(
            painter = painterResource(id = R.drawable.background_1),
            contentDescription = "Background",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Scaffold sovrapposta, con background trasparente
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
            // Box per centrare l'intera colonna verticalmente e orizzontalmente
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // 1. Microfono
                    MicrophoneItem(
                        backgroundColor = Color.White,
                        iconTint = Color.Black,
                        size = 120
                    )

                    // 2. Titolo "Messaggi Rapidi"
                    Text(
                        text = "Messaggi Rapidi",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        modifier = Modifier
                            .padding(vertical = 8.dp, horizontal = 24.dp)
                    )

                    // 3. Divider
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                        thickness = 2.dp,
                        color = Color.Gray
                    )

                    // 4. Griglia dei messaggi
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier
                            .padding(12.dp),
                        contentPadding = PaddingValues(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(QuickMessage.entries) { quickMessage ->
                            RapidMessage(messaggioRapido = quickMessage)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MicrophoneItem(
    //onClick: () -> Unit,
    backgroundColor: Color = Color.White,
    iconTint: Color = Color.Black,
    size: Int = 120
){
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(size.dp)
            .border(3.dp, Color.Black, CircleShape)
            .background(color = backgroundColor, shape = CircleShape)
        //.clickable { onClick() }
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_microfono),
            contentDescription = "Microphone Icon",
            tint = iconTint,
            modifier = Modifier.size((size / 2).dp)
        )
    }

}

@Composable
fun RapidMessage(
    messaggioRapido: QuickMessage
){
    ElevatedCard(
        modifier = Modifier.padding(4.dp),
        elevation = CardDefaults.cardElevation(6.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.elevatedCardColors(
            contentColor = Color.Black,
            containerColor = Color.White
        )
    ){
        Text(
            text = messaggioRapido.messaggio,
            modifier = Modifier.padding(12.dp)
        )
    }

}

@Preview
@Composable
fun MicrophoneItemPreview(){
    MicrophoneItem()
}

@Preview
@Composable
fun RapidMessagePreview(){
    RapidMessage(messaggioRapido = QuickMessage.COME_STAI)
}