package com.vicpin.krealmextensions.model

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

/**
 * Created by victor on 10/1/17.
 * Only for test purposes. It's placed here instead of in test folder due to problems with realm plugin and kotlin plugin.
 */
open class TestEntityPK() : RealmObject() {

    @PrimaryKey var id: Long? = null

    var name: String = ""

    constructor(id: Long) : this() {
        this.id = id
    }

    constructor(id: Long, name: String) : this() {
        this.id = id
        this.name = name
    }
}
