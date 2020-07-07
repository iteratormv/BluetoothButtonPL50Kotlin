package com.example.bluetoothbuttonpl50kotlin

import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Intent
import android.device.ScanDevice
import android.os.IBinder
import android.util.Log
import java.io.BufferedInputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

class BluetoothButtonService : Service() {

    var sd: ScanDevice? = null
    var bluetoothDevice: BluetoothDevice? = null
    private var isConnected = false
    private var isWorked = false
    private var tempSend = ""
    private var connectThread: BluetoothButtonService.ConnectThread? = null
    private var connectedThread: BluetoothButtonService.ConnectedThread? = null
    val LOG_TAG = "BluetoothLogs"
    override fun onCreate() {
        super.onCreate()
        sd = ScanDevice()
        Log.d(LOG_TAG, "onCreate")
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (bluetoothAdapter == null) {
            // Device doesn't support Bluetooth
            Log.d(LOG_TAG, "Device doesn't support Bluetooth")
        } else {
            Log.d(LOG_TAG, "Device support Bluetooth")
            Log.d(LOG_TAG, bluetoothAdapter.name)
            Log.d(LOG_TAG, bluetoothAdapter.address)
        }
        if (!bluetoothAdapter!!.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            Log.d(LOG_TAG, "Bluetooth isn't enabled")
        } else {
            Log.d(LOG_TAG, "Bluetooth is enabled")
        }
        val pairedDevices =
            bluetoothAdapter.bondedDevices
        Log.d(LOG_TAG, pairedDevices.toString())
        if (pairedDevices.size > 0) {
            // There are paired devices. Get the name and address of each paired device.
            val i = 1
            for (device in pairedDevices) {
                bluetoothDevice = device
                val deviceName = device.name
                val deviceHardwareAddress = device.address // MAC address
                Log.d(
                    LOG_TAG,
                    "$i. $deviceName $deviceHardwareAddress"
                )
            }
            Log.d(LOG_TAG, "connectThread to device - " + bluetoothDevice!!.name)
        }
    }
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(LOG_TAG, "onStartCommand")
        someTask()
        return super.onStartCommand(intent, flags, startId)
    }
    override fun onDestroy() {
        super.onDestroy()
        Log.d(LOG_TAG, "onDestroy")
        isWorked = false
        isConnected = false
        this.connectThread?.cancel()
        this.connectedThread?.cancel()
    }
    override fun onBind(intent: Intent): IBinder {
        TODO("Return the communication channel to the service.")
    }
    fun someTask() {
        if (!isWorked) {
            connectThread = this.ConnectThread(bluetoothDevice!!)
            connectThread!!.start()
        }
    }

    inner class ConnectThread(device: BluetoothDevice) : Thread() {
        private var bluetoothSocket: BluetoothSocket? = null
        private var succesess = false
        override fun run() {
            Log.d(LOG_TAG, "ConnectThread run")
            try {
                bluetoothSocket!!.connect()
                isWorked = true
                succesess = true
                Log.d(LOG_TAG, "ConnectThread bluetoothSocket connect")
            } catch (e: IOException) {
                e.printStackTrace()
                Log.d(LOG_TAG, "ConnectThread bluetoothSocket don't connect")
                cancel()
            }
            if (succesess) {
                //create class object
                connectedThread = ConnectedThread(bluetoothSocket)
                connectedThread!!.start()
                Log.d(LOG_TAG, "connectThread success and start")
                for (i in 0..99999999) {
                    if (!isConnected || !isWorked) break
                    val d = i - 1
                    val dtempSend = "test send$d\r\n"
                    if (dtempSend == tempSend) {
                        Log.d(LOG_TAG, "+++++++++++++++++Enabled++++++++++++++++++++")
                        sd?.startScan()
                        try {
                            sleep(300)
                        } catch (e: InterruptedException) {
                            e.printStackTrace()
                        }
                        sd?.stopScan()
                    } else {
                        Log.d(
                            LOG_TAG,
                            "+++++++++++++++++Disabled++++++++++++++++++++||$dtempSend||$tempSend"
                        )
                    }
                    connectedThread!!.write("test send$i\r\n")
                    Log.d(
                        LOG_TAG,
                        "connectThread connectedThread.write(\"test send\" + i + \"\\r\\n\");"
                    )
                    try {
                        sleep(100)
                        Log.d(LOG_TAG, "connectThread sleep200")
                    } catch (e: InterruptedException) {
                        e.printStackTrace()
                        Log.d(LOG_TAG, "connectThread sleepexeption")
                    }
                }
            }
        }
        fun cancel() {
            try {
                bluetoothSocket!!.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            isWorked = false
        }
        init {
            Log.d(LOG_TAG, "ConnectThread constructor")
            var tmp: BluetoothSocket? = null
            try {
                val method = device.javaClass.getMethod(
                    "createRfcommSocket", *arrayOf<Class<*>?>(
                        Int::class.javaPrimitiveType
                    )
                )
                Log.d(LOG_TAG, "ConnectThread method $method")
                tmp = method.invoke(device, 1) as BluetoothSocket
            } catch (e: Exception) {
                e.printStackTrace()
                Log.d(LOG_TAG, "ConnectThread don't get socket " + device.name)
            }
            bluetoothSocket = tmp
            Log.d(
                LOG_TAG,
                "ConnectThread buetoothsocket - " + bluetoothSocket.toString()
            )
        }
    }
    inner class ConnectedThread(bluetoothSocket: BluetoothSocket?) : Thread() {
        private val inputStream: InputStream?
        private val outputStream: OutputStream?
        override fun run() {
//            super.run();
            val unTouch = true
            val gh = 0
            val buffer = StringBuffer()
            val bis = BufferedInputStream(inputStream)
            Log.d(LOG_TAG, "connectedThread  run")
            Log.d(LOG_TAG, "ConnectedThread isConnected - $isConnected")
            while (isConnected) {
                try {
                    val bytes = bis.read()
                    buffer.append(bytes.toChar())
                    Log.d(
                        LOG_TAG,
                        "ConnectedThread buffer read - $buffer"
                    )
                    val eof = buffer.indexOf("\r\n")
                    Log.d(LOG_TAG, "ConnectedThread eof - $eof")
                    if (eof > 0) {
                        tempSend = buffer.toString()
                        buffer.delete(0, buffer.length)
                    } else {
                        Log.d(LOG_TAG, "ConnectedThread dont read ")
                    }
                } catch (e: IOException) {
                    Log.d(LOG_TAG, "ConnectedThread cant reed")
                    e.printStackTrace()
                }
            }
        }
        fun write(command: String) {
            Log.d(LOG_TAG, "connectedThread write start - $command")
            val bytes = command.toByteArray()
            if (outputStream != null) {
                try {
                    outputStream.write(bytes)
                    Log.d(LOG_TAG, "connectedThread write bites")
                    outputStream.flush()
                    Log.d(LOG_TAG, "connectedThread flush$isConnected$isWorked")
                } catch (e: IOException) {
                    e.printStackTrace()
                    Log.d(LOG_TAG, "connectedThread write exeption")
                }
            }
        }
        fun cancel() {
            isConnected = false
            try {
                inputStream!!.close()
                outputStream!!.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        init {
            Log.d(LOG_TAG, "constructor connectedTHread")
            var inputStream: InputStream? = null
            var outputStream: OutputStream? = null
            try {
                inputStream = bluetoothSocket!!.inputStream
                outputStream = bluetoothSocket.outputStream
            } catch (e: IOException) {
                e.printStackTrace()
            }
            this.inputStream = inputStream
            this.outputStream = outputStream
            isConnected = true
        }
    }

}
