package me.tatarka.shard.app;

import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import java.lang.reflect.InvocationTargetException;

import androidx.activity.OnBackPressedDispatcher;
import androidx.annotation.CallSuper;
import androidx.annotation.ContentView;
import androidx.annotation.IdRes;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LifecycleRegistry;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.SavedStateViewModelFactory;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStore;
import androidx.lifecycle.ViewTreeLifecycleOwner;
import androidx.savedstate.SavedStateRegistry;
import androidx.savedstate.SavedStateRegistryController;

import me.tatarka.shard.activity.ActivityCallbacks;
import me.tatarka.shard.content.ComponentCallbacks;
import me.tatarka.shard.content.ComponentCallbacksDispatcher;

/**
 * A simpler 'Shard' that lies on the android architecture components for most of the heavy-lifting.
 * <p>
 * It is a {@link LifecycleOwner} and {@link androidx.lifecycle.ViewModelStoreOwner}. You may use
 * {@link ContentView} to set the content view for this shard.
 */
public class Shard implements ShardOwner {

    private final LifecycleRegistry lifecycleRegistry = new LifecycleRegistry(this);

    {
        lifecycleRegistry.addObserver(new LifecycleObserver() {
            @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
            void onLifecyleEvent() {
                if (frame != null) {
                    frame.cancelPendingInputEvents();
                }
            }
        });
    }

    private final SavedStateRegistryController stateRegistryController = SavedStateRegistryController.create(this);
    private ActivityCallbacksNestedDispatcher activityCallbackDispatcher;
    private ComponentCallbacksDispatcher componentCallbacksDispatcher;
    private final Observer observer = new Observer();
    private int viewModelId = -1;
    private Container container;
    @Nullable
    private FrameLayout frame;
    private ShardOwner owner;
    private Context context;
    private Bundle args;
    private boolean destroyed;
    @Nullable
    private State state;
    @Nullable
    private Shard.Factory shardFactory;
    private final CompositeLayoutInflater inflater = new CompositeLayoutInflater();
    @Nullable
    private ViewModelProvider.Factory defaultViewModeProviderFactory;

    void restoreState(@Nullable State state) {
        checkNotCreated();
        this.state = state;
        if (state != null) {
            if (state.args != null) {
                this.args = state.args;
            }
            viewModelId = state.viewModelId;
        }
    }

    void add(@NonNull ShardOwner owner, @NonNull Container container) {
        checkNotCreated();
        if (destroyed) {
            return;
        }
        this.owner = owner;
        context = new ShardOwnerContextWrapper(owner.getContext(), this);
        this.container = container;
        stateRegistryController.performRestore(state != null ? state.savedState : null);
        activityCallbackDispatcher = new ActivityCallbacksNestedDispatcher(owner.getActivityCallbacks(), this);
        componentCallbacksDispatcher = new ComponentCallbacksDispatcher(this);
        onCreate();
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE);

        owner.getLifecycle().addObserver(observer);
        state = null;
    }

    /**
     * A {@code Container} hosts the view for a {@code Shard}. You normally don't need to deal with
     * this directly as there is already an implementation for {@link ViewGroup} and {@link android.app.Dialog}.
     *
     * @see ShardManager#add(Shard, Container).
     */
    public interface Container {
        /**
         * Adds the view to the container.
         */
        void addView(View view);

        /**
         * Removes the view from the container.
         */
        void removeView(View view);
    }

    @NonNull
    Shard.State saveInstanceState() {
        checkDestroyed();
        State state = new State(viewModelId, args);
        if (lifecycleRegistry.getCurrentState().isAtLeast(Lifecycle.State.CREATED)) {
            lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP);
            if (frame != null) {
                state.viewState = new SparseArray();
                frame.saveHierarchyState(state.viewState);
            }
            state.savedState = new Bundle();
            stateRegistryController.performSave(state.savedState);
        }
        return state;
    }

    void remove() {
        checkCreated();
        checkDestroyed();
        destroyed = true;
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP);
        removeViewModelStore(viewModelId);
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY);
        owner.getLifecycle().removeObserver(observer);

        if (container != null && frame != null) {
            container.removeView(frame);
        }
    }

    private void checkDestroyed() {
        if (destroyed) {
            throw new IllegalStateException("Shard: " + this + " is already destroyed");
        }
    }

    private void checkCreated() {
        if (owner == null) {
            throw new IllegalStateException("Shard: " + this + " is not created");
        }
    }

    private void checkNotCreated() {
        if (this.owner != null) {
            throw new IllegalStateException("Shard: " + this + " is already created");
        }
    }

    /**
     * Returns a {@link Bundle} that can be read and written to provide arguments for the
     * {@code Shard}. These will be automatically persisted in the instance state.
     *
     * @see #setArgs(Bundle)
     */
    @NonNull
    public Bundle getArgs() {
        if (args == null) {
            args = new Bundle();
        }
        return args;
    }

    /**
     * Sets the args for this {@code Shard} to the given {@link Bundle}. This will replace any
     * existing args. These will be automatically persisted in the instance state.
     *
     * @see #getArgs()
     */
    @CallSuper
    public void setArgs(@Nullable Bundle args) {
        this.args = args;
    }

    /**
     * Sets the {@code Shard}'s content view to the given view. This will immediately restore any
     * view state. After calling this, {@link #getView()} will not return null.
     *
     * @see #setContentView(int)
     */
    @CallSuper
    public void setContentView(@NonNull View view) {
        checkCreated();
        ViewGroup frame = createFrame();
        frame.removeAllViews();
        frame.addView(view);
        restoreViewState(frame);
    }

    /**
     * Inflates the given layout and sets the {@code Shard}'s content view to it. This will
     * immediately restore any view state. After calling this, {@link #getView()} will not return
     * null.
     */
    @CallSuper
    public void setContentView(@LayoutRes int layoutId) {
        checkCreated();
        ViewGroup frame = createFrame();
        frame.removeAllViews();
        inflater.get(context).inflate(layoutId, frame, true);
        restoreViewState(frame);
    }

    public final CompositeLayoutInflater getCompositeLayoutInflater() {
        return inflater;
    }

    private ViewGroup createFrame() {
        if (frame == null) {
            frame = new FrameLayout(context);
            frame.setSaveFromParentEnabled(false);
            ViewTreeLifecycleOwner.set(frame, this);
            container.addView(frame);
        }
        return frame;
    }

    /**
     * Returns the root view for the {@code Shard}. This will be null until {@link #setContentView(int)}
     * or {@link #setContentView(View)} is called.
     */
    public ViewGroup getView() {
        return frame;
    }

    /**
     * Finds the first descendant view with the given ID, the view itself if
     * the ID matches {@link View#getId()}, or {@code null} if the ID is invalid
     * (< 0) or there is no matching view in the hierarchy.
     * <p>
     * <strong>Note:</strong> In most cases -- depending on compiler support --
     * the resulting view is automatically cast to the target class type. If
     * the target class type is unconstrained, an explicit cast may be
     * necessary.
     *
     * @param id the ID to search for
     * @return a view with given ID if found, or {@code null} otherwise
     * @see View#findViewById(int)
     */
    @Nullable
    public <T extends View> T findViewById(@IdRes int id) {
        return frame != null ? frame.<T>findViewById(id) : null;
    }

    /**
     * Finds the first descendant view with the given ID, the view itself if the ID matches
     * {@link View#getId()}, or throws an IllegalArgumentException if the ID is invalid or there is no
     * matching view in the hierarchy.
     * <p>
     * <strong>Note:</strong> In most cases -- depending on compiler support --
     * the resulting view is automatically cast to the target class type. If
     * the target class type is unconstrained, an explicit cast may be
     * necessary.
     *
     * @param id the ID to search for
     * @return a view with given ID
     * @see View#requireViewById(int)
     */
    @NonNull
    public final <T extends View> T requireViewById(@IdRes int id) {
        T view = findViewById(id);
        if (view == null) {
            throw new IllegalArgumentException("ID does not reference a View inside this View");
        }
        return view;
    }

    private void restoreViewState(View frame) {
        if (state != null && state.viewState != null) {
            frame.restoreHierarchyState(state.viewState);
        }
    }

    /**
     * Called when the shard is created, it's safe to call most method on the shard after
     * this.
     */
    public void onCreate() {
    }

    /**
     * Returns a context. This must not be called before {@link #onCreate()}.
     */
    @NonNull
    @Override
    public final Context getContext() {
        checkCreated();
        return context;
    }

    /**
     * Returns the factory used to construct {@code Shard}'s. This must not be called before
     * {@link #onCreate()}.
     */
    @NonNull
    @Override
    public Factory getShardFactory() {
        checkCreated();
        return shardFactory != null ? shardFactory : owner.getShardFactory();
    }

    /**
     * Sets the factory used to construct {@code Shard}'s
     */
    public void setShardFactory(@NonNull Factory factory) {
        shardFactory = factory;
    }

    /**
     * Returns {@link ActivityCallbacks} for interacting with activity-level api's. This must not be
     * called before {@link #onCreate()}.
     */
    @NonNull
    @Override
    public ActivityCallbacks getActivityCallbacks() {
        checkCreated();
        return activityCallbackDispatcher;
    }

    /**
     * Returns {@link ComponentCallbacks} for interacting with component-level api's. This must not
     * be called before {@link #onCreate()}.
     */
    @NonNull
    @Override
    public ComponentCallbacks getComponentCallbacks() {
        checkCreated();
        return componentCallbacksDispatcher;
    }

    private ViewModelStore getOrCreateViewModelStore() {
        ShardManagerViewModel viewModel = ShardManagerViewModel.get(owner.getViewModelStore());
        if (viewModelId == -1) {
            viewModelId = viewModel.nextId();
        }
        return viewModel.get(viewModelId);
    }

    private void removeViewModelStore(int id) {
        ShardManagerViewModel.get(owner.getViewModelStore()).remove(id);
    }

    private ViewModelProvider.Factory getOrCreateDefaultViewModelProviderFactory() {
        if (defaultViewModeProviderFactory == null) {
            defaultViewModeProviderFactory = new SavedStateViewModelFactory(
                    (Application) context.getApplicationContext(),
                    this,
                    getArgs()
            );
        }
        return defaultViewModeProviderFactory;
    }

    /**
     * Returns a {@link ViewModelStore} for holding onto {@link androidx.lifecycle.ViewModel}'s.
     * This must not be called before {@link #onCreate()}.
     */
    @NonNull
    @Override
    public ViewModelStore getViewModelStore() {
        checkCreated();
        return getOrCreateViewModelStore();
    }

    @NonNull
    @Override
    public ViewModelProvider.Factory getDefaultViewModelProviderFactory() {
        checkCreated();
        return getOrCreateDefaultViewModelProviderFactory();
    }

    /**
     * Returns a {@link Lifecycle} for this {@code Shard}.
     */
    @NonNull
    @Override
    public Lifecycle getLifecycle() {
        return lifecycleRegistry;
    }

    /**
     * Returns a {@link OnBackPressedDispatcher} for this {@code Shard}.
     */
    @NonNull
    @Override
    public OnBackPressedDispatcher getOnBackPressedDispatcher() {
        return owner.getOnBackPressedDispatcher();
    }

    /**
     * Returns a {@link SavedStateRegistry} for saving additional instance state.
     */
    @NonNull
    @Override
    public SavedStateRegistry getSavedStateRegistry() {
        return stateRegistryController.getSavedStateRegistry();
    }

    class Observer implements LifecycleEventObserver {
        @Override
        public void onStateChanged(@NonNull LifecycleOwner source, @NonNull Lifecycle.Event event) {
            lifecycleRegistry.handleLifecycleEvent(event);
            if (event == Lifecycle.Event.ON_DESTROY) {
                owner.getLifecycle().removeObserver(this);
            }
        }
    }

    /**
     * The {@code Shard}'s state which can be persisted.
     */
    public static class State implements Parcelable {
        final int viewModelId;
        @Nullable
        final Bundle args;
        Bundle savedState;
        SparseArray viewState;

        State(int viewModelId, @Nullable Bundle args) {
            this.viewModelId = viewModelId;
            this.args = args;
        }

        @SuppressWarnings("unchecked")
        State(Parcel in) {
            viewModelId = in.readInt();
            args = in.readBundle(getClass().getClassLoader());
            savedState = in.readBundle(getClass().getClassLoader());
            viewState = in.readSparseArray(getClass().getClassLoader());
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(viewModelId);
            dest.writeBundle(args);
            dest.writeBundle(savedState);
            dest.writeSparseArray(viewState);
        }

        @Override
        public int describeContents() {
            return 0;
        }

        public static final Creator<State> CREATOR = new Creator<State>() {
            @Override
            public State createFromParcel(Parcel in) {
                return new State(in);
            }

            @Override
            public State[] newArray(int size) {
                return new State[size];
            }
        };
    }

    /**
     * Factory to create a shard from the given class name and args.
     */
    public interface Factory {
        /**
         * Constructs a new instance of the shard.
         *
         * @param name The shard's class name.
         */
        @NonNull
        <T extends Shard> T newInstance(@NonNull String name);
    }

    /**
     * The default {@link Factory} which calls the zero-arg constructor of the shard using
     * reflection. This does <em>not</em> call {@link #setArgs(Bundle)}.
     */
    public static class DefaultFactory implements Factory {

        private static final Factory INSTANCE = new DefaultFactory();

        @NonNull
        public static Factory getInstance() {
            return INSTANCE;
        }

        @NonNull
        @Override
        @SuppressWarnings("unchecked")
        public <T extends Shard> T newInstance(@NonNull String name) {
            try {
                return (T) Class.forName(name).getConstructor().newInstance();
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            } catch (InstantiationException e) {
                throw new RuntimeException(e);
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(e);
            } catch (InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
