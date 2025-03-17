package com.ecoeye.caratteristiche.bluetooth

import android.bluetooth.BluetoothDevice

data class DispositivoBLE(
    val nome: String?,
    val indirizzo: String,
    val rssi: Int, // rappresenta l'intensità del segnale
    val device: BluetoothDevice // oggetto necessario per la connessione al GATT
)