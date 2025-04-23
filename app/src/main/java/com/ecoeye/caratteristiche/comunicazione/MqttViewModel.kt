package com.ecoeye.caratteristiche.comunicazione

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import com.amazonaws.auth.CognitoCachingCredentialsProvider
import com.amazonaws.mobileconnectors.iot.AWSIotMqttClientStatusCallback
import com.amazonaws.mobileconnectors.iot.AWSIotMqttManager
import com.amazonaws.mobileconnectors.iot.AWSIotMqttQos
import com.amazonaws.regions.Regions
import kotlinx.coroutines.flow.MutableStateFlow
import org.json.JSONObject
import java.util.UUID

class MqttViewModel(
    application: Application,
): AndroidViewModel(application) {

    private val credentialProvider = CognitoCachingCredentialsProvider(
        application,
        "eu-central-1:9b701385-b372-47ea-8cc2-8189d77a2b5f",
        Regions.EU_CENTRAL_1
    )

    private val endpoint = "a2f2bxcm4j6lao-ats.iot.eu-central-1.amazonaws.com"

    // Creazione di un client ID univoco
    private val clientID = UUID.randomUUID().toString()

    private val AWS_IOT_SUBSCRIBE_TOPIC = "esp32/trascrizione/out"

    //Iot Mqtt manager
    val mqttManager = AWSIotMqttManager(clientID, endpoint)

    private val _isConnected = MutableStateFlow(false)
    val isConnected = _isConnected

    init {
        connectAWS()
    }

    /** Apre la connessione a AWS IoT e aggiorna _isConnected */
    fun connectAWS(){
        mqttManager.connect(credentialProvider) { status, throwable ->
            when (status) {
                AWSIotMqttClientStatusCallback.AWSIotMqttClientStatus.Connected -> {
                    Log.i("MQTT","Connesso ad AWS IoT")
                    _isConnected.value = true
                }
                AWSIotMqttClientStatusCallback.AWSIotMqttClientStatus.ConnectionLost -> {
                    Log.w("MQTT","Connessione persa", throwable)
                    _isConnected.value = false
                }
                AWSIotMqttClientStatusCallback.AWSIotMqttClientStatus.Reconnecting -> {
                    Log.i("MQTT","Riconnessione in corso")
                    _isConnected.value = false
                }
                else -> {
                    Log.i("MQTT","Stato MQTT: $status")
                }
            }
        }
    }

    /** Pubblica un testo su AWS IoT, attendendo la connessione se serve */
    fun publishAWS(testo: String){
        val json = JSONObject()
            .put("transcription", testo)
            .toString()

        try {
            mqttManager.publishString(
                json,
                AWS_IOT_SUBSCRIBE_TOPIC,
                AWSIotMqttQos.QOS0
            )
            Log.i("MQTT","Messaggio pubblicato: $json")
        } catch (e: Exception) {
            Log.e("MQTT","Errore publishString", e)
        }
    }

    /** Disconnette quando il ViewModel va in clear */
    override fun onCleared() {
        super.onCleared()
        try {
            mqttManager.disconnect()
        } catch (_: Exception) {}
    }
}