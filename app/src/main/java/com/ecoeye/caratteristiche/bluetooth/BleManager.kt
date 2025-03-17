package com.ecoeye.caratteristiche.bluetooth

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.content.pm.PackageManager
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat

class BleManager(private val context: Context) {

    // La variabile viene usata per gestire la connessione BLE, permette al servizio di interagire con il dispositovo BLE
    private var bluetoothGatt: BluetoothGatt? = null


    // Callback per comunicare lo stato della connessione al ViewModel
    var connectionStateCallback: ((BluetoothConnectionState) -> Unit)? = null

    // Memorizza l'ultimo dispositivo tentato (utile per il retry)
    private var lastDevice: BluetoothDevice? = null

    // Handler per posticipare il retry
    private val handler = Handler(Looper.getMainLooper())
    /**
     * Avvia la connessione diretta al dispositivo BLE.
     * L'oggetto BluetoothDevice si ottiene dallo scanning.
     */
    fun connectToDevice(device: BluetoothDevice) {
        connectionStateCallback?.invoke(BluetoothConnectionState.CONNECTING)
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.d("BleService", "PERMESSI NON CONCESSI")
            return
        }
        // Se c'è già una connessione aperta, chiudila
        bluetoothGatt?.close()
        bluetoothGatt = null

        lastDevice = device
        bluetoothGatt = device.connectGatt(context.applicationContext, false, gattCallback)
        Log.d("BleService", "Tentativo di connessione a ${device.address}")
    }

    /**
     * Callback per gestire gli eventi della connessione GATT.
     */
    private val gattCallback = object : BluetoothGattCallback() {
        // Funzione che gestisce i cambiamenti dello stato della connessione con il dispositivo.
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            Log.d("BleService", "onConnectionStateChange: status=$status, newState=$newState")
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {

                return
            }else {
                when (newState) {
                    BluetoothProfile.STATE_CONNECTED -> {
                        Log.d("BleService", "Connesso al GATT server")
                        gatt?.discoverServices()
                        connectionStateCallback?.invoke(BluetoothConnectionState.CONNECTED)
                        // Quando la connessione è avvenuta con successo, inizio la scoperta dei servizi messi a disposizione dal dispositivo BLE

                    }

                    BluetoothProfile.STATE_DISCONNECTED -> {
                        Log.d("BleService", "Disconnesso dal GATT server")
                        connectionStateCallback?.invoke(BluetoothConnectionState.DISCONNECTED)
                        // Se riceviamo il GATT error 133, tentiamo il retry
                        if (status == 133) {
                            gatt?.close() // Chiudiamo la connessione corrente
                            Log.d(
                                "BleService",
                                "Retry connection in 2 secondi a ${lastDevice?.address}"
                            )
                            handler.postDelayed({
                                lastDevice?.let { device ->
                                    Log.d("BleService", "Retry connection to ${device.address}")
                                    connectToDevice(device)
                                }
                            }, 5000)
                        }
                    }

                    BluetoothProfile.STATE_CONNECTING -> {
                        Log.d("BleService", "Connessione in corso al GATT server...")
                        connectionStateCallback?.invoke(BluetoothConnectionState.CONNECTING)
                    }
                }
            }
        }

        // Questa funzione viene eseguita subito dopo aver scoperto i servizi.
        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                    Log.d("BleService", "Servizi scoperti: ${gatt?.services}")
            } else {
                Log.w("BleService", "Scoperta dei servizi fallita, status: $status")
            }
        }
    }
    /**
     * Permette di chiudere la connessione GATT liberando le risorse.
     */
    @SuppressLint("MissingPermission")
    fun closeConnection() {
        bluetoothGatt?.close()
        bluetoothGatt = null
        connectionStateCallback?.invoke(BluetoothConnectionState.DISCONNECTED)
        Log.d("BleService", "Connessione GATT chiusa!")
    }
}