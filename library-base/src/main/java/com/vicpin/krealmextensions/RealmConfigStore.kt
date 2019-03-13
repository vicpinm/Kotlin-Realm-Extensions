package com.vicpin.krealmextensions

import android.util.Log
import io.realm.Realm
import io.realm.RealmConfiguration
import io.realm.RealmModel
import io.realm.RealmObject
import io.realm.annotations.RealmModule

/**
 * Realm configuration store per class
 * Created by magillus on 8/14/2017.
 */
class RealmConfigStore {
    companion object {
        var TAG = RealmConfigStore::class.java.simpleName
        private var configMap: MutableMap<Class<out RealmModel>, RealmConfiguration> = HashMap()

        /**
         * Initialize realm configuration for class
         */
        fun <T : RealmModel> init(modelClass: Class<T>, realmCfg: RealmConfiguration) {
            Log.d(TAG, "Adding class $modelClass to realm ${realmCfg.realmFileName}")
            if (!configMap.containsKey(modelClass)) {
                configMap[modelClass] = realmCfg
            }
        }

        fun <T : Any> initModule(cls: Class<T>, realmCfg: RealmConfiguration) {
            // check if class of the module
            val annotation = cls.annotations.firstOrNull { it.annotationClass.java.name == RealmModule::class.java.name }

            if (annotation != null) {
                Log.i("RealmConfigStore", "Got annotation in module " + annotation)
                val moduleAnnotation = annotation as RealmModule
                moduleAnnotation.classes.filter { cls ->
                    cls.java.interfaces.contains(RealmModel::class.java)
                }.forEach { cls ->
                    init(cls.java as Class<RealmModel>, realmCfg)
                }
                moduleAnnotation.classes.filter { cls ->
                    cls.java.superclass == RealmObject::class.java
                }.forEach { cls ->
                    init(cls.java as Class<RealmObject>, realmCfg)
                }
            }
        }

        /**
         * Fetches realm configuration for class.
         */
        fun <T : RealmModel> fetchConfiguration(modelClass: Class<T>): RealmConfiguration? {
            return configMap[modelClass]
        }


        /**
         * Clear configuration map
         */
        fun clearConfigurations() {
            configMap.clear()
        }

        /**
         * Remove configuration for class
         */
        fun <T : RealmModel> removeConfigurationFor(modelClass: Class<T>) {
            configMap.remove(modelClass)
        }
    }
}

fun <T : RealmModel> T.getRealmInstance(): Realm {
    return RealmConfigStore.fetchConfiguration(this::class.java)?.realm() ?: Realm.getDefaultInstance()
}

fun <T : RealmModel> getRealmInstance(clazz: Class<T>): Realm {
    return RealmConfigStore.fetchConfiguration(clazz)?.realm() ?: Realm.getDefaultInstance()
}

inline fun <reified D : RealmModel, T : Collection<D>> T.getRealmInstance(): Realm {
    return RealmConfigStore.fetchConfiguration(D::class.java)?.realm() ?: Realm.getDefaultInstance()
}

inline fun <reified T : RealmModel> getRealmInstance(): Realm {
    return RealmConfigStore.fetchConfiguration(T::class.java)?.realm() ?: Realm.getDefaultInstance()
}

inline fun <reified D : RealmModel> Array<D>.getRealmInstance(): Realm {
    return RealmConfigStore.fetchConfiguration(D::class.java)?.realm() ?: Realm.getDefaultInstance()
}

fun RealmConfiguration.realm(): Realm {
    return Realm.getInstance(this)
}
