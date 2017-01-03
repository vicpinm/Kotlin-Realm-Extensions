package com.vicpin.krealmextensions

import io.realm.Realm
import io.realm.RealmObject
import io.realm.RealmQuery
import io.realm.RealmResults

/**
 * Created by victor on 2/1/17.
 */
fun <T : RealmObject> T.firstItemAsync(callback: (T) -> Unit) {
    var realm = Realm.getDefaultInstance()

    var result = RealmQuery.createQuery(realm, this.javaClass).findFirstAsync()
    result.addChangeListener<T> {
        callback(realm.copyFromRealm(it))
        result.removeChangeListeners()
        realm.close()
    }

}

fun <T : RealmObject> T.lastItemAsync(callback: (T) -> Unit) {
    allItems { callback(it.last()) }
}


fun <T : RealmObject> T.allItems(callback: (List<T>) -> Unit) {
    var realm = Realm.getDefaultInstance()

    var result: RealmResults<T> = RealmQuery.createQuery(realm, this.javaClass).findAllAsync()
    result.addChangeListener {
        callback(realm.copyFromRealm(it))
        result.removeChangeListeners()
        realm.close()

    }
}

fun <T : RealmObject> T.where(query: (RealmQuery<T>) -> Unit, callback: (List<T>) -> Unit) {

    var realm = Realm.getDefaultInstance()
    var realmQuery: RealmQuery<T> = RealmQuery.createQuery(realm, this.javaClass)
    query(realmQuery)
    var result = realmQuery.findAllAsync()
    result.addChangeListener {
        callback(realm.copyFromRealm(it))
        result.removeChangeListeners()
        realm.close()
    }
}




