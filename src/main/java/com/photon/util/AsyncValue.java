package com.photon.util;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

import org.jetbrains.annotations.Nullable;

/**
 * Generic container for asynchronous values with callback support.
 * Allows multiple listeners to be notified when a value becomes available.
 * 
 * @param <T> The type of value being awaited
 */
public class AsyncValue<T> {
    
    private final T defaultValue;
    private final List<Consumer<T>> listeners = new CopyOnWriteArrayList<>();

    private @Nullable T value;
    
    public AsyncValue() { this.defaultValue = null; }

    /**
     * Creates an AsyncValue with an initial value.
     * @param initialValue The initial value to set
     */
    public AsyncValue(T initialValue) { this.defaultValue = initialValue; }

    /**
     * Sets the value and notifies all registered listeners.
     * Can only be set once.
     * 
     * @param value The value to set
     * @throws IllegalStateException if value was already set
     */
    public synchronized void set(T value) {
        this.value = value;
        
        // Notify all listeners
        for (final Consumer<T> listener : listeners) listener.accept(this.value);
        listeners.clear();
    }
    
    /**
     * Registers a callback to be executed when the value becomes available.
     * If the value is already set, the callback is executed immediately.
     * 
     * @param callback The callback to execute with the value
     */
    public synchronized void onAvailable(Consumer<T> callback) {
        if (this.value != null) callback.accept(this.value);
        else listeners.add(callback);
    }
    
    /**
     * Gets the current value if available.
     * 
     * @return The value, or null if not yet set
     */
    @Nullable
    public synchronized T get() {
        return isAvailable() ? this.value : this.defaultValue;
    }
    
    /**
     * Checks if the value has been set.
     * 
     * @return true if value is available
     */
    public synchronized boolean isAvailable() {
        return this.value != null;
    }
    
    /**
     * Resets the value, allowing it to be set again.
     * Useful for reusable scenarios like login/logout cycles.
     */
    public synchronized void reset() {
        this.value = null;
        this.listeners.clear();
    }
}
