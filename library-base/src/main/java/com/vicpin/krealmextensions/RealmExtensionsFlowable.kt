package com.vicpin.krealmextensions

import android.annotation.SuppressLint
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
import io.realm.Sort

/**
 * Created by victor on 2/1/17.
 * Extensions for Realm. All methods here are asynchronous and return Flowable from rxjava2.
 */

/**
 * Query for all entities and observe changes returning a Flowable.
 */
fun <T : RealmModel> T.queryAllAsFlowable() = performQuery()
inline fun <reified T : RealmModel> queryAllAsFlowable() = performQuery<T>()

/**
 * Query for entities in database asynchronously and observe changes returning a Flowable.
 */
fun <T : RealmModel> T.queryAsFlowable(query: Query<T>) = performQuery(query = query)
inline fun <reified T : RealmModel> queryAsFlowable(noinline query: Query<T>) = performQuery(query = query)

/**
 * Query for sorted entities and observe changes returning a Flowable.
 */
fun <T : RealmModel> T.querySortedAsFlowable(fieldName: List<String>, order: List<Sort>, query: Query<T>? = null) = performQuery(fieldName, order, query)
inline fun <reified T : RealmModel> querySortedAsFlowable(fieldName: List<String>, order: List<Sort>, noinline query: Query<T>? = null) = performQuery(fieldName, order, query)

/**
 * Query for sorted entities and observe changes returning a Flowable.
 */
fun <T : RealmModel> T.querySortedAsFlowable(fieldName: String, order: Sort, query: Query<T>? = null) = performQuery(listOf(fieldName), listOf(order), query)
inline fun <reified T : RealmModel> querySortedAsFlowable(fieldName: String, order: Sort, noinline query: Query<T>? = null) = performQuery(listOf(fieldName), listOf(order), query)

@SuppressLint("CheckResult")
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

inline fun <reified T : RealmModel> performQuery(fieldName: List<String>? = null, order: List<Sort>? = null, noinline query: Query<T>? = null): Flowable<List<T>> {

    return prepareObservableQuery(T::class.java, { realm, subscriber ->
        val realmQuery = realm.where(T::class.java)
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

@SuppressLint("CheckResult")
inline fun <D : RealmModel, T : Any> prepareObservableQuery(clazz: Class<D>, crossinline closure: (Realm, FlowableEmitter<in T>) -> Disposable): Flowable<T> {
    var realm: Realm? = null
    var mySubscription: Disposable? = null

    var backgroundThread: HandlerThread? = null
    val looper: Looper = if (Looper.myLooper() != Looper.getMainLooper()) {
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


//Added annotation based on recommendation here: https://stackoverflow.com/a/41905907
@PublishedApi internal fun getLooper(): Looper? {
    return if (Looper.myLooper() != Looper.getMainLooper()) {
        val backgroundThread = HandlerThread("Scheduler-Realm-BackgroundThread",
                Process.THREAD_PRIORITY_BACKGROUND)
        backgroundThread.start()
        backgroundThread.looper
    } else {
        Looper.getMainLooper()
    }
}
