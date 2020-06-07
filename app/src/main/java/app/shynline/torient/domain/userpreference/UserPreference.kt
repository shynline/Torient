package app.shynline.torient.domain.userpreference

interface UserPreference {
    var globalMaxConnection: Int
    var globalDownloadRateLimit: Boolean
    var globalDownloadRate: Int
    var globalUploadRateLimit: Boolean
    var globalUploadRate: Int


    fun resetToDefault()
}