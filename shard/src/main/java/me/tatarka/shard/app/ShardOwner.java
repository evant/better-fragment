package me.tatarka.shard.app;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModelStoreOwner;
import me.tatarka.shard.content.ComponentCallbacks;
import me.tatarka.shard.content.ComponentCallbacksOwner;
import me.tatarka.shard.state.InstanceStateStoreOwner;

/**
 * Interface that a class that hosts shards must implement. The owner is a {@link LifecycleOwner}
 * a {@link ViewModelStoreOwner}, and a {@link InstanceStateStoreOwner}.See {@link ShardActivity}
 * for a simple implementation.
 */
public interface ShardOwner extends LifecycleOwner,
        ViewModelStoreOwner,
        InstanceStateStoreOwner,
        ActivityCallbacksOwner,
        ComponentCallbacksOwner,
        ShardFactoryProvider {
    @NonNull
    Context getContext();

}
