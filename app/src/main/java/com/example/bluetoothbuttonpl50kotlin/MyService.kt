package com.example.bluetoothbuttonpl50kotlin

import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Intent
import android.device.ScanDevice
import android.os.Binder
import android.os.IBinder
import android.util.Log
import java.io.BufferedInputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*


class MyService : Service() {

    private val myBinder = MyLocalBinder()
    private var isServiceStop = false
    private var isRunableRun = false
    private val TAG = "bluetoothservice"

    private var sd: ScanDevice? = null

    private val REQUEST_ENABLE_BT = 1
    var bluetoothDevice: BluetoothDevice? = null
    private var isConnected = false
    private var isWorked = false
    private var tempSend = ""
    private var connectThread: ConnectThread? = null
    private var connectedThread: ConnectedThread? = null

     override fun onCreate() {
        Log.i(TAG, "Service onCreate")
        sd = ScanDevice()
    }
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.i(TAG, "Service onStartCommand " + startId)
        if(!isRunableRun){
            val runnable = Runnable {
                someTask(startId);
                stopSelf()
            }
            var thread = Thread(runnable)
            thread.start()
            isRunableRun = true
        }
        return  super.onStartCommand(intent, flags, startId)
    //    return Service.START_NOT_STICKY
    }
    override fun onBind(intent: Intent): IBinder? {
        Log.i(TAG, "Service onBind")
        return myBinder
    }
    inner class MyLocalBinder : Binder() {
        fun getService() : MyService {
            return this@MyService
        }
    }
    fun getCurrentTime(): String {
        val dateformat = SimpleDateFormat("HH:mm:ss MM/dd/yyyy",
            Locale.US)
        return dateformat.format(Date())
    }
    override fun onDestroy() {
        Log.i(TAG, "Service onDestroy")
        isServiceStop = true
    }
    fun someTask(j:Int){
        var i: Int = 0
        while (i <= 5 ) {
            if(isServiceStop) break
            try {
                Thread.sleep(1000)
                i++
            } catch (e: Exception) {
            }
            Log.i(TAG, j.toString() + " Service running " + i.toString())
        }
        sd?.startScan()
    }



//    private val REQUEST_ENABLE_BT = 1
//    private val SPP_UUID = "e1ec7041-83ac-4d9d-8ec7-16f7c3bc5470"
//
//    var sd: ScanDevice? = null
//    var bluetoothDevice: BluetoothDevice? = null
//    private var isConnected = false
//    private var isWorked = false
//    private var tempSend = ""
//
//    private var connectThread: ConnectThread? = null
//    private var connectedThread: ConnectedThread? = null
//    val LOG_TAG = "bluetoothservice"
//    override fun onCreate() {
//        super.onCreate()
//        sd = ScanDevice()
//        Log.d(LOG_TAG, "onCreate")
//        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
//        if (bluetoothAdapter == null) {
//            // Device doesn't support Bluetooth
//            Log.d(LOG_TAG, "Device doesn't support Bluetooth")
//        } else {
//            Log.d(LOG_TAG, "Device support Bluetooth")
//            Log.d(LOG_TAG, bluetoothAdapter.name)
//            Log.d(LOG_TAG, bluetoothAdapter.address)
//            //\       Log.d(TAG, bluetoothAdapter.getBluetoothLeAdvertiser());
//        }
//        if (!bluetoothAdapter!!.isEnabled) {
//            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
//            //     startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
//            Log.d(LOG_TAG, "Bluetooth isn't enabled")
//        } else {
//            Log.d(LOG_TAG, "Bluetooth is enabled")
//        }
//        val pairedDevices =
//            bluetoothAdapter.bondedDevices
//        Log.d(LOG_TAG, pairedDevices.toString())
//        if (pairedDevices.size > 0) {
//            // There are paired devices. Get the name and address of each paired device.
//            val i = 1
//            for (device in pairedDevices) {
//                bluetoothDevice = device
//                val deviceName = device.name
//                val deviceHardwareAddress = device.address // MAC address
//                Log.d(
//                    LOG_TAG,
//                    "$i. $deviceName $deviceHardwareAddress"
//                )
//            }
//            //            connectThread = new ConnectThread(bluetoothDevice);
////            connectThread.start();
//            Log.d(LOG_TAG, "connectThread to device - " + bluetoothDevice!!.name)
//        }
//    }
//    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
//        Log.d(LOG_TAG, "onStartCommand")
//        someTask()
//        return super.onStartCommand(intent, flags, startId)
//    }
//    override fun onDestroy() {
//        super.onDestroy()
//        Log.d(LOG_TAG, "onDestroy")
//        isWorked = false
//    }
//    override fun onBind(intent: Intent?): IBinder? {
//        Log.d(LOG_TAG, "onBind")
//        return null
//    }
//    fun someTask() {
//        if (!isWorked) {
//            connectThread = ConnectThread(bluetoothDevice)
//            connectThread!!.start()
//        }
//    }

    private inner class ConnectThread(device: BluetoothDevice?) : Thread() {
        private var bluetoothSocket: BluetoothSocket? = null
        private var succesess = false
        override fun run() {
            Log.d(TAG, "ConnectThread run")
            try {
                bluetoothSocket!!.connect()
                isWorked = true
                succesess = true
                Log.d(TAG, "ConnectThread bluetoothSocket connect")
            } catch (e: IOException) {
                e.printStackTrace()
                Log.d(TAG, "ConnectThread bluetoothSocket don't connect")
                //                runOnUiThread(new Runnable() {
//                    public void run(){
//                        Toast.makeText(MainActivity.this, "Can't connect!", Toast.LENGTH_SHORT).show();
//                    }
//                });
                cancel()
            }
            if (succesess) {
                //create class object
                connectedThread = ConnectedThread(bluetoothSocket)
                connectedThread!!.start()
                Log.d(TAG, "connectThread success and start")
                for (i in 0..500) {
                    if (!isConnected || !isWorked) break
                    val d = i - 1
                    val dtempSend = "test send$d\r\n"
                    if (dtempSend == tempSend) {
                        Log.d(TAG, "+++++++++++++++++Enabled++++++++++++++++++++")
                        sd?.startScan()
                        //                        runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//
//
//                                //   if(tb)
//                                Label_hello.setText("Enabled");
//                                sd.startScan();
//                            }
//                        });
                    } else {
                        Log.d(
                            TAG,
                            "+++++++++++++++++Disabled++++++++++++++++++++||$dtempSend||$tempSend"
                        )
                        //                        runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//
//
//                                //   if(tb)
//                                Label_hello.setText("Disabled");
//                            }
//                        });
                    }
                    connectedThread!!.write("test send$i\r\n")
                    Log.d(
                        TAG,
                        "connectThread connectedThread.write(\"test send\" + i + \"\\r\\n\");"
                    )
                    try {
                        sleep(200)
                        Log.d(TAG, "connectThread sleep500")
                    } catch (e: InterruptedException) {
                        e.printStackTrace()
                        Log.d(TAG, "connectThread sleepexeption")
                    }
                }
                //               Log.d(TAG,"connectThread success and start");
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
            Log.d(TAG, "ConnectThread constructor")
            var tmp: BluetoothSocket? = null
            try {
                val method = device!!.javaClass.getMethod(
                    "createRfcommSocket", *arrayOf<Class<*>?>(
                        Int::class.javaPrimitiveType
                    )
                )
                Log.d(TAG, "ConnectThread method $method")
                tmp = method.invoke(device, 1) as BluetoothSocket
                //               bluetoothSocket = (BluetoothSocket) method.invoke(device, 1);
                //               this.mmDevice = device;
                //               tmp = device.createRfcommSocketToServiceRecord(UUID.fromString(SPP_UUID));
                //           Log.d(LOG_TAG, "ConnectThread bluetoothSocket " + bluetoothSocket.toString());
//                Log.d(TAG, "ConnectThread MY_UUID - " + MY_UUID.toString());
//                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
//                Log.d(TAG, "ConnectThread tmp - " + tmp.toString());
            } catch (e: Exception) {
                e.printStackTrace()
                Log.d(TAG, "ConnectThread don't get socket " + device!!.name)
            }
            bluetoothSocket = tmp
            Log.d(
                TAG,
                "ConnectThread buetoothsocket - " + bluetoothSocket.toString()
            )
        }
    }
    private inner class ConnectedThread(bluetoothSocket: BluetoothSocket?) : Thread() {
        private val inputStream: InputStream?
        private val outputStream: OutputStream?
        override fun run() {
            val unTouch = true
            val gh = 0
            val buffer = StringBuffer()
            val bis = BufferedInputStream(inputStream)
            Log.d(TAG, "connectedThread  run")
            Log.d(TAG, "ConnectedThread isConnected - $isConnected")
            while (isConnected) {
                try {
                    val bytes = bis.read()
                    buffer.append(bytes.toChar())
                    Log.d(
                        TAG,
                        "ConnectedThread buffer read - $buffer"
                    )
                    val eof = buffer.indexOf("\r\n")
                    Log.d(TAG, "ConnectedThread eof - $eof")
                    if (eof > 0) {
                        tempSend = buffer.toString()
                        buffer.delete(0, buffer.length)
                    } else {
                        Log.d(TAG, "ConnectedThread dont read ")
                    }
                } catch (e: IOException) {
                    Log.d(TAG, "ConnectedThread cant reed")
                    e.printStackTrace()
                }
            }
        }
        fun write(command: String) {
            Log.d(TAG, "connectedThread write start - $command")
            val bytes = command.toByteArray()
            if (outputStream != null) {
                try {
                    outputStream.write(bytes)
                    Log.d(TAG, "connectedThread write bites")
                    outputStream.flush()
                    Log.d(TAG, "connectedThread flush")
                } catch (e: IOException) {
                    e.printStackTrace()
                    Log.d(TAG, "connectedThread write exeption")
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
            Log.d(TAG, "constructor connectedTHread")
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

