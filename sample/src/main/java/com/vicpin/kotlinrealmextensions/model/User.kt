package com.vicpin.kotlinrealmextensions.model

import io.realm.RealmModel
import io.realm.RealmObject
import io.realm.annotations.RealmClass
import io.realm.annotations.RealmModule

/**
 * Created by magillus on 8/14/2017.
 */
@RealmClass
open class User(var name: String? = null, var address: Address? = Address()) : RealmModel

open class Address(var street: String? = null, var city: String? = null, var zip: String? = null) : RealmObject()

@RealmModule(classes = [(User::class), (Address::class)])
class UserModule