package com.rubengees.rxbus;

import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A simple event bus using RxJava 2.
 *
 * @author Ruben Gees
 */
public class RxBus {

    private final Subject<Object> subject = PublishSubject.create().toSerialized();
    private final HashMap<Class<?>, Integer> registeredObservers = new HashMap<>();

    private final ReentrantLock lock = new ReentrantLock();

    /**
     * Posts the event object on the bus. Interested components can listen for all types of this event with the
     * {@link #register(Class)} method.
     *
     * @param event The event. Can be any class.
     * @return True, if the event was delivered to at least one subscriber.
     */
    public boolean post(@Nonnull final Object event) {
        try {
            lock.lock();

            subject.onNext(event);

            final Integer existingObserverAmount = registeredObservers.get(event.getClass());
            return existingObserverAmount != null && existingObserverAmount > 0;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Returns an {@link Observable} which emits all events of the specified type.
     * <p>
     * Note that it has to be the exact same class, inheritance is not taken into account.
     *
     * @param eventType The type of the event to observe on.
     * @param <T>       The type.
     * @return The Observable.
     */
    @Nonnull
    public <T> Observable<T> register(@Nonnull final Class<T> eventType) {
        return subject
                .filter(event -> event.getClass() == eventType)
                .cast(eventType)
                .doOnSubscribe(disposable -> {
                    try {
                        lock.lock();

                        final Integer existingObserverAmount = registeredObservers.get(eventType);

                        registeredObservers.put(eventType, existingObserverAmount == null ? 1 :
                                existingObserverAmount + 1);
                    } finally {
                        lock.unlock();
                    }
                })
                .doOnDispose(() -> {
                    try {
                        lock.lock();

                        final Integer existingObserverAmount = registeredObservers.get(eventType);

                        if (existingObserverAmount == null || existingObserverAmount - 1 <= 0) {
                            registeredObservers.remove(eventType);
                        } else {
                            registeredObservers.put(eventType, existingObserverAmount - 1);
                        }
                    } finally {
                        lock.unlock();
                    }
                });
    }
}
