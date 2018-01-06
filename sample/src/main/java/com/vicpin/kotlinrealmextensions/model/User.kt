package com.vicpin.kotlinrealmextensions.model

import io.realm.RealmModel
import io.realm.annotations.RealmClass

/**
 * Created by magillus on 8/14/2017.
 */
@RealmClass
open class User(var name: String? = null, var address: Address? = Address()) : RealmModel

@RealmClass
open class Address(var street: String? = null, var city: String? = null, var zip: String? = null) : RealmModel