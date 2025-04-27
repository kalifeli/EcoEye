package com.ecoeye.ui.schermate

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.ecoeye.R

/**
 * Visualizza una schermata che informa l'utente della mancanza di connessione Internet.
 *
 * Questa funzione composable viene utilizzata per visualizzare una schermata di avviso quando
 * non è disponibile una connessione Internet. Mostra un'immagine, un messaggio di testo e un
 * pulsante per riprovare a stabilire la connessione.
 *
 * @param onRetry Callback che viene eseguito quando l'utente preme il pulsante "Riprova".
 *                Questo permette di tentare nuovamente la connessione.
 */
@Composable
fun NoInternetScreen(
    onRetry: () -> Unit
){
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.background_1),
            contentDescription = "No Internet Connection",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(16.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Oops!",
                color =  Color.Black,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Sembra che non sei connesso ad Internet!",
                color = Color.Black ,
                style = MaterialTheme.typography.labelLarge
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Riprova ora.",
                color = Color.Black,
                modifier = Modifier.padding(horizontal = 16.dp),
                style = MaterialTheme.typography.labelLarge
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(
                    containerColor =  Color.Green,
                    contentColor =  Color.Black
                ),
                modifier = Modifier.padding(horizontal = 32.dp)
            ) {
                Text("Riprova")
            }
        }
    }
}


@Preview
@Composable
fun PreviewInternScreen(){
    NoInternetScreen {
        {}
    }
}