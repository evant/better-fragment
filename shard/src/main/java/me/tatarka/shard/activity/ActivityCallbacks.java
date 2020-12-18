package me.tatarka.shard.activity;

import androidx.annotation.NonNull;

public interface ActivityCallbacks {

    /**
     * Gets whether you should show UI with rationale for requesting a permission.
     * You should do this only if you do not have the permission and the context in
     * which the permission is requested does not clearly communicate to the user
     * what would be the benefit from granting this permission.
     * <p>
     * For example, if you write a camera app, requesting the camera permission
     * would be expected by the user and no rationale for why it is requested is
     * needed. If however, the app needs location for tagging photos then a non-tech
     * savvy user may wonder how location is related to taking photos. In this case
     * you may choose to show UI with rationale of requesting this permission.
     * </p>
     *
     * @param permission A permission your app wants to request.
     * @return Whether you can show permission rationale UI.
     * @see android.content.Context#checkSelfPermission(String)
     */
    boolean shouldShowRequestPermissionRationale(@NonNull String permission);

    boolean isInMultiWindowMode();

    interface OnMultiWindowModeChangedCallback {
        /**
         * Called when the Fragment's activity changes from fullscreen mode to multi-window mode and
         * visa-versa. This is generally tied to {@link android.app.Activity#onMultiWindowModeChanged} of the
         * containing Activity.
         *
         * @param isInMultiWindowMode True if the activity is in multi-window mode.
         */
        void onMultiWindowModeChanged(boolean isInMultiWindowMode);
    }

    void addOnMultiWindowModeChangedCallback(@NonNull OnMultiWindowModeChangedCallback onMultiWindowModeChangedCallback);

    void removeOnMultiWindowModeChangedCallback(@NonNull OnMultiWindowModeChangedCallback onMultiWindowModeChangedCallback);

    boolean isInPictureInPictureMode();

    interface OnPictureInPictureModeChangedCallback {
        /**
         * Called by the system when the activity changes to and from picture-in-picture mode. This is
         * generally tied to {@link android.app.Activity#onPictureInPictureModeChanged} of the containing Activity.
         *
         * @param isInPictureInPictureMode True if the activity is in picture-in-picture mode.
         */
        void onPictureInPictureModeChanged(boolean isInPictureInPictureMode);
    }

    void addOnPictureInPictureModeChangedCallback(@NonNull OnPictureInPictureModeChangedCallback onPictureInPictureModeChangedCallback);

    void removeOnPictureInPictureModeChangedCallback(@NonNull OnPictureInPictureModeChangedCallback onPictureInPictureModeChangedCallback);

    interface OnActivityCallbacks extends OnMultiWindowModeChangedCallback, OnPictureInPictureModeChangedCallback {
    }

    void addOnActivityCallbacks(OnActivityCallbacks callbacks);

    void removeOnActivityCallbacks(OnActivityCallbacks callbacks);
}
