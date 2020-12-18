package me.tatarka.shard.app;

import android.os.Build;

import androidx.activity.ComponentActivity;
import androidx.annotation.NonNull;

import me.tatarka.shard.activity.ActivityCallbacksOwner;

/**
 * Helps with implementing {@link ActivityCallbacksOwner}. Will dispatch  activity callbacks to any
 * registered listeners.
 *
 * @see ShardActivity for an example of how to use.
 */
public final class ActivityCallbacksActivityDispatcher extends ActivityCallbacksDispatcher {

    private final ComponentActivity activity;

    public ActivityCallbacksActivityDispatcher(ComponentActivity activity) {
        super(activity);
        this.activity = activity;
    }

    @Override
    public boolean shouldShowRequestPermissionRationale(@NonNull String permission) {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && activity.shouldShowRequestPermissionRationale(permission);
    }

    @Override
    public boolean isInMultiWindowMode() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && activity.isInMultiWindowMode();
    }

    @Override
    public boolean isInPictureInPictureMode() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && activity.isInPictureInPictureMode();
    }
}
