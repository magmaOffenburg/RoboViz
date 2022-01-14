package org.magmaoffenburg.roboviz.etc

import com.github.weisj.darklaf.DarkLaf
import com.github.weisj.darklaf.LafManager
import com.github.weisj.darklaf.theme.DarculaTheme
import com.github.weisj.darklaf.theme.IntelliJTheme
import com.github.weisj.darklaf.theme.Theme
import com.github.weisj.darklaf.theme.SolarizedDarkTheme
import com.github.weisj.darklaf.theme.SolarizedLightTheme
import java.util.logging.Level
import javax.swing.UIManager
import javax.swing.plaf.nimbus.NimbusLookAndFeel
import org.apache.logging.log4j.kotlin.logger

object LookAndFeelController {
    private val logger = logger()

    fun setLookAndFeel(lookAndFeel: String) {
        LafManager.setLogLevel(Level.WARNING)
        try {

            when (lookAndFeel) {
                "system" -> {
                    LafManager.setTheme(LafManager.themeForPreferredStyle(LafManager.getPreferredThemeStyle()))
                    UIManager.setLookAndFeel(DarkLaf::class.java.canonicalName)
                }
                "darcula" -> {
                    LafManager.setTheme(DarculaTheme())
                    UIManager.setLookAndFeel(DarkLaf::class.java.canonicalName)
                }
                "intellij" -> {
                    LafManager.setTheme(IntelliJTheme())
                    UIManager.setLookAndFeel(DarkLaf::class.java.canonicalName)
                }
                "solarized_dark" -> {
                    LafManager.setTheme(SolarizedDarkTheme())
                    UIManager.setLookAndFeel(DarkLaf::class.java.canonicalName)
                }
                "solarized_light" -> {
                    LafManager.setTheme(SolarizedLightTheme())
                    UIManager.setLookAndFeel(DarkLaf::class.java.canonicalName)
                }
                else -> {
                    UIManager.setLookAndFeel(NimbusLookAndFeel::class.java.canonicalName)
                }
            }

        } catch (ex: Exception) {
            logger.error("Error while setting Look and Feel", ex)
        }
    }

    fun isDarkMode() = Theme.isDark(LafManager.getInstalledTheme())
}