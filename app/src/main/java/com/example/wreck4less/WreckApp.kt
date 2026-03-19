package com.example.wreck4less

import android.app.Application
import androidx.compose.foundation.ComposeFoundationFlags
import androidx.compose.foundation.ExperimentalFoundationApi
import com.example.wreck4less.data.security.SecureStorage
import org.osmdroid.config.Configuration
import java.io.File

@OptIn(ExperimentalFoundationApi::class)
class WreckApp : Application() {
    override fun onCreate() {
        super.onCreate()
        ComposeFoundationFlags.isNonComposedClickableEnabled = false
        SecureStorage.init(applicationContext)
        val config = Configuration.getInstance()
        config.userAgentValue = packageName
        config.osmdroidBasePath = File(filesDir, "osmdroid")
        config.osmdroidTileCache = File(cacheDir, "osmdroid")
    }
}
