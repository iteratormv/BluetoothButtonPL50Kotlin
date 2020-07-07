package com.example.bluetoothbuttonpl50kotlin

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.widget.TextView



class MainActivity : AppCompatActivity() {
    private val TAG = "bluetoothservice"
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