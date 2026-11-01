package io.github.whoxamxl.aalyrics

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.browser.customtabs.CustomTabsIntent
import io.github.whoxamxl.aalyrics.ui.designsystem.theme.AALyricsTheme
import io.github.whoxamxl.aalyrics.ui.phone.settings.AndroidAutoCompatibilityUiStatus
import io.github.whoxamxl.aalyrics.ui.phone.setup.createAndroidAutoCompatibilitySetupView
import io.github.whoxamxl.aalyrics.ui.phone.setup.createNotificationAccessSetupView

class MainActivity : ComponentActivity() {
    private lateinit var notificationAccessController: NotificationAccessController
    private lateinit var notificationAccessGate: NotificationAccessGate
    private lateinit var androidAutoCompatibilityOnboarding: AndroidAutoCompatibilityOnboarding
    private var renderedEntryState: AppEntryState? = null
    private var compatibilitySetupRequested = false
    private val connectionDiagnosticsExportLauncher =
        registerForActivityResult(ActivityResultContracts.CreateDocument("text/plain")) { uri ->
            if (uri != null && BuildConfig.DEBUG) {
                val app = application as AALyricsApplication
                Toast.makeText(app, R.string.connection_diagnostics_saving, Toast.LENGTH_SHORT).show()
                app.exportConnectionDiagnostics(uri) { succeeded ->
                    Toast.makeText(
                        app,
                        if (succeeded) R.string.connection_diagnostics_saved else R.string.connection_diagnostics_failed,
                        Toast.LENGTH_LONG,
                    ).show()
                }
            }
        }
    private val installSourceTrustSettingsLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            (application as? AALyricsApplication)
                ?.onInstallSourceTrustReturned()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        notificationAccessController = NotificationAccessController(this)
        notificationAccessGate = NotificationAccessGate(notificationAccessController::isGranted)

        val preferences = getSharedPreferences(ENTRY_PREFERENCES_NAME, MODE_PRIVATE)
        androidAutoCompatibilityOnboarding = AndroidAutoCompatibilityOnboarding(
            readValue = {
                preferences.getString(ANDROID_AUTO_COMPATIBILITY_KEY, null)
            },
            writeValue = { value ->
                preferences.edit().apply {
                    if (value == null) {
                        remove(ANDROID_AUTO_COMPATIBILITY_KEY)
                    } else {
                        putString(ANDROID_AUTO_COMPATIBILITY_KEY, value)
                    }
                }.apply()
            },
        )

        renderEntryState()
    }

    override fun onResume() {
        super.onResume()
        (application as? AALyricsApplication)?.onPhoneResumedForDiagnostics()
        renderEntryState()
    }

    override fun onStop() {
        (application as? AALyricsApplication)
            ?.dismissInstallPermissionPrompt()
        super.onStop()
    }

    private fun renderEntryState() {
        val state = currentEntryState()
        if (state == renderedEntryState) return
        renderedEntryState = state

        when (state) {
            AppEntryState.NOTIFICATION_ACCESS_REQUIRED -> {
                setContentView(
                    createNotificationAccessSetupView(
                        context = this,
                        onGrantAccess = notificationAccessController::openSettings,
                    ),
                )
            }

            AppEntryState.ANDROID_AUTO_COMPATIBILITY -> {
                setContentView(
                    createAndroidAutoCompatibilitySetupView(
                        context = this,
                        onEnabled = {
                            androidAutoCompatibilityOnboarding.markEnabled()
                            compatibilitySetupRequested = false
                            renderedEntryState = null
                            renderEntryState()
                        },
                        onContinueWithout = {
                            androidAutoCompatibilityOnboarding.skip()
                            compatibilitySetupRequested = false
                            renderedEntryState = null
                            renderEntryState()
                        },
                    ),
                )
            }

            AppEntryState.READY -> {
                setContent {
                    AALyricsTheme {
                        PhoneRuntimeHost(
                            application = application as AALyricsApplication,
                            onExportConnectionDiagnostics = if (BuildConfig.DEBUG) {
                                { exportConnectionDiagnostics() }
                            } else null,
                            androidAutoStatus = androidAutoCompatibilityOnboarding
                                .status()
                                .toUiStatus(),
                            onAndroidAutoCompatibilitySetup = {
                                compatibilitySetupRequested = true
                                renderedEntryState = null
                                renderEntryState()
                            },
                            onResetAALyrics = {
                                androidAutoCompatibilityOnboarding.reset()
                                compatibilitySetupRequested = false
                                renderedEntryState = null
                                renderEntryState()
                            },
                            onOpenInstallSettings = {
                                val intent = InstallSourceTrustSettingsIntent.create(packageName)
                                if (intent.resolveActivity(packageManager) != null) {
                                    installSourceTrustSettingsLauncher.launch(intent)
                                }
                            },
                            onOpenSourceCode = {
                                openUrl(SOURCE_CODE_URL)
                            },
                            onOpenHelpFeedback = { destination ->
                                openCustomTabUrl(destination.githubUrl())
                            },
                            onOpenSupportAALyrics = {
                                openCustomTabUrl(SUPPORT_URL)
                            },
                            onOpenUpdateRelease = { versionName ->
                                openCustomTabUrl(
                                    RELEASE_TAG_URL_PREFIX +
                                        Uri.encode("v$versionName"),
                                )
                            },
                        )
                    }
                }
            }
        }
    }

    private fun currentEntryState(): AppEntryState {
        if (notificationAccessGate.currentState() == NotificationAccessEntryState.REQUIRED) {
            return AppEntryState.NOTIFICATION_ACCESS_REQUIRED
        }

        if (
            compatibilitySetupRequested ||
            androidAutoCompatibilityOnboarding.status() ==
            AndroidAutoCompatibilitySetupStatus.NOT_REVIEWED
        ) {
            return AppEntryState.ANDROID_AUTO_COMPATIBILITY
        }

        return AppEntryState.READY
    }

    private fun openUrl(url: String) {
        startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse(url),
            ),
        )
    }

    private fun exportConnectionDiagnostics() {
        val timestamp = java.time.LocalDateTime.now().format(
            java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"),
        )
        try {
            connectionDiagnosticsExportLauncher.launch("aalyrics-connection-$timestamp.txt")
        } catch (_: RuntimeException) {
            Toast.makeText(this, R.string.connection_diagnostics_failed, Toast.LENGTH_LONG).show()
        }
    }

    private fun openCustomTabUrl(url: String) {
        val uri = Uri.parse(url)
        runCatching {
            CustomTabsIntent.Builder()
                .build()
                .launchUrl(this, uri)
        }.onFailure {
            openUrl(url)
        }
    }

    private fun AndroidAutoCompatibilitySetupStatus.toUiStatus():
        AndroidAutoCompatibilityUiStatus = when (this) {
        AndroidAutoCompatibilitySetupStatus.NOT_REVIEWED ->
            AndroidAutoCompatibilityUiStatus.NOT_REVIEWED
        AndroidAutoCompatibilitySetupStatus.ENABLED ->
            AndroidAutoCompatibilityUiStatus.ENABLED
        AndroidAutoCompatibilitySetupStatus.SKIPPED ->
            AndroidAutoCompatibilityUiStatus.SKIPPED
    }

    private enum class AppEntryState {
        NOTIFICATION_ACCESS_REQUIRED,
        ANDROID_AUTO_COMPATIBILITY,
        READY,
    }

    private companion object {
        const val ENTRY_PREFERENCES_NAME = "app_entry_setup"
        const val ANDROID_AUTO_COMPATIBILITY_KEY = "android_auto_compatibility"
        const val SOURCE_CODE_URL = "https://github.com/whoxamxl/AALyrics"
        const val RELEASE_TAG_URL_PREFIX =
            "https://github.com/whoxamxl/AALyrics/releases/tag/"
        const val SUPPORT_URL = "https://buymeacoffee.com/whoxamxi"
    }
}
