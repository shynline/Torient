package app.shynline.torient.common.userpreference

interface UserPreference {
    var globalMaxConnection: Int
    var globalDownloadRateLimit: Boolean
    var globalDownloadRate: Int
    var globalUploadRateLimit: Boolean
    var globalUploadRate: Int


    fun resetToDefault()
}