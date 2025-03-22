package com.ecoeye.caratteristiche.navigazione

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ecoeye.caratteristiche.bluetooth.BluetoothViewModel
import com.ecoeye.ui.schermate.HomeScreen
import com.ecoeye.ui.schermate.NearbyDevicesScreen

@Composable
fun NavGraph(){
    val navController = rememberNavController()
    val bluetoothViewModel: BluetoothViewModel = viewModel()

    NavHost(navController = navController, startDestination = Schermate.Home.rotta ){
        composable(route = Schermate.Home.rotta) { HomeScreen(navController, bluetoothViewModel) }
        composable(route = Schermate.RicercaDispositivi.rotta) { NearbyDevicesScreen(
            bluetoothViewModel = bluetoothViewModel,
            navController = navController
        ) }
    }
}