package com.vicpin.krealmextensions

import io.realm.Realm
import io.realm.RealmObject
import io.realm.RealmQuery
import io.realm.Sort
typealias Query<T> = (RealmQuery<T>) -> Unit

/**
 * Created by victor on 2/1/17.
 * Extensions for Realm. All methods here are synchronous.
 */

/**
 * Query to the database with RealmQuery instance as argument
 */
fun <T : RealmObject> T.query(query: Query<T>, realm: Realm = Realm.getDefaultInstance()): List<T> {

    realm.use {
        val result = it.forEntity(this).withQuery(query).findAll()
        return it.copyFromRealm(result)
    }
}

/**
 * Query to the database with RealmQuery instance as argument and returns all items founded
 */
fun <T : RealmObject> T.queryAll(realm: Realm = Realm.getDefaultInstance()): List<T> {

    realm.use {
        val result = it.forEntity(this).findAll()
        return it.copyFromRealm(result)
    }
}

/**
 * Query to the database with RealmQuery instance as argument. Return first result, or null.
 */
fun <T : RealmObject> T.queryFirst(realm: Realm = Realm.getDefaultInstance()): T? {
    realm.use {
        val item : T? = it.forEntity(this).findFirst()
        return if(item != null && item.isValid) it.copyFromRealm(item) else null
    }
}

/**
 * Query to the database with RealmQuery instance as argument. Return first result, or null.
 */
fun <T : RealmObject> T.queryFirst(query: Query<T>, realm: Realm = Realm.getDefaultInstance()): T? {
    realm.use {
        val item : T? = it.forEntity(this).withQuery(query).findFirst()
        return if(item != null && item.isValid) it.copyFromRealm(item) else null
    }
}

/**
 * Query to the database with RealmQuery instance as argument. Return last result, or null.
 */
fun <T : RealmObject> T.queryLast(realm: Realm = Realm.getDefaultInstance()): T? {
    realm.use {
        val result = it.forEntity(this).findAll()
        return if(result != null && result.isNotEmpty()) it.copyFromRealm(result.last()) else null
    }
}

/**
 * Query to the database with RealmQuery instance as argument. Return last result, or null.
 */
fun <T : RealmObject> T.queryLast(query: Query<T>, realm: Realm = Realm.getDefaultInstance()): T? {
    realm.use {
        val result = it.forEntity(this).withQuery(query).findAll()
        return if(result != null && result.isNotEmpty()) it.copyFromRealm(result.last()) else null
    }
}

/**
 * Query to the database with RealmQuery instance as argument
 */
fun <T : RealmObject> T.querySorted(fieldName : String, order : Sort, query: Query<T>, realm: Realm = Realm.getDefaultInstance()): List<T> {

    realm.use {
        val result = it.forEntity(this).withQuery(query).findAll().sort(fieldName, order)
        return it.copyFromRealm(result)
    }
}

/**
 * Query to the database with a specific order and a RealmQuery instance as argument
 */
fun <T : RealmObject> T.querySorted(fieldName : List<String>, order : List<Sort>, query: Query<T>, realm: Realm = Realm.getDefaultInstance()): List<T> {

    realm.use {
        val result = it.forEntity(this).withQuery(query).findAll().sort(fieldName.toTypedArray(), order.toTypedArray())
        return it.copyFromRealm(result)
    }
}

/**
 * Query to the database with a specific order
 */
fun <T : RealmObject> T.querySorted(fieldName : String, order : Sort, realm: Realm = Realm.getDefaultInstance()): List<T> {

    realm.use {
        val result = it.forEntity(this).findAll().sort(fieldName, order)
        return it.copyFromRealm(result)
    }
}

/**
 * Query to the database with a specific order
 */
fun <T : RealmObject> T.querySorted(fieldName : List<String>, order : List<Sort>, realm: Realm = Realm.getDefaultInstance()): List<T> {

    realm.use {
        val result = it.forEntity(this).findAll().sort(fieldName.toTypedArray(), order.toTypedArray())
        return it.copyFromRealm(result)
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
 * Creates a new entry in database. Useful for RealmObject with no primary key.
 */
fun <T : RealmObject> T.create(realm: Realm = Realm.getDefaultInstance()) {
    realm.transaction {
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
fun <T : RealmObject> T.createOrUpdate(realm: Realm = Realm.getDefaultInstance()) {
    realm.transaction { it.copyToRealmOrUpdate(this) }
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
fun <T : RealmObject> T.save(realm: Realm = Realm.getDefaultInstance()) {
    realm.transaction {
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

fun <T : Collection<RealmObject>> T.saveAll(realm: Realm = Realm.getDefaultInstance()) {
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

fun  Array<out RealmObject>.saveAll(realm: Realm = Realm.getDefaultInstance()) {
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
fun <T : RealmObject> T.deleteAll(realm: Realm = Realm.getDefaultInstance()) {
    realm.transaction { it.forEntity(this).findAll().deleteAllFromRealm() }
}

/**
 * Delete all entries returned by the specified query
 */
fun <T : RealmObject> T.delete(myQuery: Query<T>, realm: Realm = Realm.getDefaultInstance()) {
    realm.transaction {
        it.forEntity(this).withQuery(myQuery).findAll().deleteAllFromRealm()
    }
}

/**
 * Get count of entries
 */
inline fun <reified T: RealmObject> T.count(realm: Realm = Realm.getDefaultInstance()): Long {
    realm.use {
      return it.where(T::class.java).count()
    }
}


/**
 * UTILITY METHODS
 */
private fun <T : RealmObject> Realm.forEntity(instance : T) : RealmQuery<T>{
    return RealmQuery.createQuery(this, instance.javaClass)
}

private fun <T> T.withQuery(block: (T) -> Unit): T { block(this); return this }

private fun <T : RealmObject> T.hasPrimaryKey(realm : Realm) : Boolean {
    if(realm.schema.get(this.javaClass.simpleName) == null){
        throw IllegalArgumentException(this.javaClass.simpleName + " is not part of the schema for this Realm. Did you added realm-android plugin in your build.gradle file?")
    }
    return realm.schema.get(this.javaClass.simpleName).hasPrimaryKey()
}






