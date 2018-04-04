package com.vicpin.krealmextensions

import android.support.test.runner.AndroidJUnit4
import com.google.common.truth.Truth
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
 * Rx test based on global functions
 */
@RunWith(AndroidJUnit4::class)
class KRealmExtensionsGlobalRxTests {

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

        populateDBWithTestEntity(numItems = itemsCount)

        block {
            disposable = queryAllAsFlowable<TestEntity>().subscribe({
                Truth.assertThat(it).hasSize(itemsCount)
                release()
            }, { it.printStackTrace() })
        }

        block {
            //Add one item more to db
            ++itemsCount
            populateDBWithTestEntity(numItems = 1)
        }

        disposable?.dispose()
    }

    @Test
    fun testQueryAsFlowable() {

        populateDBWithTestEntityPK(numItems = 5)

        block {
            disposable = queryAsFlowable<TestEntityPK> { equalToValue("id", 1) }.subscribe({
                Truth.assertThat(it).hasSize(1)
                Truth.assertThat(it[0].isManaged).isFalse()
                release()
            }, { it.printStackTrace() })
        }

        disposable?.dispose()
    }

    @Test
    fun testQueryAllAsSingle() {

        var itemsCount = 5

        populateDBWithTestEntity(numItems = itemsCount)

        block {
            disposable = queryAllAsSingle<TestEntity>().subscribe({ result ->
                Truth.assertThat(result).hasSize(itemsCount)
                Truth.assertThat(result[0].isManaged).isFalse()
                release()
            }, { it.printStackTrace() })
        }

        Truth.assertThat(disposable?.isDisposed ?: false).isTrue()
    }

    @Test
    fun testQueryAsSingle() {

        populateDBWithTestEntityPK(numItems = 5)

        block {
            disposable = queryAsSingle<TestEntityPK> { equalToValue("id", 1) }.subscribe({ it ->
                Truth.assertThat(it).hasSize(1)
                Truth.assertThat(it[0].isManaged).isFalse()
                release()
            }, { it.printStackTrace() })
        }

        Truth.assertThat(disposable?.isDisposed ?: false).isTrue()
    }

    @Test
    fun testQuerySortedAsFlowable() {

        populateDBWithTestEntityPK(numItems = 5)

        block {
            disposable = querySortedAsFlowable<TestEntityPK>("id", Sort.DESCENDING).subscribe({
                Truth.assertThat(it).hasSize(5)
                Truth.assertThat(it[0].isManaged).isFalse()
                Truth.assertThat(it[0].id).isEqualTo(4)
                release()
            }, { it.printStackTrace() })
        }

        disposable?.dispose()
    }

    @Test
    fun testQuerySortedAsFlowableWithQuery() {

        populateDBWithTestEntityPK(numItems = 5)

        block {
            disposable = querySortedAsFlowable<TestEntityPK>("id", Sort.DESCENDING) { equalToValue("id", 1) }.subscribe({
                Truth.assertThat(it).hasSize(1)
                Truth.assertThat(it[0].isManaged).isFalse()
                Truth.assertThat(it[0].id).isEqualTo(1)
                release()
            }, { it.printStackTrace() })
        }

        disposable?.dispose()
    }

    @Test
    fun testQuerySortedAsSingle() {

        populateDBWithTestEntityPK(numItems = 5)

        block {
            disposable = querySortedAsSingle<TestEntityPK>("id", Sort.DESCENDING).subscribe({ it ->
                Truth.assertThat(it).hasSize(5)
                Truth.assertThat(it[0].isManaged).isFalse()
                Truth.assertThat(it[0].id).isEqualTo(4)
                release()
            }, { it.printStackTrace() })
        }

        Truth.assertThat(disposable?.isDisposed ?: false).isTrue()
    }

    @Test
    fun testQuerySortedAsSingleWithQuery() {

        populateDBWithTestEntityPK(numItems = 5)

        block {
            disposable = querySortedAsSingle<TestEntityPK>("id", Sort.DESCENDING) { equalToValue("id", 1) }.subscribe({ it ->
                Truth.assertThat(it).hasSize(1)
                Truth.assertThat(it[0].isManaged).isFalse()
                Truth.assertThat(it[0].id).isEqualTo(1)
                release()
            }, { it.printStackTrace() })
        }

        Truth.assertThat(disposable?.isDisposed ?: false).isTrue()
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