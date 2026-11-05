package com.example.deepsource.ui

import android.Manifest
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.deepsource.AppViewModel
import com.example.deepsource.ui.theme.Muted
import java.io.File

private enum class CameraRequest { Idle, Pending, Denied }

@Composable
internal fun ProfilePhotoPanel(vm: AppViewModel, owner: String) {
    val context = LocalContext.current
    var capturePath by rememberSaveable(owner) { mutableStateOf<String?>(null) }
    var cameraRequest by rememberSaveable(owner) { mutableStateOf(CameraRequest.Idle) }
    val cameraContract = remember {
        object : ActivityResultContracts.TakePicture() {
            override fun createIntent(context: Context, input: Uri): Intent =
                super.createIntent(context, input).apply {
                    clipData = ClipData.newRawUri("Foto de perfil", input)
                    addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
        }
    }
    val camera = rememberLauncherForActivityResult(cameraContract) { saved ->
        val file = capturePath?.let(::File)
        capturePath = null
        if (file != null) {
            if (saved) vm.updateProfilePhoto(owner, Uri.fromFile(file), file)
            else file.delete()
        }
    }

    fun launchCamera() {
        try {
            val directory = context.cacheDir.resolve("profile-capture").apply { mkdirs() }
            val file = File.createTempFile("camera-", ".jpg", directory)
            capturePath = file.absolutePath
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.profile-photos", file)
            camera.launch(uri)
        } catch (_: Exception) {
            capturePath?.let(::File)?.delete()
            capturePath = null
            vm.message = "No pudimos abrir la cámara. Puedes elegir una foto de la galería."
        }
    }

    val cameraPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        cameraRequest = if (granted) CameraRequest.Idle else CameraRequest.Denied
        if (granted) launchCamera()
    }
    val gallery = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) vm.updateProfilePhoto(owner, uri)
    }

    val enabled = !vm.busy && !vm.photoBusy && capturePath == null && cameraRequest != CameraRequest.Pending
    PanelCard {
        Text("Foto de perfil", style = MaterialTheme.typography.titleMedium)
        Text("Toma una foto o elige una imagen. Se guarda en este dispositivo.", color = Muted,
            style = MaterialTheme.typography.bodySmall)
        if (vm.photoBusy) LinearProgressIndicator(Modifier.fillMaxWidth())
        Secondary("Tomar foto", enabled = enabled) {
            if (cameraRequest == CameraRequest.Pending || capturePath != null) return@Secondary
            val permission = Manifest.permission.CAMERA
            if (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED) {
                cameraRequest = CameraRequest.Idle
                launchCamera()
            } else {
                cameraRequest = CameraRequest.Pending
                cameraPermission.launch(permission)
            }
        }
        if (cameraRequest == CameraRequest.Denied) {
            Text(
                "Permiso de cámara denegado. Puedes habilitarlo en Ajustes o elegir una foto de la galería.",
                color = Muted,
                style = MaterialTheme.typography.bodySmall,
            )
        }
        Secondary("Elegir de la galería", enabled = enabled) {
            try {
                gallery.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            } catch (_: Exception) {
                vm.message = "No pudimos abrir el selector de fotos"
            }
        }
        if (vm.profilePhoto != null) {
            TextButton(onClick = { vm.updateProfilePhoto(owner, source = null) }, enabled = enabled) {
                Text("Quitar foto")
            }
        }
    }
}
