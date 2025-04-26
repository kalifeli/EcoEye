package com.ecoeye.caratteristiche.comunicazione.wifi

import android.annotation.SuppressLint
import android.content.Context
import android.media.MediaRecorder
import android.util.Log
import com.amplifyframework.core.Amplify
import java.io.File

/**
 * Gestisce la registrazione audio dal microfono del dispositivo e l'upload su AWS S3.
 *
 * Questa classe incapsula la logica di configurazione di MediaRecorder,
 * la gestione del file di output e l'invio asincrono del file audio tramite Amplify.
 *
 * @param context Context dell'applicazione, utilizzato per accedere alla directory filesDir.
 */
@SuppressLint("MissingPermission")
class AudioRecorderManager(
    private val context: Context
) {
    /**
     * Istanza di MediaRecorder utilizzata per catturare audio dal microfono.
     * Viene configurata al momento di startRecording() e rilasciata in stopRecording().
     */
    private var recorder: MediaRecorder? = null

    /**
     * File di output in cui vengono salvati i dati audio registrati.
     * Viene creato in startRecording() e utilizzato in stopRecording() per l'upload.
     */
    private var outputFile: File? = null

    /**
     * Avvia la registrazione audio.
     *
     * - Crea un file univoco in filesDir con suffisso .m4a
     * - Configura MediaRecorder con sorgente MIC, formato MPEG_4 e codifica AAC
     * - Prepara e avvia la cattura audio
     *
     * @throws IOException in caso di errore nella preparazione del recorder
     * @throws IllegalStateException in caso di chiamata in stato non valido
     */
    fun startRecording(){
        val file = File(context.filesDir, "file_${System.currentTimeMillis()}.m4a")
        outputFile = file
        recorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile(outputFile!!.absolutePath)
            prepare()
            start()
        }
    }

    /**
     * Termina la registrazione audio e avvia l'upload del file su S3.
     *
     * - Ferma e rilascia il MediaRecorder
     * - Verifica che il file sia stato inizializzato
     * - Chiama uploadFile() per inviare il file al bucket S3
     */
    fun stopRecording(){
        // Ferma e rilascia il recorder, se esiste
        recorder?.apply {
            stop()
            release()
        }
        recorder = null

        // Verifica che sia presente un file di output
        val file = outputFile
        if(file == null){
            Log.e("AudioRecorderManager", "stopRecording chiamato senza startRecording")
            return
        }

        // Upload asincrono del file audio
        outputFile?.let { uploadFile(it) }
    }

    /**
     * Carica il file audio specificato su AWS S3 utilizzando Amplify Storage.
     *
     * - Genera una chiave univoca nel bucket (prefisso "uploads/")
     * - Gestisce callback di successo ed errore con log
     *
     * @param outputFile File audio da caricare
     */
    private fun uploadFile(outputFile: File){
        val key = "uploads/${outputFile.name}"
        Amplify.Storage.uploadFile(
            key,
            outputFile,
            { Log.i("Amplify", "File audio caricato correttamente")},
            {Log.e("Amplify","Caricamento File fallito", it)}
        )
    }
}