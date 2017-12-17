package com.vicpin.krealmextensions

import android.support.test.runner.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.vicpin.krealmextensions.model.TestEntity
import com.vicpin.krealmextensions.model.TestEntityAutoPK
import com.vicpin.krealmextensions.model.TestEntityPK
import com.vicpin.krealmextensions.util.TestRealmConfigurationFactory
import io.realm.Realm
import io.realm.Sort
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
    fun testPersistEntityWithCreate() {
        TestEntity().create() //No exception expected
    }

    @Test
    fun testPersistEntityWithCreateManaged() {
        val result = TestEntity().createManaged(realm) //No exception expected
        assertThat(result.isManaged).isTrue()
    }

    @Test
    fun testPersistPKEntityWithCreate() {
        TestEntityPK(1).create() //No exception expected
    }

    @Test
    fun testPersistPKEntityWithCreateManaged() {
        val result = TestEntityPK(1).createManaged(realm) //No exception expected
        assertThat(result.isManaged).isTrue()
    }

    @Test(expected = IllegalArgumentException::class)
    fun testPersistEntityWithCreateOrUpdateMethod() {
        TestEntity().createOrUpdate() //Exception expected due to TestEntity has no primary key
    }

    @Test(expected = IllegalArgumentException::class)
    fun testPersistEntityWithCreateOrUpdateMethodManaged() {
        TestEntity().createOrUpdateManaged(
                realm) //Exception expected due to TestEntity has no primary key
    }

    fun testPersistPKEntityWithCreateOrUpdateMethod() {
        TestEntityPK(1).createOrUpdate() //No exception expected
    }

    fun testPersistPKEntityWithCreateOrUpdateMethodManaged() {
        val result = TestEntityPK(1).createOrUpdateManaged(realm) //No exception expected
        assertThat(result.isManaged).isTrue()
    }

    @Test
    fun testPersistEntityWithSaveMethod() {
        TestEntity().save() //No exception expected
    }

    @Test
    fun testPersistEntityWithSaveMethodManaged() {
        val result = TestEntity().saveManaged(realm) //No exception expected
        assertThat(result.isManaged)
        assertThat(count<TestEntity>(realm)).isEqualTo(1)
    }

    @Test
    fun testPersistPKEntityWithSaveMethod() {
        TestEntityPK(1).save() //No exception expected
    }

    @Test
    fun testPersistPKEntityWithSaveMethodManaged() {
        val result = TestEntityPK(1).saveManaged(realm) //No exception expected
        assertThat(result.isManaged).isTrue()
        assertThat(count<TestEntityPK>(realm)).isEqualTo(1)
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

    @Test
    fun testPersistPKEntityListWithSaveMethod() {
        val list = listOf(TestEntityPK(1), TestEntityPK(2), TestEntityPK(3))
        list.saveAll()
    }

    @Test
    fun testPersistPKEntityListWithSaveMethodManaged() {
        val list = listOf(TestEntityPK(1), TestEntityPK(2), TestEntityPK(3))
        val results = list.saveAllManaged(realm)
        results.forEach { assertThat(it.isManaged).isTrue() }
    }

    @Test
    fun testCountPKEntity() {
        val list = listOf(TestEntityPK(1), TestEntityPK(2), TestEntityPK(3))
        list.saveAll()
        assertThat(TestEntityPK().count()).isEqualTo(3)
        assertThat(count<TestEntityPK>()).isEqualTo(3) //Parametrised method produces same result
    }

    @Test
    fun testCountDuplicatePKEntity() {
        val list = listOf(TestEntityPK(1), TestEntityPK(1), TestEntityPK(1))
        list.saveAll()
        assertThat(TestEntityPK().count()).isEqualTo(1)
        assertThat(count<TestEntityPK>()).isEqualTo(1) //Parametrised method produces same result
    }

    @Test
    fun testCountEntity() {
        val list = listOf(TestEntity(), TestEntity(), TestEntity())
        list.saveAll()
        assertThat(TestEntity().count()).isEqualTo(3)
        assertThat(count<TestEntity>()).isEqualTo(3) //Parametrised method produces same result
    }

    /**
     *  PERSISTENCE TEST WITH AUTO PRIMARY KEY
     */
    @Test
    fun testPersistAutoPKEntityWithSaveMethod() {
        TestEntityAutoPK().save() //No exception expected
    }

    @Test
    fun testPersistAutoPKEntityWithSaveMethodShouldHavePK() {
        TestEntityAutoPK().save()
        assertThat(TestEntityAutoPK().count()).isEqualTo(1)
        assertThat(count<TestEntityAutoPK>()).isEqualTo(1) //Parametrised method produces same result
        assertThat(TestEntityAutoPK().queryLast()?.id).isEqualTo(1)
        assertThat(queryLast<TestEntityAutoPK>()?.id).isEqualTo(1) //Parametrised method produces same result
        TestEntityAutoPK().save()
        assertThat(TestEntityAutoPK().count()).isEqualTo(2)
        assertThat(TestEntityAutoPK().queryLast()?.id).isEqualTo(2)
        assertThat(queryLast<TestEntityAutoPK>()?.id).isEqualTo(2) //Parametrised method produces same result
        TestEntityAutoPK().save()
        assertThat(TestEntityAutoPK().count()).isEqualTo(3)
        assertThat(count<TestEntityAutoPK>()).isEqualTo(3) //Parametrised method produces same result
        assertThat(TestEntityAutoPK().queryLast()?.id).isEqualTo(3)
        assertThat(queryLast<TestEntityAutoPK>()?.id).isEqualTo(3) //Parametrised method produces same result
    }

    @Test
    fun testPersistAutoPkEntityWithPkShouldNotBeOverrided() {
        TestEntityAutoPK(4, "").save()
        assertThat(TestEntityAutoPK().count()).isEqualTo(1)
        assertThat(count<TestEntityAutoPK>()).isEqualTo(1) //Parametrised method produces same result
        assertThat(TestEntityAutoPK().queryLast()?.id).isEqualTo(4)
        assertThat(queryLast<TestEntityAutoPK>()?.id).isEqualTo(4) //Parametrised method produces same result
        TestEntityAutoPK(10, "").save()
        assertThat(TestEntityAutoPK().count()).isEqualTo(2)
        assertThat(count<TestEntityAutoPK>()).isEqualTo(2) //Parametrised method produces same result
        assertThat(TestEntityAutoPK().queryLast()?.id).isEqualTo(10)
        assertThat(queryLast<TestEntityAutoPK>()?.id).isEqualTo(10) //Parametrised method produces same result
        TestEntityAutoPK(12, "").save()
        assertThat(TestEntityAutoPK().count()).isEqualTo(3)
        assertThat(count<TestEntityAutoPK>()).isEqualTo(3) //Parametrised method produces same result
        assertThat(TestEntityAutoPK().queryLast()?.id).isEqualTo(12)
        assertThat(queryLast<TestEntityAutoPK>()?.id).isEqualTo(12) //Parametrised method produces same result
    }

    @Test
    fun testPersistAutoPKEntityWithSaveManagedMethod() {
        val result = TestEntityAutoPK().saveManaged(realm)
        assertThat(result.isManaged)
        assertThat(count<TestEntityAutoPK>(realm)).isEqualTo(1)
    }

    @Test
    fun testPersistAutoPKEntityListWithSaveMethod() {
        val list = listOf(TestEntityAutoPK(), TestEntityAutoPK(), TestEntityAutoPK())
        list.saveAll()
        assertThat(TestEntityAutoPK().count()).isEqualTo(3)
        assertThat(count<TestEntityAutoPK>()).isEqualTo(3) //Parametrised method produces same result
        assertThat(TestEntityAutoPK().queryFirst()?.id).isEqualTo(1)
        assertThat(queryFirst<TestEntityAutoPK>()?.id).isEqualTo(1) //Parametrised method produces same result
        assertThat(TestEntityAutoPK().queryLast()?.id).isEqualTo(3)
        assertThat(queryLast<TestEntityAutoPK>()?.id).isEqualTo(3) //Parametrised method produces same result
    }

    @Test
    fun testPersistAutoPKEntityArrayWithSaveMethod() {
        val list = arrayOf(TestEntityAutoPK(), TestEntityAutoPK(), TestEntityAutoPK())
        list.saveAll()
        assertThat(TestEntityAutoPK().count()).isEqualTo(3)
        assertThat(count<TestEntityAutoPK>()).isEqualTo(3) //Parametrised method produces same result
        assertThat(TestEntityAutoPK().queryFirst()?.id).isEqualTo(1)
        assertThat(queryFirst<TestEntityAutoPK>()?.id).isEqualTo(1) //Parametrised method produces same result
        assertThat(TestEntityAutoPK().queryLast()?.id).isEqualTo(3)
        assertThat(queryLast<TestEntityAutoPK>()?.id).isEqualTo(3) //Parametrised method produces same result
    }

    @Test
    fun testPersistAutoPKEntityListWithSaveManagedMethod() {
        val list = listOf(TestEntityAutoPK(), TestEntityAutoPK(), TestEntityAutoPK())
        list.saveAllManaged(realm)
        assertThat(count<TestEntityAutoPK>(realm)).isEqualTo(3)
    }

    @Test
    fun testPersistAutoPKEntityArrayWithSavemanagedMethod() {
        val list = arrayOf(TestEntityAutoPK(), TestEntityAutoPK(), TestEntityAutoPK())
        list.saveAllManaged(realm)
        assertThat(count<TestEntityAutoPK>(realm)).isEqualTo(3)
    }

    @Test
    fun testUpdateEntity() {
        TestEntity("test").save()
        TestEntity().queryAndUpdate({ it.equalTo("name", "test") }) {
            it.name = "updated"
        }

        val result = TestEntity().queryFirst { it.equalTo("name", "updated") }
        assertThat(result).isNotNull()
        assertThat(result?.name).isEqualTo("updated")
    }

    @Test
    fun testUpdatePKEntity() {
        TestEntityPK(1, "test").save()
        TestEntityPK().queryAndUpdate({ it.equalTo("name", "test") }) {
            it.name = "updated"
        }

        val result = TestEntityPK().queryFirst { it.equalTo("name", "updated") }
        val otherResult = queryFirst<TestEntityPK> { equalTo("name", "updated") }
        assertThat(result).isNotNull()
        assertThat(otherResult).isNotNull()
        assertThat(result?.name).isEqualTo("updated")
        assertThat(otherResult?.name).isEqualTo("updated")
    }

    /**
     * QUERY TESTS WITH EMPTY DB
     */
    @Test
    fun testQueryFirstObjectWithEmptyDBShouldReturnNull() {
        assertThat(TestEntity().queryFirst()).isNull()
        assertThat(queryFirst<TestEntity>()).isNull() //Parametrised method produces same result
    }

    @Test
    fun testAsyncQueryFirstObjectWithEmptyDBShouldReturnNull() {
        block {
            TestEntity().queryFirstAsync { assertThat(it).isNull();release() }
        }
    }

    @Test
    fun testQueryLastObjectWithEmptyDBShouldReturnNull() {
        assertThat(TestEntity().queryLast()).isNull()
        assertThat(queryLast<TestEntity>()).isNull() //Parametrised method produces same result
    }

    @Test
    fun testQueryLastObjectWithConditionAndEmptyDBShouldReturnNull() {
        assertThat(TestEntity().queryLast { it.equalTo("name", "test") }).isNull()
        assertThat(queryLast<TestEntity> { equalTo("name", "test") }).isNull() //Parametrised method produces same result
    }

    @Test
    fun testAsyncQueryLastObjectWithEmptyDBShouldReturnNull() {
        block {
            TestEntity().queryLastAsync { assertThat(it).isNull(); release() }
        }
    }

    @Test
    fun testAllItemsShouldReturnEmptyCollectionWhenDBIsEmpty() {
        assertThat(TestEntity().queryAll()).hasSize(0)
        assertThat(queryAll<TestEntity>()).hasSize(0) //Parametrised method produces same result
    }

    @Test
    fun testAllItemsAsyncShouldReturnEmptyCollectionWhenDBIsEmpty() {
        block {
            TestEntity().queryAllAsync { assertThat(it).hasSize(0); release() }
        }
    }

    @Test
    fun testQueryConditionalWhenDBIsEmpty() {
        val result = TestEntity().query { it.equalTo("name", "test") }
        val otherResult = query<TestEntity> { equalTo("name", "test") } //Parametrised method produces same result
        assertThat(result).hasSize(0)
        assertThat(otherResult).hasSize(0)
    }

    @Test
    fun etestQueryFirstItemWhenDBIsEmpty() {
        val result = TestEntity().queryFirst { it.equalTo("name", "test") }
        val otherResult = queryFirst<TestEntity> { equalTo("name", "test") }
        assertThat(result).isNull()
        assertThat(otherResult).isNull()
    }

    @Test
    fun testQuerySortedWhenDBIsEmpty() {
        val result = TestEntity().querySorted("name", Sort.ASCENDING) { it.equalTo("name", "test") }
        val otherResult = querySorted<TestEntity>("name", Sort.ASCENDING) { equalTo("name", "test") }
        assertThat(result).hasSize(0)
        assertThat(otherResult).hasSize(0) //Parametrised method produces same result
    }

    /**
     * QUERY TESTS WITH POPULATED DB
     */
    @Test
    fun testQueryFirstItemShouldReturnFirstItemWhenDBIsNotEmpty() {
        populateDBWithTestEntityPK(numItems = 5)
        assertThat(TestEntityPK().queryFirst()).isNotNull()
        assertThat(queryFirst<TestEntityPK>()).isNotNull() //Parametrised method produces same result
        assertThat(TestEntityPK().queryFirst()?.id).isEqualTo(0)
        assertThat(queryFirst<TestEntityPK>()?.id).isEqualTo(0) //Parametrised method produces same result
    }

    @Test
    fun testAsyncQueryFirstItemShouldReturnFirstItemWhenDBIsNotEmpty() {
        populateDBWithTestEntityPK(numItems = 5)

        block {
            TestEntityPK().queryFirstAsync {
                assertThat(it).isNotNull()
                assertThat(it?.id).isEqualTo(0)
                release()
            }
        }
    }

    @Test
    fun testQueryLastItemShouldReturnLastItemWhenDBIsNotEmpty() {
        populateDBWithTestEntityPK(numItems = 5)
        assertThat(TestEntityPK().queryLast()?.id).isEqualTo(4)
        assertThat(queryLast<TestEntityPK>()?.id).isEqualTo(4) //Parametrised method produces same result
    }

    @Test
    fun testQueryLastItemWithConditionShouldReturnLastItemWhenDBIsNotEmpty() {
        populateDBWithTestEntityPK(numItems = 5)
        assertThat(TestEntityPK().queryLast { it.equalToValue("id", 3) }?.id).isEqualTo(3)
        assertThat(queryLast<TestEntityPK> { equalToValue("id", 3) }?.id).isEqualTo(3) //Parametrised method produces same result
    }

    @Test
    fun testAsyncQueryLastItemShouldReturnLastItemWhenDBIsNotEmpty() {
        populateDBWithTestEntityPK(numItems = 5)

        block {
            TestEntityPK().queryLastAsync {
                assertThat(it).isNotNull()
                release()
            }
        }
    }

    @Test
    fun testQueryAllItemsShouldReturnAllItemsWhenDBIsNotEmpty() {
        populateDBWithTestEntity(numItems = 5)
        assertThat(TestEntity().queryAll()).hasSize(5)
        assertThat(queryAll<TestEntity>()).hasSize(5) //Parametrised method produces same result
    }

    @Test
    fun testAsyncQueryAllItemsShouldReturnAllItemsWhenDBIsNotEmpty() {
        populateDBWithTestEntity(numItems = 5)

        block {
            TestEntity().queryAllAsync { assertThat(it).hasSize(5); release() }
        }
    }


    @Test
    fun testQueryAllItemsAfterSaveCollection() {
        val list = listOf(TestEntityPK(1), TestEntityPK(2), TestEntityPK(3))
        list.saveAll()

        assertThat(TestEntityPK().queryAll()).hasSize(3)
        assertThat(queryAll<TestEntityPK>()).hasSize(3) //Parametrised method produces same result
    }

    /**
     * QUERY TESTS WITH WHERE STATEMENT
     */
    @Test
    fun testWhereQueryShouldReturnExpectedItems() {
        populateDBWithTestEntityPK(numItems = 5)
        val results = TestEntityPK().query { query -> query.equalToValue("id", 1) }
        val otherResults = query<TestEntityPK> { equalToValue("id", 1) }

        assertThat(results).hasSize(1)
        assertThat(otherResults).hasSize(1) //Parametrised method produces same result
        assertThat(results.first().id).isEqualTo(1)
        assertThat(otherResults.first().id).isEqualTo(1) //Parametrised method produces same result
        assertThat(otherResults.map { it.id }).containsAllIn(results.map { it.id }) //Parametrised method produces same result
    }

    @Test
    fun testAsyncWhereQueryShouldReturnExpectedItems() {
        populateDBWithTestEntityPK(numItems = 5)

        block {
            TestEntityPK().queryAsync({ query -> query.equalToValue("id", 1) }) { results ->
                assertThat(results).hasSize(1)
                assertThat(results.first().id).isEqualTo(1)
                release()
            }

            //Parametrised method produces same result
            queryAsync<TestEntityPK>({
                equalToValue("id", 1)
            }) { results ->
                assertThat(results).hasSize(1)
                assertThat(results.first().id).isEqualTo(1)
                release()
            }
        }
    }

    @Test
    fun testWhereQueryShouldNotReturnAnyItem() {
        populateDBWithTestEntityPK(numItems = 5)
        val results = TestEntityPK().query { query -> query.equalToValue("id", 6) }
        val otherResults = query<TestEntityPK> { equalToValue("id", 6) }


        assertThat(results).hasSize(0)
        assertThat(otherResults).hasSize(0) //Parametrised method produces same result

    }

    @Test
    fun testAsyncWhereQueryShouldNotReturnAnyItem() {
        populateDBWithTestEntityPK(numItems = 5)

        block {
            TestEntityPK().queryAsync({ query -> query.equalToValue("id", 6) }) { results ->
                assertThat(results).hasSize(0)
                release()
            }
        }
    }

    @Test
    fun testFirstItemWhenDbIsNotEmpty() {
        populateDBWithTestEntityPK(numItems = 5)

        val result = TestEntityPK().queryFirst { it.equalToValue("id", 2) }
        val otherResult = queryFirst<TestEntityPK> { equalToValue("id", 2) }

        assertThat(result).isNotNull()
        assertThat(otherResult).isNotNull() //Parametrised method produces same result
        assertThat(result?.id).isEqualTo(2)
        assertThat(otherResult?.id).isEqualTo(2) //Parametrised method produces same result
    }

    @Test
    fun testQueryAscendingShouldReturnOrderedResults() {
        populateDBWithTestEntityPK(numItems = 5)

        val result = TestEntityPK().querySorted("id", Sort.ASCENDING)
        val otherResult = querySorted<TestEntityPK>("id", Sort.ASCENDING)

        assertThat(result).hasSize(5)
        assertThat(otherResult).hasSize(5) //Parametrised method produces same result
        assertThat(result.first().id).isEqualTo(0)
        assertThat(otherResult.first().id).isEqualTo(0) //Parametrised method produces same result
        assertThat(result.last().id).isEqualTo(4)
        assertThat(otherResult.last().id).isEqualTo(4) //Parametrised method produces same result
    }

    @Test
    fun testQueryDescendingShouldReturnOrderedResults() {
        populateDBWithTestEntityPK(numItems = 5)

        val result = TestEntityPK().querySorted("id", Sort.DESCENDING)
        val otherResult = querySorted<TestEntityPK>("id", Sort.DESCENDING)

        assertThat(result).hasSize(5)
        assertThat(otherResult).hasSize(5) //Parametrised method produces same result
        assertThat(result.first().id).isEqualTo(4)
        assertThat(otherResult.first().id).isEqualTo(4) //Parametrised method produces same result
        assertThat(result.last().id).isEqualTo(0)
        assertThat(otherResult.last().id).isEqualTo(0) //Parametrised method produces same result
    }

    @Test
    fun testQueryDescendingWithFilterShouldReturnOrderedResults() {
        populateDBWithTestEntityPK(numItems = 5)

        val result = TestEntityPK().querySorted("id", Sort.DESCENDING) {
            it.lessThan("id", 3).greaterThan("id", 0)
        }

        val otherResult = querySorted<TestEntityPK>("id", Sort.DESCENDING) {
            lessThan("id", 3).greaterThan("id", 0)
        }

        assertThat(result).hasSize(2)
        assertThat(otherResult).hasSize(2) //Parametrised method produces same result
        assertThat(result.first().id).isEqualTo(2)
        assertThat(otherResult.first().id).isEqualTo(2) //Parametrised method produces same result
        assertThat(result.last().id).isEqualTo(1)
        assertThat(otherResult.last().id).isEqualTo(1) //Parametrised method produces same result
    }

    /**
     * DELETION TESTS
     */
    @Test
    fun testDeleteEntities() {
        populateDBWithTestEntity(numItems = 5)

        TestEntity().deleteAll()

        assertThat(TestEntity().queryAll()).hasSize(0)
        assertThat(queryAll<TestEntity>()).hasSize(0) //Parametrised method produces same result
    }

    @Test
    fun testDeleteEntitiesParameterized() {
        populateDBWithTestEntity(numItems = 5)

        deleteAll<TestEntity>()

        assertThat(TestEntity().queryAll()).hasSize(0)
        assertThat(queryAll<TestEntity>()).hasSize(0) //Parametrised method produces same result
    }

    @Test
    fun testDeleteEntitiesWithPK() {
        populateDBWithTestEntityPK(numItems = 5)

        TestEntityPK().deleteAll()

        assertThat(TestEntityPK().queryAll()).hasSize(0)
        assertThat(queryAll<TestEntityPK>()).hasSize(0) //Parametrised method produces same result
    }

    @Test
    fun testDeleteEntitiesWithPKParameterized() {
        populateDBWithTestEntityPK(numItems = 5)

        deleteAll<TestEntityPK>()

        assertThat(TestEntityPK().queryAll()).hasSize(0)
        assertThat(queryAll<TestEntityPK>()).hasSize(0) //Parametrised method produces same result
    }

    @Test
    fun testDeleteEntitiesWithStatement() {
        populateDBWithTestEntityPK(numItems = 5)

        TestEntityPK().delete { query -> query.equalToValue("id", 1) }
        delete<TestEntityPK> { equalToValue("id", 1) }

        assertThat(TestEntityPK().queryAll()).hasSize(4)
        assertThat(queryAll<TestEntityPK>()).hasSize(4) //Parametrised method produces same result
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


