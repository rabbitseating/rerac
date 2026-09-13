package com.example.mapbox

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.mapbox.android.core.permissions.PermissionsListener
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.android.core.permissions.PermissionsManager.areLocationPermissionsGranted

class MainActivity : AppCompatActivity(), PermissionsListener {

    private val permissionsManager = PermissionsManager(this)
    private var navigationStarted = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!isMapboxTokenProvided()) {
            showNoTokenErrorDialog()
            return
        }

        if (areLocationPermissionsGranted(this)) {
            requestOptionalPermissions()
            launchNavigation()
        } else {
            permissionsManager.requestLocationPermissions(this)
        }
    }

    override fun onExplanationNeeded(permissionsToExplain: MutableList<String>?) {
        Toast.makeText(
            this,
            "Location permission is required to provide navigation and proximity alerts.",
            Toast.LENGTH_LONG
        ).show()
    }

    override fun onPermissionResult(granted: Boolean) {
        if (granted) {
            requestOptionalPermissions()
            launchNavigation()
        } else {
            Toast.makeText(
                this,
                "Location permission is required to use the navigation features.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun requestOptionalPermissions() {
        val permissionsToRequest = mutableListOf<String>()

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            permissionsToRequest.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permissionsToRequest.toTypedArray(), 10)
        }
    }

    private fun launchNavigation() {
        if (navigationStarted) return
        navigationStarted = true
        startActivity(Intent(this, TurnByTurnActivity::class.java))
        finish()
    }

    private fun isMapboxTokenProvided(): Boolean =
        getString(R.string.mapbox_access_token).isNotBlank()

    private fun showNoTokenErrorDialog() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.noTokenDialogTitle))
            .setMessage(getString(R.string.noTokenDialogBody))
            .setCancelable(false)
            .setPositiveButton("OK") { _, _ -> finish() }
            .show()
    }
}
