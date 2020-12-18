package me.tatarka.shard.fragment.app;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;

import androidx.activity.OnBackPressedDispatcher;
import androidx.activity.result.ActivityResultRegistry;
import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import me.tatarka.shard.activity.ActivityCallbacks;
import me.tatarka.shard.app.Shard;
import me.tatarka.shard.app.ShardOwner;
import me.tatarka.shard.content.ComponentCallbacks;

/**
 * A {@link Fragment} that can hosts shards.
 */
public class ShardFragment extends Fragment implements ShardOwner {

    private final ShardFragmentDelegate delegate = new ShardFragmentDelegate(this);

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        delegate.onCreate();
    }

    @Nullable
    @Override
    public Context getContext() {
        return delegate.wrapContext(super.getContext());
    }

    @NonNull
    @Override
    public ActivityResultRegistry getActivityResultRegistry() {
        return requireActivity().getActivityResultRegistry();
    }

    @NonNull
    @Override
    public LayoutInflater onGetLayoutInflater(@Nullable Bundle savedInstanceState) {
        return delegate.wrapLayoutInflater(super.onGetLayoutInflater(savedInstanceState));
    }

    @NonNull
    @Override
    public Shard.Factory getShardFactory() {
        return delegate.getShardFactory();
    }

    public void setShardFactory(@NonNull Shard.Factory factory) {
        delegate.setShardFactory(factory);
    }

    @NonNull
    @Override
    public ActivityCallbacks getActivityCallbacks() {
        return delegate.getActivityCallbacks();
    }

    @NonNull
    @Override
    public ComponentCallbacks getComponentCallbacks() {
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

    @NonNull
    @Override
    public OnBackPressedDispatcher getOnBackPressedDispatcher() {
        return requireActivity().getOnBackPressedDispatcher();
    }
}
