package com.rubengees.rxbus;

import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;

import javax.annotation.Nonnull;

/**
 * A simple event bus using RxJava 2.
 *
 * @author Ruben Gees
 */
public class RxBus {

    private final Subject<Object> subject = PublishSubject.create().toSerialized();

    /**
     * Posts the event object on the bus. Interested components can listen for all types of this event with the
     * {@link #register(Class)} method.
     *
     * @param event The event. Can be any class.
     * @return If the event was delivered to at least one subscriber.
     */
    public boolean post(@Nonnull final Object event) {
        final boolean hasObservers = subject.hasObservers();

        subject.onNext(event);

        return hasObservers;
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
                .cast(eventType);
    }

    /**
     * Returns an {@link Observable} which emits all events on the event bus.
     *
     * @return The Observable.
     */
    @Nonnull
    public Observable<Object> observeAll() {
        return subject;
    }
}
