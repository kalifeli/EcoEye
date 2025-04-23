package com.ecoeye.caratteristiche.bluetooth

import android.annotation.SuppressLint
import android.content.Context
import android.media.MediaRecorder
import android.util.Log
import com.amplifyframework.core.Amplify
import java.io.File

@SuppressLint("MissingPermission")
class AudioRecorderManager(
    private val context: Context
) {
    private var recorder: MediaRecorder? = null
    private var outputFile: File? = null

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

    fun stopRecording(){
        recorder?.apply {
            stop()
            release()
        }
        recorder = null
        val file = outputFile
        if(file == null){
            Log.e("AudioRecorderManager", "stopRecording chiamato senza startRecording")
            return
        }

        //caricamento file audio sul bucket S3
        outputFile?.let { uploadFile(it) }
    }

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