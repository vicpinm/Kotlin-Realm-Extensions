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
fun <T : RealmModel> T.queryAllAsSingle() = singleQuery()

inline fun <reified T : RealmModel> queryAllAsSingle() = singleQuery<T>()

/**
 * Queries for entities in database asynchronously, and observe changes returning an Single.
 */
fun <T : RealmModel> T.queryAsSingle(query: Query<T>) = singleQuery(query = query)

inline fun <reified T : RealmModel> queryAsSingle(noinline query: Query<T>) = singleQuery(query = query)

/**
 * Query for sorted entities and observe changes returning a Single.
 */
fun <T : RealmModel> T.querySortedAsSingle(fieldName: List<String>, order: List<Sort>, query: Query<T>? = null) = singleQuery(fieldName, order, query)

inline fun <reified T : RealmModel> querySortedAsSingle(fieldName: List<String>, order: List<Sort>, noinline query: Query<T>? = null) = singleQuery(fieldName, order, query)

/**
 * Query for sorted entities and observe changes returning a Single.
 */
fun <T : RealmModel> T.querySortedAsSingle(fieldName: String, order: Sort, query: Query<T>? = null) = singleQuery(listOf(fieldName), listOf(order), query)

inline fun <reified T : RealmModel> querySortedAsSingle(fieldName: String, order: Sort, noinline query: Query<T>? = null) = singleQuery(listOf(fieldName), listOf(order), query)

/**
 * INTERNAL FUNCTIONS
 */
@PublishedApi
internal inline fun <reified T : RealmModel> singleQuery(fieldName: List<String>? = null, order: List<Sort>? = null, noinline query: Query<T>? = null) = performSingleQuery(fieldName, order, query, T::class.java)

private fun <T : RealmModel> T.singleQuery(fieldName: List<String>? = null, order: List<Sort>? = null, query: Query<T>? = null) = performSingleQuery(fieldName, order, query, this.javaClass)

@PublishedApi
internal fun <T : RealmModel> performSingleQuery(fieldName: List<String>? = null, order: List<Sort>? = null, query: Query<T>? = null, javaClass: Class<T>): Single<List<T>> {
    val looper = getLooper()
    return Single.create<List<T>> { emitter ->

        val realm = com.vicpin.krealmextensions.RealmConfigStore.Companion.fetchConfiguration(javaClass)?.realm()
                ?: Realm.getDefaultInstance()
        val realmQuery: RealmQuery<T> = realm.where(javaClass)
        query?.invoke(realmQuery)

        val result = if (fieldName != null && order != null) {
            realmQuery.sort(fieldName.toTypedArray(), order.toTypedArray()).findAllAsync()
        } else {
            realmQuery.findAllAsync()
        }

        result.addChangeListener { it ->
            emitter.onSuccess(realm.copyFromRealm(it))
        }

        emitter.setDisposable(Disposables.fromAction {
            result.removeAllChangeListeners()
            realm.close()
            if (isRealmThread()) {
                looper?.thread?.interrupt()
            }
        })
    }.subscribeOn(AndroidSchedulers.from(looper))
            .unsubscribeOn(AndroidSchedulers.from(looper))
}
