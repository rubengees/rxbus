package com.rubengees.rxbus;

import io.reactivex.annotations.Nullable;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;

import static org.junit.Assert.fail;

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

        bus.observe(Integer.class).subscribe(it -> countDownOrFail(lock));
        bus.post(123);

        lock.await();
    }

    @Test(timeout = 5000L)
    public void postObject() throws Exception {
        final CountDownLatch lock = new CountDownLatch(1);

        bus.observe(TestEvent.class).subscribe(it -> {
            if (it.member.equals("abc")) {
                countDownOrFail(lock);
            }
        });

        bus.post(new TestEvent("abc"));

        lock.await();
    }

    @Test(timeout = 5000L)
    public void postMessage() throws Exception {
        final CountDownLatch lock = new CountDownLatch(1);

        bus.observeMessages("test").subscribe(it -> countDownOrFail(lock));
        bus.postMessage("test");

        lock.await();
    }

    @Test(timeout = 5000L)
    public void postMultiple() throws Exception {
        final CountDownLatch lock = new CountDownLatch(3);

        bus.observe(String.class).subscribe(it -> countDownOrFail(lock));
        bus.post("test");
        bus.post("test");
        bus.post("test");

        lock.await();
    }

    @Test(timeout = 5000L)
    public void multipleSubscribers() throws Exception {
        final CountDownLatch lock = new CountDownLatch(2);

        bus.observe(String.class).subscribe(it -> countDownOrFail(lock));
        bus.observe(String.class).subscribe(it -> countDownOrFail(lock));
        bus.post("test");

        lock.await();
    }

    @Test
    public void observeAll() throws Exception {
        final CountDownLatch lock = new CountDownLatch(2);

        bus.observeAll().subscribe(it -> countDownOrFail(lock));
        bus.post(new TestEvent("test"));
        bus.post(123);
    }

    @Test
    public void noObserver() throws Exception {
        final CountDownLatch lock = new CountDownLatch(2);

        bus.observe(String.class).subscribe(it -> {
            countDownOrFail(lock);
        });

        bus.post("test");
        bus.post(123);
        bus.post("tset");
        bus.post(321);

        lock.await();
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