package com.ecoeye.caratteristiche.bluetooth

import android.Manifest
import android.annotation.SuppressLint
import android.app.Application
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class BluetoothViewModel(application: Application): AndroidViewModel(application), BluetoothController{
    private val context = getApplication<Application>()
    private val bluetoothManager by lazy {
        application.getSystemService(BluetoothManager::class.java)
    }

    private val bluetoothAdapter by lazy {
        bluetoothManager?.adapter
    }
    private val bluetoothLeScanner = bluetoothAdapter?.bluetoothLeScanner

    private val TIMEOUT : Long = 10000

    private val handler = Handler(Looper.getMainLooper())

    private val _scanning :MutableStateFlow<Boolean> = MutableStateFlow(false)
    val scanning: StateFlow<Boolean> = _scanning

    private val _scannedDevices: MutableStateFlow<List<DispositivoBLE>> = MutableStateFlow(emptyList())
    override val scannedDevices: StateFlow<List<DispositivoBLE>> = _scannedDevices
    private val _pairedDevices: MutableStateFlow<List<DispositivoBLE>> = MutableStateFlow(emptyList())
    override val pairedDevies: StateFlow<List<DispositivoBLE>> = _pairedDevices

    // Seleziona i permessi richiesti in base alla versione Android
    private val requiredPermissions: Array<String> = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        arrayOf(
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT,
        )
    } else {
        arrayOf(
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    }

    fun hasPermission(): Boolean{
        return requiredPermissions.all { permesso ->
            ContextCompat.checkSelfPermission(context,permesso) == PackageManager.PERMISSION_GRANTED
        }
    }

    fun addScannedDevice(newDevice: DispositivoBLE){
        handler.post {
            val currentList = _scannedDevices.value.toMutableList()
            if (currentList.none { it.indirizzo == newDevice.indirizzo }) {
                currentList.add(newDevice)
                _scannedDevices.value = currentList
                Log.d("BluetoothViewModel", "Dispositivo aggiunto: ${newDevice.nome} - ${newDevice.indirizzo}")
            }
        }
    }



    private val  scanCallback : ScanCallback = object : ScanCallback(){
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            super.onScanResult(callbackType, result)
            if (ActivityCompat.checkSelfPermission(
                            getApplication(),
                            Manifest.permission.BLUETOOTH_CONNECT
                        ) != PackageManager.PERMISSION_GRANTED)
            {
                Log.d("BluetoothViewModel", "Non hai i permessi necessari!")
                return
            }else{
                result?.let { dispositivo ->
                    val newDevice = DispositivoBLE(
                            nome = dispositivo.device.name ?: "Dispositivo sconosciuto",
                            indirizzo = dispositivo.device.address,
                            rssi = dispositivo.rssi
                        )
                        addScannedDevice(newDevice)
                }
            }
        }

        override fun onBatchScanResults(results: MutableList<ScanResult>?) {
            super.onBatchScanResults(results)
        }

        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            Log.e("BluetoothViewModel", "errore: $errorCode")
        }

    }



    override fun startDiscovery() {
            if (_scanning.value == false) {
                _scanning.value = true
                // Avvia la scansione
                if (ActivityCompat.checkSelfPermission(
                        getApplication(),
                        Manifest.permission.BLUETOOTH_SCAN
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    Log.d("BluetoothViewModel", "Non hai i permessi necessari!")
                    _scanning.value = false
                    return
                }
                bluetoothLeScanner?.startScan(scanCallback)
                Log.d("BluetoothViewModel", "La scansione è iniziata: ${_scanning.value}")
                // Ferma la scansione dopo un timeout
                handler.postDelayed({
                    _scanning.value = false
                    bluetoothLeScanner?.stopScan(scanCallback)
                    Log.d("BluetoothViewModel", "La scansione è finita: ${_scanning.value}")
                }, TIMEOUT)
            } else {
                _scanning.value = false
                bluetoothLeScanner?.stopScan(scanCallback)
            }
    }

    @SuppressLint("MissingPermission")
    override fun stopDiscovery() {
        if (_scanning.value == true) {
            _scanning.value = false
            bluetoothLeScanner?.stopScan(scanCallback)
            handler.removeCallbacksAndMessages(null)
        }
    }

    override fun release() {
        TODO("Not yet implemented")
    }


}