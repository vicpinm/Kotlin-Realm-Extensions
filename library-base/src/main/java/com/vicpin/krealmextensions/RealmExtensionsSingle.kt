package com.vicpin.krealmextensions

import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposables
import io.realm.Realm
import io.realm.RealmModel
import io.realm.RealmQuery
import io.realm.Sort


/**
 * Query for all items and listen to changes returning an Single.
 */
fun <T : RealmModel> T.queryAllAsSingle() = performQuery()
inline fun <reified T : RealmModel> queryAllAsSingle() = performQueryAndReturnSingle<T>()

/**
 * Queries for entities in database asynchronously, and observe changes returning an Single.
 */
fun <T : RealmModel> T.queryAsSingle(query: Query<T>) = performQuery(query = query)
inline fun <reified T : RealmModel> queryAsSingle(noinline query: Query<T>) = performQueryAndReturnSingle(query = query)

/**
 * Query for sorted entities and observe changes returning a Single.
 */
fun <T : RealmModel> T.querySortedAsSingle(fieldName : List<String>, order : List<Sort>, query: Query<T>? = null) = performQuery(fieldName, order, query)
inline fun <reified T : RealmModel> querySortedAsSingle(fieldName : List<String>, order : List<Sort>, noinline query: Query<T>? = null) = performQueryAndReturnSingle(fieldName, order, query)

/**
 * Query for sorted entities and observe changes returning a Single.
 */
fun <T : RealmModel> T.querySortedAsSingle(fieldName : String, order : Sort, query: Query<T>? = null) = performQuery(listOf(fieldName), listOf(order), query)
inline fun <reified T : RealmModel> querySortedAsSingle(fieldName : String, order : Sort, noinline query: Query<T>? = null) = performQueryAndReturnSingle(listOf(fieldName), listOf(order), query)

inline fun <reified T:RealmModel> performQueryAndReturnSingle(fieldName: List<String>? = null, order: List<Sort>? = null, noinline query: Query<T>? = null):Single<List<T>>{
    val looper = getLooper()
    return Single.create<List<T>>({ emitter ->

        val realm = com.vicpin.krealmextensions.RealmConfigStore.Companion.fetchConfiguration(T::class.java)?.realm() ?: Realm.getDefaultInstance()
        val realmQuery: RealmQuery<T> = realm.where(T::class.java)
        query?.invoke(realmQuery)

        val result = if (fieldName != null && order != null) {
            realmQuery.findAllSortedAsync(fieldName.toTypedArray(), order.toTypedArray())
        } else {
            realmQuery.findAllAsync()
        }

        result.addChangeListener { it ->
            emitter.onSuccess(realm.copyFromRealm(it))
        }

        emitter.setDisposable(Disposables.fromAction {
            result.removeAllChangeListeners()
            realm.close()
            if (android.os.Looper.getMainLooper() != looper) {
                looper?.thread?.interrupt()
            }
        })
    }).subscribeOn(AndroidSchedulers.from(looper))
            .unsubscribeOn(AndroidSchedulers.from(looper))
}

private fun <T : RealmModel> T.performQuery(fieldName : List<String>? = null, order : List<Sort>? = null, query: Query<T>? = null): Single<List<T>> {
    val looper = getLooper()
    return Single.create<List<T>>({ emitter ->

        val realm = com.vicpin.krealmextensions.RealmConfigStore.Companion.fetchConfiguration(javaClass)?.realm() ?: Realm.getDefaultInstance()
        val realmQuery: RealmQuery<T> = realm.where(this.javaClass)
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
            if (android.os.Looper.getMainLooper() != looper) {
                looper?.thread?.interrupt()
            }
        })
    }).subscribeOn(AndroidSchedulers.from(looper))
            .unsubscribeOn(AndroidSchedulers.from(looper))
}