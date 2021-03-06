package me.tatarka.shard.sample

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import leakcanary.AppWatcher
import me.tatarka.shard.app.Shard

class LeakLifecycleWatcher(private val shard: Shard) : LifecycleObserver {

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy() {
        shard.lifecycle.removeObserver(this)
        // An animation might be running, wait a bit for it to finish.
        if (shard.view != null) {
            shard.view.postDelayed({
                AppWatcher.objectWatcher.watch(shard, "Shard")
            }, 500)
        } else {
            AppWatcher.objectWatcher.watch(shard, "Shard")
        }
    }

    companion object {
        fun <T : Shard> attach(shard: T): T = shard.apply {
            shard.lifecycle.addObserver(LeakLifecycleWatcher(shard))
        }

        fun wrap(factory: Shard.Factory): Shard.Factory = object : Shard.Factory {
            override fun <T : Shard> newInstance(name: String): T =
                attach(factory.newInstance(name))
        }
    }
}