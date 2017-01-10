package com.vicpin.krealmextensions.model

import io.realm.RealmObject

/**
 * Created by victor on 10/1/17.
 * Only for test purposes. It's placed here instead of in test folder due to problems with realm plugin and kotlin plugin.
 */
open class TestEntity() : RealmObject() {

    var name: String = ""

    constructor(name : String) : this(){
        this.name = name
    }

}
