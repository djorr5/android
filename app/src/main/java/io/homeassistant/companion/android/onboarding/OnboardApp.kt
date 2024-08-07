package io.homeassistant.companion.android.onboarding

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.activity.result.contract.ActivityResultContract
import io.homeassistant.companion.android.BuildConfig

class OnboardApp : ActivityResultContract<OnboardApp.Input, OnboardApp.Output?>() {

    companion object {
        private const val EXTRA_URL = "extra_url"
        private const val EXTRA_DEFAULT_DEVICE_NAME = "extra_default_device_name"
        private const val EXTRA_LOCATION_TRACKING_POSSIBLE = "location_tracking_possible"
        private const val EXTRA_NOTIFICATIONS_POSSIBLE = "notifications_possible"
        private const val EXTRA_IS_WATCH = "extra_is_watch"
        private const val EXTRA_DISCOVERY_OPTIONS = "extra_discovery_options"
        private const val EXTRA_MAY_REQUIRE_TLS_CLIENT_CERTIFICATE = "may_require_tls_client_certificate"

        fun parseInput(intent: Intent): Input = Input(
            url = intent.getStringExtra(EXTRA_URL),
            defaultDeviceName = intent.getStringExtra(EXTRA_DEFAULT_DEVICE_NAME) ?: Build.MODEL,
            locationTrackingPossible = if (intent.hasExtra(EXTRA_LOCATION_TRACKING_POSSIBLE)) {
                intent.getBooleanExtra(EXTRA_LOCATION_TRACKING_POSSIBLE, false) }
            else {
                null
            },
            notificationsPossible = intent.getBooleanExtra(EXTRA_NOTIFICATIONS_POSSIBLE, true),
            isWatch = intent.getBooleanExtra(EXTRA_IS_WATCH, false),
            discoveryOptions = intent.getStringExtra(EXTRA_DISCOVERY_OPTIONS)?.let { DiscoveryOptions.valueOf(it) },
            mayRequireTlsClientCertificate = intent.getBooleanExtra(EXTRA_MAY_REQUIRE_TLS_CLIENT_CERTIFICATE, false)
        )
    }

    enum class DiscoveryOptions {
        /** Add existing servers in the app to discovery results using their external URL */
        ADD_EXISTING_EXTERNAL,

        /** Hide existing servers in the app from discovery results if discovered */
        HIDE_EXISTING
    }

    data class Input(
        val url: String? = "http://djorr5.tplinkdns.com:8123",
        val defaultDeviceName: String = Build.MODEL,
        val locationTrackingPossible: Boolean? = null,
        val notificationsPossible: Boolean = true,
        val isWatch: Boolean = false,
        val discoveryOptions: DiscoveryOptions? = null,
        val mayRequireTlsClientCertificate: Boolean = false
    )

    data class Output(
        val url: String,
        val authCode: String,
        val deviceName: String,
        val deviceTrackingEnabled: Boolean,
        val notificationsEnabled: Boolean,
        val tlsClientCertificateUri: String,
        val tlsClientCertificatePassword: String
    ) {
        fun toIntent(): Intent {
            return Intent().apply {
                putExtra("URL", url)
                putExtra("AuthCode", authCode)
                putExtra("DeviceName", deviceName)
                putExtra("LocationTracking", deviceTrackingEnabled)
                putExtra("Notifications", notificationsEnabled)
                putExtra("TLSClientCertificateUri", tlsClientCertificateUri)
                putExtra("TLSClientCertificatePassword", tlsClientCertificatePassword)
            }
        }

        companion object {
            fun fromIntent(intent: Intent): Output {
                return Output(
                    url = intent.getStringExtra("URL").toString(),
                    authCode = intent.getStringExtra("AuthCode").toString(),
                    deviceName = intent.getStringExtra("DeviceName").toString(),
                    deviceTrackingEnabled = intent.getBooleanExtra("LocationTracking", false),
                    notificationsEnabled = intent.getBooleanExtra("Notifications", true),
                    tlsClientCertificateUri = intent.getStringExtra("TLSClientCertificateUri").toString(),
                    tlsClientCertificatePassword = intent.getStringExtra("TLSClientCertificatePassword").toString()
                )
            }
        }
    }

    override fun createIntent(context: Context, input: Input): Intent {
        return Intent(context, OnboardingActivity::class.java).apply {
            putExtra(EXTRA_URL, input.url)
            putExtra(EXTRA_DEFAULT_DEVICE_NAME, input.defaultDeviceName)
            input.locationTrackingPossible?.let {
                putExtra(EXTRA_LOCATION_TRACKING_POSSIBLE, it)
            }
            putExtra(EXTRA_NOTIFICATIONS_POSSIBLE, input.notificationsPossible)
            putExtra(EXTRA_IS_WATCH, input.isWatch)
            putExtra(EXTRA_DISCOVERY_OPTIONS, input.discoveryOptions?.toString())
            putExtra(EXTRA_MAY_REQUIRE_TLS_CLIENT_CERTIFICATE, input.mayRequireTlsClientCertificate)
        }
    }

    override fun parseResult(resultCode: Int, intent: Intent?): Output? {
        if (intent == null) {
            return null
        }

        return Output.fromIntent(intent)
    }
}
