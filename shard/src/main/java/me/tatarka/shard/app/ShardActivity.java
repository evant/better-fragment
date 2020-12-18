package me.tatarka.shard.app;

import android.content.Context;
import android.os.Bundle;

import androidx.activity.ComponentActivity;
import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import me.tatarka.shard.activity.ActivityCallbacks;
import me.tatarka.shard.content.ComponentCallbacks;

/**
 * A base class for hosting {@link Shard}s. You can either subclass this or duplicate it's
 * implementation into your own base activity.
 */
public class ShardActivity extends ComponentActivity implements ShardOwner {

    private final ShardActivityDelegate delegate = new ShardActivityDelegate(this);

    @Override
    @CallSuper
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        delegate.onCreate();
    }

    @NonNull
    @Override
    public Context getContext() {
        return this;
    }

    @NonNull
    @Override
    public final Shard.Factory getShardFactory() {
        return delegate.getShardFactory();
    }

    public final void setShardFactory(@NonNull Shard.Factory factory) {
        delegate.setShardFactory(factory);
    }

    @NonNull
    @Override
    public final ActivityCallbacks getActivityCallbacks() {
        return delegate.getActivityCallbacks();
    }

    @NonNull
    @Override
    public final ComponentCallbacks getComponentCallbacks() {
        return delegate.getComponentCallbacks();
    }

    @Override
    @CallSuper
    public void onMultiWindowModeChanged(boolean isInMultiWindowMode) {
        delegate.onMultiWindowModeChanged(isInMultiWindowMode);
    }

    @Override
    @CallSuper
    public void onPictureInPictureModeChanged(boolean isInPictureInPictureMode) {
        delegate.onPictureInPictureModeChanged(isInPictureInPictureMode);
    }
}
