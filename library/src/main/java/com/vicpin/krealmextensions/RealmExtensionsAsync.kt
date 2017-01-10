package com.vicpin.krealmextensions

import android.os.Handler
import android.os.Looper
import io.realm.Realm
import io.realm.RealmObject
import io.realm.RealmQuery
import io.realm.RealmResults


/**
 * Created by victor on 2/1/17.
 * Extensions for Realm. All methods here are asynchronous, and only notify changes once.
 */

/**
 * Returns first entity in database asynchronously.
 */
fun <T : RealmObject> T.firstItemAsync(callback: (T?) -> Unit) {
    mainThread {

        var realm = Realm.getDefaultInstance()

        var result = RealmQuery.createQuery(realm, this.javaClass).findFirstAsync()
        result.addChangeListener<T> {
            callback(if(it != null && it.isValid) realm.copyFromRealm(it) else null)
            result.removeChangeListeners()
            realm.close()
        }
    }

}

/**
 * Returns last entity in database asynchronously.
 */
fun <T : RealmObject> T.lastItemAsync(callback: (T?) -> Unit) {
    allItemsAsync { callback(if(it.isNotEmpty() && it.last().isValid) it.last() else null) }
}


/**
 * Returns all entities in database asynchronously.
 */
fun <T : RealmObject> T.allItemsAsync(callback: (List<T>) -> Unit) {
    mainThread {

        var realm = Realm.getDefaultInstance()

        var result: RealmResults<T> = RealmQuery.createQuery(realm, this.javaClass).findAllAsync()
        result.addChangeListener {
            callback(realm.copyFromRealm(it))
            result.removeChangeListeners()
            realm.close()

        }
    }
}

/**
 * Queries for entities in database asynchronously.
 */
fun <T : RealmObject> T.whereAsync(query: (RealmQuery<T>) -> Unit, callback: (List<T>) -> Unit) {
    mainThread {

        val realm = Realm.getDefaultInstance()
        val realmQuery: RealmQuery<T> = RealmQuery.createQuery(realm, this.javaClass)
        query(realmQuery)
        val result = realmQuery.findAllAsync()
        result.addChangeListener {
            callback(realm.copyFromRealm(it))
            result.removeChangeListeners()
            realm.close()
        }
    }
}

private fun mainThread(block: () -> Unit) {
    Handler(Looper.getMainLooper()).post(block)
}




