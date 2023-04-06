package com.example.rx

import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.observers.TestObserver
import io.reactivex.rxjava3.schedulers.TestScheduler
import io.reactivex.rxjava3.subscribers.TestSubscriber
import org.hamcrest.CoreMatchers
import org.hamcrest.MatcherAssert
import org.junit.Test
import java.util.concurrent.TimeUnit

class RxTest {
    private var testSubscriber = TestSubscriber.create<Int>()
    private var testObserver = TestObserver.create<Long>()

    @Test
    fun observableTest() {
        val observable = Observable.just(1L, 2, 3, 4)
        observable.subscribe(testObserver)

        testObserver.assertResult(1, 2, 3, 4)
    }

    @Test
    fun awaitTest() {
        Observable.interval(500, TimeUnit.MILLISECONDS)
            .take(10)
            .subscribe(testObserver)

        testObserver.await(5000, TimeUnit.MILLISECONDS)

        testObserver.assertComplete()
    }

    @Test
    fun awaitWithTimeoutTest() {
        Observable.interval(1000, TimeUnit.MILLISECONDS)
            .take(10)
            .subscribe(testObserver)

        testObserver.awaitCount(8)

        MatcherAssert.assertThat(testObserver.values(), CoreMatchers.hasItem(4L))
    }

    @Test
    fun advanceTimeToTest() {
        val scheduler = TestScheduler()

        Observable.interval(1000, TimeUnit.MILLISECONDS, scheduler)
            .take(10)
            .subscribe(testObserver)

        scheduler.advanceTimeTo(4500, TimeUnit.MILLISECONDS)
        testObserver.assertValueCount(4)

        scheduler.advanceTimeBy(2000, TimeUnit.MILLISECONDS)
        testObserver.assertValueCount(6)
    }

    @Test
    fun advanceTimeToTest2() {
        val scheduler = TestScheduler()

        Observable.interval(1000, TimeUnit.MILLISECONDS, scheduler)
            .take(10)
            .subscribe(testObserver)

        scheduler.advanceTimeTo(1500, TimeUnit.MILLISECONDS)
        testObserver.assertValueCount(1)
    }
}
