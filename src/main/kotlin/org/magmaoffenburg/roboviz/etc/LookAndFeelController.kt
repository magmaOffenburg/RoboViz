package org.magmaoffenburg.roboviz.etc

import com.github.weisj.darklaf.DarkLaf
import com.github.weisj.darklaf.LafManager
import com.github.weisj.darklaf.theme.DarculaTheme
import com.github.weisj.darklaf.theme.IntelliJTheme
import com.github.weisj.darklaf.theme.SolarizedDarkTheme
import com.github.weisj.darklaf.theme.SolarizedLightTheme
import javax.swing.UIManager
import javax.swing.plaf.nimbus.NimbusLookAndFeel

object LookAndFeelController {

    fun setLookAndFeel(lookAndFeel: String) {
        LafManager.enableLogging(false)
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
            System.err.println("Error while setting Look and Feel")
        }


    }
}