package com.vicpin.krealmextensions

import io.realm.Realm
import io.realm.RealmConfiguration
import io.realm.RealmObject

/**
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
            var cfg = configMap[modelClass]
            return configMap[modelClass] ?: RealmConfiguration.Builder().build()
        }
    }

}

inline fun <T> RealmConfiguration.use(block: (Realm) -> T): T {
    return Realm.getInstance(this).use { return block(it) }
}