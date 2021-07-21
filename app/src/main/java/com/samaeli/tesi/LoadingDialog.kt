package com.samaeli.tesi

import android.app.Activity
import android.app.AlertDialog

// This class display LoadingDialog

class LoadingDialog(private var activity: Activity) {

    private var dialog : AlertDialog? = null

    fun startLoadingDialog(){
        val builder = AlertDialog.Builder(activity)

        val inflater = activity.layoutInflater
        builder.setView(inflater.inflate(R.layout.loading_dialog,null))
        builder.setCancelable(false)

        dialog = builder.create()
        dialog?.show()
    }

    fun dismissLoadingDialog(){
        dialog?.dismiss()
    }

}