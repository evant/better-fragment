package me.tatarka.betterfragment.sample;

import android.util.Log;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.OnLifecycleEvent;

public class LifecycleLogger implements LifecycleObserver {
    @OnLifecycleEvent(Lifecycle.Event.ON_ANY)
    public void onCreate(LifecycleOwner owner, Lifecycle.Event event) {
        Log.d("LIFECYCLE", event + " " + owner);
    }
}
