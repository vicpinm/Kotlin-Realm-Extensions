package com.vicpin.krealmextensions

import io.realm.Realm
import io.realm.RealmObject
import io.realm.RealmQuery
import kotlin.reflect.KClass

/**
 * Created by victor on 2/1/17.
 */
val <T : RealmObject> T.firstItem: T
    get() {
        Realm.getDefaultInstance().use {
            val user = it.forEntity(this).findFirst()
            return it.copyFromRealm(user)
        }
    }

val <T : RealmObject> T.lastItem: T
    get() = allItems.last()


val <T : RealmObject> T.allItems: List<T>
    get() {
        Realm.getDefaultInstance().use {
            val result: List<T> = it.forEntity(this).findAll()
            return it.copyFromRealm(result)
        }
    }

fun <T : RealmObject> Realm.forEntity(instance : T) : RealmQuery<T>{
    return RealmQuery.createQuery(this, instance.javaClass)
}

fun <T> T.withQuery(block: (T) -> Unit): T { block(this); return this }


fun <T : RealmObject> T.where(query: (RealmQuery<T>) -> Unit): List<T> {

    val realm = Realm.getDefaultInstance()
    realm.use {
        val result = it.forEntity(this).withQuery(query).findAll()
        return realm.copyFromRealm(result)
    }
}

fun Realm.update(action: (Realm) -> Unit) {
    use { executeTransaction { action(this) } }
}

fun <T : RealmObject> T.create() {
    Realm.getDefaultInstance().update {
        it.copyToRealm(this)
    }
}

fun <T : RealmObject> T.save() {
    Realm.getDefaultInstance().update { it.copyToRealmOrUpdate(this) }
}


fun <T : RealmObject> T.deleteAll() {
    Realm.getDefaultInstance().update { it.forEntity(this).findAll().deleteAllFromRealm() }
}

fun <T : RealmObject> T.delete(myQuery: (RealmQuery<T>) -> Unit) {
    Realm.getDefaultInstance().update {
        it.forEntity(this).withQuery(myQuery).findAll().deleteAllFromRealm()
    }
}






