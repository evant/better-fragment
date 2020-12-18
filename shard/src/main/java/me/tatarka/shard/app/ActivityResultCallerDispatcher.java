package me.tatarka.shard.app;

import android.os.Bundle;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultCaller;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.ActivityResultRegistry;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityOptionsCompat;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.savedstate.SavedStateRegistry;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

class ActivityResultCallerDispatcher implements ActivityResultCaller, SavedStateRegistry.SavedStateProvider {

    private static final String STATE_ACTIVITY_RESULT_CALLER_DISPATCHER = "me.tatarka.shard.ActivityResultCallerDispatcher";
    private static final String KEY = "key";
    private static final AtomicInteger WHICH = new AtomicInteger();

    private final ActivityResultRegistry activityResultRegistry;
    private final LifecycleOwner lifecycleOwner;
    private final int which;
    private final AtomicInteger mNextLocalRequestCode = new AtomicInteger();

    public ActivityResultCallerDispatcher(ActivityResultRegistry activityResultRegistry, SavedStateRegistry savedStateRegistry, LifecycleOwner lifecycleOwner) {
        this.activityResultRegistry = activityResultRegistry;
        this.lifecycleOwner = lifecycleOwner;
        Bundle bundle = savedStateRegistry.consumeRestoredStateForKey(STATE_ACTIVITY_RESULT_CALLER_DISPATCHER);
        if (bundle != null) {
            this.which = bundle.getInt(KEY);
        } else {
            this.which = WHICH.getAndIncrement();
        }
        savedStateRegistry.registerSavedStateProvider(STATE_ACTIVITY_RESULT_CALLER_DISPATCHER, this);
    }

    @NonNull
    @Override
    public <I, O> ActivityResultLauncher<I> prepareCall(@NonNull final ActivityResultContract<I, O> contract, @NonNull final ActivityResultCallback<O> callback) {
        final String key = generateActivityResultKey();
        final AtomicReference<ActivityResultLauncher<I>> ref = new AtomicReference<>();

        lifecycleOwner.getLifecycle().addObserver(new LifecycleEventObserver() {
            @Override
            public void onStateChanged(@NonNull LifecycleOwner lifecycleOwner,
                                       @NonNull Lifecycle.Event event) {

                if (Lifecycle.Event.ON_CREATE.equals(event)) {
                    ref.set(activityResultRegistry
                            .register(key, lifecycleOwner, contract, callback));
                }
            }
        });

        return new ActivityResultLauncher<I>() {
            @Override
            public void launch(I input, @Nullable ActivityOptionsCompat options) {
                ActivityResultLauncher<I> delegate = ref.get();
                if (delegate == null) {
                    throw new IllegalStateException("Operation cannot be started before shard "
                            + "is in created state");
                }
                delegate.launch(input, options);
            }

            @Override
            public void unregister() {
                ActivityResultLauncher<I> delegate = ref.getAndSet(null);
                if (delegate != null) {
                    delegate.unregister();
                }
            }
        };
    }

    @NonNull
    private String generateActivityResultKey() {
        return "shard_" + which + "_rq#" + mNextLocalRequestCode.getAndIncrement();
    }

    @NonNull
    @Override
    public <I, O> ActivityResultLauncher<I> prepareCall(@NonNull ActivityResultContract<I, O> contract, @NonNull ActivityResultRegistry registry, @NonNull ActivityResultCallback<O> callback) {
        return registry.register(generateActivityResultKey(), lifecycleOwner, contract, callback);
    }

    @NonNull
    @Override
    public Bundle saveState() {
        Bundle bundle = new Bundle();
        bundle.putInt(KEY, which);
        return bundle;
    }
}
