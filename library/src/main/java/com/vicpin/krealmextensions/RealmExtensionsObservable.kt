package com.vicpin.krealmextensions

import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.os.Process
import io.realm.Realm
import io.realm.RealmObject
import io.realm.RealmQuery
import rx.Observable
import rx.Subscriber
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers


/**
 * Created by victor on 2/1/17.
 * Extensions for Realm. All methods here return observables.
 */


/**
 * Query for all items and listen to changes returning an observable.
 */
fun <T : RealmObject> T.allItemsAsObservable(): Observable<List<T>> {

    var realm : Realm? = null
    var subscription: Subscription? = null

    val backgroundThread = BackgroundThread()
    backgroundThread.start()
    val backgroundLooper = backgroundThread.looper

    return Observable.create <List<T>> { subscriber ->
        realm = Realm.getDefaultInstance()
       subscription = RealmQuery.createQuery(realm, this.javaClass)
                .findAllAsync()
                .asObservable()
                .filter { it.isLoaded }
                .map { realm?.copyFromRealm(it) }
                .subscribe({ subscriber.onNext(it)
                }, { subscriber.onError(it) })

    }.doOnUnsubscribe ({ realm?.close(); subscription?.unsubscribe() })
        .unsubscribeOn(AndroidSchedulers.from(backgroundLooper))
        .subscribeOn(AndroidSchedulers.from(backgroundLooper))

}

/**
 * Queries for entities in database asynchronously, and observe changes returning an observable.
 */
fun <T : RealmObject> T.queryAsObservable(query: (RealmQuery<T>) -> Unit): Observable<List<T>> {

    var realm : Realm? = null
    var subscription: Subscription? = null

    val backgroundThread = BackgroundThread()
    backgroundThread.start()
    val backgroundLooper = backgroundThread.looper

    return Observable.create <List<T>> { subscriber ->

        realm = Realm.getDefaultInstance()
        val realmQuery: RealmQuery<T> = RealmQuery.createQuery(realm, this.javaClass)
        query(realmQuery)

        subscription = realmQuery.findAllAsync()
                .asObservable()
                .filter { it.isLoaded }
                .map { realm?.copyFromRealm(it) }
                .subscribe({ subscriber.onNext(it)
                }, { subscriber.onError(it) })

    }.doOnUnsubscribe ({ realm?.close(); subscription?.unsubscribe()  })
        .unsubscribeOn(AndroidSchedulers.from(backgroundLooper))
        .subscribeOn(AndroidSchedulers.from(backgroundLooper))
}

class BackgroundThread : HandlerThread("Scheduler-Realm-BackgroundThread",
    Process.THREAD_PRIORITY_BACKGROUND)






