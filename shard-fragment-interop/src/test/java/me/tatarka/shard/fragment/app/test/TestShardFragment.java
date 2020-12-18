package me.tatarka.shard.fragment.app.test;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import me.tatarka.shard.fragment.app.ShardFragment;
import me.tatarka.shard.fragment.test.R;
import me.tatarka.shard.wiget.ShardHost;

public class TestShardFragment extends ShardFragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.test_shard_host, container, false);
    }

    public TestShard getShard() {
        ShardHost host = requireView().findViewById(R.id.host);
        return (TestShard) host.getShard();
    }
}
