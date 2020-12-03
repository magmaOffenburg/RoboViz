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
            System.err.println("Error reading parsed values. The configuration file might be corrupt or incompatible with this version of RoboViz, try resetting it.")
        }
    }

    object General {
        var recordLogs = false
        var logfileDirectory = ""
        var logReplayFile = ""
        var lookAndFeel = "javax.swing.plaf.nimbus.NimbusLookAndFeel"
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
    }

    /**
     * set object variables to values from parser
     */
    fun read() {
        // General
        General.recordLogs = parser.getValue("Record Logfiles").toBoolean()
        General.logfileDirectory = parser.getValue("Logfile Directory")
        General.lookAndFeel = parser.getValue("Look and Feel")

        // Graphics
        Graphics.useBloom = parser.getValue("Bloom").toBoolean()
        Graphics.usePhong = parser.getValue("Phong").toBoolean()
        Graphics.useShadows = parser.getValue("Shadows").toBoolean()
        Graphics.useSoftShadows = parser.getValue("Soft Shadows").toBoolean()
        Graphics.shadowResolution = parser.getValue("Shadow Resolution").toInt()
        Graphics.useStereo = parser.getValue("Stereo 3D").toBoolean()
        Graphics.useVsync = parser.getValue("V-Sync").toBoolean()
        Graphics.useFsaa = parser.getValue("FSAA").toBoolean()
        Graphics.fsaaSamples = parser.getValue("FSAA Samples").toInt()
        Graphics.targetFPS = parser.getValue("Target FPS").toInt()
        Graphics.firstPersonFOV = parser.getValue("First Person FOV").toInt()
        Graphics.thirdPersonFOV = parser.getValue("Third Person FOV").toInt()

        Graphics.frameWidth = parser.getValue("Frame Width").toInt()
        Graphics.frameHeight = parser.getValue("Frame Height").toInt()
        Graphics.frameX = parser.getValue("Frame X").toInt()
        Graphics.frameY = parser.getValue("Frame Y").toInt()
        Graphics.centerFrame = parser.getValue("Center Frame").toBoolean()
        Graphics.isMaximized = parser.getValue("Frame Maximized").toBoolean()
        Graphics.saveFrameState = parser.getValue("Save Frame State").toBoolean()

        // Networking
        Networking.autoConnect = parser.getValue("Auto-Connect").toBoolean()
        Networking.autoConnectDelay = parser.getValue("Auto-Connect Delay").toInt()
        Networking.listenPort = parser.getValue("Drawing Port").toInt()

        parser.getValueSuperKey("Server :").forEach {
            Networking.servers.add(Pair(it.value.substringBefore(":"), it.value.substringAfter(":").toInt()))
        }
        Networking.defaultServerHost = parser.getValue("Default Server").substringBefore(":")
        Networking.defaultServerPort = parser.getValue("Default Server").substringAfter(":").toInt()

        // OverlayVisibility
        OverlayVisibility.serverSpeed = parser.getValue("Server Speed").toBoolean()
        OverlayVisibility.foulOverlay = parser.getValue("Foul Overlay").toBoolean()
        OverlayVisibility.fieldOverlay = parser.getValue("Field Overlay").toBoolean()
        OverlayVisibility.numberOfPlayers = parser.getValue("Number of Players").toBoolean()
        OverlayVisibility.playerIDs = parser.getValue("Player IDs").toBoolean()

        // TeamColors
        parser.getValueSuperKey("Team Color").forEach {
            TeamColors.byTeamNames[it.key.substringAfter(":").trim()] = Color(Integer.decode(it.value))
        }
        TeamColors.byTeamNames.putIfAbsent("<Left>", TeamColors.defaultLeft)
        TeamColors.byTeamNames.putIfAbsent("<Right>", TeamColors.defaultRight)

        // Args
        Networking.currentHost = parser.argsList.firstOrNull {
            it.first == "serverHost"
        }?.second ?: Networking.defaultServerHost
        Networking.currentPort = parser.argsList.firstOrNull {
            it.first == "serverPort"
        }?.second?.toInt() ?: Networking.defaultServerPort
        General.logReplayFile = parser.argsList.firstOrNull {
            it.first == "logFile"
        }?.second.toString()
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

        Networking.servers.forEach {
            val key = "Server : ${it.first}:${it.second}"
            val value = "${it.first}:${it.second}"
            parser.setValue(key, value)
        }
        parser.getValueSuperKey("Server :").forEach { (key, value) ->
            val pair = Pair(value.substringBefore(":"), value.substringAfter(":").toInt())
            if (!Networking.servers.contains(pair)) {
                parser.removeValue(key) // remove deleted server
            }
        }

        parser.setValue("Drawing Port", Networking.listenPort.toString())
        parser.setValue("Default Server", "${Networking.defaultServerHost}:${Networking.defaultServerPort}")

        // OverlayVisibility
        parser.setValue("Server Speed", OverlayVisibility.serverSpeed.toString())
        parser.setValue("Foul Overlay", OverlayVisibility.foulOverlay.toString())
        parser.setValue("Field Overlay", OverlayVisibility.fieldOverlay.toString())
        parser.setValue("Number of Players", OverlayVisibility.numberOfPlayers.toString())
        parser.setValue("Player IDs", OverlayVisibility.playerIDs.toString())

        // team colors
        TeamColors.byTeamNames.forEach {
            parser.setValue(it.key, "0x${Integer.toHexString(it.value.rgb and 0xFFFFFF)}")
        }
        parser.getValueSuperKey("Team Color").forEach {
            if (!TeamColors.byTeamNames.containsKey(it.key)) {
                parser.removeValue(it.key) // remove deleted team color
            }
        }

        try {
            parser.writeFile(filePath)
        } catch (e: Exception) {
            logger.error { "Error while trying to save the config: ${e.printStackTrace()}" }
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

    fun removeConfigChangedListener(listener: ConfigChangeListener) {
        configChangeListeners.remove(listener)
    }

}
