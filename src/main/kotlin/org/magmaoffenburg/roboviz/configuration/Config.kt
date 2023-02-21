package org.magmaoffenburg.roboviz.configuration

import org.apache.logging.log4j.kotlin.logger
import org.magmaoffenburg.roboviz.etc.ConfigChangeListener
import java.awt.Color
import java.io.File

/**
 * To support multiple Team colors and servers without breaking existing
 * Config files, "Server :" and "Team Color" are SuperKeys. The ConfigParser can
 * get all lines starting with a SuperKey. The raw SuperKeys are trimmed to only
 * contain one space.
 */
class Config(args: Array<String>) {

    private val logger = logger()

    private val localPath = "config.txt"
    private val globalPath = System.getProperty("user.home") + "/.roboviz/config.txt"

    private val parser = ConfigParser()
    private val filePath = if (File(globalPath).exists()) globalPath else localPath

    private val configChangeListeners = arrayListOf<ConfigChangeListener>()

    init {
        parser.parseArgs(args)
        parser.parseFile(filePath)

        try {
            read()
        } catch (ex: Exception) {
            logger.error("Error reading parsed values. The configuration file might be corrupt or incompatible with this version of RoboViz, try resetting it.")
        }
    }

    object General {
        var recordLogs = false
        var logfileDirectory = ""
        var logReplayFile = ""
        var lookAndFeel = "javax.swing.plaf.nimbus.NimbusLookAndFeel"
        var drawingFilter = ".*"
    }

    object Graphics {
        var useBloom = false
        var usePhong = false
        var useShadows = false
        var useSoftShadows = false
        var shadowResolution = 1024
        var useStereo = false
        var useVsync = true
        var useFsaa = false
        var fsaaSamples = 4
        var targetFPS = 60
        var firstPersonFOV = 120
        var thirdPersonFOV = 80

        var frameWidth = 1024
        var frameHeight = 768
        var frameX = 0
        var frameY = 0

        var centerFrame = true
        var isMaximized = false
        var saveFrameState = true
    }

    object Networking {
        var autoConnect = true
        var autoConnectDelay = 1000
        var listenPort = 32769

        var servers = arrayListOf<Pair<String, Int>>()
        var defaultServerHost = "localhost"
        var defaultServerPort = 3200

        var currentHost = defaultServerHost
        var currentPort = defaultServerPort

        var monitorStep = 0.04
        var useBuffer = false
    }

    object OverlayVisibility {
        var serverSpeed = true
        var foulOverlay = true
        var fieldOverlay = false
        var numberOfPlayers = false
        var playerIDs = false
    }

    object TeamColors {
        val byTeamNames = hashMapOf<String, Color>()
        val defaultLeft = Color(0x2626ff)
        val defaultRight = Color(0xff2626)

        fun getLeftColor(teamNameLeft: String?, teamNameRight: String?) =
            byTeamNames[teamNameLeft] ?: if (teamNameLeft != null && teamNameRight != null) {
                getOrderedColor(teamNameLeft, teamNameRight, true)
            } else {
                byTeamNames.getOrDefault(defaultLeftTeamName, defaultLeft)
            }

        fun getRightColor(teamNameLeft: String?, teamNameRight: String?) =
            byTeamNames[teamNameRight] ?: if (teamNameLeft != null && teamNameRight != null) {
                getOrderedColor(teamNameLeft, teamNameRight, false)
            } else {
                byTeamNames.getOrDefault(defaultRightTeamName, defaultRight)
            }

        private fun getOrderedColor(leftTeam: String, rightTeam: String, left: Boolean): Color {
            val switchColors = leftTeam < rightTeam
            return if ((left && !switchColors) || (!left && switchColors)) {
                byTeamNames.getOrDefault(defaultLeftTeamName, defaultLeft)
            } else {
                byTeamNames.getOrDefault(defaultRightTeamName, defaultRight)
            }
        }
    }

    companion object {
        const val defaultLeftTeamName = "<Left>"
        const val defaultRightTeamName = "<Right>"
    }

    /**
     * set object variables to values from parser
     */
    fun read() {
        // General
        parser.getValue("Record Logfiles")?.let { General.recordLogs = it.toBoolean() }
        parser.getValue("Logfile Directory")?.let { General.logfileDirectory = it }
        parser.getValue("Look and Feel")?.let { General.lookAndFeel = it }

        // Graphics
        parser.getValue("Bloom")?.let { Graphics.useBloom = it.toBoolean() }
        parser.getValue("Phong")?.let { Graphics.usePhong = it.toBoolean() }
        parser.getValue("Shadows")?.let { Graphics.useShadows = it.toBoolean() }
        parser.getValue("Soft Shadows")?.let { Graphics.useSoftShadows = it.toBoolean() }
        parser.getValue("Shadow Resolution")?.let { Graphics.shadowResolution = it.toInt() }
        parser.getValue("Stereo 3D")?.let { Graphics.useStereo = it.toBoolean() }
        parser.getValue("V-Sync")?.let { Graphics.useVsync = it.toBoolean() }
        parser.getValue("FSAA")?.let { Graphics.useFsaa = it.toBoolean() }
        parser.getValue("FSAA Samples")?.let { Graphics.fsaaSamples = it.toInt() }
        parser.getValue("Target FPS")?.let { Graphics.targetFPS = it.toInt() }
        parser.getValue("First Person FOV")?.let { Graphics.firstPersonFOV = it.toInt() }
        parser.getValue("Third Person FOV")?.let { Graphics.thirdPersonFOV = it.toInt() }

        parser.getValue("Frame Width")?.let { Graphics.frameWidth = it.toInt() }
        parser.getValue("Frame Height")?.let { Graphics.frameHeight = it.toInt() }
        parser.getValue("Frame X")?.let { Graphics.frameX = it.toInt() }
        parser.getValue("Frame Y")?.let { Graphics.frameY = it.toInt() }
        parser.getValue("Center Frame")?.let { Graphics.centerFrame = it.toBoolean() }
        parser.getValue("Frame Maximized")?.let { Graphics.isMaximized = it.toBoolean() }
        parser.getValue("Save Frame State")?.let { Graphics.saveFrameState = it.toBoolean() }

        // Networking
        parser.getValue("Auto-Connect")?.let { Networking.autoConnect = it.toBoolean() }
        parser.getValue("Auto-Connect Delay")?.let { Networking.autoConnectDelay = it.toInt() }
        parser.getValue("Drawing Port")?.let { Networking.listenPort = it.toInt() }
        parser.getValue("Monitor Step")?.let { Networking.monitorStep = it.toDouble() }
        parser.getValue("Network Buffer")?.let { Networking.useBuffer = it.toBoolean() }

        parser.getValuePairList("Server").forEach {
            Networking.servers.add(Pair(it.first, it.second.toInt()))
        }
        parser.getValue("Default Server")?.let { Networking.defaultServerHost = it.substringBefore(":") }
        parser.getValue("Default Server")?.let { Networking.defaultServerPort = it.substringAfter(":").toInt() }

        // OverlayVisibility
        parser.getValue("Server Speed")?.let { OverlayVisibility.serverSpeed = it.toBoolean() }
        parser.getValue("Foul Overlay")?.let { OverlayVisibility.foulOverlay = it.toBoolean() }
        parser.getValue("Field Overlay")?.let { OverlayVisibility.fieldOverlay = it.toBoolean() }
        parser.getValue("Number of Players")?.let { OverlayVisibility.numberOfPlayers = it.toBoolean() }
        parser.getValue("Player IDs")?.let { OverlayVisibility.playerIDs = it.toBoolean() }

        // TeamColors
        parser.getValuePairList("Team Color").forEach {
            TeamColors.byTeamNames[it.first] = Color(Integer.decode(it.second))
        }
        TeamColors.byTeamNames.putIfAbsent(defaultLeftTeamName, TeamColors.defaultLeft)
        TeamColors.byTeamNames.putIfAbsent(defaultRightTeamName, TeamColors.defaultRight)

        // Args
        Networking.currentHost = parser.argsList.firstOrNull {
            it.first == "serverHost"
        }?.second ?: Networking.defaultServerHost
        Networking.currentPort = parser.argsList.firstOrNull {
            it.first == "serverPort"
        }?.second?.toInt() ?: Networking.defaultServerPort
        General.logReplayFile = parser.argsList.firstOrNull {
            it.first == "logFile"
        }?.second ?: ""
        General.drawingFilter = parser.argsList.firstOrNull {
            it.first == "drawingFilter"
        }?.second ?: ".*"
    }

    /**
     *  set parser list to object variable values
     */
    fun write() {
        // general
        parser.setValue("Record Logfiles", General.recordLogs.toString())
        parser.setValue("Logfile Directory", General.logfileDirectory)
        parser.setValue("Look and Feel", General.lookAndFeel)

        // Graphics
        parser.setValue("Bloom", Graphics.useBloom.toString())
        parser.setValue("Phong", Graphics.usePhong.toString())
        parser.setValue("Shadows", Graphics.useShadows.toString())
        parser.setValue("Soft Shadows", Graphics.useSoftShadows.toString())
        parser.setValue("Shadow Resolution", Graphics.shadowResolution.toString())
        parser.setValue("Stereo 3D", Graphics.useStereo.toString())
        parser.setValue("V-Sync", Graphics.useVsync.toString())
        parser.setValue("FSAA", Graphics.useFsaa.toString())
        parser.setValue("FSAA Samples", Graphics.fsaaSamples.toString())
        parser.setValue("Target FPS", Graphics.targetFPS.toString())
        parser.setValue("First Person FOV", Graphics.firstPersonFOV.toString())
        parser.setValue("Third Person FOV", Graphics.thirdPersonFOV.toString())

        parser.setValue("Frame Width", Graphics.frameWidth.toString())
        parser.setValue("Frame Height", Graphics.frameHeight.toString())
        parser.setValue("Frame X", Graphics.frameX.toString())
        parser.setValue("Frame Y", Graphics.frameY.toString())
        parser.setValue("Center Frame", Graphics.centerFrame.toString())
        parser.setValue("Frame Maximized", Graphics.isMaximized.toString())
        parser.setValue("Save Frame State", Graphics.saveFrameState.toString())

        // Networking
        parser.setValue("Auto-Connect", Networking.autoConnect.toString())
        parser.setValue("Auto-Connect Delay", Networking.autoConnectDelay.toString())
        parser.setValue("Drawing Port", Networking.listenPort.toString())

        parser.setValuePairList("Server", Networking.servers.map { Pair(it.first, it.second.toString()) })
        parser.setValue("Default Server", "${Networking.defaultServerHost}:${Networking.defaultServerPort}")

        parser.setValue("Monitor Step", Networking.monitorStep.toString())
        parser.setValue("Network Buffer", Networking.useBuffer.toString())

        // OverlayVisibility
        parser.setValue("Server Speed", OverlayVisibility.serverSpeed.toString())
        parser.setValue("Foul Overlay", OverlayVisibility.foulOverlay.toString())
        parser.setValue("Field Overlay", OverlayVisibility.fieldOverlay.toString())
        parser.setValue("Number of Players", OverlayVisibility.numberOfPlayers.toString())
        parser.setValue("Player IDs", OverlayVisibility.playerIDs.toString())

        // team colors
        parser.setValuePairList(
            "Team Color",
            TeamColors.byTeamNames.map { Pair(it.key, "0x${Integer.toHexString(it.value.rgb and 0xFFFFFF)}") })

        try {
            parser.writeFile(filePath)
        } catch (e: ArrayIndexOutOfBoundsException) {
            logger.error("Error while trying to save the config.", e)
        }

    }

    fun configChanged() {
        logger.info { "Config changed" }

        configChangeListeners.forEach {
            it.onConfigChanged()
        }
    }

    fun addConfigChangedListener(listener: ConfigChangeListener) {
        configChangeListeners.add(listener)
    }

    @Suppress("unused")
    fun removeConfigChangedListener(listener: ConfigChangeListener) {
        configChangeListeners.remove(listener)
    }

}
