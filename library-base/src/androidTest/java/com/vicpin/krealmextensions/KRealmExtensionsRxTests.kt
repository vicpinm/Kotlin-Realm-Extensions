package com.vicpin.krealmextensions

import android.support.test.runner.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.vicpin.krealmextensions.model.TestEntity
import com.vicpin.krealmextensions.model.TestEntityPK
import com.vicpin.krealmextensions.util.TestRealmConfigurationFactory
import io.reactivex.disposables.Disposable
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
 */
@RunWith(AndroidJUnit4::class)
class KRealmExtensionsRxTests {

    @get:Rule
    var configFactory = TestRealmConfigurationFactory()
    lateinit var realm: Realm
    lateinit var latch: CountDownLatch
    var latchReleased = false
    var disposable: Disposable? = null


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
        realm.close()
        latchReleased = false
        disposable = null
    }

    /**
     * SINGLE AND FLOWABLE TESTS
     */

    @Test
    fun testQueryAllAsFlowable() {

        var itemsCount = 5
        var disposable: Disposable? = null
        var anotherDisposable: Disposable? = null

        populateDBWithTestEntity(numItems = itemsCount)

        block {
            disposable = TestEntity().queryAllAsFlowable().subscribe({
                assertThat(it).hasSize(itemsCount)
                release()
            })
        }

        block {
            anotherDisposable = queryAllAsFlowable<TestEntity>().subscribe({
                assertThat(it).hasSize(itemsCount)
                release()
            })
        }

        block {
            //Add one item more to db
            ++itemsCount
            populateDBWithTestEntity(numItems = 1)
        }

        disposable?.dispose()
        anotherDisposable?.dispose()

    }

    @Test
    fun testQueryAsFlowable() {

        populateDBWithTestEntityPK(numItems = 5)

        block {
            disposable = TestEntityPK().queryAsFlowable { query -> query.equalToValue("id", 1) }.subscribe({
                assertThat(it).hasSize(1)
                assertThat(it[0].isManaged).isFalse()
                release()
            })
        }

        disposable?.dispose()

        block {
            disposable = queryAsFlowable<TestEntityPK> { it.equalToValue("id", 1) }.subscribe({
                assertThat(it).hasSize(1)
                assertThat(it[0].isManaged).isFalse()
                release()
            })
        }
        disposable?.dispose()
    }

    @Test
    fun testQueryAllAsSingle() {

        val itemsCount = 5

        populateDBWithTestEntity(numItems = itemsCount)

        block {
            disposable = TestEntity().queryAllAsSingle().subscribe({ result ->
                assertThat(result).hasSize(itemsCount)
                assertThat(result[0].isManaged).isFalse()
                release()
            })

        }

        assertThat(disposable?.isDisposed ?: false).isTrue()

        block {
            disposable = queryAllAsSingle<TestEntity>().subscribe({ result ->
                assertThat(result).hasSize(itemsCount)
                assertThat(result[0].isManaged).isFalse()
                release()
            })

        }

        assertThat(disposable?.isDisposed ?: false).isTrue()
    }

    @Test
    fun testQueryAsSingle() {

        populateDBWithTestEntityPK(numItems = 5)

        block {
            disposable = TestEntityPK().queryAsSingle { query -> query.equalToValue("id", 1) }.subscribe({ it ->
                assertThat(it).hasSize(1)
                assertThat(it[0].isManaged).isFalse()
                release()
            })
        }

        assertThat(disposable?.isDisposed ?: false).isTrue()


    }

    @Test
    fun testQueryAsSingleParameterized() {
        populateDBWithTestEntityPK(numItems = 5)
        block {
            queryAsSingle<TestEntityPK> { query -> query.equalToValue("id", 1) }.subscribe({ it ->
                assertThat(it).hasSize(1)
                assertThat(it[0].isManaged).isFalse()
                release()
            })
        }

    }

    @Test
    fun testQuerySortedAsFlowable() {

        populateDBWithTestEntityPK(numItems = 5)

        block {
            disposable = TestEntityPK().querySortedAsFlowable("id", Sort.DESCENDING).subscribe({
                assertThat(it).hasSize(5)
                assertThat(it[0].isManaged).isFalse()
                assertThat(it[0].id).isEqualTo(4)
                release()
            })
        }

        disposable?.dispose()

        block {
            disposable = querySortedAsFlowable<TestEntityPK>("id", Sort.DESCENDING).subscribe({
                assertThat(it).hasSize(5)
                assertThat(it[0].isManaged).isFalse()
                assertThat(it[0].id).isEqualTo(4)
                release()
            })
        }

        disposable?.dispose()
    }

    @Test
    fun testQuerySortedAsFlowableWithQuery() {

        populateDBWithTestEntityPK(numItems = 5)

        block {
            disposable = TestEntityPK().querySortedAsFlowable("id", Sort.DESCENDING) { query -> query.equalToValue("id", 1) }.subscribe({
                assertThat(it).hasSize(1)
                assertThat(it[0].isManaged).isFalse()
                assertThat(it[0].id).isEqualTo(1)
                release()
            })
        }

        disposable?.dispose()

    }

    @Test
    fun testQuerySortedAsFlowableWithQueryParameterized() {
        populateDBWithTestEntityPK(numItems = 5)
        block {
            disposable = querySortedAsFlowable<TestEntityPK>("id", Sort.DESCENDING) { query -> query.equalToValue("id", 1) }.subscribe({
                assertThat(it).hasSize(1)
                assertThat(it[0].isManaged).isFalse()
                assertThat(it[0].id).isEqualTo(1)
                release()
            })
        }

        disposable?.dispose()
    }


    @Test
    fun testQuerySortedAsSingle() {

        populateDBWithTestEntityPK(numItems = 5)

        block {
            disposable = TestEntityPK().querySortedAsSingle("id", Sort.DESCENDING).subscribe({ it ->
                assertThat(it).hasSize(5)
                assertThat(it[0].isManaged).isFalse()
                assertThat(it[0].id).isEqualTo(4)
                release()
            })
        }

        assertThat(disposable?.isDisposed ?: false).isTrue()

    }

    @Test
    fun testQuerySortedAsSingleParameterized() {
        populateDBWithTestEntityPK(numItems = 5)
        block {
            disposable = querySortedAsSingle<TestEntityPK>("id", Sort.DESCENDING).subscribe({ it ->
                assertThat(it).hasSize(5)
                assertThat(it[0].isManaged).isFalse()
                assertThat(it[0].id).isEqualTo(4)
                release()
            })
        }

        assertThat(disposable?.isDisposed ?: false).isTrue()
    }

    @Test
    fun testQuerySortedAsSingleWithQuery() {

        populateDBWithTestEntityPK(numItems = 5)

        block {
            disposable = TestEntityPK().querySortedAsSingle("id", Sort.DESCENDING) { query -> query.equalToValue("id", 1) }.subscribe({ it ->
                assertThat(it).hasSize(1)
                assertThat(it[0].isManaged).isFalse()
                assertThat(it[0].id).isEqualTo(1)
                release()
            })
        }

        assertThat(disposable?.isDisposed ?: false).isTrue()
        block {
            disposable = TestEntityPK().querySortedAsSingle("id", Sort.DESCENDING) { query -> query.equalToValue("id", 1) }.subscribe({ it ->
                assertThat(it).hasSize(1)
                assertThat(it[0].isManaged).isFalse()
                assertThat(it[0].id).isEqualTo(1)
                release()
            })
        }

        assertThat(disposable?.isDisposed ?: false).isTrue()
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


