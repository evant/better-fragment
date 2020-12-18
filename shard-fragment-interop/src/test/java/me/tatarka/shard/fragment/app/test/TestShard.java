package me.tatarka.shard.fragment.app.test;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityOptionsCompat;
import androidx.fragment.app.FragmentManager;
import androidx.savedstate.SavedStateRegistry;

import me.tatarka.shard.activity.ActivityCallbacks;
import me.tatarka.shard.app.Shard;
import me.tatarka.shard.fragment.test.R;

import static me.tatarka.shard.fragment.app.ShardFragmentManager.getFragmentManager;

public class TestShard extends Shard {
    public boolean onCreateCalled;
    public int state;
    public TestFragment fragment;
    public int resultCode;
    public boolean permissionGranted;

    private ActivityResultLauncher<Intent> activityResult;
    private ActivityResultLauncher<String> permissionResult2;

    @Override
    public void onCreate() {
        onCreateCalled = true;
        setContentView(R.layout.test_shard);

        Bundle bundle = getSavedStateRegistry().consumeRestoredStateForKey("state");
        if (bundle != null) {
            state = bundle.getInt("state");
        }
        getSavedStateRegistry().registerSavedStateProvider("state", new SavedStateRegistry.SavedStateProvider() {
            @NonNull
            @Override
            public Bundle saveState() {
                Bundle bundle = new Bundle();
                bundle.putInt("state", state);
                return bundle;
            }
        });

        activityResult = prepareCall(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                TestShard.this.resultCode = result.getResultCode();
            }
        });
        permissionResult2 = prepareCall(new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
            @Override
            public void onActivityResult(Boolean result) {
                permissionGranted = result;
            }
        });

        FragmentManager fm = getFragmentManager(this);
        if ((fragment = (TestFragment) fm.findFragmentById(R.id.content)) == null) {
            fragment = new TestFragment();
            fm.beginTransaction().add(R.id.content, fragment).commitNow();
        }
    }

    public void startActivityForResult(Class<? extends Activity> activity) {
        activityResult.launch(new Intent(getContext(), activity));
    }

    public void startActivityForResultWithOptions(Class<? extends Activity> activity) {
        activityResult.launch(new Intent(getContext(), activity), ActivityOptionsCompat.makeBasic());
    }

    public void requestPermission(String permission) {
        permissionResult2.launch(permission);
    }
}
