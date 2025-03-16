package com.heroesofpenta

import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat


class PermissionUtils {
  fun checkAndRequestPermissions(context: Context?): List<String> {
    val camera = ContextCompat.checkSelfPermission(context!!, android.Manifest.permission.CAMERA)
//    val readStorage =
//      ContextCompat.checkSelfPermission(context!!, Manifest.permission.READ_EXTERNAL_STORAGE)
//    val writeStorage = ContextCompat.checkSelfPermission(
//      context!!,
//      android.Manifest.permission.WRITE_EXTERNAL_STORAGE
//    )
//    val fineLoc =
//      ContextCompat.checkSelfPermission(context!!, android.Manifest.permission.ACCESS_FINE_LOCATION)
//    val coarseLoc = ContextCompat.checkSelfPermission(
//      context!!,
//      android.Manifest.permission.ACCESS_COARSE_LOCATION
//    )
    val listPermissionsNeeded: MutableList<String> = ArrayList()

    if (camera != PackageManager.PERMISSION_GRANTED) {
      listPermissionsNeeded.add(android.Manifest.permission.CAMERA)
    }
//    if (readStorage != PackageManager.PERMISSION_GRANTED) {
//      listPermissionsNeeded.add(android.Manifest.permission.READ_EXTERNAL_STORAGE)
//    }
//    if (writeStorage != PackageManager.PERMISSION_GRANTED) {
//      listPermissionsNeeded.add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
//    }
//    if (fineLoc != PackageManager.PERMISSION_GRANTED) {
//      listPermissionsNeeded.add(android.Manifest.permission.ACCESS_FINE_LOCATION)
//    }
//    if (coarseLoc != PackageManager.PERMISSION_GRANTED) {
//      listPermissionsNeeded.add(android.Manifest.permission.ACCESS_COARSE_LOCATION)
//    }

    return listPermissionsNeeded
  }
}