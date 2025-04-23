package com.ecoeye.caratteristiche.bluetooth

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import java.util.UUID

private const val SERVICE_UUID = "4fafc201-1fb5-459e-8fcc-c5c9c331914b"
private const val CHARACTERISTIC_UUID = "beb5483e-36e1-4688-b7f5-ea07361b26a8"

@Suppress("DEPRECATION")
@SuppressLint("MissingPermission")
class BleManager(private val context: Context) {

    // La variabile viene usata per gestire la connessione BLE, permette al servizio di interagire con il dispositovo BLE
    private var bluetoothGatt: BluetoothGatt? = null


    // Callback per comunicare lo stato della connessione al ViewModel
    var connectionStateCallback: ((BluetoothConnectionState) -> Unit)? = null

    // Memorizza l'ultimo dispositivo tentato (utile per il retry)
    private var lastDevice: BluetoothDevice? = null

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

        override fun onCharacteristicWrite(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?,
            status: Int
        ) {
            super.onCharacteristicWrite(gatt, characteristic, status)
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d("BleService", "Scrittura completata con successo per: ${characteristic?.uuid}")
            } else {
                Log.e("BleService", "Errore nella scrittura della caratteristica, status: $status")
            }
        }
    }

    fun writeText(text: String){
        val gatt = bluetoothGatt

        if(gatt == null){
            Log.e("BleService", "Impossibile inviare testo: connessione non stabilita")
            return
        }

        //Ottengo il servizio dall'UUID che ho definito
        val serviceUUID = UUID.fromString(SERVICE_UUID)
        val service = gatt.getService(serviceUUID)
        if(service == null){
            Log.e("BleService", "Servizio non trovato")
            return
        }

        //Ottengo la caratteristica dall' UUID che ho definito
        val characteristicUUID = UUID.fromString(CHARACTERISTIC_UUID)
        val characteristic = service.getCharacteristic(characteristicUUID)
        if(characteristic == null ){
            Log.e("BleService", "Caratteristica BLE non trovata")
            return
        }

        // Imposto il tipo di scrittura
        characteristic.writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT

        // Imposto il valore della caratteristica convertendo il testo in byte
        val bytes = text.toByteArray(Charsets.UTF_8)
        val result = characteristic.setValue(bytes) //modifica il valore della caratteristica memorizzato localmente nella cache. Restituisce un booleano per capire se l'operazione di set del nuovo valore della caratteristica è andato a buon fine

        if (!result) {
            Log.e("BleService", "Errore nell'impostazione del valore della caratteristica")
            return
        }

        val write = gatt.writeCharacteristic(characteristic)

        if (write) {
            Log.d("BleService", "Testo inviato: $text")
        } else {
            Log.e("BleService", "Scrittura della caratteristica fallita")
        }
    }

    fun sendAudioData(audioData : ByteArray){
        val gatt = bluetoothGatt ?: run {
            Log.e("BleService", "Connessione non stabilita per invio audio")
            return
        }

        //Ottengo il servizio dall'UUID che ho definito
        val serviceUUID = UUID.fromString(SERVICE_UUID)
        val service = gatt.getService(serviceUUID) ?: run {
            Log.e("BleService", "Servizio BLE non trovato")
            return
        }

        //Ottengo la caratteristica dall' UUID che ho definito
        val characteristicUUID = UUID.fromString(CHARACTERISTIC_UUID)
        val characteristic = service.getCharacteristic(characteristicUUID) ?: run {
            Log.e("BleService", "Caratteristica BLE non trovata")
            return
        }

        // VERIFICA SE E' NECESSARIA LA FRAMMENTAZIONE DEI DATI

        characteristic.writeType = BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE

        val result = characteristic.setValue(audioData)
        if (!result) {
            Log.e("BleService", "Errore nell'impostazione dei dati audio")
            return
        }
        val write = gatt.writeCharacteristic(characteristic)

        if (write) {
            Log.d("BleService", "Audio inviato, lunghezza: ${audioData.size} bytes")
        } else {
            Log.e("BleService", "Scrittura dei dati audio fallita")
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