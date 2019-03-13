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
fun <T : RealmModel> T.queryAllAsFlowable(managed: Boolean = false) = flowableQuery(managed = managed)

inline fun <reified T : RealmModel> queryAllAsFlowable(managed: Boolean = false) = flowableQuery<T>(managed = managed)

/**
 * Query for entities in database asynchronously and observe changes returning a Flowable.
 */
fun <T : RealmModel> T.queryAsFlowable(managed: Boolean = false, query: Query<T>) = flowableQuery(managed = managed, query = query)

inline fun <reified T : RealmModel> queryAsFlowable(managed: Boolean = false, noinline query: Query<T>) = flowableQuery(managed = managed, query = query)

/**
 * Query for sorted entities and observe changes returning a Flowable.
 */
fun <T : RealmModel> T.querySortedAsFlowable(fieldName: List<String>, order: List<Sort>, managed: Boolean = false, query: Query<T>? = null) = flowableQuery(fieldName, order, managed, query)

inline fun <reified T : RealmModel> querySortedAsFlowable(fieldName: List<String>, order: List<Sort>, managed: Boolean = false, noinline query: Query<T>? = null) = flowableQuery(fieldName, order, managed, query)

/**
 * Query for sorted entities and observe changes returning a Flowable.
 */
fun <T : RealmModel> T.querySortedAsFlowable(fieldName: String, order: Sort, managed: Boolean = false, query: Query<T>? = null) = flowableQuery(listOf(fieldName), listOf(order), managed, query)

inline fun <reified T : RealmModel> querySortedAsFlowable(fieldName: String, order: Sort, managed: Boolean = false, noinline query: Query<T>? = null) = flowableQuery(listOf(fieldName), listOf(order), managed, query)

/**
 * INTERNAL FUNCTIONS
 */
private fun <T : RealmModel> T.flowableQuery(fieldName: List<String>? = null, order: List<Sort>? = null, managed: Boolean = false, query: Query<T>? = null) = performFlowableQuery(fieldName, order, managed, query, this.javaClass)

@PublishedApi
internal inline fun <reified T : RealmModel> flowableQuery(fieldName: List<String>? = null, order: List<Sort>? = null, managed: Boolean = false, noinline query: Query<T>? = null) = performFlowableQuery(fieldName, order, managed, query, T::class.java)

@PublishedApi
internal fun <T : RealmModel> performFlowableQuery(fieldName: List<String>? = null, order: List<Sort>? = null, managed: Boolean = false, query: Query<T>? = null, javaClass: Class<T>): Flowable<List<T>> {
    return prepareObservableQuery(javaClass) { realm, subscriber ->
        val realmQuery = realm.where(javaClass)
        query?.invoke(realmQuery)

        val result = if (fieldName != null && order != null) {
            realmQuery.sort(fieldName.toTypedArray(), order.toTypedArray()).findAllAsync()
        } else {
            realmQuery.findAllAsync()
        }

        result.asFlowable()
                .filter { it.isLoaded }
                .map { if (!managed) realm.copyFromRealm(it) else it }
                .subscribe({
                    subscriber.onNext(it)
                }, { subscriber.onError(it) })
    }
}

private inline fun <D : RealmModel, T : Any> prepareObservableQuery(clazz: Class<D>, crossinline closure: (Realm, FlowableEmitter<in T>) -> Disposable): Flowable<T> {
    var realm: Realm? = null
    var mySubscription: Disposable? = null

    val looper = getLooper()

    return Flowable.defer {
        Flowable.create(FlowableOnSubscribe<T> {
            realm = getRealmInstance(clazz)
            mySubscription = closure(realm!!, it)
        }, BackpressureStrategy.BUFFER)
                .doFinally {
                    realm?.close()
                    mySubscription?.dispose()
                    if (isRealmThread()) {
                        looper?.thread?.interrupt()
                    }
                }
                .unsubscribeOn(AndroidSchedulers.from(looper))
                .subscribeOn(AndroidSchedulers.from(looper))
    }
}

const val REALM_THREAD_NAME = "Scheduler-Realm-BackgroundThread"

fun isRealmThread() = Thread.currentThread().name == REALM_THREAD_NAME

internal fun getLooper(): Looper? {
    return if (Looper.myLooper() == null) {
        val backgroundThread = HandlerThread(REALM_THREAD_NAME,
                Process.THREAD_PRIORITY_BACKGROUND)
        backgroundThread.start()
        backgroundThread.looper
    } else {
        Looper.myLooper()
    }
}
