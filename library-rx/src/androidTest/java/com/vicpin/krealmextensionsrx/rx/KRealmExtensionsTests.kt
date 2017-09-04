package com.vicpin.krealmextensions.rx

import android.support.test.runner.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.vicpin.krealmextensions.model.TestEntity
import com.vicpin.krealmextensions.model.TestEntityPK
import com.vicpin.krealmextensions.util.TestRealmConfigurationFactory
import io.realm.Realm
import io.realm.Sort
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import rx.Subscription
import java.util.concurrent.CountDownLatch

/**
 * Created by victor on 10/1/17.
 */
@RunWith(AndroidJUnit4::class)
class KRealmExtensionsTests {

    @get:Rule var configFactory = TestRealmConfigurationFactory()
    lateinit var realm: Realm
    lateinit var latch: CountDownLatch
    var latchReleased = false
    var subscription: Subscription? = null


    @Before fun setUp() {
        val realmConfig = configFactory.createConfiguration()
        realm = Realm.getInstance(realmConfig)
        latch = CountDownLatch(1)
    }

    @After fun tearDown() {
        TestEntity().deleteAll()
        TestEntityPK().deleteAll()
        realm.close()
        latchReleased = false
        subscription = null
    }

    /**
     * OBSERVABLE TESTS
     */
    @Test fun testQueryAllAsObservable() {

        var itemsCount = 5

        populateDBWithTestEntity(numItems = itemsCount)

        block {
            subscription = TestEntity().queryAllAsObservable().subscribe({
                assertThat(it).hasSize(itemsCount)
                release()
            })

        }

        block {
            //Add one item more to db
            ++itemsCount
            populateDBWithTestEntity(numItems = 1)
        }

        subscription?.unsubscribe()

    }

    @Test fun testQueryAsObservable() {

        populateDBWithTestEntityPK(numItems = 5)

        block {
            subscription = TestEntityPK().queryAsObservable { query -> query.equalTo("id", 1) }.subscribe({
                assertThat(it).hasSize(1)
                assertThat(it[0].isManaged).isFalse()
                release()
            })
        }

        subscription?.unsubscribe()
    }

    @Test fun testQueryAllSortedAsObservable() {

        populateDBWithTestEntityPK(numItems = 5)

        block {
            subscription = TestEntityPK().querySortedAsObservable("id", Sort.DESCENDING).subscribe({
                assertThat(it).hasSize(5)
                assertThat(it[0].isManaged).isFalse()
                assertThat(it[0].id).isEqualTo(4)
                release()
            })
        }

        subscription?.unsubscribe()
    }

    @Test fun testQueryAllSortedAsObservableTwoSortingFields() {

        populateDBWithTestEntityPK(numItems = 5)

        block {
            subscription = TestEntityPK().querySortedAsObservable(listOf("id","name"), listOf(Sort.DESCENDING, Sort.DESCENDING)).subscribe({
                assertThat(it).hasSize(5)
                assertThat(it[0].isManaged).isFalse()
                assertThat(it[0].id).isEqualTo(4)
                release()
            })
        }

        subscription?.unsubscribe()
    }

    @Test fun testQuerySortedAsObservable() {

        populateDBWithTestEntityPK(numItems = 5)

        block {
            subscription = TestEntityPK().querySortedAsObservable("id", Sort.DESCENDING) {
                query ->  query.equalTo("id", 3)
            }.subscribe({
                assertThat(it).hasSize(1)
                assertThat(it[0].isManaged).isFalse()
                assertThat(it[0].id).isEqualTo(3)
                release()
            })
        }

        subscription?.unsubscribe()
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


