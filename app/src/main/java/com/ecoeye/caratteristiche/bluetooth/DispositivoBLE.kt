package com.ecoeye.caratteristiche.bluetooth

data class DispositivoBLE(
    val nome: String?,
    val indirizzo: String,
    val rssi: Int // rappresenta l'intensità del segnale
)