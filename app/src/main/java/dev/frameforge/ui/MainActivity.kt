package dev.frameforge.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.frameforge.overlay.OverlayService
import rikka.shizuku.Shizuku

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MaterialTheme(colorScheme = darkColorScheme()) { Surface { Home() } } }
    }

    @Composable
    private fun Home() {
        var layer by remember { mutableStateOf("") }
        Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("FrameForge", style = MaterialTheme.typography.headlineMedium)
            Button(onClick = { startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))) }) { Text("1. Allow overlay") }
            Button(onClick = { runCatching { Shizuku.requestPermission(1) } }) { Text("2. Grant Shizuku access") }
            OutlinedTextField(layer, { layer = it }, label = { Text("SurfaceFlinger layer name") }, modifier = Modifier.fillMaxWidth())
            Button(onClick = { startForegroundService(Intent(this@MainActivity, OverlayService::class.java).putExtra("layer", layer)) }) { Text("3. Start overlay") }
            OutlinedButton(onClick = { stopService(Intent(this@MainActivity, OverlayService::class.java)) }) { Text("Stop") }
        }
    }
}
