package com.ecoeye.caratteristiche.comunicazione.wifi

/**
 * Enum delle frasi rapide per la comunicazione quotidiana,
 * pensate per facilitare la conversazione con persone anziane.
 *
 * @property messaggio La stringa testuale da inviare.
 * @property isQuestion Indica se la frase è una domanda (true) o una frase di cortesia/saluto (false).
 */
enum class QuickMessage(
    val messaggio: String,
    val isQuestion: Boolean
) {
    // Domande comuni
    COME_STAI("Come stai?", true),
    COME_TI_SENTI_OGGI("Come ti senti oggi?", true),
    CHE_FAI("Che fai?", true),
    COSA_HAI_MANGIATO("Cosa hai mangiato oggi?", true),
    HAI_FAME("Hai fame?", true),
    VUOI_BERE("Vuoi qualcosa da bere?", true),
    VUOI_RIPOSARE("Vuoi riposare un po'?", true),
    VUOI_FARE_PASSEGGIATA("Andiamo a fare una passeggiata?", true),
    POSSO_AIUTARTI("Posso aiutarti in qualcosa?", true),
    TI_SERVE_QUALCOSA("Ti serve qualcosa?", true),

    // Frasi di cortesia e saluti
    BUONGIORNO("Buongiorno!", false),
    BUON_APPETITO("Buon appetito!", false),
    GRAZIE("Grazie mille", false),
    AUGURI("Auguri!", false),
    BUONANOTTE("Buonanotte!", false),
    A_PRESTO("A presto!", false),
    RITORNO_SUBITO("Torno subito", false),
    SALUTO("Ciao!", false)
}
