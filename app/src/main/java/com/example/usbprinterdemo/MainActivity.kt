package com.example.usbprinterdemo

import android.app.PendingIntent
import android.app.PendingIntent.FLAG_MUTABLE
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbDeviceConnection
import android.hardware.usb.UsbManager
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.IntentCompat


class MainActivity : AppCompatActivity() {
    private lateinit var edTv:EditText
    private lateinit var btnhPrint:Button
    private var usbDevice1: UsbDevice? = null
    private var usbConnection: UsbDeviceConnection? = null
    private lateinit var usbManager: UsbManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        edTv = findViewById(R.id.edText)
        btnhPrint = findViewById(R.id.btnPrint)

        val filter = IntentFilter(ACTION_USB_PERMISSION)
        registerReceiver(usbReceiver, filter)

        btnhPrint.setOnClickListener {
            val usbManager = getSystemService(Context.USB_SERVICE) as UsbManager
            val deviceList = usbManager.deviceList
            val device = deviceList.values.firstOrNull { true }
            device?.let {
                val permissionIntent = PendingIntent.getBroadcast(this, 0, Intent(ACTION_USB_PERMISSION), FLAG_MUTABLE)
                usbManager.requestPermission(device, permissionIntent)
            }
        }

    }

    override fun onResume() {
        super.onResume()
        usbManager = getSystemService(Context.USB_SERVICE) as UsbManager
        val filter = IntentFilter(ACTION_USB_PERMISSION)
        registerReceiver(usbReceiver, filter)

        // Find and request permission for the USB device (replace VENDOR_ID and PRODUCT_ID)
        val deviceList = usbManager.deviceList
        val device = deviceList.values.firstOrNull()
        device?.let {
            val permissionIntent = PendingIntent.getBroadcast(this, 0, Intent(ACTION_USB_PERMISSION), FLAG_MUTABLE)
            usbManager.requestPermission(device, permissionIntent)
        }
    }


    private fun openUsbDevice() {
        usbDevice1?.let { device ->
            val usbInterface = device.getInterface(0)
            usbConnection = usbManager.openDevice(device)
            usbConnection?.claimInterface(usbInterface, true)
            Log.e("onReceive1: ", "worked")
            // Now you can communicate with the USB device
            val endpointOut = usbInterface.getEndpoint(0)
            val buffer = edTv.text.toString().toByteArray()
            Log.e("onReceive2: ", "worked")
            usbConnection?.bulkTransfer(endpointOut, buffer, buffer.size, 1000)
            Log.e("onReceive3: ", usbInterface.name.toString())
        }
    }

    private val ACTION_USB_PERMISSION = "com.example.usbprinterdemo.USB_PERMISSION"

    private val usbReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (ACTION_USB_PERMISSION == action) {
                synchronized(this) {
                        val usbDevice:UsbDevice? = IntentCompat.getParcelableExtra(intent,UsbManager.EXTRA_DEVICE,UsbDevice::class.java)
                    Log.e("onReceive: ", "$intent asd")
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        Log.e("onReceive: ", "Granted")
                        usbDevice?.let {
                            // Permission granted, proceed with opening and using the printer
                            Log.e("onReceive: ", it.deviceName)
                        usbDevice1 = it
                        openUsbDevice()
                        }
                    } else {
                        // Permission denied, handle accordingly
                        Log.e("onReceive: ", "Denied")
                    }
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(usbReceiver)
        usbConnection?.close()
    }
}