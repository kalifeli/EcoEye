package com.ecoeye.caratteristiche.comunicazione.wifi

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.amazonaws.auth.CognitoCachingCredentialsProvider
import com.amazonaws.mobileconnectors.iot.AWSIotMqttClientStatusCallback
import com.amazonaws.mobileconnectors.iot.AWSIotMqttManager
import com.amazonaws.mobileconnectors.iot.AWSIotMqttQos
import com.amazonaws.regions.Regions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.util.UUID

/**
 * ViewModel responsabile della comunicazione MQTT con AWS IoT Core.
 *
 * Gestisce:
 *  - la connessione sicura via Cognito Identity Pool (WebSocket/TLS)
 *  - lo stato di connessione (_isConnected)
 *  - la pubblicazione di messaggi JSON
 *  - il controllo del flusso di registrazione audio (start/stop)
 *
 * @param application Application context utilizzato per inizializzare Cognito
 */
class MqttViewModel(
    application: Application,
): AndroidViewModel(application) {

    /**
     * Provider Cognito per ottenere credenziali IAM temporanee
     * Necessario per autenticarsi con AWS IoT Core via WebSocket.
     */
    private val credentialProvider = CognitoCachingCredentialsProvider(
        application,
        "eu-central-1:9b701385-b372-47ea-8cc2-8189d77a2b5f",
        Regions.EU_CENTRAL_1
    )

    /**
     * Endpoint MQTT di AWS IoT Core (awsiot...amazonaws.com)
     */
    private val endpoint = "a2f2bxcm4j6lao-ats.iot.eu-central-1.amazonaws.com"

    /**
     * Client ID univoco per la sessione MQTT, generato una sola volta.
     */
    private val clientID = UUID.randomUUID().toString()

    /**
     * Client ID univoco per la sessione MQTT, generato una sola volta.
     */
    private val AWS_IOT_SUBSCRIBE_TOPIC = "esp32/trascrizione/out"

    /**
     * Manager AWS IoT MQTT, configurato con clientID ed endpoint.
     */
    private val mqttManager = AWSIotMqttManager(clientID, endpoint)

    /**
     * Stato di connessione MQTT: true se connesso, false altrimenti.
     */
    private val _isConnectedToAWS = MutableStateFlow(false)
    val isConnectedToAWS = _isConnectedToAWS

    /**
     * Flag che indica se la registrazione audio è in corso.
     */
    private val _recording: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val recording: StateFlow<Boolean> = _recording

    init {
        connectAWS()
    }

    /**
     * Avvia la registrazione audio delegando al manager fornito.
     * Aggiorna lo stato _recording a true.
     *
     * @param audioRecorderManager Implementazione di AudioRecorderController
     */
    fun startRecording(audioRecorderManager: AudioRecorderManager){
        try {
            _recording.value = true
            audioRecorderManager.startRecording()
        }catch (e: Exception){
            Log.e("BluetoothViewModel", "errore nella registrazione:", e)
        }
    }

    /**
     * Ferma la registrazione audio e resetta lo stato _recording.
     * Viene eseguito in un coroutine scope per operazioni asincrone.
     *
     * @param audioRecorderManager Implementazione di AudioRecorderController
     */
    fun stopRecording(audioRecorderManager: AudioRecorderManager){
        viewModelScope.launch {
            try {
                audioRecorderManager.stopRecording()
                _recording.value = false
                Log.d("BluetoothViewModel", "stopRecording")
            }catch (e:Exception) {
                Log.e("BluetoothViewModel", "errore nel caricamento:", e)
            }
        }
    }

    /**
     * Stabilisce la connessione MQTT con AWS IoT Core usando Cognito per l'autenticazione.
     * Aggiorna `_isConnected` in base allo stato di connessione.
     */
    fun connectAWS(){
        mqttManager.connect(credentialProvider) { status, throwable ->
            when (status) {
                AWSIotMqttClientStatusCallback.AWSIotMqttClientStatus.Connected -> {
                    Log.i("MQTT","Connesso ad AWS IoT")
                    _isConnectedToAWS.value = true
                }
                AWSIotMqttClientStatusCallback.AWSIotMqttClientStatus.ConnectionLost -> {
                    Log.w("MQTT","Connessione persa", throwable)
                    _isConnectedToAWS.value = false
                }
                AWSIotMqttClientStatusCallback.AWSIotMqttClientStatus.Reconnecting -> {
                    Log.i("MQTT","Riconnessione in corso")
                    _isConnectedToAWS.value = false
                }
                else -> {
                    Log.i("MQTT","Stato MQTT: $status")
                }
            }
        }
    }

    /**
     * Pubblica un JSON contenente la trascrizione sul topic MQTT.
     * Struttura payload: { "transcription": "testo" }
     *
     * @param testo Testo da inviare all'ESP32
     */
    fun publishAWS(testo: String){
        // Costruzione del payload JSON
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

    /**
     * Chiude la connessione MQTT quando il ViewModel viene distrutto.
     */
    override fun onCleared() {
        super.onCleared()
        try {
            mqttManager.disconnect()
        } catch (_: Exception) {}
    }
}