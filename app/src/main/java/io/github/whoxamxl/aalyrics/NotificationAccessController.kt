package io.github.whoxamxl.aalyrics

import android.app.Activity
import android.app.NotificationManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import io.github.whoxamxl.aalyrics.platform.media.MediaSessionListenerService

/**
 * Framework boundary for the Notification Listener access required by MediaSession observation.
 *
 * The grant itself is system-owned; callers must re-check [isGranted] after returning from
 * Settings rather than assuming that launching Settings changed anything.
 */
internal class NotificationAccessController(
    private val activity: Activity,
) {
    private val listenerComponent =
        ComponentName(activity, MediaSessionListenerService::class.java)

    fun isGranted(): Boolean = isNotificationAccessGranted(activity)

    fun openSettings() {
        activity.startActivity(createSettingsIntent())
    }

    private fun createSettingsIntent(): Intent {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val detailIntent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_DETAIL_SETTINGS)
                .putExtra(
                    Settings.EXTRA_NOTIFICATION_LISTENER_COMPONENT_NAME,
                    listenerComponent.flattenToString(),
                )
            if (detailIntent.resolveActivity(activity.packageManager) != null) {
                return detailIntent
            }
        }

        val listenerSettingsIntent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
        if (listenerSettingsIntent.resolveActivity(activity.packageManager) != null) {
            return listenerSettingsIntent
        }

        return Intent(Settings.ACTION_SETTINGS)
    }
}

/** Shared read-only grant check for entry gating and process-level diagnostic snapshots. */
internal fun isNotificationAccessGranted(context: Context): Boolean {
    val component = ComponentName(context, MediaSessionListenerService::class.java)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
        return context.getSystemService(NotificationManager::class.java)
            .isNotificationListenerAccessGranted(component)
    }
    return Settings.Secure.getString(context.contentResolver, "enabled_notification_listeners")
        .orEmpty().split(':').any { ComponentName.unflattenFromString(it) == component }
}
