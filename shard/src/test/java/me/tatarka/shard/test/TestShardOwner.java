package me.tatarka.shard.test;

import android.content.Context;

import androidx.activity.ComponentActivity;
import androidx.activity.OnBackPressedDispatcher;
import androidx.activity.OnBackPressedDispatcherOwner;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.ActivityResultRegistry;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityOptionsCompat;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleRegistry;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStore;
import androidx.savedstate.SavedStateRegistry;
import androidx.savedstate.SavedStateRegistryController;
import androidx.test.platform.app.InstrumentationRegistry;

import me.tatarka.shard.activity.ActivityCallbacks;
import me.tatarka.shard.app.ActivityCallbacksActivityDispatcher;
import me.tatarka.shard.app.Shard;
import me.tatarka.shard.app.ShardOwner;
import me.tatarka.shard.content.ComponentCallbacks;
import me.tatarka.shard.content.ComponentCallbacksDispatcher;

public class TestShardOwner implements ShardOwner {

    final LifecycleRegistry lifecycleRegistry = new LifecycleRegistry(this);
    final ViewModelStore viewModelStore = new ViewModelStore();
    final ViewModelProvider.Factory viewModelProviderFactory = new ViewModelProvider.NewInstanceFactory();
    final SavedStateRegistryController savedStateRegistryController = SavedStateRegistryController.create(this);
    final ActivityCallbacks activityCallbacks;
    final ComponentCallbacks componentCallbacks = new ComponentCallbacksDispatcher(this);
    final OnBackPressedDispatcher dispatcher = new OnBackPressedDispatcher();
    final ActivityResultRegistry activityResultRegistry = new ActivityResultRegistry() {
        @Override
        public <I, O> void invoke(int requestCode, @NonNull ActivityResultContract<I, O> contract, I input, @Nullable ActivityOptionsCompat options) {

        }
    };

    public TestShardOwner() {
        this(null);
    }

    public TestShardOwner(@Nullable ComponentActivity activity) {
        activityCallbacks = new ActivityCallbacksActivityDispatcher(activity);
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
    public ViewModelProvider.Factory getDefaultViewModelProviderFactory() {
        return viewModelProviderFactory;
    }

    @NonNull
    @Override
    public Context getContext() {
        return InstrumentationRegistry.getInstrumentation().getTargetContext();
    }

    @NonNull
    @Override
    public Shard.Factory getShardFactory() {
        return Shard.DefaultFactory.getInstance();
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

    @NonNull
    @Override
    public SavedStateRegistry getSavedStateRegistry() {
        return savedStateRegistryController.getSavedStateRegistry();
    }

    @NonNull
    @Override
    public OnBackPressedDispatcher getOnBackPressedDispatcher() {
        return dispatcher;
    }

    @NonNull
    @Override
    public ActivityResultRegistry getActivityResultRegistry() {
        return activityResultRegistry;
    }

    @NonNull
    @Override
    public <I, O> ActivityResultLauncher<I> prepareCall(@NonNull ActivityResultContract<I, O> contract, @NonNull ActivityResultCallback<O> callback) {
        return activityResultRegistry.register("key", contract, callback);
    }

    @NonNull
    @Override
    public <I, O> ActivityResultLauncher<I> prepareCall(@NonNull ActivityResultContract<I, O> contract, @NonNull ActivityResultRegistry registry, @NonNull ActivityResultCallback<O> callback) {
        return registry.register("key", contract, callback);
    }
}
