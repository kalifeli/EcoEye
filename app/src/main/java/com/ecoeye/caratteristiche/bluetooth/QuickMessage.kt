package com.ecoeye.caratteristiche.bluetooth

enum class QuickMessage(
    val messaggio: String,
    val isQuestion: Boolean
) {
    COME_STAI("Come stai?", true),
    CHE_FAI("Che fai?", true),
    COSA_HAI_MANGIATO("Cosa hai mangiato oggi?", true),
    BUONGIORNO("Buongiorno!", false),
    BUONANOTTE("Buonanotte!", false),
    GRAZIE("Grazie", false)
}
