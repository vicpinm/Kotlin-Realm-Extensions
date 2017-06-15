package com.vicpin.krealmextensions

import android.os.Looper
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposables
import io.realm.Realm
import io.realm.RealmObject
import io.realm.RealmQuery
import io.realm.RealmResults


/**
 * Query for all items and listen to changes returning an Single.
 */
fun <T : RealmObject> T.allItemsAsSingle(): Single<List<T>> {
    val looper = getLooper()
    return Single.create<List<T>>({ emitter ->
        val realm = Realm.getDefaultInstance()
        val result: RealmResults<T> = RealmQuery.createQuery(realm, this.javaClass).findAllAsync()
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


/**
 * Queries for entities in database asynchronously, and observe changes returning an Single.
 */
fun <T : RealmObject> T.queryAsSingle(query: (RealmQuery<T>) -> Unit): Single<List<T>> {
    val looper = getLooper()
    return Single.create<List<T>>({ emitter ->
        val realm = Realm.getDefaultInstance()
        val realmQuery: RealmQuery<T> = RealmQuery.createQuery(realm, this.javaClass)
        query(realmQuery)
        val result = realmQuery.findAllAsync()
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