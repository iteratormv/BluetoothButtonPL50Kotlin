package com.example.bluetoothbuttonpl50kotlin

import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity

import android.content.ComponentName;
import android.content.Context
import android.content.res.Resources
import android.os.IBinder;
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    private val TAG = "bluetoothservice"
    var myService: MyService? = null
    var isBound = false
    var label : TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Log.d(TAG, "onCreate")
        label = findViewById(R.id.label_connection_status)
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart")
    }
//    private val myConnection = object : ServiceConnection {
//        override fun onServiceConnected(className: ComponentName, service: IBinder) {
//            isBound = true
//        }
//        override fun onServiceDisconnected(name: ComponentName) {
//            isBound = false
//        }
//    }
    fun onStopService(view: View) {
        var intent =  Intent(this, BluetoothButtonService::class.java)
        stopService(intent)
    label!!.text = "Disable"
    }
    fun onStartService(view: View) {
        var intent = Intent(this, BluetoothButtonService::class.java)
        startService(intent)
        label!!.text = "Enable"
    }

}