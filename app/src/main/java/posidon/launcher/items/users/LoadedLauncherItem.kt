package posidon.launcher.items.users

import android.content.Context
import android.os.UserHandle

class LoadedLauncherItem internal constructor(
    val packageName: String,
    val name: String,
    val profile: UserHandle,
    val rawLabel: String,
) {
    fun getBadgedLabel(context: Context): String = context.packageManager.getUserBadgedLabel(rawLabel, profile).toString()
}