package me.tatarka.shard.app;

import androidx.annotation.NonNull;

import me.tatarka.shard.activity.ActivityCallbacks;
import me.tatarka.shard.content.ComponentCallbacks;
import me.tatarka.shard.content.ComponentCallbacksDispatcher;

public class ShardOwnerDelegate {

    private final ShardOwner owner;
    private ActivityCallbacksDispatcherFactory activityCallbacksDispatcherFactory;
    private ActivityCallbacksDispatcher activityCallbacksDispatcher;
    private ComponentCallbacksDispatcher componentCallbacksDispatcher;
    private Shard.Factory shardFactory = Shard.DefaultFactory.getInstance();

    public ShardOwnerDelegate(ShardOwner owner, ActivityCallbacksDispatcherFactory activityCallbacksDispatcherFactory) {
        this.owner = owner;
        this.activityCallbacksDispatcherFactory = activityCallbacksDispatcherFactory;
    }

    public void onCreate() {
        activityCallbacksDispatcher = activityCallbacksDispatcherFactory.create(owner);
        componentCallbacksDispatcher = new ComponentCallbacksDispatcher(owner);
    }

    public void onMultiWindowModeChanged(boolean isInMultiWindowMode) {
        activityCallbacksDispatcher.dispatchOnMultiWindowModeChanged(isInMultiWindowMode);
    }

    public void onPictureInPictureModeChanged(boolean isInPictureInPictureMode) {
        activityCallbacksDispatcher.dispatchOnMultiWindowModeChanged(isInPictureInPictureMode);
    }

    @NonNull
    public ActivityCallbacks getActivityCallbacks() {
        return activityCallbacksDispatcher;
    }

    @NonNull
    public Shard.Factory getShardFactory() {
        return shardFactory;
    }

    public void setShardFactory(@NonNull Shard.Factory factory) {
        this.shardFactory = factory;
    }

    @NonNull
    public ComponentCallbacks getComponentCallbacks() {
        return componentCallbacksDispatcher;
    }

    public interface ActivityCallbacksDispatcherFactory {
        ActivityCallbacksDispatcher create(ShardOwner owner);
    }
}
