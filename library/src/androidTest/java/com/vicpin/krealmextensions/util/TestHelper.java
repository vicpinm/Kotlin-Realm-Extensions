package com.vicpin.krealmextensions.util;

import android.util.Log;

import io.realm.internal.async.RealmThreadPoolExecutor;

import static junit.framework.Assert.fail;

/**
 * Created by victor on 10/1/17.
 */

public class TestHelper {

    static final RealmThreadPoolExecutor asyncTaskExecutor = RealmThreadPoolExecutor.newDefaultExecutor();

    /**
     * Wait and check if all tasks in BaseRealm.asyncTaskExecutor can be finished in 5 seconds, otherwise fail the test.
     */
    public static void waitRealmThreadExecutorFinish() {
        int counter = 50;
        while (counter > 0) {
            if (asyncTaskExecutor.getActiveCount() == 0) {
                return;
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                fail(e.getMessage());
            }
            counter--;
        }
        fail("'BaseRealm.asyncTaskExecutor' is not finished in " + counter/10 + " seconds");
    }


}
