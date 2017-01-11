package com.vicpin.krealmextensions

import io.realm.Realm
import io.realm.RealmObject
import io.realm.RealmQuery

/**
 * Created by victor on 2/1/17.
 * Extensions for Realm. All methods here are synchronous.
 */

/**
 * Computed variable for getting first entity in database
 */
val <T : RealmObject> T.firstItem: T?
    get() {
        Realm.getDefaultInstance().use {
            val item : T? = it.forEntity(this).findFirst()
            return if(item != null && item.isValid) it.copyFromRealm(item) else null
        }
    }

/**
 * Computed variable for getting last entity in database
 */
val <T : RealmObject> T.lastItem: T?
    get() {
        return if(allItems.isNotEmpty() && allItems.last().isValid) allItems.last() else null
    }


/**
 * Computed variable for getting all entities in database
 */
val <T : RealmObject> T.allItems: List<T>
    get() {
        Realm.getDefaultInstance().use { realm ->
            val result: List<T> = realm.forEntity(this).findAll()
            return realm.copyFromRealm(result)
        }
    }


/**
 * Query to the database with RealmQuery instance as argument
 */
fun <T : RealmObject> T.where(query: (RealmQuery<T>) -> Unit): List<T> {

    Realm.getDefaultInstance().use { realm ->
        val result = realm.forEntity(this).withQuery(query).findAll()
        return realm.copyFromRealm(result)
    }
}

/**
 * Utility extension for modifying database. Create a transaction, run the function passed as argument,
 * commit transaction and close realm instance.
 */
fun Realm.transaction(action: (Realm) -> Unit) {
    use { executeTransaction { action(this) } }
}

/**
 * Creates a new entry in database. Usefull for RealmObject with no primary key.
 */
fun <T : RealmObject> T.create() {
    Realm.getDefaultInstance().transaction {
        it.copyToRealm(this)
    }
}

/**
 * Creates or updates a entry in database. Requires a RealmObject with primary key, or IllegalArgumentException will be thrown
 */
fun <T : RealmObject> T.createOrUpdate() {
    Realm.getDefaultInstance().transaction { it.copyToRealmOrUpdate(this) }
}

/**
 * Creates a new entry in database or updates an existing one. If entity has no primary key, always create a new one.
 * If has primary key, it tries to updates an existing one.
 */
fun <T : RealmObject> T.save() {
    Realm.getDefaultInstance().transaction {
        if(this.hasPrimaryKey(it)) it.copyToRealmOrUpdate(this) else it.copyToRealm(this)
    }
}

fun <T : List<out RealmObject>> T.saveAll() {
    val realm = Realm.getDefaultInstance()
    realm.transaction {
        forEach { if(it.hasPrimaryKey(realm)) realm.copyToRealmOrUpdate(it) else realm.copyToRealm(it) }
    }
}

/**
 * Delete all entries of this type in database
 */
fun <T : RealmObject> T.deleteAll() {
    Realm.getDefaultInstance().transaction { it.forEntity(this).findAll().deleteAllFromRealm() }
}

/**
 * Delete all entries returned by the specified query
 */
fun <T : RealmObject> T.delete(myQuery: (RealmQuery<T>) -> Unit) {
    Realm.getDefaultInstance().transaction {
        it.forEntity(this).withQuery(myQuery).findAll().deleteAllFromRealm()
    }
}


/**
 * UTILITY METHODS
 */
private fun <T : RealmObject> Realm.forEntity(instance : T) : RealmQuery<T>{
    return RealmQuery.createQuery(this, instance.javaClass)
}

private fun <T> T.withQuery(block: (T) -> Unit): T { block(this); return this }

private fun <T : RealmObject> T.hasPrimaryKey(realm : Realm) = realm.schema.get(this.javaClass.simpleName).hasPrimaryKey()






