package posidon.launcher.items.users

import android.content.Context
import android.content.pm.LauncherApps
import android.content.res.Resources
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.UserHandle
import posidon.launcher.tools.IconTheming
import java.lang.ref.WeakReference
import java.util.concurrent.Executors
import java.util.concurrent.Future

class AppIconLoader(
    context: Context,
    val iconConfig: IconConfig,
) {

    inline fun load(
        context: Context,
        packageName: String,
        name: String,
        profile: UserHandle,
        crossinline onLoaded: (Drawable) -> Unit,
    ): Future<Unit> = submit {
        onLoaded(load(context, packageName, name, profile))
    }

    fun load(
        context: Context,
        packageName: String,
        name: String,
        profile: UserHandle,
    ): Drawable {
        val k = "$packageName/$name/${profile.hashCode()}"
        icons[k]?.get()?.let { return it }
        val iconPackIconInfo = iconPacks.firstNotNullOfOrNull { iconPackInfo ->
            iconPackInfo.getDrawableResource(packageName, name).let {
                if (it == 0) null else iconPackInfo.res to it
            }
        }
        var iconPackInfoToTheme: IconTheming.IconGenerationInfo? = null
        if (iconPackIconInfo == null) {
            iconPackInfoToTheme = iconPacks.firstNotNullOfOrNull {
                if (it.iconModificationInfo.areUnthemedIconsChanged) it.iconModificationInfo.also {
                    it.size = iconConfig.size
                } else null
            }
        }
        val c = iconPackIconInfo?.let { (res, iconRes) ->
            loadIcon(
                res,
                iconRes,
                iconConfig.density,
            )
        } ?: run {
            loadIcon(
                iconConfig.density,
                iconPackInfoToTheme,
                context.resources,
                packageName,
                name,
                profile,
            )
        }
        icons[k] = WeakReference(c)
        return c
    }

    inline fun submit(noinline action: () -> Unit): Future<Unit> = threadPool.submit<Unit>(action)

    @PublishedApi
    internal val threadPool = Executors.newWorkStealingPool((Runtime.getRuntime().availableProcessors() / 2).coerceAtLeast(1))
    private val icons = HashMap<String, WeakReference<Drawable>>()
    private val iconPacks = iconConfig.packPackages.mapNotNull { iconPackPackage ->
        var iconPackInfo: IconTheming.IconPackInfo? = null
        try {
            val themeRes = context.packageManager.getResourcesForApplication(iconPackPackage)
            iconPackInfo = IconTheming.getIconPackInfo(themeRes, iconPackPackage)
        } catch (e: Exception) { e.printStackTrace() }
        iconPackInfo
    }
    private val launcherApps = context.getSystemService(LauncherApps::class.java)

    private val p = Paint(Paint.FILTER_BITMAP_FLAG).apply {
        isAntiAlias = true
    }
    private val maskp = Paint(Paint.FILTER_BITMAP_FLAG).apply {
        isAntiAlias = true
        xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_OUT)
    }

    private fun loadIcon(
        res: Resources,
        iconRes: Int,
        density: Int,
    ): Drawable? {
        var icon: Drawable? = null
        try {
            icon = res.getDrawableForDensity(
                iconRes,
                density,
                null
            )
        } catch (e: Resources.NotFoundException) {}
        return icon
    }

    private fun loadIcon(
        density: Int,
        iconPackInfoToTheme: IconTheming.IconGenerationInfo?,
        resources: Resources,
        packageName: String,
        name: String,
        userHandle: UserHandle,
    ): Drawable {
        var icon = launcherApps.getActivityList(packageName, userHandle)?.find { it.name == name }?.getIcon(density) ?: ColorDrawable()
        iconPackInfoToTheme?.let {
            icon = themeIcon(
                icon,
                it,
                resources
            )
        }
        return icon
    }

    private fun themeIcon(
        icon: Drawable,
        iconPackInfo: IconTheming.IconGenerationInfo,
        resources: Resources
    ): Drawable = try {
        var orig = Bitmap.createBitmap(
            icon.intrinsicWidth,
            icon.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        icon.setBounds(0, 0, icon.intrinsicWidth, icon.intrinsicHeight)
        icon.draw(Canvas(orig))
        val scaledBitmap =
            Bitmap.createBitmap(iconPackInfo.size, iconPackInfo.size, Bitmap.Config.ARGB_8888)
        Canvas(scaledBitmap).run {
            val uniformOptions = BitmapFactory.Options().apply {
                inScaled = false
            }
            val back = iconPackInfo.getBackBitmap(uniformOptions)
            if (back != null) {
                drawBitmap(
                    back,
                    Rect(0, 0, back.width, back.height),
                    Rect(0, 0, iconPackInfo.size, iconPackInfo.size),
                    p
                )
                back.recycle()
            }
            val scaledOrig =
                Bitmap.createBitmap(iconPackInfo.size, iconPackInfo.size, Bitmap.Config.ARGB_8888)
            Canvas(scaledOrig).run {
                val s = (iconPackInfo.size * iconPackInfo.scaleFactor).toInt()
                val oldOrig = orig
                orig = Bitmap.createScaledBitmap(orig, s, s, true)
                oldOrig.recycle()
                drawBitmap(
                    orig,
                    scaledOrig.width - orig.width / 2f - scaledOrig.width / 2f,
                    scaledOrig.width - orig.width / 2f - scaledOrig.width / 2f,
                    p
                )
                val mask = iconPackInfo.getMaskBitmap(uniformOptions)
                if (mask != null) {
                    drawBitmap(
                        mask,
                        Rect(0, 0, mask.width, mask.height),
                        Rect(0, 0, iconPackInfo.size, iconPackInfo.size),
                        maskp
                    )
                    mask.recycle()
                }
            }
            drawBitmap(
                Bitmap.createScaledBitmap(scaledOrig, iconPackInfo.size, iconPackInfo.size, true),
                0f,
                0f,
                p
            )
            val front = iconPackInfo.getFrontBitmap(uniformOptions)
            if (front != null) {
                drawBitmap(
                    front,
                    Rect(0, 0, front.width, front.height),
                    Rect(0, 0, iconPackInfo.size, iconPackInfo.size),
                    p
                )
                front.recycle()
            }
            orig.recycle()
            scaledOrig.recycle()
        }
        BitmapDrawable(resources, scaledBitmap)
    } catch (e: Exception) {
        e.printStackTrace()
        icon
    }
}