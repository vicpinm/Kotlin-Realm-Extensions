package com.vicpin.krealmextensions

import android.support.test.runner.AndroidJUnit4
import com.google.common.truth.Truth
import com.vicpin.krealmextensions.model.TestEntity
import com.vicpin.krealmextensions.model.TestEntityAutoPK
import com.vicpin.krealmextensions.model.TestEntityPK
import com.vicpin.krealmextensions.util.TestRealmConfigurationFactory
import io.realm.Realm
import io.realm.Sort
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch

/**
 * Created by victor on 10/1/17.
 * Test based on global functions
 */
@RunWith(AndroidJUnit4::class)
class KRealmExtensionsGlobalTests {


    @get:Rule
    var configFactory = TestRealmConfigurationFactory()
    lateinit var realm: Realm
    lateinit var latch: CountDownLatch
    var latchReleased = false


    @Before
    fun setUp() {
        val realmConfig = configFactory.createConfiguration()
        realm = Realm.getInstance(realmConfig)
        latch = CountDownLatch(1)
    }

    @After
    fun tearDown() {
        deleteAll<TestEntity>()
        deleteAll<TestEntityPK>()
        deleteAll<TestEntityAutoPK>()
        realm.close()
        latchReleased = false
    }

    /**
     * PERSISTENCE TESTS
     */
    @Test
    fun testPersistEntityWithSaveMethodManaged() {
        val result = TestEntity().saveManaged(realm) //No exception expected
        Truth.assertThat(result.isManaged)
        Truth.assertThat(count<TestEntity>(realm)).isEqualTo(1)
    }


    @Test
    fun testPersistPKEntityWithSaveMethodManaged() {
        val result = TestEntityPK(1).saveManaged(realm) //No exception expected
        Truth.assertThat(result.isManaged).isTrue()
        Truth.assertThat(count<TestEntityPK>(realm)).isEqualTo(1)
    }

    @Test
    fun testCountPKEntity() {
        val list = listOf(TestEntityPK(1), TestEntityPK(2), TestEntityPK(3))
        list.saveAll()
        Truth.assertThat(count<TestEntityPK>()).isEqualTo(3) 
    }

    @Test
    fun testCountDuplicatePKEntity() {
        val list = listOf(TestEntityPK(1), TestEntityPK(1), TestEntityPK(1))
        list.saveAll()
        Truth.assertThat(count<TestEntityPK>()).isEqualTo(1) 
    }

    @Test
    fun testCountEntity() {
        val list = listOf(TestEntity(), TestEntity(), TestEntity())
        list.saveAll()
        Truth.assertThat(count<TestEntity>()).isEqualTo(3) 
    }

    /**
     *  PERSISTENCE TEST WITH AUTO PRIMARY KEY
     */
    @Test
    fun testPersistAutoPKEntityWithSaveMethodShouldHavePK() {
        TestEntityAutoPK().save()
        Truth.assertThat(count<TestEntityAutoPK>()).isEqualTo(1) 
        Truth.assertThat(queryLast<TestEntityAutoPK>()?.id).isEqualTo(1) 
        TestEntityAutoPK().save()
        Truth.assertThat(count<TestEntityAutoPK>()).isEqualTo(2)
        Truth.assertThat(queryLast<TestEntityAutoPK>()?.id).isEqualTo(2) 
        TestEntityAutoPK().save()
        Truth.assertThat(count<TestEntityAutoPK>()).isEqualTo(3) 
        Truth.assertThat(queryLast<TestEntityAutoPK>()?.id).isEqualTo(3) 
    }

    @Test
    fun testPersistAutoPkEntityWithPkShouldNotBeOverrided() {
        TestEntityAutoPK(4, "").save()
        Truth.assertThat(count<TestEntityAutoPK>()).isEqualTo(1) 
        Truth.assertThat(queryLast<TestEntityAutoPK>()?.id).isEqualTo(4) 
        TestEntityAutoPK(10, "").save()
        Truth.assertThat(count<TestEntityAutoPK>()).isEqualTo(2) 
        Truth.assertThat(queryLast<TestEntityAutoPK>()?.id).isEqualTo(10) 
        TestEntityAutoPK(12, "").save()
        Truth.assertThat(count<TestEntityAutoPK>()).isEqualTo(3) 
        Truth.assertThat(queryLast<TestEntityAutoPK>()?.id).isEqualTo(12) 
    }

    @Test
    fun testPersistAutoPKEntityWithSaveManagedMethod() {
        val result = TestEntityAutoPK().saveManaged(realm)
        Truth.assertThat(result.isManaged)
        Truth.assertThat(count<TestEntityAutoPK>(realm)).isEqualTo(1)
    }

    @Test
    fun testPersistAutoPKEntityListWithSaveMethod() {
        val list = listOf(TestEntityAutoPK(), TestEntityAutoPK(), TestEntityAutoPK())
        list.saveAll()
        Truth.assertThat(count<TestEntityAutoPK>()).isEqualTo(3) 
        Truth.assertThat(queryFirst<TestEntityAutoPK>()?.id).isEqualTo(1) 
        Truth.assertThat(queryLast<TestEntityAutoPK>()?.id).isEqualTo(3) 
    }

    @Test
    fun testPersistAutoPKEntityArrayWithSaveMethod() {
        val list = arrayOf(TestEntityAutoPK(), TestEntityAutoPK(), TestEntityAutoPK())
        list.saveAll()
        Truth.assertThat(count<TestEntityAutoPK>()).isEqualTo(3) 
        Truth.assertThat(queryFirst<TestEntityAutoPK>()?.id).isEqualTo(1) 
        Truth.assertThat(queryLast<TestEntityAutoPK>()?.id).isEqualTo(3) 
    }

    @Test
    fun testPersistAutoPKEntityListWithSaveManagedMethod() {
        val list = listOf(TestEntityAutoPK(), TestEntityAutoPK(), TestEntityAutoPK())
        list.saveAllManaged(realm)
        Truth.assertThat(count<TestEntityAutoPK>(realm)).isEqualTo(3)
    }

    @Test
    fun testPersistAutoPKEntityArrayWithSavemanagedMethod() {
        val list = arrayOf(TestEntityAutoPK(), TestEntityAutoPK(), TestEntityAutoPK())
        list.saveAllManaged(realm)
        Truth.assertThat(count<TestEntityAutoPK>(realm)).isEqualTo(3)
    }

    @Test
    fun testUpdateEntity() {
        TestEntity("test").save()
        TestEntity().queryAndUpdate({ equalTo("name", "test") }) {
            it.name = "updated"
        }
        val result = queryFirst<TestEntity> { equalTo("name", "updated") }
        Truth.assertThat(result).isNotNull()
        Truth.assertThat(result?.name).isEqualTo("updated")
    }

    @Test
    fun testUpdatePKEntity() {
        TestEntityPK(1, "test").save()
        TestEntityPK().queryAndUpdate({ equalTo("name", "test") }) {
            it.name = "updated"
        }

        val result = queryFirst<TestEntityPK> { equalTo("name", "updated") }
        Truth.assertThat(result).isNotNull()
        Truth.assertThat(result?.name).isEqualTo("updated")
    }

    /**
     * QUERY TESTS WITH EMPTY DB
     */
    @Test
    fun testQueryFirstObjectWithEmptyDBShouldReturnNull() {
        Truth.assertThat(queryFirst<TestEntity>()).isNull()
    }

    @Test
    fun testAsyncQueryFirstObjectWithEmptyDBShouldReturnNull() {
        block {
            queryFirstAsync<TestEntity> { Truth.assertThat(it).isNull();release() }
        }
    }

    @Test
    fun testQueryLastObjectWithEmptyDBShouldReturnNull() {
        Truth.assertThat(queryLast<TestEntity>()).isNull() 
    }

    @Test
    fun testQueryLastObjectWithConditionAndEmptyDBShouldReturnNull() {
        Truth.assertThat(queryLast<TestEntity> { equalTo("name", "test") }).isNull() 
    }

    @Test
    fun testAsyncQueryLastObjectWithEmptyDBShouldReturnNull() {
        block {
            queryLastAsync<TestEntity> { Truth.assertThat(it).isNull(); release() }
        }
    }

    @Test
    fun testAllItemsShouldReturnEmptyCollectionWhenDBIsEmpty() {
        Truth.assertThat(queryAll<TestEntity>()).hasSize(0)
    }

    @Test
    fun testAllItemsAsyncShouldReturnEmptyCollectionWhenDBIsEmpty() {
        block {
            queryAllAsync<TestEntity> { Truth.assertThat(it).hasSize(0); release() }
        }
    }

    @Test
    fun testQueryConditionalWhenDBIsEmpty() {
        val result = query<TestEntity> { equalTo("name", "test") }
        Truth.assertThat(result).hasSize(0)
    }

    @Test
    fun etestQueryFirstItemWhenDBIsEmpty() {
        val result = queryFirst<TestEntity> { equalTo("name", "test") }
        Truth.assertThat(result).isNull()
    }

    @Test
    fun testQuerySortedWhenDBIsEmpty() {
        val result = querySorted<TestEntity>("name", Sort.ASCENDING) { equalTo("name", "test") }
        Truth.assertThat(result).hasSize(0)
    }

    /**
     * QUERY TESTS WITH POPULATED DB
     */
    @Test
    fun testQueryFirstItemShouldReturnFirstItemWhenDBIsNotEmpty() {
        populateDBWithTestEntityPK(numItems = 5)
        Truth.assertThat(queryFirst<TestEntityPK>()).isNotNull()
        Truth.assertThat(queryFirst<TestEntityPK>()?.id).isEqualTo(0)
    }

    @Test
    fun testAsyncQueryFirstItemShouldReturnFirstItemWhenDBIsNotEmpty() {
        populateDBWithTestEntityPK(numItems = 5)

        block {
            queryFirstAsync<TestEntityPK> {
                Truth.assertThat(it).isNotNull()
                Truth.assertThat(it?.id).isEqualTo(0)
                release()
            }
        }

    }

    @Test
    fun testQueryLastItemShouldReturnLastItemWhenDBIsNotEmpty() {
        populateDBWithTestEntityPK(numItems = 5)
        Truth.assertThat(queryLast<TestEntityPK>()?.id).isEqualTo(4)
    }

    @Test
    fun testQueryLastItemWithConditionShouldReturnLastItemWhenDBIsNotEmpty() {
        populateDBWithTestEntityPK(numItems = 5)
        Truth.assertThat(queryLast<TestEntityPK> { equalToValue("id", 3) }?.id).isEqualTo(3)
    }

    @Test
    fun testAsyncQueryLastItemShouldReturnLastItemWhenDBIsNotEmpty() {
        populateDBWithTestEntityPK(numItems = 5)

        block {
            queryLastAsync<TestEntityPK> {
                Truth.assertThat(it).isNotNull()
                release()
            }
        }
    }

    @Test
    fun testQueryAllItemsShouldReturnAllItemsWhenDBIsNotEmpty() {
        populateDBWithTestEntity(numItems = 5)
        Truth.assertThat(queryAll<TestEntity>()).hasSize(5)
    }

    @Test
    fun testAsyncQueryAllItemsShouldReturnAllItemsWhenDBIsNotEmpty() {
        populateDBWithTestEntity(numItems = 5)

        block {
            queryAllAsync<TestEntity> { Truth.assertThat(it).hasSize(5); release() }
        }
    }


    @Test
    fun testQueryAllItemsAfterSaveCollection() {
        val list = listOf(TestEntityPK(1), TestEntityPK(2), TestEntityPK(3))
        list.saveAll()
        Truth.assertThat(queryAll<TestEntityPK>()).hasSize(3)
    }

    /**
     * QUERY TESTS WITH WHERE STATEMENT
     */
    @Test
    fun testWhereQueryShouldReturnExpectedItems() {
        populateDBWithTestEntityPK(numItems = 5)
        val results = query<TestEntityPK> { equalToValue("id", 1) }

        Truth.assertThat(results).hasSize(1)
        Truth.assertThat(results.first().id).isEqualTo(1)
        Truth.assertThat(results.map { it.id }).containsAllIn(results.map { it.id })
    }

    @Test
    fun testAsyncWhereQueryShouldReturnExpectedItems() {
        populateDBWithTestEntityPK(numItems = 5)

        block {
            queryAsync<TestEntityPK>({
                equalToValue("id", 1)
            }) { results ->
                Truth.assertThat(results).hasSize(1)
                Truth.assertThat(results.first().id).isEqualTo(1)
                release()
            }
        }
    }

    @Test
    fun testWhereQueryShouldNotReturnAnyItem() {
        populateDBWithTestEntityPK(numItems = 5)
        val results = query<TestEntityPK> { equalToValue("id", 6) }
        Truth.assertThat(results).hasSize(0)
    }

    @Test
    fun testAsyncWhereQueryShouldNotReturnAnyItem() {
        populateDBWithTestEntityPK(numItems = 5)

        block {
            queryAsync<TestEntityPK>({ equalToValue("id", 6) }) { results ->
                Truth.assertThat(results).hasSize(0)
                release()
            }
        }
    }

    @Test
    fun testFirstItemWhenDbIsNotEmpty() {
        populateDBWithTestEntityPK(numItems = 5)

        val result = queryFirst<TestEntityPK> { equalToValue("id", 2) }

        Truth.assertThat(result).isNotNull()
        Truth.assertThat(result?.id).isEqualTo(2)
    }

    @Test
    fun testQueryAscendingShouldReturnOrderedResults() {
        populateDBWithTestEntityPK(numItems = 5)

        val result = querySorted<TestEntityPK>("id", Sort.ASCENDING)

        Truth.assertThat(result).hasSize(5)
        Truth.assertThat(result.first().id).isEqualTo(0)
        Truth.assertThat(result.last().id).isEqualTo(4)
    }

    @Test
    fun testQueryDescendingShouldReturnOrderedResults() {
        populateDBWithTestEntityPK(numItems = 5)

        val result = querySorted<TestEntityPK>("id", Sort.DESCENDING)

        Truth.assertThat(result).hasSize(5)
        Truth.assertThat(result.first().id).isEqualTo(4)
        Truth.assertThat(result.last().id).isEqualTo(0)
    }

    @Test
    fun testQueryDescendingWithFilterShouldReturnOrderedResults() {
        populateDBWithTestEntityPK(numItems = 5)

        val result = querySorted<TestEntityPK>("id", Sort.DESCENDING) {
            lessThan("id", 3).greaterThan("id", 0)
        }

        Truth.assertThat(result).hasSize(2)
        Truth.assertThat(result.first().id).isEqualTo(2)
        Truth.assertThat(result.last().id).isEqualTo(1)
    }

    /**
     * DELETION TESTS
     */
    @Test
    fun testDeleteEntities() {
        populateDBWithTestEntity(numItems = 5)

        deleteAll<TestEntity>()

        Truth.assertThat(TestEntity().queryAll()).hasSize(0)
        Truth.assertThat(queryAll<TestEntity>()).hasSize(0) 
    }


    @Test
    fun testDeleteEntitiesWithPK() {
        populateDBWithTestEntityPK(numItems = 5)

        deleteAll<TestEntityPK>()

        Truth.assertThat(TestEntityPK().queryAll()).hasSize(0)
        Truth.assertThat(queryAll<TestEntityPK>()).hasSize(0) 
    }

    @Test
    fun testDeleteEntitiesWithStatement() {
        populateDBWithTestEntityPK(numItems = 5)

        delete<TestEntityPK> { equalToValue("id", 1) }

        Truth.assertThat(queryAll<TestEntityPK>()).hasSize(4)
    }


    /**
     * UTILITY TEST METHODS
     */
    private fun populateDBWithTestEntity(numItems: Int) {
        (0 until numItems).forEach { TestEntity().save() }
    }

    private fun populateDBWithTestEntityPK(numItems: Int) {
        (0 until numItems).forEach { TestEntityPK(it.toLong()).save() }
    }

    private fun blockLatch() {
        if (!latchReleased) {
            latch.await()
        }
    }

    private fun release() {
        latchReleased = true
        latch.countDown()
        latch = CountDownLatch(1)
    }


    fun block(closure: () -> Unit) {
        latchReleased = false
        closure()
        blockLatch()
    }


}