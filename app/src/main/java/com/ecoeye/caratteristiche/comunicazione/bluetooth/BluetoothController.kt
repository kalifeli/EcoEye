package com.ecoeye.caratteristiche.comunicazione.bluetooth

import kotlinx.coroutines.flow.StateFlow

interface BluetoothController{
    // Dispositivi scansionati
    val scannedDevices: StateFlow<List<DispositivoBLE>>
    // Dispositivi accoppiati
    val pairedDevies: StateFlow<List<DispositivoBLE>>

    fun startDiscovery()
    fun stopDiscovery()

    fun release()
}