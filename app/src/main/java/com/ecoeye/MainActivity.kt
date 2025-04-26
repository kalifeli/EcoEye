package com.ecoeye

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.amplifyframework.AmplifyException
import com.amplifyframework.auth.cognito.AWSCognitoAuthPlugin
import com.amplifyframework.core.Amplify
import com.amplifyframework.storage.s3.AWSS3StoragePlugin
import com.ecoeye.caratteristiche.comunicazione.bluetooth.BluetoothViewModel
import com.ecoeye.caratteristiche.comunicazione.wifi.MqttViewModel
import com.ecoeye.caratteristiche.navigazione.NavGraph

private  const val REQUEST_CODE_PERMISSIONS = 1001

class MainActivity : ComponentActivity() {

    private lateinit var bluetoothViewModel: BluetoothViewModel
    private lateinit var mqttViewModel: MqttViewModel

    /**
     * Lista dei permessi necessari per le funzionalitò dell'applicazione.
     */
    private val requiredPermissions: Array<String> =
        arrayOf(
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_ADVERTISE,
            Manifest.permission.RECORD_AUDIO
        )

    /**
     * Funzione per il controllo dei permessi e la loro richiesta.
     */
    private fun checkAndRequestPermissions(){
        // Lista che contiene i permessi ancora non concessi
        val permissionsToRequest = requiredPermissions.filter { permission ->
            ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED
        }

        if(permissionsToRequest.isNotEmpty()){
            val showRationale = permissionsToRequest.any{ permission ->
                ActivityCompat.shouldShowRequestPermissionRationale(this, permission)
            }

            if (showRationale) {
                // Mostra una finestra di dialogo per spiegare all'utente il motivo della richiesta
                AlertDialog.Builder(this)
                    .setTitle("Permessi necessari")
                    .setMessage("L'app necessita dei seguenti permessi per funzionare correttamente.")
                    .setPositiveButton("OK") { _, _ ->
                        ActivityCompat.requestPermissions(
                            this,
                            permissionsToRequest.toTypedArray(),
                            REQUEST_CODE_PERMISSIONS
                        )
                    }
                    .setNegativeButton("Annulla") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .show()
            } else {
                // Richiedi direttamente i permessi senza mostrare una spiegazione
                ActivityCompat.requestPermissions(
                    this,
                    permissionsToRequest.toTypedArray(),
                    REQUEST_CODE_PERMISSIONS
                )
            }
        }else{
            Log.d("Permessi", "I permessi sono stati concessi" )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inizializza il ViewModel
        bluetoothViewModel = BluetoothViewModel(application)
        mqttViewModel = MqttViewModel(application)

        mqttViewModel.connectAWS()

        //Controllo e richiesta dei permessi necessari
        checkAndRequestPermissions()

        // COnfigurazione Amplify
        configureAmplify(application)

        setContent {
            NavGraph()
        }
    }

    override fun onStart() {
        super.onStart()
        checkAndRequestPermissions()
    }

    override fun onDestroy() {
        super.onDestroy()
        // libero le risorse
        bluetoothViewModel.disconnectDevice()
    }
}

/**
 * Funzione utile per la configurazione di Amplify, utile per interagire con i servizi AWS
 */
private fun configureAmplify(applicationContext: Context){
    try {
        Amplify.addPlugin(AWSCognitoAuthPlugin())
        Amplify.addPlugin(AWSS3StoragePlugin())
        Amplify.configure(applicationContext)
        Log.i("Amplify", "Amplify initialized")
    } catch (error: AmplifyException) {
        Log.e("Amplify", "Error initializing Amplify", error)
    }
}
