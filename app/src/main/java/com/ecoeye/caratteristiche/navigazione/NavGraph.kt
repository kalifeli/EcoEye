package com.ecoeye.caratteristiche.navigazione

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ecoeye.caratteristiche.bluetooth.AudioRecorderManager
import com.ecoeye.caratteristiche.bluetooth.BluetoothViewModel
import com.ecoeye.caratteristiche.comunicazione.MqttViewModel
import com.ecoeye.ui.schermate.HomeScreen
import com.ecoeye.ui.schermate.NearbyDevicesScreen

@Composable
fun NavGraph(){
    val context = LocalContext.current
    val navController = rememberNavController()
    val bluetoothViewModel: BluetoothViewModel = viewModel()
    val mqttViewModel: MqttViewModel = viewModel()
    val audioRecorderManager = AudioRecorderManager(context)

    NavHost(navController = navController, startDestination = Schermate.Home.rotta ){
        composable(route = Schermate.Home.rotta) { HomeScreen(navController, bluetoothViewModel, mqttViewModel,audioRecorderManager) }
        composable(route = Schermate.RicercaDispositivi.rotta) { NearbyDevicesScreen(
            bluetoothViewModel = bluetoothViewModel,
            navController = navController
        ) }
    }
}