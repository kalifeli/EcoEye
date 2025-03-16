package com.ecoeye

import android.Manifest
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalGraphicsContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.ecoeye.caratteristiche.bluetooth.BluetoothViewModel
import com.ecoeye.caratteristiche.navigazione.NavGraph
private  const val REQUEST_CODE_PERMISSIONS = 1001

class MainActivity : ComponentActivity() {

    private lateinit var bluetoothViewModel: BluetoothViewModel


    private val requiredPermissions: Array<String> = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        arrayOf(
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_ADVERTISE
        )
    } else {
        arrayOf(
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_ADVERTISE,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    }

    fun checkAndRequestPermissions(){
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
                    .setPositiveButton("OK") { dialog, _ ->
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
        checkAndRequestPermissions()

        setContent {
            NavGraph()
        }
    }

    override fun onStart() {
        super.onStart()
        checkAndRequestPermissions()
    }
}
