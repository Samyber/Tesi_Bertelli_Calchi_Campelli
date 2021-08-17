package com.samaeli.tesi

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.util.Log

class ConnectivityReceiver : BroadcastReceiver() {

    companion object{
        val TAG = "Connectivity Receiver"
        var status = false
    }

    /*override fun onReceive(context: Context?, intent: Intent?) {

        Log.d(TAG,"Connectivity change")

        val connectivityManager = context!!.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        if(checkConnectivity(connectivityManager)){
            Log.d(TAG,"Connection OK")
        }else{
            Log.d(TAG,"NOT connected")
        }

    }

    private fun checkConnectivity(connectivityManager: ConnectivityManager):Boolean{
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val nw      = connectivityManager.activeNetwork ?: return false
            val actNw = connectivityManager.getNetworkCapabilities(nw) ?: return false
            return when {
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                //for other device how are able to connect with Ethernet
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                //for check internet over Bluetooth
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH) -> true
                else -> false
            }
        } else {
            return connectivityManager.activeNetworkInfo?.isConnected ?: false
        }
    }*/

    override fun onReceive(context: Context, intent: Intent?) {
        Log.d("News reader", "Connectivity changed")
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        //val service = Intent(context, NewsReaderService::class.java)
        if (networkInfo != null && networkInfo.isConnected) {
            Log.d("News reader", "Connected")
            // Intent che carica MainActivity
            if(MainActivity.active==false) {
                val intent = Intent(context, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            }
        } else {
            Log.d("News reader", "NOT connected")
            //Intent che carica schermata che dice che non si è connessi
            val intent = Intent(context,NotConnectedActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
    }

}