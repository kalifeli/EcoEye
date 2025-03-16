package com.ecoeye.caratteristiche.bluetooth

sealed class StatoBluetooth {
    data object Disconnesso: StatoBluetooth()
    data object ConnessioneInCorso: StatoBluetooth()
    data object Connesso: StatoBluetooth()
    data class ConnessioneFallita(val errore: String)
}