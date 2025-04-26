package com.ecoeye.ui.schermate

import android.bluetooth.BluetoothDevice
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.ecoeye.caratteristiche.comunicazione.bluetooth.BluetoothConnectionState
import com.ecoeye.caratteristiche.comunicazione.bluetooth.BluetoothViewModel
import com.ecoeye.caratteristiche.comunicazione.bluetooth.DispositivoBLE
import com.example.ecoeye.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NearbyDevicesScreen(
    bluetoothViewModel: BluetoothViewModel,
    navController: NavController
) {
    // Dispositivi scansionati
    val scannedDevices by bluetoothViewModel.scannedDevices.collectAsState()
    // Stato della scansione
    val scanning by bluetoothViewModel.scanning.collectAsState()
    // Stato della connessione bluetooth
    val connectionState by bluetoothViewModel.connectionState.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.background_2),
            contentDescription = "Background",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("Dispositivi Vicini") },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Torna alla schermata Home"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = Color.Black,
                        actionIconContentColor = Color.Black
                    )
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(
                        border = BorderStroke(1.dp, Color.DarkGray),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Green,
                            contentColor = Color.White,
                            disabledContentColor = Color.DarkGray,
                            disabledContainerColor = Color.Gray
                        ),
                        enabled = !scanning,
                        onClick = {
                            bluetoothViewModel.startDiscovery()
                        }
                    ) {
                       // Text("Avvia Scansione")
                        Icon(imageVector = Icons.Filled.PlayArrow, contentDescription = "Inizia Scansione")
                    }
                    Button(
                        border = BorderStroke(1.dp, Color.DarkGray),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Green,
                            contentColor = Color.White,
                            disabledContentColor = Color.DarkGray,
                            disabledContainerColor = Color.Gray
                        ),
                        enabled = scanning,
                        onClick = { bluetoothViewModel.stopDiscovery() }
                    ) {
                        // Text("Ferma Scansione")
                        Icon(imageVector = Icons.Filled.Close, contentDescription = "Ferma Scansione")
                    }
                }

                if (scanning) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(
                                color = Color.Gray,
                                trackColor = Color.Black,
                                strokeCap = ProgressIndicatorDefaults.CircularIndeterminateStrokeCap
                            )
                            Text(
                                text = "Ricerca dispositivi in corso...",
                                color = Color.Black,
                                modifier = Modifier.padding(top = 16.dp)
                            )
                        }
                    }
                }

                Box(modifier = Modifier.fillMaxSize()) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        items(scannedDevices) { dispositivoBLE ->
                            CardDispositivoBLE(
                                dispositivoBLE = dispositivoBLE,
                                onClickConnect = {
                                    bluetoothViewModel.connectToDevice(dispositivoBLE.device)
                                },
                                onClickDisconnect = {
                                    bluetoothViewModel.disconnectDevice()
                                },
                                bluetoothConnectionState = connectionState
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CardDispositivoBLE(
    dispositivoBLE: DispositivoBLE,
    onClickConnect: (BluetoothDevice) -> Unit,
    onClickDisconnect: () -> Unit,
    bluetoothConnectionState: BluetoothConnectionState
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        colors = CardColors(
            contentColor = Color.Black,
            containerColor = Color.White,
            disabledContentColor = Color.Unspecified,
            disabledContainerColor = Color.Unspecified
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = dispositivoBLE.nome ?: "Unknown",
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Address: ${dispositivoBLE.indirizzo}", color = Color.DarkGray)
                Text(text = "Rssi: ${dispositivoBLE.rssi}", color = Color.DarkGray)
            }
            Button(
                onClick = {
                    if(bluetoothConnectionState == BluetoothConnectionState.DISCONNECTED){
                        onClickConnect(dispositivoBLE.device)
                    }else if(bluetoothConnectionState == BluetoothConnectionState.CONNECTED){
                        onClickDisconnect()
                    } },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Green,
                    contentColor = Color.Black,
                    disabledContainerColor = Color.Gray,
                    disabledContentColor = Color.DarkGray
                )
            ) {
                Text(text = if(bluetoothConnectionState == BluetoothConnectionState.DISCONNECTED) "Connect" else "Disconnect")
            }
        }
    }
}