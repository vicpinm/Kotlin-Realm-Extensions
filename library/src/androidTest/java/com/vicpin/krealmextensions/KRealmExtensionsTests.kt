package com.vicpin.krealmextensions

import android.support.test.runner.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.vicpin.krealmextensions.model.TestEntity
import com.vicpin.krealmextensions.model.TestEntityPK
import com.vicpin.krealmextensions.util.TestRealmConfigurationFactory
import io.realm.Realm
import io.realm.exceptions.RealmPrimaryKeyConstraintException
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch

/**
 * Created by victor on 10/1/17.
 */
@RunWith(AndroidJUnit4::class)
class KRealmExtensionsTests {

    @get:Rule var configFactory = TestRealmConfigurationFactory()
    lateinit var realm: Realm
    lateinit var latch: CountDownLatch

    @Before fun setUp() {
        val realmConfig = configFactory.createConfiguration()
        realm = Realm.getInstance(realmConfig)
        latch = CountDownLatch(1)
    }

    @After fun tearDown() {
        TestEntity().deleteAll()
        TestEntityPK().deleteAll()
        realm.close()
    }

    /**
     * PERSISTENCE TESTS
     */

    @Test fun testPersistEntityWithCreate() {
        TestEntity().create() //No exception expected
    }

    @Test fun testPersistEntityWithCreateManaged() {
        val result = TestEntity().createManaged(realm) //No exception expected
        assertThat(result.isManaged).isTrue()
    }

    @Test fun testPersistPKEntityWithCreate() {
        TestEntityPK(1).create() //No exception expected
    }

    @Test fun testPersistPKEntityWithCreateManaged() {
        val result = TestEntityPK(1).createManaged(realm) //No exception expected
        assertThat(result.isManaged).isTrue()
    }

    @Test(expected = IllegalArgumentException::class)
    fun testPersistEntityWithCreateOrUpdateMethod() {
        TestEntity().createOrUpdate() //Exception expected due to TestEntity has no primary key
    }

    @Test(expected = IllegalArgumentException::class)
    fun testPersistEntityWithCreateOrUpdateMethodManaged() {
        TestEntity().createOrUpdateManaged(realm) //Exception expected due to TestEntity has no primary key
    }

    fun testPersistPKEntityWithCreateOrUpdateMethod() {
        TestEntityPK(1).createOrUpdate() //No exception expected
    }

    fun testPersistPKEntityWithCreateOrUpdateMethodManaged() {
        val result = TestEntityPK(1).createOrUpdateManaged(realm) //No exception expected
        assertThat(result.isManaged).isTrue()
    }

    @Test fun testPersistEntityWithSaveMethod() {
        TestEntity().save() //No exception expected
    }

    @Test fun testPersistEntityWithSaveMethodManaged() {
        val result = TestEntity().saveManaged(realm) //No exception expected
        assertThat(result.isManaged)
    }

    @Test fun testPersistPKEntityWithSaveMethod() {
        TestEntityPK(1).save() //No exception expected
    }

    @Test fun testPersistPKEntityWithSaveMethodManaged() {
        val result = TestEntityPK(1).saveManaged(realm) //No exception expected
        assertThat(result.isManaged).isTrue()
    }

    @Test(expected = RealmPrimaryKeyConstraintException::class)
    fun testPersistPKEntityWithCreateMethodAndSamePrimaryKey() {
        TestEntityPK(1).create() //No exception expected
        TestEntityPK(1).create() //Exception expected
    }

    @Test(expected = RealmPrimaryKeyConstraintException::class)
    fun testPersistPKEntityWithCreateMethodAndSamePrimaryKeyManaged() {
        val result = TestEntityPK(1).createManaged(realm) //No exception expected
        assertThat(result.isManaged).isTrue()
        TestEntityPK(1).createManaged(realm) //Exception expected
    }

    @Test fun testPersistPKEntityListWithSaveMethod() {
        val list = listOf(TestEntityPK(1), TestEntityPK(2), TestEntityPK(3))
        list.saveAll()
    }

    @Test fun testPersistPKEntityListWithSaveMethodManaged() {
        val list = listOf(TestEntityPK(1), TestEntityPK(2), TestEntityPK(3))
        val results = list.saveAllManaged(realm)
        results.forEach { assertThat(it.isManaged).isTrue() }
    }

    /**
     * QUERY TESTS WITH EMPTY DB
     */
    @Test fun testQueryFirstObjectWithEmptyDBShouldReturnNull() {
        assertThat(TestEntity().firstItem).isNull()
    }

    @Test fun testAsyncQueryFirstObjectWithEmptyDBShouldReturnNull() {
        TestEntity().firstItemAsync { assertThat(it).isNull();release() }
        block()
    }

    @Test fun testQueryLastObjectWithEmptyDBShouldReturnNull() {
        assertThat(TestEntity().lastItem).isNull()
    }

    @Test fun testAsyncQueryLastObjectWithEmptyDBShouldReturnNull() {
        TestEntity().lastItemAsync { assertThat(it).isNull(); release() }
        block()
    }

    @Test fun testAllItemsShouldReturnEmptyCollectionWhenDBIsEmpty() {
        assertThat(TestEntity().allItems).hasSize(0)
    }

    @Test fun testAllItemsAsyncShouldReturnEmptyCollectionWhenDBIsEmpty() {
        TestEntity().allItemsAsync { assertThat(it).hasSize(0); release() }
        block()
    }

    /**
     * QUERY TESTS WITH POPULATED DB
     */
    @Test fun testQueryFirstItemShouldReturnFirstItemWhenDBIsNotEmpty() {
        populateDBWithTestEntity(numItems = 5)
        assertThat(TestEntity().firstItem).isNotNull()
    }

    @Test fun testAsyncQueryFirstItemShouldReturnFirstItemWhenDBIsNotEmpty() {
        populateDBWithTestEntityPK(numItems = 5)
        TestEntityPK().firstItemAsync {
            assertThat(it).isNotNull()
            assertThat(it?.id).isEqualTo(0)
            release()
        }

        block()
    }

    @Test fun testQueryLastItemShouldReturnLastItemWhenDBIsNotEmpty() {
        populateDBWithTestEntityPK(numItems = 5)
        assertThat(TestEntityPK().lastItem?.id).isEqualTo(4)
    }

    @Test fun testAsyncQueryLastItemShouldReturnLastItemWhenDBIsNotEmpty() {
        populateDBWithTestEntityPK(numItems = 5)
        TestEntityPK().lastItemAsync {
            assertThat(it).isNotNull()
            assertThat(it?.id).isEqualTo(4)
            release()
        }

        block()
    }

    @Test fun testQueryAllItemsShouldReturnAllItemsWhenDBIsNotEmpty() {
        populateDBWithTestEntity(numItems = 5)
        assertThat(TestEntity().allItems).hasSize(5)
    }

    @Test fun testAsyncQueryAllItemsShouldReturnAllItemsWhenDBIsNotEmpty() {
        populateDBWithTestEntity(numItems = 5)
        TestEntity().allItemsAsync { assertThat(it).hasSize(5); release() }
        block()
    }


    @Test fun testQueryAllItemsAfterSaveCollection() {
        val list = listOf(TestEntityPK(1), TestEntityPK(2), TestEntityPK(3))
        list.saveAll()

        assertThat(TestEntityPK().allItems).hasSize(3)

    }

    /**
     * QUERY TESTS WITH WHERE STATEMENT
     */
    @Test fun testWhereQueryShouldReturnExpectedItems(){
        populateDBWithTestEntityPK(numItems = 5)
        val results = TestEntityPK().query { query -> query.equalTo("id",1) }

        assertThat(results).hasSize(1)
        assertThat(results.first().id).isEqualTo(1)
    }

    @Test fun testAsyncWhereQueryShouldReturnExpectedItems(){
        populateDBWithTestEntityPK(numItems = 5)
        TestEntityPK().queryAsync({ query -> query.equalTo("id",1) }){ results ->
            assertThat(results).hasSize(1)
            assertThat(results.first().id).isEqualTo(1)
            release()
        }

        block()
    }

    @Test fun testWhereQueryShouldNotReturnAnyItem(){
        populateDBWithTestEntityPK(numItems = 5)
        val results = TestEntityPK().query { query -> query.equalTo("id",6) }

        assertThat(results).hasSize(0)
    }

    @Test fun testAsyncWhereQueryShouldNotReturnAnyItem(){
        populateDBWithTestEntityPK(numItems = 5)
        TestEntityPK().queryAsync({ query -> query.equalTo("id",6) }){ results ->
            assertThat(results).hasSize(0)
            release()
        }

        block()
    }

    /**
     * DELETION TESTS
     */
    @Test fun testDeleteEntities(){
        populateDBWithTestEntity(numItems = 5)

        TestEntity().deleteAll()

        assertThat(TestEntity().allItems).hasSize(0)
    }

    @Test fun testDeleteEntitiesWithPK(){
        populateDBWithTestEntityPK(numItems = 5)

        TestEntityPK().deleteAll()

        assertThat(TestEntityPK().allItems).hasSize(0)
    }

    @Test fun testDeleteEntitiesWithStatement(){
        populateDBWithTestEntityPK(numItems = 5)

        TestEntityPK().delete { query -> query.equalTo("id",1) }

        assertThat(TestEntityPK().allItems).hasSize(4)
    }

    /**
     * OBSERVABLE TESTS
     */
    @Test fun testAllItemsAsObservable(){

        var itemsCount = 5

        populateDBWithTestEntity(numItems = itemsCount)

        val subscription = TestEntity().allItemsAsObservable().subscribe({
            assertThat(it).hasSize(itemsCount)
            release()
        })

        block()

        //Add one item more to db
        ++itemsCount
        populateDBWithTestEntity(numItems = 1)

        block()

        subscription.unsubscribe()

    }

    @Test fun testQueryAsObservable(){

        populateDBWithTestEntityPK(numItems = 5)

        val subscription = TestEntityPK().queryAsObservable { query -> query.equalTo("id",1) }.subscribe({
            assertThat(it).hasSize(1)
            release()
        })

        block()

        subscription.unsubscribe()
    }

    /**
     * UTILITY TEST METHODS
     */
    private fun populateDBWithTestEntity(numItems: Int) {
        (0..numItems - 1).forEach { TestEntity().save() }
    }

    private fun populateDBWithTestEntityPK(numItems: Int) {
        (0..numItems - 1).forEach { TestEntityPK(it.toLong()).save() }
    }

    private fun block() {
        latch.await()
    }

    private fun release() {
        latch.countDown()
        latch = CountDownLatch(1)
    }
}
