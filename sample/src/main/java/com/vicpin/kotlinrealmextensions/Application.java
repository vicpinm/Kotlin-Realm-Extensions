package com.vicpin.kotlinrealmextensions;

import com.vicpin.kotlinrealmextensions.model.Address;
import com.vicpin.kotlinrealmextensions.model.User;
import com.vicpin.krealmextensions.RealmConfigStore;

import io.realm.Realm;
import io.realm.RealmConfiguration;

/**
 * Created by victor on 2/1/17.
 */

public class Application extends android.app.Application {

    @Override
    public void onCreate() {
        super.onCreate();

        Realm.init(this);
        RealmConfiguration userAddressConfig = new RealmConfiguration.Builder().name("user-db").schemaVersion(1).deleteRealmIfMigrationNeeded().build();
        RealmConfigStore.Companion.init(User.class, userAddressConfig);
        RealmConfigStore.Companion.init(Address.class, userAddressConfig);
    }
}
