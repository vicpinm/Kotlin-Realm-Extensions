package com.vicpin.kotlinrealmextensions.view

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.vicpin.kotlinrealmextensions.R
import com.vicpin.krealmextensions.firstItemAsync
import com.vicpin.krealmextensions.model.TestEntityPK
import com.vicpin.krealmextensions.save

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //***********************************
        //See tests for complete usage
        //***********************************

        populateDBWithTestEntityPK(5)
        TestEntityPK().firstItemAsync {
            it -> Log.d("Result", it?.name)
        }

    }

    private fun populateDBWithTestEntityPK(numItems : Int){
        (0..numItems - 1).forEach { TestEntityPK(it.toLong()).save() }
    }
}
