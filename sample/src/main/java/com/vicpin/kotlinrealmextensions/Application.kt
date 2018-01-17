package com.vicpin.kotlinrealmextensions

import com.vicpin.kotlinrealmextensions.model.UserModule
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
        // clear previous data for fresh start
        Realm.deleteRealm(Realm.getDefaultConfiguration())
        Realm.deleteRealm(userAddressConfig)

        //Optional: if you want to specify your own realm configuration, you have two ways:

        //1. If you want to specify a configuration for a specific module, you can use:
        RealmConfigStore.initModule(UserModule::class.java, userAddressConfig)

        //2. You can specify any configuration per model with:
        //RealmConfigStore.init(User::class.java, userAddressConfig)
        //RealmConfigStore.init(Address::class.java, userAddressConfig)
    }
}
