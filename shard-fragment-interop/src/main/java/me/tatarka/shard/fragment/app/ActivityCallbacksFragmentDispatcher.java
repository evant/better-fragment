package me.tatarka.shard.fragment.app;

import android.os.Build;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import me.tatarka.shard.activity.ActivityCallbacksOwner;
import me.tatarka.shard.app.ActivityCallbacksDispatcher;
import me.tatarka.shard.app.ShardActivity;

/**
 * Helps with implementing {@link ActivityCallbacksOwner}. Will dispatch  activity callbacks to any
 * registered listeners.
 *
 * @see ShardActivity for an example of how to use.
 */
public final class ActivityCallbacksFragmentDispatcher extends ActivityCallbacksDispatcher {

    private final Fragment fragment;

    public ActivityCallbacksFragmentDispatcher(Fragment fragment) {
        super(fragment);
        this.fragment = fragment;
    }

    @Override
    public boolean shouldShowRequestPermissionRationale(@NonNull String permission) {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && fragment.shouldShowRequestPermissionRationale(permission);
    }

    @Override
    public boolean isInMultiWindowMode() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && fragment.requireActivity().isInMultiWindowMode();
    }

    @Override
    public boolean isInPictureInPictureMode() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && fragment.requireActivity().isInPictureInPictureMode();
    }
}
