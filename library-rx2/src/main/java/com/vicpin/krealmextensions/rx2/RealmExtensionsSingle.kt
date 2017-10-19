package com.vicpin.krealmextensions.rx2

import android.os.Looper
import com.vicpin.krealmextensions.RealmConfigStore
import com.vicpin.krealmextensions.realm
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposables
import io.realm.Realm
import io.realm.RealmObject
import io.realm.RealmQuery
import io.realm.Sort


/**
 * Query for all items and listen to changes returning an Single.
 */
fun <T : RealmObject> T.queryAllAsSingle() = performQuery()

/**
 * Queries for entities in database asynchronously, and observe changes returning an Single.
 */
fun <T : RealmObject> T.queryAsSingle(query: Query<T>) = performQuery(query = query)

/**
 * Query for sorted entities and observe changes returning a Single.
 */
fun <T : RealmObject> T.querySortedAsSingle(fieldName : List<String>, order : List<Sort>, query: Query<T>? = null) = performQuery(fieldName, order, query)

/**
 * Query for sorted entities and observe changes returning a Single.
 */
fun <T : RealmObject> T.querySortedAsSingle(fieldName : String, order : Sort, query: Query<T>? = null) = performQuery(listOf(fieldName), listOf(order), query)

private fun <T : RealmObject> T.performQuery(fieldName : List<String>? = null, order : List<Sort>? = null, query: Query<T>? = null): Single<List<T>> {
    val looper = getLooper()
    return Single.create<List<T>>({ emitter ->

        val realm = RealmConfigStore.fetchConfiguration(javaClass)?.realm() ?: Realm.getDefaultInstance()
        val realmQuery: RealmQuery<T> = RealmQuery.createQuery(realm, this.javaClass)
        query?.invoke(realmQuery)

        val result = if(fieldName != null && order != null ) {
            realmQuery.findAllSortedAsync(fieldName.toTypedArray(), order.toTypedArray())
        }
        else {
            realmQuery.findAllAsync()
        }

        result.addChangeListener { it ->
            emitter.onSuccess(realm.copyFromRealm(it))
        }

        emitter.setDisposable(Disposables.fromAction {
            result.removeAllChangeListeners()
            realm.close()
            if (Looper.getMainLooper() != looper) {
                looper?.thread?.interrupt()
            }
        })
    }).subscribeOn(AndroidSchedulers.from(looper))
            .unsubscribeOn(AndroidSchedulers.from(looper))
}