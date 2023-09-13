package com.cbnu.project.cpr.heartsignal.adapter

import android.bluetooth.BluetoothDevice
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.cbnu.project.cpr.heartsignal.R // 리소스 파일 ID로 변경 필요
import com.cbnu.project.cpr.heartsignal.data.BluetoothDeviceInfo

class BluetoothDeviceListAdapter(
    private val deviceList: List<BluetoothDeviceInfo>,
    private val onItemClick: (BluetoothDevice) -> Unit) :
    RecyclerView.Adapter<BluetoothDeviceListAdapter.ViewHolder>() {
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // ...
        val nameTextView: TextView = itemView.findViewById(R.id.deviceNameTextView) // 변경 필요
        val addressTextView: TextView = itemView.findViewById(R.id.deviceAddressTextView) // 변경 필요
        val deviceuuidsTextView:TextView = itemView.findViewById(R.id.deviceuuidsTextView)
        val deviceNumberTextView: TextView = itemView.findViewById(R.id.deviceNumberTextView)
        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val device = deviceList[position].device
                    onItemClick(device) // 클릭한 BLE 기기를 전달
                }
            }
        }
    }
//    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
//        val nameTextView: TextView = itemView.findViewById(R.id.deviceNameTextView) // 변경 필요
//        val addressTextView: TextView = itemView.findViewById(R.id.deviceAddressTextView) // 변경 필요
//        val deviceuuidsTextView:TextView = itemView.findViewById(R.id.deviceuuidsTextView)
//        val deviceNumberTextView: TextView = itemView.findViewById(R.id.deviceNumberTextView)
//    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_bluetooth_device, parent, false) // 변경 필요
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val device = deviceList[position]
        holder.deviceNumberTextView.text = "${(position + 1).toString()}번: "
        holder.nameTextView.text = device.name
        holder.addressTextView.text = device.address
        holder.deviceuuidsTextView.text = device.uuids
    }

    override fun getItemCount(): Int {
        return deviceList.size
    }


}