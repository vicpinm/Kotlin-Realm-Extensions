package com.vicpin.krealmextensions

import android.util.Log
import io.realm.Realm
import io.realm.RealmConfiguration
import io.realm.RealmModel
import io.realm.RealmObject
import io.realm.annotations.RealmModule
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf

/**
 * Realm configuration store per class
 * Created by magillus on 8/14/2017.
 */
class RealmConfigStore {
    companion object {
        var TAG = RealmConfigStore::class.java.simpleName
        private var configMap: MutableMap<Class<out RealmModel>, RealmConfiguration> = HashMap()

        /**
         * Initialize realm configuration for module.
         */
        fun initModule(module: Any, realmCfg: RealmConfiguration) {
            Log.d(TAG, "Initialize classes from module ${module.javaClass.name}")
            if (module::class.java.isAnnotationPresent(RealmModule::class.java)) {
                var realmModuleAnnotation = module::class.java.getAnnotation(RealmModule::class.java)
                if (realmModuleAnnotation.allClasses == true) {
                    // todo use this config as default??
                    // what if there is more modules with allClasses?
                }
                realmModuleAnnotation.classes.forEach { kclz ->
                    try {
                        if (kclz.isSubclassOf(RealmModel::class)) {
                            val clz = kclz as KClass<RealmModel>
                            init(clz.java, realmCfg)
                        } else {
                            Log.w(TAG, "Class not for Realm")
                        }
                    } catch (ex: Exception) {
                        Log.e(TAG, "Error testing class in RealmModel for compatibility", ex)
                    }
                }
            } else {
                Log.w(TAG, "Module ${module.javaClass.name} is not RealmModule")
            }
        }

        /**
         * Initialize realm configuration for class
         */
        fun <T : RealmModel> init(modelClass: Class<T>, realmCfg: RealmConfiguration) {
            Log.d(TAG, "Adding class $modelClass to realm ${realmCfg.realmFileName}")
            if (!configMap.containsKey(modelClass)) {
                configMap.put(modelClass, realmCfg)
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
