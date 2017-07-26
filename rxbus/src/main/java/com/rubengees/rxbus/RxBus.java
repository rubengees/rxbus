package com.rubengees.rxbus;

import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;

import javax.annotation.Nonnull;

/**
 * A dead simple event bus using RxJava.
 *
 * @author Ruben Gees
 */
public class RxBus {

    private final Subject<Object> subject = PublishSubject.create().toSerialized();

    /**
     * Posts the event object on the bus. Interested components can listen for all types of this event with the
     * {@link #observe(Class)} method.
     *
     * @param event The event. Can be any class.
     */
    public void post(@Nonnull final Object event) {
        subject.onNext(event);
    }

    /**
     * Posts the message on the bus. Interested components can listen for this exact message with the
     * {@link #observeMessages(String)} method.
     *
     * @param message The message.
     */
    public void postMessage(@Nonnull final String message) {
        subject.onNext(message);
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
    public <T> Observable<T> observe(@Nonnull final Class<T> eventType) {
        return subject
                .filter(event -> event.getClass() == eventType)
                .cast(eventType);
    }

    /**
     * Returns an {@link Observable} which emits a notification object for each posted event as specified by the
     * parameter. The emitted item is just a plain {@link Object} with not further information.
     *
     * @param message The message.
     * @return The Observable.
     */
    @Nonnull
    public Observable<Object> observeMessages(@Nonnull final String message) {
        return subject
                .filter(message::equals)
                .map(event -> new Object());
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
