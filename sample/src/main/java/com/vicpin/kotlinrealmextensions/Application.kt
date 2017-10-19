package com.vicpin.kotlinrealmextensions

import com.vicpin.kotlinrealmextensions.model.Address
import com.vicpin.kotlinrealmextensions.model.User
import com.vicpin.krealmextensions.RealmConfigStore

import io.realm.Realm
import io.realm.RealmConfiguration

/**
 * Created by victor on 2/1/17.
 */

class Application : android.app.Application() {

    override fun onCreate() {
        super.onCreate()

        Realm.init(this)
        val userAddressConfig = RealmConfiguration.Builder().name("user-db").schemaVersion(1).deleteRealmIfMigrationNeeded().build()
        RealmConfigStore.init(User::class.java, userAddressConfig)
        RealmConfigStore.init(Address::class.java, userAddressConfig)
    }

}
