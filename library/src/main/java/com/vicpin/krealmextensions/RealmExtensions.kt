package com.vicpin.krealmextensions

import io.realm.Realm
import io.realm.RealmObject
import io.realm.RealmQuery
import io.realm.Sort

/**
 * Created by victor on 2/1/17.
 * Extensions for Realm. All methods here are synchronous.
 */

/**
 * Computed variable for getting first entity in database
 */
@Deprecated("Deprecated in 1.0.6, use queryFirst() method instead", ReplaceWith("this.queryFirst()"))
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
@Deprecated("Deprecated in 1.0.6, use queryLast() method instead", ReplaceWith("this.queryLast()"))
val <T : RealmObject> T.lastItem: T?
    get() {
        return if(queryAll().isNotEmpty() && queryAll().last().isValid) queryAll().last() else null
    }


/**
 * Computed variable for getting all entities in database
 */
@Deprecated("Deprecated in 1.0.6, use queryAll() method instead", ReplaceWith("this.queryAll()"))
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
fun <T : RealmObject> T.query(query: (RealmQuery<T>) -> Unit): List<T> {

    Realm.getDefaultInstance().use { realm ->
        val result = realm.forEntity(this).withQuery(query).findAll()
        return realm.copyFromRealm(result)
    }
}

/**
 * Query to the database with RealmQuery instance as argument and returns all items founded
 */
fun <T : RealmObject> T.queryAll(): List<T> {

    Realm.getDefaultInstance().use { realm ->
        val result = realm.forEntity(this).findAll()
        return realm.copyFromRealm(result)
    }
}

/**
 * Query to the database with RealmQuery instance as argument. Return first result, or null.
 */
fun <T : RealmObject> T.queryFirst(): T? {
    Realm.getDefaultInstance().use {
        val item : T? = it.forEntity(this).findFirst()
        return if(item != null && item.isValid) it.copyFromRealm(item) else null
    }
}

/**
 * Query to the database with RealmQuery instance as argument. Return first result, or null.
 */
fun <T : RealmObject> T.queryFirst(query: (RealmQuery<T>) -> Unit): T? {
    Realm.getDefaultInstance().use {
        val item : T? = it.forEntity(this).withQuery(query).findFirst()
        return if(item != null && item.isValid) it.copyFromRealm(item) else null
    }
}

/**
 * Query to the database with RealmQuery instance as argument. Return last result, or null.
 */
fun <T : RealmObject> T.queryLast(): T? {
    Realm.getDefaultInstance().use {
        val result = it.forEntity(this).findAll()
        return if(result != null && result.isNotEmpty()) it.copyFromRealm(result.last()) else null
    }
}

/**
 * Query to the database with RealmQuery instance as argument. Return last result, or null.
 */
fun <T : RealmObject> T.queryLast(query: (RealmQuery<T>) -> Unit): T? {
    Realm.getDefaultInstance().use {
        val result = it.forEntity(this).withQuery(query).findAll()
        return if(result != null && result.isNotEmpty()) it.copyFromRealm(result.last()) else null
    }
}

/**
 * Query to the database with RealmQuery instance as argument
 */
fun <T : RealmObject> T.querySorted(fieldName : String, order : Sort, query: (RealmQuery<T>) -> Unit): List<T> {

    Realm.getDefaultInstance().use { realm ->
        val result = realm.forEntity(this).withQuery(query).findAll().sort(fieldName, order)
        return realm.copyFromRealm(result)
    }
}

/**
 * Query to the database with a specific order and a RealmQuery instance as argument
 */
fun <T : RealmObject> T.querySorted(fieldName : List<String>, order : List<Sort>, query: (RealmQuery<T>) -> Unit): List<T> {

    Realm.getDefaultInstance().use { realm ->
        val result = realm.forEntity(this).withQuery(query).findAll().sort(fieldName.toTypedArray(), order.toTypedArray())
        return realm.copyFromRealm(result)
    }
}

/**
 * Query to the database with a specific order
 */
fun <T : RealmObject> T.querySorted(fieldName : String, order : Sort): List<T> {

    Realm.getDefaultInstance().use { realm ->
        val result = realm.forEntity(this).findAll().sort(fieldName, order)
        return realm.copyFromRealm(result)
    }
}

/**
 * Query to the database with a specific order
 */
fun <T : RealmObject> T.querySorted(fieldName : List<String>, order : List<Sort>): List<T> {

    Realm.getDefaultInstance().use { realm ->
        val result = realm.forEntity(this).findAll().sort(fieldName.toTypedArray(), order.toTypedArray())
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
 * Creates a new entry in database. Useful for RealmObject with no primary key.
 * @return a managed version of a saved object
 */
fun <T : RealmObject> T.createManaged(realm: Realm): T {
    var result: T? = null
    realm.executeTransaction { result = it.copyToRealm(this) }
    return result!!
}

/**
 * Creates or updates a entry in database. Requires a RealmObject with primary key, or IllegalArgumentException will be thrown
 */
fun <T : RealmObject> T.createOrUpdate() {
    Realm.getDefaultInstance().transaction { it.copyToRealmOrUpdate(this) }
}

/**
 * Creates or updates a entry in database. Requires a RealmObject with primary key, or IllegalArgumentException will be thrown
 * @return a managed version of a saved object
 */
fun <T : RealmObject> T.createOrUpdateManaged(realm: Realm): T {
    var result: T? = null
    realm.executeTransaction { result = it.copyToRealmOrUpdate(this) }
    return result!!
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

/**
 * Creates a new entry in database or updates an existing one. If entity has no primary key, always create a new one.
 * If has primary key, it tries to update an existing one.
 * @return a managed version of a saved object
 */
fun <T : RealmObject> T.saveManaged(realm: Realm): T {
    var result: T? = null
    realm.executeTransaction {
        result = if(this.hasPrimaryKey(it)) it.copyToRealmOrUpdate(this) else it.copyToRealm(this)
    }
    return result!!
}

fun <T : Collection<out RealmObject>> T.saveAll() {
    val realm = Realm.getDefaultInstance()
    realm.transaction {
        forEach { if(it.hasPrimaryKey(realm)) realm.copyToRealmOrUpdate(it) else realm.copyToRealm(it) }
    }
}

fun <T : RealmObject> Collection<T>.saveAllManaged(realm: Realm): List<T> {
    val results = mutableListOf<T>()
    realm.executeTransaction {
        forEach { results += if(it.hasPrimaryKey(realm)) realm.copyToRealmOrUpdate(it) else realm.copyToRealm(it) }
    }
    return results
}

fun  Array<out RealmObject>.saveAll() {
    val realm = Realm.getDefaultInstance()
    realm.transaction {
        forEach { if(it.hasPrimaryKey(realm)) realm.copyToRealmOrUpdate(it) else realm.copyToRealm(it) }
    }
}

fun <T : RealmObject> Array<T>.saveAllManaged(realm: Realm): List<T> {
    val results = mutableListOf<T>()
    realm.executeTransaction {
        forEach { results += if(it.hasPrimaryKey(realm)) realm.copyToRealmOrUpdate(it) else realm.copyToRealm(it) }
    }
    return results
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
 * Get count of entries
 */
inline fun <reified T: RealmObject> T.count(): Long {
    val realm = Realm.getDefaultInstance()
    return realm.where(T::class.java).count()
}


/**
 * UTILITY METHODS
 */
private fun <T : RealmObject> Realm.forEntity(instance : T) : RealmQuery<T>{
    return RealmQuery.createQuery(this, instance.javaClass)
}

private fun <T> T.withQuery(block: (T) -> Unit): T { block(this); return this }

private fun <T : RealmObject> T.hasPrimaryKey(realm : Realm) = realm.schema.get(this.javaClass.simpleName).hasPrimaryKey()






