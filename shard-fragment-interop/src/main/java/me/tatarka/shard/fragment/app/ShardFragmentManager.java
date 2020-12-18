package me.tatarka.shard.fragment.app;

import android.app.Activity;
import android.content.ComponentCallbacks2;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.IntentSender;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.collection.ArrayMap;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentController;
import androidx.fragment.app.FragmentHostCallback;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModelStore;
import androidx.lifecycle.ViewModelStoreOwner;
import androidx.savedstate.SavedStateRegistry;
import androidx.savedstate.SavedStateRegistryOwner;

import java.util.IdentityHashMap;
import java.util.Map;

import me.tatarka.shard.activity.ActivityCallbacks;
import me.tatarka.shard.app.ActivityCallbacksNestedDispatcher;
import me.tatarka.shard.app.CompositeLayoutInflater;
import me.tatarka.shard.app.Shard;
import me.tatarka.shard.content.ComponentCallbacks;

public final class ShardFragmentManager {

    private static final String FRAGMENT_STATE_KEY = "me.tatarka.shard.FragmentShard";
    private static final String FRAGMENTS_TAG = "android:support:fragments";
    private static IdentityHashMap<Shard, ShardFragmentManager> fragmentManagers = new IdentityHashMap<>();

    public static FragmentManager getFragmentManager(@NonNull Shard shard) {
        return getInstance(shard).getFragmentManager();
    }

    static ShardFragmentManager getInstance(@NonNull final Shard shard) {
        SavedStateRegistry registry = shard.getSavedStateRegistry();
        if (!registry.isRestored()) {
            throw new IllegalStateException("Must not be called before onCreate()");
        }
        ShardFragmentManager fm = fragmentManagers.get(shard);
        if (fm == null) {
            fm = new ShardFragmentManager(shard);
            fragmentManagers.put(shard, fm);
            shard.getLifecycle().addObserver(new LifecycleEventObserver() {
                @Override
                public void onStateChanged(@NonNull LifecycleOwner source, @NonNull Lifecycle.Event event) {
                    if (event == Lifecycle.Event.ON_DESTROY) {
                        fragmentManagers.remove((Shard) source);
                    }
                }
            });
        }
        return fm;
    }

    public static Shard.Factory wrapFactory(final Shard.Factory factory) {
        return new Shard.Factory() {
            @NonNull
            @Override
            public <T extends Shard> T newInstance(@NonNull String name) {
                final T shard = factory.newInstance(name);
                shard.getCompositeLayoutInflater().addFactory(new CompositeLayoutInflater.Factory() {
                    @Override
                    public View onCreateView(Next next, View parent, String name, Context context, AttributeSet attrs) {
                        View view = getInstance(shard).fragmentController.onCreateView(parent, name, context, attrs);
                        return view != null ? view : next.createView(parent, name, context, attrs);
                    }
                });
                return shard;
            }
        };
    }

    private final Shard shard;
    private final HostCallback hostCallback;
    private FragmentController fragmentController;

    private boolean created;
    private LayoutInflater inflater;

    ShardFragmentManager(final Shard shard) {
        this.shard = shard;

        hostCallback = new HostCallback();
        fragmentController = FragmentController.createController(hostCallback);

        fragmentController.attachHost(null);

        SavedStateRegistry registry = shard.getSavedStateRegistry();
        Bundle savedState = registry.consumeRestoredStateForKey(FRAGMENT_STATE_KEY);
        if (savedState != null) {
            Parcelable fragmentState = savedState.getParcelable(FRAGMENTS_TAG);
            fragmentController.restoreSaveState(fragmentState);
        }
        registry.registerSavedStateProvider(FRAGMENT_STATE_KEY, new SavedStateRegistry.SavedStateProvider() {
            @NonNull
            @Override
            public Bundle saveState() {
                Parcelable state = fragmentController.saveAllState();
                if (state != null) {
                    Bundle bundle = new Bundle();
                    bundle.putParcelable(FRAGMENTS_TAG, state);
                    return bundle;
                } else {
                    return Bundle.EMPTY;
                }
            }
        });
        registry.runOnNextRecreation(RestoreShardFragmentManager.class);

        shard.getComponentCallbacks().addOnConfigurationChangedListener(hostCallback);
        shard.getComponentCallbacks().addOnTrimMemoryListener(hostCallback);
        shard.getActivityCallbacks().addOnMultiWindowModeChangedCallback(hostCallback);
        shard.getActivityCallbacks().addOnPictureInPictureModeChangedCallback(hostCallback);

        fragmentController.dispatchCreate();

        shard.getLifecycle().addObserver(new LifecycleEventObserver() {
            @Override
            public void onStateChanged(@NonNull LifecycleOwner source, @NonNull Lifecycle.Event event) {
                switch (event) {
                    case ON_START:
                        if (!created) {
                            created = true;
                            fragmentController.dispatchActivityCreated();
                        }
                        fragmentController.noteStateNotSaved();
                        fragmentController.execPendingActions();
                        fragmentController.dispatchStart();
                        break;
                    case ON_RESUME:
                        fragmentController.noteStateNotSaved();
                        fragmentController.execPendingActions();
                        fragmentController.dispatchResume();
                        break;
                    case ON_PAUSE:
                        fragmentController.dispatchPause();
                        break;
                    case ON_STOP:
                        fragmentController.dispatchStop();
                        break;
                    case ON_DESTROY:
                        fragmentController.dispatchDestroy();
                        break;
                }
            }
        });
    }

    FragmentManager getFragmentManager() {
        return fragmentController.getSupportFragmentManager();
    }

    LayoutInflater getLayoutInflater() {
        if (inflater == null) {
            Context context = shard.getContext();
            inflater = LayoutInflater.from(context).cloneInContext(context);
            inflater.setFactory2(hostCallback);
        }
        return inflater;
    }

    class HostCallback extends FragmentHostCallback<Shard>
            implements ViewModelStoreOwner,
            ComponentCallbacks.OnConfigurationChangedListener,
            ComponentCallbacks.OnTrimMemoryListener,
            ActivityCallbacks.OnMultiWindowModeChangedCallback,
            ActivityCallbacks.OnPictureInPictureModeChangedCallback,
            LayoutInflater.Factory2 {

        HostCallback() {
            super(ShardFragmentManager.this.getActivity(), new Handler(), 0);
        }

        @NonNull
        @Override
        public ViewModelStore getViewModelStore() {
            return shard.getViewModelStore();
        }

        @Override
        public boolean onShouldSaveFragmentState(@NonNull Fragment fragment) {
            // Shard will always explicitly save state before destroying.
            return false;
        }

        @NonNull
        @Override
        public LayoutInflater onGetLayoutInflater() {
            return super.onGetLayoutInflater().cloneInContext(shard.getContext());
        }

        @Nullable
        @Override
        public Shard onGetHost() {
            return shard;
        }

        @Override
        public void onSupportInvalidateOptionsMenu() {
            getActivity().invalidateOptionsMenu();
        }

        @Override
        public void onStartActivityFromFragment(@NonNull Fragment fragment, Intent intent, int requestCode) {
            onStartActivityFromFragment(fragment, intent, requestCode, null);
        }

        @Override
        public boolean onShouldShowRequestPermissionRationale(@NonNull String permission) {
            return shard.getActivityCallbacks().shouldShowRequestPermissionRationale(permission);
        }

        @Override
        public boolean onHasWindowAnimations() {
            return getActivity().getWindow() != null;
        }

        @Override
        public int onGetWindowAnimations() {
            Window window = getActivity().getWindow();
            return window != null ? window.getAttributes().windowAnimations : 0;
        }

        @Nullable
        @Override
        public View onFindViewById(int id) {
            return shard.findViewById(id);
        }

        @Override
        public boolean onHasView() {
            return shard.getView() != null;
        }

        @Override
        public void onMultiWindowModeChanged(boolean isInMultiWindowMode) {
            fragmentController.dispatchMultiWindowModeChanged(isInMultiWindowMode);
        }

        @Override
        public void onPictureInPictureModeChanged(boolean isInPictureInPictureMode) {
            fragmentController.dispatchPictureInPictureModeChanged(isInPictureInPictureMode);
        }

        @Override
        public void onConfigurationChanged(@NonNull Configuration newConfig) {
            fragmentController.noteStateNotSaved();
            fragmentController.dispatchConfigurationChanged(newConfig);
        }

        @Override
        public void onTrimMemory(int level) {
            if (level == ComponentCallbacks2.TRIM_MEMORY_COMPLETE) {
                fragmentController.dispatchLowMemory();
            }
        }

        @Override
        public View onCreateView(View parent, String name, Context context, AttributeSet attrs) {
            return fragmentController.onCreateView(parent, name, context, attrs);
        }

        @Override
        public View onCreateView(String name, Context context, AttributeSet attrs) {
            return null;
        }
    }

    private FragmentActivity getActivity() {
        return getActivity(shard.getContext());
    }

    private static FragmentActivity getActivity(Context context) {
        if (context instanceof Activity) {
            return (FragmentActivity) context;
        } else if (context instanceof ContextWrapper) {
            return getActivity(((ContextWrapper) context).getBaseContext());
        } else {
            throw new IllegalArgumentException("Cannot obtains activity from context: " + context);
        }
    }

    static class RestoreShardFragmentManager implements SavedStateRegistry.AutoRecreated {

        @Override
        public void onRecreated(@NonNull SavedStateRegistryOwner owner) {
            Shard shard = (Shard) owner;
            if (fragmentManagers.get(shard) == null) {
                ShardFragmentManager fm = new ShardFragmentManager(shard);
                fragmentManagers.put(shard, fm);
            }
        }
    }
}
