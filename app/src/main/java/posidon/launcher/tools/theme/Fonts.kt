package posidon.launcher.tools.theme

import android.app.Activity
import android.content.Context
import android.graphics.Typeface
import posidon.launcher.R
import posidon.launcher.storage.Settings

object Fonts {

    const val SANS_SERIF = "sansserif"
    const val POSIDON_SANS = "posidonsans"
    const val MONOSPACE = "monospace"
    const val UBUNTU = "ubuntu"
    const val LEXEND_DECA = "lexendDeca"
    const val INTER = "inter"
    const val OPEN_DYSLEXIC = "openDyslexic"

    fun getFontName(context: Context): CharSequence? = when (Settings["font", LEXEND_DECA]) {
        SANS_SERIF -> context.getString(R.string.sans_serif)
        POSIDON_SANS -> context.getString(R.string.posidon_sans)
        MONOSPACE -> context.getString(R.string.monospace)
        UBUNTU -> context.getString(R.string.ubuntu)
        LEXEND_DECA -> context.getString(R.string.lexend_deca)
        INTER -> context.getString(R.string.inter)
        OPEN_DYSLEXIC -> context.getString(R.string.open_dyslexic)
        else -> null
    }
}

inline fun Activity.applyFontSetting() {
    when (Settings["font", Fonts.LEXEND_DECA]) {
        Fonts.SANS_SERIF -> theme.applyStyle(R.style.font_sans_serif, true)
        Fonts.POSIDON_SANS -> theme.applyStyle(R.style.font_posidon_sans, true)
        Fonts.MONOSPACE -> theme.applyStyle(R.style.font_monospace, true)
        Fonts.UBUNTU -> theme.applyStyle(R.style.font_ubuntu, true)
        Fonts.LEXEND_DECA -> theme.applyStyle(R.style.font_lexend_deca, true)
        Fonts.INTER -> theme.applyStyle(R.style.font_inter, true)
        Fonts.OPEN_DYSLEXIC -> theme.applyStyle(R.style.font_open_dyslexic, true)
    }
}

inline val Context.mainFont: Typeface
    get() = if (Settings["font", Fonts.LEXEND_DECA] == Fonts.SANS_SERIF) Typeface.SANS_SERIF
    else {
        when (Settings["font", Fonts.LEXEND_DECA]) {
            Fonts.POSIDON_SANS -> resources.getFont(R.font.posidon_sans)
            Fonts.MONOSPACE -> resources.getFont(R.font.ubuntu_mono)
            Fonts.UBUNTU -> resources.getFont(R.font.ubuntu_medium)
            Fonts.OPEN_DYSLEXIC -> resources.getFont(R.font.open_dyslexic3)
            Fonts.INTER -> resources.getFont(R.font.inter)
            else -> resources.getFont(R.font.lexend_deca)
        }
    }