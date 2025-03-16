package com.ecoeye.caratteristiche.navigazione

sealed class Schermate(val rotta: String) {
    object Home: Schermate("Schermata Home")
    object RicercaDispositivi: Schermate("Schermata Ricerca Dispsoitivi")
}