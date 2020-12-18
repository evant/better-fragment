package me.tatarka.shard.app;

import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.savedstate.SavedStateRegistryOwner;

import me.tatarka.shard.activity.ActivityCallbacks;

/**
 * Helps dispatch {@link ActivityCallbacks} to nested components.
 */
public final class ActivityCallbacksNestedDispatcher extends ActivityCallbacksDispatcher implements
        LifecycleEventObserver,
        ActivityCallbacks.OnActivityCallbacks {

    private final ActivityCallbacks parentCallbacks;

    public <O extends SavedStateRegistryOwner & LifecycleOwner> ActivityCallbacksNestedDispatcher(ActivityCallbacks parentCallbacks, O owner) {
        super(owner);
        this.parentCallbacks = parentCallbacks;
        parentCallbacks.addOnActivityCallbacks(this);
        owner.getLifecycle().addObserver(this);
    }

    @Override
    public boolean shouldShowRequestPermissionRationale(@NonNull String permission) {
        return parentCallbacks.shouldShowRequestPermissionRationale(permission);
    }

    @Override
    public boolean isInMultiWindowMode() {
        return parentCallbacks.isInMultiWindowMode();
    }

    @Override
    public boolean isInPictureInPictureMode() {
        return parentCallbacks.isInPictureInPictureMode();
    }

    @Override
    public void onMultiWindowModeChanged(boolean isInMultiWindowMode) {
        dispatchOnMultiWindowModeChanged(isInMultiWindowMode);
    }

    @Override
    public void onPictureInPictureModeChanged(boolean isInPictureInPictureMode) {
        dispatchOnPictureInPictureModeChanged(isInPictureInPictureMode);
    }

    @Override
    public void onStateChanged(@NonNull LifecycleOwner source, @NonNull Lifecycle.Event event) {
        if (event == Lifecycle.Event.ON_DESTROY) {
            parentCallbacks.removeOnActivityCallbacks(this);
            lifecycleOwner.getLifecycle().removeObserver(this);
        }
    }
}
