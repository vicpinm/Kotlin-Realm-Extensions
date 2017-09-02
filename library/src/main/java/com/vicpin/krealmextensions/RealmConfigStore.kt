package com.vicpin.krealmextensions

import io.realm.Realm
import io.realm.RealmConfiguration
import io.realm.RealmObject

/**
 * Realm configuration store per class
 * Created by magillus on 8/14/2017.
 */
class RealmConfigStore {
    companion object {
        private var configMap: MutableMap<Class<out RealmObject>, RealmConfiguration> = HashMap()

        fun <T : RealmObject> init(modelClass: Class<T>, realmCfg: RealmConfiguration) {
            if (!configMap.containsKey(modelClass)) {
                configMap.put(modelClass, realmCfg)
            }
        }

        fun <T : RealmObject> fetchConfiguration(modelClass: Class<T>): RealmConfiguration {
            return configMap[modelClass] ?: RealmConfiguration.Builder().build()
        }
    }
}

fun RealmConfiguration.realm(): Realm {
    return Realm.getInstance(this)
}

fun <T> RealmConfiguration.use(block: (Realm) -> T): T {
    return Realm.getInstance(this).use { return@use block(it) }
}