package tech.aranda.myKyBCombineDroid.ble

import android.annotation.SuppressLint
import android.bluetooth.*
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.os.Handler
import android.os.Looper
import tech.aranda.myKyBCombineDroid.protocol.JXProtocol
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.UUID

sealed class HubState {
    object Idle : HubState()
    object Scanning : HubState()
    object Connecting : HubState()
    object Connected : HubState()
    data class Error(val message: String) : HubState()

    val description get() = when (this) {
        is Idle -> "READY TO SCAN"
        is Scanning -> "SCANNING..."
        is Connecting -> "CONNECTING..."
        is Connected -> "CONNECTED"
        is Error -> "ERROR: $message"
    }

    val isConnected get() = this is Connected
}

@SuppressLint("MissingPermission")
class BLEManager(private val context: Context) {

    companion object {
        const val TARGET_NAME = "JX-APP-A"
        val SERVICE_UUID: UUID = UUID.fromString("0000FFF0-0000-1000-8000-00805F9B34FB")
        val WRITE_UUID: UUID   = UUID.fromString("0000FFF2-0000-1000-8000-00805F9B34FB")
        val NOTIFY_UUID: UUID  = UUID.fromString("0000FFF1-0000-1000-8000-00805F9B34FB")
        val CCCD_UUID: UUID    = UUID.fromString("00002902-0000-1000-8000-00805F9B34FB")
        const val SCAN_TIMEOUT_MS = 30_000L
    }

    val protocol = JXProtocol()

    private val _state = MutableStateFlow<HubState>(HubState.Idle)
    val state: StateFlow<HubState> = _state

    private val _log = MutableStateFlow<List<String>>(emptyList())
    val log: StateFlow<List<String>> = _log

    private val bluetoothAdapter: BluetoothAdapter? =
        (context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager)?.adapter

    private var gatt: BluetoothGatt? = null
    private var writeChr: BluetoothGattCharacteristic? = null
    private val handler = Handler(Looper.getMainLooper())

    init {
        handler.post {
            if (bluetoothAdapter?.isEnabled == true) {
                addLog("Bluetooth ON")
            } else {
                addLog("Bluetooth OFF — enable it first")
            }
        }
    }

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val device = result.device
            if (device.name == TARGET_NAME) {
                handler.post {
                    addLog("Found $TARGET_NAME (RSSI: ${result.rssi})")
                    stopScan()
                    connect(device)
                }
            }
        }

        override fun onScanFailed(errorCode: Int) {
            handler.post {
                addLog("Scan failed: $errorCode")
                _state.value = HubState.Error("Scan failed")
            }
        }
    }

    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            handler.post {
                when (newState) {
                    BluetoothProfile.STATE_CONNECTED -> {
                        addLog("Connected! Discovering services...")
                        gatt.discoverServices()
                    }
                    BluetoothProfile.STATE_DISCONNECTED -> {
                        addLog("Disconnected")
                        writeChr = null
                        this@BLEManager.gatt = null
                        _state.value = HubState.Idle
                    }
                }
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            handler.post {
                val service = gatt.getService(SERVICE_UUID)
                if (service == null) {
                    addLog("Service FFF0 not found")
                    _state.value = HubState.Error("Service not found")
                    return@post
                }
                addLog("Found service FFF0")

                writeChr = service.getCharacteristic(WRITE_UUID)
                if (writeChr != null) addLog("Found FFF2 (write)")

                val notifyChr = service.getCharacteristic(NOTIFY_UUID)
                if (notifyChr != null) {
                    gatt.setCharacteristicNotification(notifyChr, true)
                    notifyChr.getDescriptor(CCCD_UUID)?.let {
                        it.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                        gatt.writeDescriptor(it)
                    }
                    addLog("Found FFF1 (notify)")
                }

                if (writeChr != null) {
                    _state.value = HubState.Connected
                    addLog("Ready!")
                    send(protocol.idlePacket)
                }
            }
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic
        ) {
            val hex = characteristic.value.joinToString(" ") { "%02x".format(it) }
            handler.post { addLog("FFF1: $hex") }
        }

        override fun onCharacteristicWrite(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
            if (status != BluetoothGatt.GATT_SUCCESS) {
                handler.post { addLog("Write error: $status") }
            }
        }
    }

    fun startScan() {
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) {
            handler.post {
                _state.value = HubState.Error("Bluetooth not enabled")
                addLog("Bluetooth not enabled")
            }
            return
        }
        handler.post {
            _state.value = HubState.Scanning
            addLog("Scanning for $TARGET_NAME...")
        }
        bluetoothAdapter.bluetoothLeScanner?.startScan(scanCallback)
        handler.postDelayed({
            if (_state.value is HubState.Scanning) {
                stopScan()
                handler.post {
                    _state.value = HubState.Idle
                    addLog("Scan timed out")
                }
            }
        }, SCAN_TIMEOUT_MS)
    }

    private fun stopScan() {
        bluetoothAdapter?.bluetoothLeScanner?.stopScan(scanCallback)
    }

    private fun connect(device: BluetoothDevice) {
        _state.value = HubState.Connecting
        addLog("Connecting to $TARGET_NAME...")
        gatt = device.connectGatt(context, false, gattCallback, BluetoothDevice.TRANSPORT_LE)
    }

    fun disconnect() {
        gatt?.disconnect()
        gatt?.close()
        gatt = null
        writeChr = null
        handler.post {
            _state.value = HubState.Idle
            addLog("Disconnected")
        }
    }

    // Fire-and-forget write — matches iOS behavior exactly
    fun send(data: ByteArray) {
        val chr = writeChr ?: return
        val g = gatt ?: return
        chr.value = data
        chr.writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
        g.writeCharacteristic(chr)
    }

    fun sendDrive(driveY: Double, steerX: Double) {
        send(protocol.buildDrivePacket(driveY, steerX))
    }

    // Stop: send idle packet 3 times to ensure it gets through
    fun sendStop() {
        val idle = protocol.idlePacket
        send(idle)
        handler.postDelayed({ send(idle) }, 20)
        handler.postDelayed({ send(idle) }, 60)
    }

    private fun addLog(message: String) {
        val time = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault())
            .format(java.util.Date())
        val entry = "[$time] $message"
        handler.post {
            _log.value = listOf(entry) + _log.value.take(49)
        }
    }
}