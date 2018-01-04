package com.vicpin.krealmextensions

import android.util.Log
import io.realm.Realm
import io.realm.RealmConfiguration
import io.realm.RealmModel
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
                configMap.put(modelClass, realmCfg)
            }
        }
        fun <T : Any> initModule(cls: Class<T>, realmCfg: RealmConfiguration) {
            var annotation = cls.annotations.filter { it.annotationClass.qualifiedName == RealmModule::classes.name }
                    .firstOrNull()
            if (annotation != null) {
                Log.i("RealmConfigStore", "Got annotation in module " + annotation)
                var v: RealmModule = annotation as RealmModule
                v.classes.filter { cls ->
                    cls.isInstance(RealmObject::class)
                }.forEach { cls ->

                    init(cls as Class<RealmObject>, realmCfg)
                }

            }
        }
        /**
         * Fetches realm configuration for class.
         */
        fun <T : RealmModel> fetchConfiguration(modelClass: Class<T>): RealmConfiguration? {
            return configMap[modelClass]
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

inline fun <reified D : RealmModel> Array<D>.getRealmInstance(): Realm {
    return RealmConfigStore.fetchConfiguration(D::class.java)?.realm() ?: Realm.getDefaultInstance()
}

fun RealmConfiguration.realm(): Realm {
    return Realm.getInstance(this)
}
