package com.vicpin.krealmextensions

import android.util.Log
import io.realm.Realm
import io.realm.RealmConfiguration
import io.realm.RealmObject
import io.realm.annotations.RealmModule

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

        fun <T : RealmObject> fetchConfiguration(modelClass: Class<T>): RealmConfiguration? {
            return configMap[modelClass]
        }
    }
}

fun <T : RealmObject> T.getRealm(): Realm {
    return RealmConfigStore.fetchConfiguration(this::class.java)?.realm() ?: Realm.getDefaultInstance()
}

fun <T : RealmObject> getRealm(clazz: Class<T>): Realm {
    return RealmConfigStore.fetchConfiguration(clazz)?.realm() ?: Realm.getDefaultInstance()
}

inline fun <reified D : RealmObject, T : Collection<D>> T.getRealm(): Realm {
    return RealmConfigStore.fetchConfiguration(D::class.java)?.realm() ?: Realm.getDefaultInstance()
}

inline fun <reified D : RealmObject> Array<D>.getRealm(): Realm {
    return RealmConfigStore.fetchConfiguration(D::class.java)?.realm() ?: Realm.getDefaultInstance()
}

fun RealmConfiguration.realm(): Realm {
    return Realm.getInstance(this)
}
