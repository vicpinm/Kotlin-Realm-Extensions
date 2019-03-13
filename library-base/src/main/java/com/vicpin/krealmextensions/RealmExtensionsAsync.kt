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
fun <T : RealmModel> T.queryFirstAsync(callback: (T?) -> Unit) = queryFirstAsync(callback, this.javaClass)

inline fun <reified T : RealmModel> queryFirstAsync(noinline callback: (T?) -> Unit) = queryFirstAsync(callback, T::class.java)

@PublishedApi
internal fun <T : RealmModel> queryFirstAsync(callback: (T?) -> Unit, javaClass: Class<T>) {
    onLooperThread {

        val realm = getRealmInstance(javaClass)

        val result = realm.where(javaClass).findFirstAsync()
        RealmObject.addChangeListener(result) { it ->
            callback(if (RealmObject.isValid(it)) realm.copyFromRealm(it) else null)
            RealmObject.removeAllChangeListeners(result)
            realm.close()
            if (isRealmThread()) {
                Looper.myLooper().thread?.interrupt()
            }
        }
    }
}

/**
 * Returns last entity in database asynchronously.
 */
fun <T : RealmModel> T.queryLastAsync(callback: (T?) -> Unit) {
    queryAllAsync {
        callback(if (it.isNotEmpty() && RealmObject.isValid(it.last())) it.last() else null)
    }
}

inline fun <reified T : RealmModel> queryLastAsync(crossinline callback: (T?) -> Unit) {
    queryAllAsync<T> {
        callback(if (it.isNotEmpty() && RealmObject.isValid(it.last())) it.last() else null)
    }
}

/**
 * Returns all entities in database asynchronously.
 */
fun <T : RealmModel> T.queryAllAsync(callback: (List<T>) -> Unit) = queryAllAsync(callback, this.javaClass)

inline fun <reified T : RealmModel> queryAllAsync(noinline callback: (List<T>) -> Unit) = queryAllAsync(callback, T::class.java)

@PublishedApi
internal fun <T : RealmModel> queryAllAsync(callback: (List<T>) -> Unit, javaClass: Class<T>) {
    onLooperThread {

        val realm = getRealmInstance(javaClass)

        val result = realm.where(javaClass).findAllAsync()

        result.addChangeListener { it ->
            callback(realm.copyFromRealm(it))
            result.removeAllChangeListeners()
            realm.close()
            if (isRealmThread()) {
                Looper.myLooper().thread?.interrupt()
            }
        }
    }
}

/**
 * Queries for entities in database asynchronously.
 */
fun <T : RealmModel> T.queryAsync(query: Query<T>, callback: (List<T>) -> Unit) = queryAsync(query, callback, this.javaClass)

inline fun <reified T : RealmModel> queryAsync(noinline query: Query<T>, noinline callback: (List<T>) -> Unit) = queryAsync(query, callback, T::class.java)

@PublishedApi
internal fun <T : RealmModel> queryAsync(query: Query<T>, callback: (List<T>) -> Unit, javaClass: Class<T>) {
    onLooperThread {

        val realm = getRealmInstance(javaClass)
        val realmQuery = realm.where(javaClass)
        query(realmQuery)
        val result = realmQuery.findAllAsync()
        result.addChangeListener { it ->
            callback(realm.copyFromRealm(it))
            result.removeAllChangeListeners()
            realm.close()
            if (isRealmThread()) {
                Looper.myLooper().thread?.interrupt()
            }
        }
    }
}

fun onLooperThread(block: () -> Unit) {
    if (Looper.myLooper() != null) {
        block()
    } else {
        Handler(getLooper()).post(block)
    }
}
