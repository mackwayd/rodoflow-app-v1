package com.example.rodoflow.ui.components

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import java.io.File

@Composable
fun ComprovantePicker(
    imageUri: Uri?,
    onImageSelected: (Uri?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val authority = remember { "${context.packageName}.fileprovider" }
    var pendingCameraUri by remember { mutableStateOf<Uri?>(null) }

    val pickGallery = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri ->
        onImageSelected(uri)
    }

    val takePicture = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
    ) { success ->
        if (success) {
            onImageSelected(pendingCameraUri)
        }
        pendingCameraUri = null
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Comprovante (opcional)",
            style = MaterialTheme.typography.titleSmall,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            OutlinedButton(
                onClick = {
                    val dir = File(context.cacheDir, "comprovantes").apply { mkdirs() }
                    val file = File(dir, "capture_${System.currentTimeMillis()}.jpg")
                    val uri = FileProvider.getUriForFile(context, authority, file)
                    pendingCameraUri = uri
                    takePicture.launch(uri)
                },
                modifier = Modifier.weight(1f),
            ) {
                Icon(Icons.Outlined.CameraAlt, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Câmera")
            }
            OutlinedButton(
                onClick = {
                    pickGallery.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                    )
                },
                modifier = Modifier.weight(1f),
            ) {
                Icon(Icons.Outlined.PhotoLibrary, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Galeria")
            }
        }
        imageUri?.let { uri ->
            Spacer(modifier = Modifier.height(12.dp))
            val bitmap = remember(uri) {
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    BitmapFactory.decodeStream(stream)
                }
            }
            bitmap?.let { bmp ->
                Image(
                    bitmap = bmp.asImageBitmap(),
                    contentDescription = "Pré-visualização do comprovante",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    contentScale = ContentScale.Crop,
                )
            }
            TextButton(
                onClick = { onImageSelected(null) },
                modifier = Modifier.align(Alignment.End),
            ) {
                Text("Remover foto")
            }
        }
    }
}
