package com.vicpin.kotlinrealmextensions;

import io.realm.Realm;

/**
 * Created by victor on 2/1/17.
 */

public class Application extends android.app.Application {

    @Override
    public void onCreate() {
        super.onCreate();

        Realm.init(this);
    }
}
