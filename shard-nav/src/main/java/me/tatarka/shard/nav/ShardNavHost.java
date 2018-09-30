package me.tatarka.shard.nav;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.CallSuper;
import androidx.annotation.NavigationRes;
import androidx.annotation.NonNull;
import androidx.navigation.NavController;
import androidx.navigation.NavGraph;
import androidx.navigation.NavHost;
import androidx.navigation.Navigation;
import me.tatarka.shard.app.Shard;
import me.tatarka.shard.app.ShardOwner;
import me.tatarka.shard.app.ShardOwners;

public class ShardNavHost extends FrameLayout implements NavHost {

    private NavController navController;
    private ShardOwner owner;
    private int graphId = 0;

    public ShardNavHost(Context context) {
        this(context, null);
    }

    public ShardNavHost(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (!isInEditMode()) {
            owner = ShardOwners.get(context);
            navController = new NavController(context);
        }
        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ShardNavHost);
            graphId = a.getResourceId(R.styleable.ShardNavHost_graphId, View.NO_ID);
            a.recycle();
            Navigation.setViewNavController(this, navController);
            ShardNavigator shardNavigator = new ShardNavigator(this);
            navController.getNavigatorProvider().addNavigator(shardNavigator);
        }
    }

    public void setGraph(NavGraph graph) {
        navController.setGraph(graph);
    }

    public void setGraph(@NavigationRes int graphId) {
        navController.setGraph(graphId);
    }

    public void setShardFactory(Shard.Factory factory) {
        navController.getNavigatorProvider().getNavigator(ShardNavigator.class).setShardFactory(factory);
    }

    public Shard.Factory getShardFactory() {
        return navController.getNavigatorProvider().getNavigator(ShardNavigator.class).getShardFactory();
    }

    @Override
    @CallSuper
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (graphId != 0 && !owner.getInstanceStateStore().isStateRestored()) {
            navController.setGraph(graphId);
        }
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        return new SavedState(super.onSaveInstanceState(), navController.saveState());
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        SavedState savedState = (SavedState) state;
        super.onRestoreInstanceState(savedState.getSuperState());
        navController.restoreState(savedState.navState);
    }

    @NonNull
    @Override
    public NavController getNavController() {
        return navController;
    }

    public static class SavedState extends BaseSavedState {
        final Bundle navState;

        SavedState(Parcel source) {
            super(source);
            navState = source.readBundle(getClass().getClassLoader());
        }

        SavedState(Parcelable superState, Bundle navState) {
            super(superState);
            this.navState = navState;
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeBundle(navState);
        }

        public static Parcelable.Creator<SavedState> CREATOR = new Creator<SavedState>() {
            @Override
            public SavedState createFromParcel(Parcel parcel) {
                return new SavedState(parcel);
            }

            @Override
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }
}