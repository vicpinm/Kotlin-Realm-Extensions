package com.vicpin.kotlinrealmextensions.view

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.vicpin.kotlinrealmextensions.R
import com.vicpin.kotlinrealmextensions.extensions.toast
import com.vicpin.kotlinrealmextensions.model.Item
import com.vicpin.krealmextensions.allItems
import com.vicpin.krealmextensions.where

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        toast("Items count: " + Item().where { query -> query.lessThan("name",1) })

    }
}
