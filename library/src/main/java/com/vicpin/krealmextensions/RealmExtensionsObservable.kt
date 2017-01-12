package com.vicpin.krealmextensions

import android.os.Handler
import android.os.Looper
import io.realm.Realm
import io.realm.RealmObject
import io.realm.RealmQuery
import rx.Observable
import rx.Subscriber
import rx.Subscription


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

    return observeOnMainThread<List<T>> { subscriber ->
        realm = Realm.getDefaultInstance()
       subscription = RealmQuery.createQuery(realm, this.javaClass)
                .findAllAsync()
                .asObservable()
                .filter { it.isLoaded }
                .map { realm?.copyFromRealm(it) }
                .subscribe({ subscriber.onNext(it)
                }, { subscriber.onError(it) })

    }.doOnUnsubscribe { realm?.close(); subscription?.unsubscribe() }

}

/**
 * Queries for entities in database asynchronously, and observe changes returning an observable.
 */
fun <T : RealmObject> T.queryAsObservable(query: (RealmQuery<T>) -> Unit): Observable<List<T>> {

    var realm : Realm? = null
    var subscription: Subscription? = null

    return observeOnMainThread<List<T>> { subscriber ->

        realm = Realm.getDefaultInstance()
        val realmQuery: RealmQuery<T> = RealmQuery.createQuery(realm, this.javaClass)
        query(realmQuery)

        subscription = realmQuery.findAllAsync()
                .asObservable()
                .filter { it.isLoaded }
                .map { realm?.copyFromRealm(it) }
                .subscribe({ subscriber.onNext(it)
                }, { subscriber.onError(it) })

    }.doOnUnsubscribe { realm?.close(); subscription?.unsubscribe() }

}


private fun mainThread(block: () -> Unit) {
    Handler(Looper.getMainLooper()).post(block)
}

private fun <T> observeOnMainThread(block: (Subscriber<in T>) -> Unit) : Observable<T> {
    return Observable.create { subscriber ->
        mainThread { block(subscriber) }
    }
}





