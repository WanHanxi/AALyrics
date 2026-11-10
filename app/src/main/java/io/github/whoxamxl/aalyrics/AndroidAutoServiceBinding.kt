package io.github.whoxamxl.aalyrics

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.service.media.MediaBrowserService
import io.github.whoxamxl.aalyrics.ui.automotive.service.LyricsBrowserService

/** Explicit self-binding creates the existing media service without a started/foreground service. */
internal class AndroidAutoServiceBinding(context: Context) : AutomotiveActivationBinding {
    private val applicationContext = context.applicationContext
    private var connection: ServiceConnection? = null

    override fun bind(listener: AutomotiveActivationBinding.Listener): Boolean {
        check(connection == null)
        val requested = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName, service: IBinder) = listener.onConnected()
            override fun onServiceDisconnected(name: ComponentName) = listener.onDisconnected()
            override fun onBindingDied(name: ComponentName) = listener.onBindingLost("binding_died")
            override fun onNullBinding(name: ComponentName) = listener.onBindingLost("null_binding")
        }
        // Retain before calling Android so even rejected/throwing requests can be cleaned up.
        connection = requested
        val intent = Intent(applicationContext, LyricsBrowserService::class.java)
            .setAction(MediaBrowserService.SERVICE_INTERFACE)
        return applicationContext.bindService(intent, requested, Context.BIND_AUTO_CREATE)
    }

    override fun unbind() {
        val released = connection ?: return
        connection = null
        applicationContext.unbindService(released)
    }
}
