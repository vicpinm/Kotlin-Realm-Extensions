package com.vicpin.krealmextensions

import android.os.Looper
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposables
import io.realm.Realm
import io.realm.RealmObject
import io.realm.RealmQuery
import io.realm.Sort

/**
 * Created by victor on 2/1/17.
 * Extensions for Realm. All methods here are asynchronous and return Flowable from rxjava2.
 */


/**
 * Query for all entities and observe changes returning a Flowable.
 */
fun <T : RealmObject> T.queryAllAsFlowable() = performQuery()

/**
 * Query for entities in database asynchronously and observe changes returning a Flowable.
 */
fun <T : RealmObject> T.queryAsFlowable(query: Query<T>) = performQuery(query =  query)

/**
 * Query for sorted entities and observe changes returning a Flowable.
 */
fun <T : RealmObject> T.querySortedAsFlowable(fieldName : List<String>, order : List<Sort>, query: Query<T>? = null) = performQuery(fieldName, order, query)

/**
 * Query for sorted entities and observe changes returning a Flowable.
 */
fun <T : RealmObject> T.querySortedAsFlowable(fieldName : String, order : Sort, query: Query<T>? = null) = performQuery(listOf(fieldName), listOf(order), query)

private fun <T: RealmObject> T.performQuery(fieldName : List<String>? = null, order : List<Sort>? = null, query: Query<T>? = null): Flowable<List<T>> {
    val looper = getLooper()

    return Flowable.create<List<T>>({ emitter ->

        val realm = RealmConfigStore.fetchConfiguration(javaClass).realm()
        val realmQuery: RealmQuery<T> = RealmQuery.createQuery(realm, this.javaClass)
        query?.invoke(realmQuery)

        val result = if(fieldName != null && order != null ) {
            realmQuery.findAllSortedAsync(fieldName.toTypedArray(), order.toTypedArray())
        }
        else {
            realmQuery.findAllAsync()
        }

        result.addChangeListener { it ->
            emitter.onNext(realm.copyFromRealm(it))
        }

        emitter.setDisposable(Disposables.fromAction {
            result.removeAllChangeListeners()
            realm.close()
            if (Looper.getMainLooper() != looper) {
                looper?.thread?.interrupt()
            }
        })
    }, BackpressureStrategy.LATEST)
            .subscribeOn(AndroidSchedulers.from(looper))
            .unsubscribeOn(AndroidSchedulers.from(looper))
}

internal fun getLooper(): Looper? {
    return if (Looper.myLooper() != Looper.getMainLooper()) {
        val backgroundThread = BackgroundThread()
        backgroundThread.start()
        backgroundThread.looper
    } else {
        Looper.getMainLooper()
    }
}
