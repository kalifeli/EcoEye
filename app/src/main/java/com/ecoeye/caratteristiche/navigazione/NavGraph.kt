package com.ecoeye.caratteristiche.navigazione

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ecoeye.caratteristiche.comunicazione.wifi.AudioRecorderManager
import com.ecoeye.caratteristiche.comunicazione.bluetooth.BluetoothViewModel
import com.ecoeye.caratteristiche.comunicazione.wifi.MqttViewModel
import com.ecoeye.ui.schermate.HomeScreen
import com.ecoeye.ui.schermate.NearbyDevicesScreen

/**
 * Grafo di navigazione Jetpack Compose per EcoEye.
 * Definisce le rotte e fornisce i ViewModel e il controller di registrazione audio alle schermate.
 */
@Composable
fun NavGraph(){
    val context = LocalContext.current

    val navController = rememberNavController()

    // ViewModel per funzionalità Bluetooth e MQTT
    val bluetoothViewModel: BluetoothViewModel = viewModel()
    val mqttViewModel: MqttViewModel = viewModel()

    // Controller audio tramite interfaccia per migliore testabilità
    val audioRecorderManager = AudioRecorderManager(context)

    NavHost(navController = navController, startDestination = Schermate.Home.rotta ){
        // Schermata principale con trascrizione e registrazione
        composable(route = Schermate.Home.rotta) { HomeScreen(navController, mqttViewModel,audioRecorderManager) }
        // Schermata di ricerca dispositivi Bluetooth
        composable(route = Schermate.RicercaDispositivi.rotta) { NearbyDevicesScreen(
            bluetoothViewModel = bluetoothViewModel,
            navController = navController
        ) }
    }
}