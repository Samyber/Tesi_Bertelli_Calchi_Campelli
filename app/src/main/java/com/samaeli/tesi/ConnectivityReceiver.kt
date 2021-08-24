package com.samaeli.tesi

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.util.Log

/*
    Connectivity receiver che ha il compito di controllare che il telefono sia connesso ad internet.
    Se non lo è viene visualizzata una pagina di errore
 */
class ConnectivityReceiver : BroadcastReceiver() {

    companion object{
        val TAG = "Connectivity Receiver"
        var status = false
    }

    override fun onReceive(context: Context, intent: Intent?) {
        Log.d("News reader", "Connectivity changed")
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo

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