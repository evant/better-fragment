package me.tatarka.shard.sample

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.view.View
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import me.tatarka.shard.app.Shard
import me.tatarka.shard.app.newInstance
import me.tatarka.shard.app.showDialog
import javax.inject.Inject

class DialogHostShard @Inject constructor() : Shard() {

    override fun onCreate() {
        setContentView(R.layout.dialogs)

        val activityForResult = prepareCall(StartActivityForResult()) { result ->
            requireViewById<Button>(R.id.start_activity_for_result).text =
                    "Result: ${if (result.resultCode == Activity.RESULT_OK) "Ok" else "Cancel"}"
        }
        val permissionCallback = prepareCall(RequestPermission()) { result ->
            requireViewById<Button>(R.id.request_permission).text =
                    "Result: ${if (result) "Granted" else "Denied"}"
        }

        requireViewById<View>(R.id.simple_dialog).setOnClickListener {
            showDialog(shardFactory.newInstance<SimpleDialogShard>().withNumber(1))
        }
        requireViewById<View>(R.id.alert_dialog).setOnClickListener {
            showDialog(shardFactory.newInstance<MyAlertDialogShard>())
        }
        requireViewById<View>(R.id.alert_dialog_custom_view).setOnClickListener {
            showDialog(shardFactory.newInstance<MyAlertDialogShard>().withCustomView(2))
        }
        requireViewById<View>(R.id.start_activity_for_result).setOnClickListener {
            activityForResult.launch(Intent(context, ResultActivity::class.java))
        }
        requireViewById<View>(R.id.request_permission).setOnClickListener {
            permissionCallback.launch(Manifest.permission.SEND_SMS)
        }
    }
}