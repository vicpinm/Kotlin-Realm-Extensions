package com.vicpin.krealmextensions

import android.os.Handler
import android.os.Looper
import io.realm.RealmModel
import io.realm.RealmObject


/**
 * Created by victor on 2/1/17.
 * Extensions for Realm. All methods here are asynchronous, and only notify changes once.
 */


/**
 * Returns first entity in database asynchronously.
 */
fun <T : RealmModel> T.queryFirstAsync(callback: (T?) -> Unit) {
    mainThread {

        val realm = getRealmInstance()

        val result = realm.where(this.javaClass).findFirstAsync()
        RealmObject.addChangeListener(result, { it ->
            callback(if (it != null && RealmObject.isValid(it)) realm.copyFromRealm(it) else null)
            RealmObject.removeAllChangeListeners(result)
            realm.close()
        })
    }
}

/**
 * Returns last entity in database asynchronously.
 */
fun <T : RealmModel> T.queryLastAsync(callback: (T?) -> Unit) {
    queryAllAsync { callback(if (it.isNotEmpty() && RealmObject.isValid(it.last())) it.last() else null) }
}

/**
 * Returns all entities in database asynchronously.
 */
fun <T : RealmModel> T.queryAllAsync(callback: (List<T>) -> Unit) {
    mainThread {

        val realm = getRealmInstance()

        val result = realm.where(this.javaClass).findAllAsync()

        result.addChangeListener { it ->
            callback(realm.copyFromRealm(it))
            result.removeAllChangeListeners()
            realm.close()
        }
    }
}

/**
 * Queries for entities in database asynchronously.
 */
fun <T : RealmModel> T.queryAsync(query: Query<T>, callback: (List<T>) -> Unit) {
    mainThread {

        val realm = getRealmInstance()
        val realmQuery = realm.where(this.javaClass)
        query(realmQuery)
        val result = realmQuery.findAllAsync()
        result.addChangeListener { it ->
            callback(realm.copyFromRealm(it))
            result.removeAllChangeListeners()
            realm.close()
        }
    }
}

private fun mainThread(block: () -> Unit) {
    Handler(Looper.getMainLooper()).post(block)
}




