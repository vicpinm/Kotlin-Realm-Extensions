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
import io.realm.Sort

/**
 * Created by victor on 2/1/17.
 * Extensions for Realm. All methods here are asynchronous and return Flowable from rxjava2.
 */

/**
 * Query for all entities and observe changes returning a Flowable.
 */
fun <T : RealmModel> T.queryAllAsFlowable() = flowableQuery()
inline fun <reified T : RealmModel> queryAllAsFlowable() = flowableQuery<T>()

/**
 * Query for entities in database asynchronously and observe changes returning a Flowable.
 */
fun <T : RealmModel> T.queryAsFlowable(query: Query<T>) = flowableQuery(query = query)
inline fun <reified T : RealmModel> queryAsFlowable(noinline query: Query<T>) = flowableQuery(query = query)

/**
 * Query for sorted entities and observe changes returning a Flowable.
 */
fun <T : RealmModel> T.querySortedAsFlowable(fieldName: List<String>, order: List<Sort>, query: Query<T>? = null) = flowableQuery(fieldName, order, query)
inline fun <reified T : RealmModel> querySortedAsFlowable(fieldName: List<String>, order: List<Sort>, noinline query: Query<T>? = null) = flowableQuery(fieldName, order, query)

/**
 * Query for sorted entities and observe changes returning a Flowable.
 */
fun <T : RealmModel> T.querySortedAsFlowable(fieldName: String, order: Sort, query: Query<T>? = null) = flowableQuery(listOf(fieldName), listOf(order), query)
inline fun <reified T : RealmModel> querySortedAsFlowable(fieldName: String, order: Sort, noinline query: Query<T>? = null) = flowableQuery(listOf(fieldName), listOf(order), query)

/**
 * INTERNAL FUNCTIONS
 */
private fun <T : RealmModel> T.flowableQuery(fieldName: List<String>? = null, order: List<Sort>? = null, query: Query<T>? = null) = performFlowableQuery(fieldName, order, query, this.javaClass)
@PublishedApi internal inline fun <reified T : RealmModel> flowableQuery(fieldName: List<String>? = null, order: List<Sort>? = null, noinline query: Query<T>? = null) = performFlowableQuery(fieldName, order, query, T::class.java)

@PublishedApi internal fun <T:RealmModel> performFlowableQuery(fieldName: List<String>? = null, order: List<Sort>? = null, query: Query<T>? = null, javaClass : Class<T>): Flowable<List<T>> {
    return prepareObservableQuery(javaClass, { realm, subscriber ->
        val realmQuery = realm.where(javaClass)
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

private class BackgroundThread : HandlerThread("Scheduler-Realm-BackgroundThread",
        Process.THREAD_PRIORITY_BACKGROUND)

private inline fun <D : RealmModel, T : Any> prepareObservableQuery(clazz: Class<D>, crossinline closure: (Realm, FlowableEmitter<in T>) -> Disposable): Flowable<T> {
    var realm: Realm? = null
    var mySubscription: Disposable? = null

    var backgroundThread: HandlerThread? = null
    val looper: Looper = if (Looper.myLooper() == null) {
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
