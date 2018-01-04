package com.vicpin.krealmextensions

import android.os.HandlerThread
import android.os.Looper
import android.os.Process
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.FlowableEmitter
import io.reactivex.FlowableOnSubscribe
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.realm.Realm
import io.realm.RealmModel
import io.realm.RealmObject
import io.realm.Sort

/**
 * Created by victor on 2/1/17.
 * Extensions for Realm. All methods here are asynchronous and return Flowable from rxjava2.
 */

/**
 * Query for all entities and observe changes returning a Flowable.
 */
fun <T : RealmModel> T.queryAllAsFlowable() = performQuery()

/**
 * Query for entities in database asynchronously and observe changes returning a Flowable.
 */
fun <T : RealmModel> T.queryAsFlowable(query: Query<T>) = performQuery(query = query)

/**
 * Query for sorted entities and observe changes returning a Flowable.
 */
fun <T : RealmModel> T.querySortedAsFlowable(fieldName: List<String>, order: List<Sort>, query: Query<T>? = null) = performQuery(fieldName, order, query)

/**
 * Query for sorted entities and observe changes returning a Flowable.
 */
fun <T : RealmModel> T.querySortedAsFlowable(fieldName: String, order: Sort, query: Query<T>? = null) = performQuery(listOf(fieldName), listOf(order), query)

private fun <T : RealmModel> T.performQuery(fieldName: List<String>? = null, order: List<Sort>? = null, query: Query<T>? = null): Flowable<List<T>> {

    return prepareObservableQuery(javaClass, { realm, subscriber ->
        val realmQuery = realm.where(this.javaClass)
        query?.invoke(realmQuery)

        val result = if (fieldName != null && order != null) {
            realmQuery.findAllSortedAsync(fieldName.toTypedArray(), order.toTypedArray())
        } else {
            realmQuery.findAllAsync()
        }

        result.asFlowable()
                .filter { it.isLoaded }
                .map { realm.copyFromRealm(it) }
                .subscribe({
                    subscriber.onNext(it)
                }, { subscriber.onError(it) })
    })
}

class BackgroundThread : HandlerThread("Scheduler-Realm-BackgroundThread",
        Process.THREAD_PRIORITY_BACKGROUND)

private fun <D : RealmModel, T : Any> prepareObservableQuery(clazz: Class<D>, closure: (Realm, FlowableEmitter<in T>) -> Disposable): Flowable<T> {
    var realm: Realm? = null
    var mySubscription: Disposable? = null

    var backgroundThread: HandlerThread? = null
    var looper: Looper = if (Looper.myLooper() != Looper.getMainLooper()) {
        backgroundThread = BackgroundThread()
        backgroundThread.start()
        backgroundThread.looper
    } else {
        Looper.getMainLooper()
    }

    return Flowable.defer {
        Flowable.create(FlowableOnSubscribe<T> {
            realm = getRealmInstance(clazz)
            mySubscription = closure(realm!!, it)
        }, BackpressureStrategy.BUFFER)
                .doOnCancel {
                    realm?.close()
                    mySubscription?.dispose()
                    backgroundThread?.interrupt()
                }
                .unsubscribeOn(AndroidSchedulers.from(looper))
                .subscribeOn(AndroidSchedulers.from(looper))
    }

}

internal fun getLooper(): Looper? {
    return if (Looper.myLooper() != Looper.getMainLooper()) {
        val backgroundThread = HandlerThread("Scheduler-Realm-BackgroundThread",
                Process.THREAD_PRIORITY_BACKGROUND)
        backgroundThread.start()
        backgroundThread.looper
    } else {
        Looper.getMainLooper()
    }
}
