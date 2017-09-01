package com.vicpin.krealmextensions.model

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

/**
 * Example entities added for testing separate configuration usage
 * Created by magillus on 8/31/2017.
 */

open class TestEntitySeparate(var name: String = "") : RealmObject()

open class TestEntitySeparatePK(@PrimaryKey var id: Long? = null, var name: String = "") : RealmObject()