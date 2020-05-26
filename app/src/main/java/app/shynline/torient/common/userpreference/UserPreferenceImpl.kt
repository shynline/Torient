package app.shynline.torient.common.userpreference

import android.content.SharedPreferences

class UserPreferenceImpl(
    private val userSharedPreferences: SharedPreferences
) : UserPreference {

    init {
        if (!userSharedPreferences.getBoolean(KEY_USER_PREF_INITIATED, false)) {
            resetToDefault()
            userSharedPreferences.edit().putBoolean(KEY_USER_PREF_INITIATED, true).apply()
        }
    }

    override var globalMaxConnection: Int = -1
        get() {
            if (field == -1)
                field = userSharedPreferences.getInt(KEY_USER_PREF_GLOBAL_MAX_CONNECTION, -1)
            return field
        }
        set(value) {
            field = value
            userSharedPreferences.edit().putInt(KEY_USER_PREF_GLOBAL_MAX_CONNECTION, value).apply()
        }

    override var globalDownloadRateLimit: Boolean
        get() = userSharedPreferences.getBoolean(KEY_USER_PREF_GLOBAL_DOWNLOAD_RATE_LIMIT, false)
        set(value) {
            userSharedPreferences.edit().putBoolean(KEY_USER_PREF_GLOBAL_DOWNLOAD_RATE_LIMIT, value)
                .apply()
        }

    override var globalDownloadRate: Int = -1
        get() {
            if (field == -1) {
                field = userSharedPreferences.getInt(KEY_USER_PREF_GLOBAL_DOWNLOAD_RATE, -1)
            }
            return field
        }
        set(value) {
            field = value
            userSharedPreferences.edit().putInt(KEY_USER_PREF_GLOBAL_DOWNLOAD_RATE, value).apply()
        }

    override var globalUploadRateLimit: Boolean
        get() = userSharedPreferences.getBoolean(KEY_USER_PREF_GLOBAL_UPLOAD_RATE_LIMIT, false)
        set(value) {
            userSharedPreferences.edit().putBoolean(KEY_USER_PREF_GLOBAL_UPLOAD_RATE_LIMIT, value)
                .apply()
        }

    override var globalUploadRate: Int = -1
        get() {
            if (field == -1) {
                field = userSharedPreferences.getInt(KEY_USER_PREF_GLOBAL_UPLOAD_RATE, -1)
            }
            return field
        }
        set(value) {
            field = value
            userSharedPreferences.edit().putInt(KEY_USER_PREF_GLOBAL_UPLOAD_RATE, value).apply()
        }

    override fun resetToDefault() {
        globalMaxConnection = 200
        globalDownloadRateLimit = false
        globalDownloadRate = 100
        globalUploadRateLimit = false
        globalUploadRate = 100
    }

    companion object {
        const val KEY_USER_PREF_INITIATED = "user.pref.initiated"
        const val KEY_USER_PREF_GLOBAL_MAX_CONNECTION = "user.pref.global.max.connection"
        const val KEY_USER_PREF_GLOBAL_DOWNLOAD_RATE_LIMIT = "user.pref.global.download.rate.limit"
        const val KEY_USER_PREF_GLOBAL_UPLOAD_RATE_LIMIT = "user.pref.global.upload.rate.limit"
        const val KEY_USER_PREF_GLOBAL_DOWNLOAD_RATE = "user.pref.global.download.rate"
        const val KEY_USER_PREF_GLOBAL_UPLOAD_RATE = "user.pref.global.upload.rate"

    }
}