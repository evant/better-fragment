package me.tatarka.shard.test;

import android.app.Activity;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleRegistry;
import androidx.lifecycle.ViewModelStore;
import androidx.test.InstrumentationRegistry;
import me.tatarka.shard.app.ActivityCallbacks;
import me.tatarka.shard.app.ActivityCallbacksDispatcher;
import me.tatarka.shard.app.Shard;
import me.tatarka.shard.app.ShardOwner;
import me.tatarka.shard.content.ComponentCallbacks;
import me.tatarka.shard.content.ComponentCallbacksDispatcher;
import me.tatarka.shard.state.InstanceStateRegistry;
import me.tatarka.shard.state.InstanceStateStore;

public class TestShardOwner implements ShardOwner {

    final LifecycleRegistry lifecycleRegistry = new LifecycleRegistry(this);
    final ViewModelStore viewModelStore = new ViewModelStore();
    final InstanceStateRegistry stateStore = new InstanceStateRegistry();
    final ActivityCallbacks activityCallbacks;
    final ComponentCallbacks componentCallbacks = new ComponentCallbacksDispatcher(this);

    public TestShardOwner(Activity activity) {
        activityCallbacks = new ActivityCallbacksDispatcher(activity);
    }

    @NonNull
    @Override
    public Lifecycle getLifecycle() {
        return lifecycleRegistry;
    }

    @NonNull
    @Override
    public ViewModelStore getViewModelStore() {
        return viewModelStore;
    }

    @NonNull
    @Override
    public Context getContext() {
        return InstrumentationRegistry.getTargetContext();
    }

    @NonNull
    @Override
    public Shard.Factory getShardFactory() {
        return Shard.DefaultFactory.getInstance();
    }

    @NonNull
    @Override
    public InstanceStateStore getInstanceStateStore() {
        return stateStore;
    }

    @NonNull
    @Override
    public ActivityCallbacks getActivityCallbacks() {
        return activityCallbacks;
    }

    @NonNull
    @Override
    public ComponentCallbacks getComponentCallbacks() {
        return componentCallbacks;
    }
}
