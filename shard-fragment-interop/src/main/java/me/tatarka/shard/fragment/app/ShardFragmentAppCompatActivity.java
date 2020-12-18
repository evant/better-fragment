package me.tatarka.shard.fragment.app;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import me.tatarka.shard.activity.ActivityCallbacks;
import me.tatarka.shard.app.Shard;
import me.tatarka.shard.app.ShardActivityDelegate;
import me.tatarka.shard.app.ShardOwner;
import me.tatarka.shard.content.ComponentCallbacks;

public class ShardFragmentAppCompatActivity extends AppCompatActivity implements ShardOwner {

    private final ShardActivityDelegate delegate = new ShardActivityDelegate(this);

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
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
        return ShardFragmentManager.wrapFactory(delegate.getShardFactory());
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
        super.onMultiWindowModeChanged(isInMultiWindowMode);
        delegate.onMultiWindowModeChanged(isInMultiWindowMode);
    }

    @Override
    @CallSuper
    public void onPictureInPictureModeChanged(boolean isInPictureInPictureMode) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode);
        delegate.onPictureInPictureModeChanged(isInPictureInPictureMode);
    }
}
