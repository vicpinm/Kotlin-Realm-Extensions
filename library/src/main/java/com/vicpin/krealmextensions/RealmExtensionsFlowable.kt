package com.vicpin.krealmextensions

import android.os.Looper
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposables
import io.realm.Realm
import io.realm.RealmObject
import io.realm.RealmQuery
import io.realm.RealmResults

/**
 * Query for all items and listen to changes returning an Flowable.
 */
fun <T : RealmObject> T.allItemsAsFlowable(): Flowable<List<T>> {
    val looper = getLooper()
    return Flowable.create<List<T>>({ emitter ->
        val realm = Realm.getDefaultInstance()
        val result: RealmResults<T> = RealmQuery.createQuery(realm, this.javaClass).findAllAsync()
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

/**
 * Queries for entities in database asynchronously, and observe changes returning an Flowable.
 */
fun <T : RealmObject> T.queryAsFlowable(query: (RealmQuery<T>) -> Unit): Flowable<List<T>> {
    val looper = getLooper()
    return Flowable.create<List<T>>({ emitter ->
        val realm = Realm.getDefaultInstance()
        val realmQuery: RealmQuery<T> = RealmQuery.createQuery(realm, this.javaClass)
        query(realmQuery)
        val result = realmQuery.findAllAsync()
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
