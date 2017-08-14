package com.rubengees.rxbus;

import io.reactivex.annotations.Nullable;
import io.reactivex.disposables.Disposable;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;

import static org.junit.Assert.*;

/**
 * @author Ruben Gees
 */
public class RxBusTest {

    private RxBus bus;

    @Before
    public void setUp() {
        bus = new RxBus();
    }

    @Test(timeout = 5000L)
    public void postInteger() throws InterruptedException {
        final CountDownLatch lock = new CountDownLatch(1);

        bus.register(Integer.class).subscribe(it -> countDownOrFail(lock));
        bus.post(123);

        lock.await();
    }

    @Test(timeout = 5000L)
    public void postObject() throws InterruptedException {
        final CountDownLatch lock = new CountDownLatch(1);

        bus.register(TestEvent.class).subscribe(it -> {
            if (it.member.equals("abc")) {
                countDownOrFail(lock);
            }
        });

        bus.post(new TestEvent("abc"));

        lock.await();
    }

    @Test(timeout = 5000L)
    public void postMultiple() throws InterruptedException {
        final CountDownLatch lock = new CountDownLatch(3);

        bus.register(String.class).subscribe(it -> countDownOrFail(lock));
        bus.post("test");
        bus.post("test");
        bus.post("test");

        lock.await();
    }

    @Test(timeout = 5000L)
    public void multipleSubscribers() throws InterruptedException {
        final CountDownLatch lock = new CountDownLatch(2);

        bus.register(String.class).subscribe(it -> countDownOrFail(lock));
        bus.register(String.class).subscribe(it -> countDownOrFail(lock));
        bus.post("test");

        lock.await();
    }

    @Test
    public void noSubscriber() throws InterruptedException {
        final CountDownLatch lock = new CountDownLatch(2);

        bus.register(String.class).subscribe(it -> countDownOrFail(lock));

        bus.post("test");
        bus.post(123);
        bus.post("tset");
        bus.post(321);

        lock.await();
    }

    @Test
    public void hasObservers() {
        bus.register(String.class).subscribe();

        assertTrue(bus.post("123"));
    }

    @Test
    public void hasNoObservers() {
        assertFalse(bus.post("123"));
    }

    @Test
    public void differentObservers() {
        bus.register(String.class).subscribe();

        assertFalse(bus.post(123));
    }

    @Test
    public void dispose() {
        final Disposable disposable = bus.register(String.class).subscribe(it -> fail());
        disposable.dispose();

        assertFalse(bus.post("123"));
    }

    private void countDownOrFail(final CountDownLatch lock) {
        if (lock.getCount() == 0) {
            fail();
        } else {
            lock.countDown();
        }
    }

    private class TestEvent {
        String member;

        TestEvent(@Nullable String member) {
            this.member = member;
        }
    }
}