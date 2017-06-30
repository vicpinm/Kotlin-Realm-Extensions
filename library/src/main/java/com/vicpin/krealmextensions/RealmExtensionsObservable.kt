package com.vicpin.krealmextensions

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
@Deprecated("Deprecated in 1.0.9, use queryAllAsObservable() method instead", ReplaceWith("this.queryAllAsObservable()"))
fun <T : RealmObject> T.allItemsAsObservable(): Observable<List<T>> {

    return prepareObservableQuery { realm, subscriber ->
         RealmQuery.createQuery(realm, this.javaClass)
                .findAllAsync()
                .asObservable()
                .filter { it.isLoaded }
                .map { realm.copyFromRealm(it) }
                .subscribe({ subscriber.onNext(it)
                }, { subscriber.onError(it) })
    }
}

/**
 * Query for all items and listen to changes returning an observable.
 */
fun <T : RealmObject> T.queryAllAsObservable(): Observable<List<T>> {

    return prepareObservableQuery { realm, subscriber ->
        RealmQuery.createQuery(realm, this.javaClass)
                .findAllAsync()
                .asObservable()
                .filter { it.isLoaded }
                .map { realm.copyFromRealm(it) }
                .subscribe({ subscriber.onNext(it)
                }, { subscriber.onError(it) })
    }
}

/**
 * Queries for entities in database asynchronously, and observe changes returning an observable.
 */
fun <T : RealmObject> T.queryAsObservable(query: (RealmQuery<T>) -> Unit): Observable<List<T>> {

    return prepareObservableQuery { realm, subscriber ->
        val realmQuery = RealmQuery.createQuery(realm, this.javaClass)
        query(realmQuery)

        realmQuery.findAllAsync()
                .asObservable()
                .filter { it.isLoaded }
                .map { realm.copyFromRealm(it) }
                .subscribe({ subscriber.onNext(it)
                }, { subscriber.onError(it) })
    }

}

class BackgroundThread : HandlerThread("Scheduler-Realm-BackgroundThread",
    Process.THREAD_PRIORITY_BACKGROUND)

fun <T : Any> prepareObservableQuery(closure : (Realm, Subscriber<in T>) -> Subscription) : Observable<T> {
    var realm : Realm? = null
    var mySubscription: Subscription? = null

    var backgroundThread: HandlerThread? = null
    var looper: Looper = if (Looper.myLooper() != Looper.getMainLooper()) {
        backgroundThread = BackgroundThread()
        backgroundThread.start()
        backgroundThread.looper
    } else {
        Looper.getMainLooper()
    }

    return Observable.defer {
        Observable.create(Observable.OnSubscribe<T> {
            realm = Realm.getDefaultInstance()
            mySubscription = closure(realm!!, it)
        }).doOnUnsubscribe({
            realm?.close()
            mySubscription?.unsubscribe()
            backgroundThread?.interrupt()

        }).unsubscribeOn(AndroidSchedulers.from(looper))
                .subscribeOn(AndroidSchedulers.from(looper))
    }
}







