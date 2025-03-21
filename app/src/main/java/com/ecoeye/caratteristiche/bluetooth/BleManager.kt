package com.ecoeye.caratteristiche.bluetooth

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.content.Context
import android.content.pm.PackageManager
import android.os.Handler
import android.os.Looper

import android.util.Log
import androidx.core.app.ActivityCompat

private const val SERVICE_UUID = "4fafc201-1fb5-459e-8fcc-c5c9c331914b"
private const val CHARACTERISTIC_UUID = "beb5483e-36e1-4688-b7f5-ea07361b26a8"

@SuppressLint("MissingPermission")
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
        lastDevice = device
        bluetoothGatt = device.connectGatt(context.applicationContext, false, gattCallback, BluetoothDevice.TRANSPORT_LE)
        Log.d("BleService", "Tentativo di connessione a ${device.address}")
    }

    private var currentConnectionAttempt = 1
    private val MAX_CONNECTION_ATTEMPTS = 5
    /**
     * Callback per gestire gli eventi della connessione GATT.
     */
    private val gattCallback = object : BluetoothGattCallback() {
        // Funzione che gestisce i cambiamenti dello stato della connessione con il dispositivo.
        @SuppressLint("MissingPermission")
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            Log.d("BleService", "onConnectionStateChange: status=$status, newState=$newState")
            if(status == BluetoothGatt.GATT_SUCCESS){
                if(newState == BluetoothGatt.STATE_CONNECTED){
                    Log.d("BleService", "Connessione Stabilita!")
                    connectionStateCallback?.invoke(BluetoothConnectionState.CONNECTED)
                    gatt.discoverServices()
                    this@BleManager.bluetoothGatt = gatt
                    lastDevice = null
                    currentConnectionAttempt = 1
                }else if(newState == BluetoothGatt.STATE_DISCONNECTED){
                    Log.d("BleService", "Dispositivo Disconnesso!")
                    connectionStateCallback?.invoke(BluetoothConnectionState.DISCONNECTED)
                    gatt.close()
                }
            }else{
                gatt.close()
                currentConnectionAttempt += 1
                Log.d("BleService", "Tentativo di connessione $currentConnectionAttempt / $MAX_CONNECTION_ATTEMPTS")
                connectionStateCallback?.invoke(BluetoothConnectionState.CONNECTING)

                if(currentConnectionAttempt <= MAX_CONNECTION_ATTEMPTS){
                    lastDevice?.let { connectToDevice(it) }
                }else{
                    Log.d("BleService", "Non è possibile connettersi al dispositivo BLE")
                }
            }
        }

        // Questa funzione viene eseguita subito dopo aver scoperto i servizi.
        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
           with(gatt){
               printGattTable()
               gatt.requestMtu(517) //23
               Log.d("BleService", "Modifica dello spazio MTU...")
           }
        }

        override fun onMtuChanged(gatt: BluetoothGatt?, mtu: Int, status: Int) {
            super.onMtuChanged(gatt, mtu, status)

        }
    }
    /**
     * Permette di chiudere la connessione GATT liberando le risorse.
     */
    fun closeConnection() {
        bluetoothGatt?.close()
        bluetoothGatt = null
        connectionStateCallback?.invoke(BluetoothConnectionState.DISCONNECTED)
        Log.d("BleService", "Connessione GATT chiusa!")
    }
}