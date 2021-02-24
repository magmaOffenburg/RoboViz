package org.magmaoffenburg.roboviz.gui.menus

import org.magmaoffenburg.roboviz.configuration.Config.Networking
import org.magmaoffenburg.roboviz.etc.ConfigChangeListener
import org.magmaoffenburg.roboviz.rendering.Renderer
import java.awt.Component
import java.awt.event.KeyEvent
import java.net.InetAddress
import java.net.UnknownHostException
import javax.swing.*

/**
 * the connection menu depends on swing more than other menus,
 * therefore it does not have a separate actions class
 */
class ConnectionMenu(private val parent: Component) : MenuBase(), ConfigChangeListener {

    private val group = ButtonGroup()

    init {
        initializeMenu()
    }

    private fun initializeMenu() {
        text = "Connection"

        addServerItems()
        addSeparator()
        addItem("Connect to...", KeyEvent.VK_C, KeyEvent.CTRL_DOWN_MASK) { connectTo() }
    }

    private fun addServerItems() {
        Networking.servers.forEach { pair ->
            val item = JRadioButtonMenuItem("${pair.first}:${pair.second}")
            item.isSelected = pair.first == Networking.currentHost && pair.second == Networking.currentPort
            item.addActionListener {
                changeServer(pair.first, pair.second)
            }
            group.add(item)
            add(item)
        }
    }

    private fun connectTo() {
        val hostsComboBox = JComboBox<String>()
        hostsComboBox.isEditable = true
        Networking.servers.forEachIndexed { index, pair ->
            hostsComboBox.addItem("${pair.first}:${pair.second}")
            // select the current host
            if (pair.first == Networking.currentHost && pair.second == Networking.currentPort) {
                hostsComboBox.selectedIndex = index
            }
        }
        SwingUtilities.invokeLater(hostsComboBox::requestFocusInWindow)

        val saveCheckBox = JCheckBox("Add to config file")

        val result = JOptionPane.showConfirmDialog(
                parent,
                arrayOf<Any>(hostsComboBox, saveCheckBox),
                "Connect to...",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        )

        if (result == JOptionPane.OK_OPTION) {
            hostsComboBox.selectedItem?.toString()?.let {
                connectToHost(it, saveCheckBox.isSelected)
            }
        }
    }

    private fun connectToHost(raw: String, save: Boolean) {
        var host = Networking.defaultServerHost
        var port = Networking.defaultServerPort

        if (raw.contains(":")) {
            host = raw.substringBefore(":")
            try {
                port = raw.substringAfter(":").toInt()
            } catch (ex: NumberFormatException) {
                showErrorDialog("Invalid port", "The entered port ${raw.substringAfter(":")} is invalid")
                return
            }
        }

        // make sure the host is valid
        try {
            InetAddress.getByName(host)
        } catch (ex: UnknownHostException) {
            showErrorDialog("Invalid host", "The entered host $host is invalid")
            return
        }

        if (save) {
            Networking.servers.add(Pair(host, port)) // currently save is called on exit
        }

        // add server to menu and select it
        val server = JRadioButtonMenuItem("$host:$port")
        group.add(server)
        add(server, itemCount - 2)

        // change server
        changeServer(host, port)
    }

    private fun showErrorDialog(title: String, message: String) {
        JOptionPane.showMessageDialog(parent, message, title, JOptionPane.ERROR_MESSAGE)
    }

    private fun changeServer(host: String, port: Int) {
        // update selection
        group.elements.toList().forEach {
            it.isSelected = it.text == "$host:$port"
        }

        Renderer.drawings.clearAllShapeSets()
        Renderer.netManager.server.changeConnection(host, port)

        Networking.currentHost = host
        Networking.currentPort = port
    }

    override fun onConfigChanged() {
        removeAll()
        initializeMenu()
    }
}