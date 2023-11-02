package com.cbnu.project.cpr.heartsignal.fragment.mainFragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.cbnu.project.cpr.heartsignal.R
import com.cbnu.project.cpr.heartsignal.ble.BluetoothManager
import java.util.UUID


class MainCalendarFragment : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_main_calendar, container, false)

        val btn = view.findViewById<TextView>(R.id.textBtn)
        btn.setOnClickListener {
            val data = "ST".toByteArray(Charsets.UTF_8)
            val characteristicUUID = UUID.fromString("6e400002-b5a3-f393-e0a9-e50e24dcca9e")
            val bluetoothManager = BluetoothManager.getInstance(requireContext() )
            bluetoothManager.writeDataToCharacteristic(data, characteristicUUID)
        }



        return view
    }

}