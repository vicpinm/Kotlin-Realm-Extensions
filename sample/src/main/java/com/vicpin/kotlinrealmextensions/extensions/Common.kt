package com.vicpin.kotlinrealmextensions.extensions

import android.app.Activity
import android.widget.Toast

/**
 * Created by victor on 2/1/17.
 */
fun Activity.toast(text : String){
    Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
}
