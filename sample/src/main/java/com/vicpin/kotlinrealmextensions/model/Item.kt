package com.vicpin.kotlinrealmextensions.model

import io.realm.RealmObject

/**
 * Created by victor on 2/1/17.
 */
open class Item() : RealmObject(){

    var name : String = ""

    constructor(name : String) : this() {
        this.name = name
    }
}