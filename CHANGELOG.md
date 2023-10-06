2.0.0 (October 6, 2023)
------------------------------
* update minimum required Java version to 17
* require strings sent via the drawing protocol to be UTF-8 encoded
* update JOGL to 2.4.0 (adds support for macOS Ventura)
* move entire build logic to Gradle (use `./gradlew binDir` instead of
  the old build script)
* add a build task to generate a macOS application bundle
  (`./gradlew macOSApp`)
* support running without a present config file
* make the team color table in the configuration window scrollable
* fix a NullPointerException when stopping the log player
* fix an IndexOutOfBoundsException when the connection ends unexpectedly

1.8.5 (July 2, 2023)
------------------------------
* add timer for penalties in RCSSServer3D 0.7.6

1.8.4 (April 29, 2023)
------------------------------
* increase tracking speed of camera when tracking the ball

1.8.3 (March 18, 2023)
------------------------------
* add support for ball holding foul introduced with RCSSServer3D 0.7.6
* add optional buffering mechanism for bad network connections

1.8.2 (July 14, 2022)
------------------------------
* assign default team color based on team name

1.8.1 (April 7, 2022)
------------------------------
* fixed crashing RoboViz due to an invalid regex ([#52](https://github.com/magmaOffenburg/RoboViz/issues/52))
* migrated logging to Log4j ([#124](https://github.com/magmaOffenburg/RoboViz/pull/124))

1.8.0 (December 30, 2021)
------------------------------
* included the [darklaf](https://github.com/weisJ/darklaf) library for better look and feels ([#110](https://github.com/magmaOffenburg/RoboViz/pull/110))
* added "Look and Feel" to "General Settings" in `config.txt` - possible values are:
    * `system` (default)
    * `darcula`
    * `intellij`
    * `solarized_dark`
    * `solarized_light`
* reimplemented the RoboViz GUI in Kotlin to better separate GUI and Rendering ([#117](https://github.com/magmaOffenburg/RoboViz/pull/117))
    * settings window can be opened via menu (graphics settings can be changed without a restart)
    * default team colors can be overwritten
    * switch between live and log mode via menu ([#55](https://github.com/magmaOffenburg/RoboViz/issues/55))
    * fixed playback speed text field not selectable on Linux ([#31](https://github.com/magmaOffenburg/RoboViz/issues/31))
* configuration improvements ([#112](https://github.com/magmaOffenburg/RoboViz/pull/112))
    * Commented out lines are kept on save
    * New syntax for storing multiple servers and team colors
* made camera movement speed relative to the distance from the ground
* pass mode timer update ([#114](https://github.com/magmaOffenburg/RoboViz/pull/114))

1.7.0 (February 21, 2020)
------------------------------
* added "Connection -> Connect to..." ([#105](https://github.com/magmaOffenburg/RoboViz/issues/105))
* switched to Gradle for build / dependency management ([#106](https://github.com/magmaOffenburg/RoboViz/issues/106))
    * simplified build scripts (there's only `build.sh` for Linux / Mac and `build.bat` for Windows)
    * builds are now universal (same `bin` folder works on Windows, Linux and Mac)
    * updated jogl to 2.3.2 (fixes Mac compatibility - [#93](https://github.com/magmaOffenburg/RoboViz/issues/93))
* fixed "Auto Connect" causing unresponsiveness in the UI 
* allow more precise zooming with Shift + Mouse Wheel

1.6.1 (June 17, 2019)
------------------------------
* added support for the rectangular goals in rcssserver 0.7.2
* updated the pass mode duration for rcssserver 0.7.2

1.6.0 (May 1, 2019)
------------------------------
* updated the required Java version to 1.8
* added a fade-out effect to the ball circle in pass mode etc.
* added a workaround for goals sometimes being missed ([#104](https://github.com/magmaOffenburg/RoboViz/issues/104))
* added an overlay text and a gray-out effect when in `GameOver`
* changed the default config to enable all overlays as in competitions
* removed red cards from the foul overlay to avoid confusion
* removed the "Fouls:" headline from the foul overlay

1.5.0 (April 27, 2019)
------------------------------
* added differently colored goalie jerseys ([#102](https://github.com/magmaOffenburg/RoboViz/issues/102))
* added a player tracking camera
* added support for the new self collision foul
* added a circle around the ball for pass / free kick / corner kick / kick in
* added a timer for the scoring cooldown after pass mode 
* changed the time formatting to `mm:ss`
* more gracefully handle `NaN` values sent by the server ([#99](https://github.com/magmaOffenburg/RoboViz/issues/99))
* fixed hangs when switching between servers in Connection UI ([#100](https://github.com/magmaOffenburg/RoboViz/issues/100))
* fixed `config.txt` changes made while RoboViz is running being lost on save

1.4.0 (April 12, 2018)
------------------------------
* added a menu bar with Connection / Server / View / Camera menus
* allowed specifying multiple servers in `config.txt` (comma-separated)
* allowed switching between different servers without restarting RoboViz
* fixed handling of packets with multiple drawings in logs ([#98](https://github.com/magmaOffenburg/RoboViz/issues/98))
* changed the ball tracker camera's speed to scale with ball speed ([#95](https://github.com/magmaOffenburg/RoboViz/issues/95))
 
1.3.0 (July 29, 2016)
------------------------------
* server speed and foul overlay are now visible by default
* the ball is now selected by default when connecting
* completely reworked the ball tracking camera
* FSAA is now enabled by default
* increased the default window size to 1024x768
* increased the default size of the help panel to 600x800
* the default visibility of overlays is now configurable in `config.txt`
* the foul overlay is now slightly bigger
* changed the color format in `config.txt` to 0xRRGGBB
* the default colors of the left and right team are now configurable

1.2.0 (May 27, 2016)
------------------------------
* fixed the log player getting stuck with draw commands occasionally ([#78](https://github.com/magmaOffenburg/RoboViz/issues/78))
* fixed `--logFile` paths starting with `~` in `roboviz.sh` ([#80](https://github.com/magmaOffenburg/RoboViz/issues/80))
* fixed the server host displayed in the window title when overridden with `--serverHost` ([#83](https://github.com/magmaOffenburg/RoboViz/issues/83))
* changed the connection overlay text from `Trying to connect to <ip>...` to `Waiting for second half...` if time is at 300
* changed the connection overlay text to dynamically resize with the window
* changed the the "reset time" shortcut (`Shift+R`) to `Shift+T` ([#85](https://github.com/magmaOffenburg/RoboViz/issues/85))
* added an overlay to display fouls (can be toggled with `Q`) ([#82](https://github.com/magmaOffenburg/RoboViz/issues/82))
* added `Shift+L` and `Shift+R` shortcuts to switch to the `direct_free_kick_left` / `right` play modes added in [[r405]](https://sourceforge.net/p/simspark/svn/405/) ([#85](https://github.com/magmaOffenburg/RoboViz/issues/85))
* added support for using `~/.roboviz/config.txt` instead of the local `config.txt` if present
* added the RoboViz version to the window title

1.1.2 (March 20, 2016)
------------------------------
* fixed drawing of annotations in different robot vantages
* fixed incorrect shadow rendering near field corners
* adjusted .sh start scripts so they can run from any directory
* added current server IP / logfile path to window title

1.1.1 (June 17, 2015)
------------------------------

* fixed third person robot vantage not working with new robot models
* fixed the first frame of logfiles not being parsed
* draw commands are now recorded with "Record Logfiles" and replayable in log mode
* changed the drawings panel shortcut to `Y`
* improved the enabled status handling of some log player buttons
* fixed the log player not pausing when reaching the start of the file with negative playback speed
* fixed camera position shortcuts (`1`-`8`) not always working right away in log mode
* fixed draw commands including agent IDs to use to use the agent's uniform number instead of the index in the current team array
* fixed agent annotations being discarded after scene graph changes

1.1.0 (June 13, 2015)
------------------------------

*Note: Java 7 is required to compile RoboViz now, whereas the version on the SourceForge repository works with Java 6.*

* Unified Log Mode and Live Mode - Log Mode now has the same features and shortcuts as Live Mode, except for a few necessary restrictions (server commands and drawings)
* **Log Player:**
	* fixed another Logplayer window being created when dragging RoboViz to a different monitor on Linux
	* added a separate thread for log playback to prevent the UI from freezing when jumping a lot of frames (especially noticeable with the slider)
	* added a "Jump to previous / next" goal feature (a separate thread analyzes the logfile in the background to find goals, so the functionality is not available right away)
	* playback speed is now determined by a factor instead of FPS
	* negative playback speeds are now allowed
	* replace the decrease / increase playback speed buttons with a spinner
	* fixed playback being slightly too fast (150ms per frame instead of 200ms)
	* the "ms per frame"-value is now extracted from the logfile, making it so that the playback speed is independent of the `$monitorLoggerStep` value of the server config while recording it
	* removed the progress slider from the RoboViz main window
	* fixed score / team colors / names etc. not being reset properly when switching logfiles
* **Shortcuts:**
	* added a `F1` shortcut that displays a help page with a list of all shortcuts
	* changed the ball selection shortcut from `Ctrl+0` to just `0`
	* fixed `Numpad 0` not working with the ball selection shortcut
	* removed the `Q` shortcut to prevent accidentally closing RoboViz
	* added an `Escape` shortcut to close dialogs and to cancel the current selection
	* added `Ctrl+[F1-F11]` shortcuts to select players from the left team
	* added `Ctrl+Shift+[F1-F11]` shortcuts to select players from the right team
	* added a `Tab` shortcut to select the next player in the current team
	* added a `Shift+Tab` shortcut to select the previous player in the current team
	* added a `Ctrl+Alt+Click` shortcut to move the ball to a position with a velocity
	* added a `Shift+Alt+Click` shortcut to launch the ball to a position at a 45 degree angle
	* added an `E` shortcut to toggle third person perspective
	* added `W / Up / Mouse wheel up` shortcuts to decrease the FOV in Robot Vantage mode
	* added `S / Down / Mouse wheel down` shortcuts to increase the FOV in Robot Vantage mode
	* it's now possible to hold `Shift` more more precise camera movement
	* added a `Shift+R` shortcut to reset the server time (needs [[r386]](http://sourceforge.net/p/simspark/svn/386/))
	* all shortcuts that change playmodes now reset the server time if necessary (time >= 600)
	* added a `U` shortcut to request a full state update from the server (for when RoboViz gets out of sync, like missing agents)
	* changed the `Z` (increase playback speed) and `X` (decrease playback speed) shortcuts to `X` and `C` (more convenient on QWERTZ keyboards)
	* added a `G` shortcut to jump to the previous goal (log mode)
	* added a `H` shortcut to jump to the next goal (log mode)
	* changed the cycle annotations shortcut (`I`) to only toggle between "none" and "player numbers" if there aren't any custom agent annotations
	* added a `M` shortcut toggling the display of the estimated server speed (in percent, relative to real time) in live mode
* **Playmode Overlay:**
	* allow cycling through playmodes (pressing up while the first playmode is selected selects the last playmode and vice-versa)
	* allow filtering playmodes by a string
* **Field 2D Overlay:**
	* made the field transparent and thus less obstructive
	* slightly decreased the size of the ball
	* the overlay is now rendered under the connection overlay
* **Command-line arguments:**
	* instead of simply passing a log file path as the first argument to the start scripts, it now has to be in the from of `--logFile=path`
	* fixed file paths starting with `~` not working on Linux
	* output a more helpful error message for invalid logfile paths (includes the path now)
	* output a more helpful error message when the logfile path is a directory
	* added a `--logMode` argument that enables Log Mode even without specifying a `--logFile`
	* added a `--serverHost=` argument that allows overriding the server host specified in the config
	* added a `--serverPort=` argument that allows overriding the server port specified in the config
	* added a `--drawingFilter=` argument that allows specifying the filter for the drawings panel
* **Configuration Window:**
	* added a UI for settings that could only be changed in the `config.txt` file before:
		* Record Logfiles
		* VSync
		* Team Colors
	* added several new settings:
		* Frame State (X, Y, Center Position, Maximized)
		* Save Frame State - enabled by default, automatically saves and restores the main window's position, dimensions and state
		* First Person FOV
		* Third Person FOV
		* Logfile Directory (the directory the "Open Logfile" dialog is opened at)
	* now uses the same look and feel as the other windows
	* made "Start RoboViz" the default button (allows to use `Enter` as a shortcut)
	* made sure OS-specific line endings are used when saving the config (previously `LF` was used even on Windows)
* **Bugfixes:**
	* fixed taking screenshots and recording logfiles on Windows
	* fixed the `config.bat` not working
	* fixed the ball's velocity not being reset to `(0 0 0)` when it's beamed
	* fixed `ConcurrentModificationException`s
	* fixed the main window not using the RoboViz icon on Linux
	* fixed RoboViz thinking that modifier keys are still pressed if they are pressed while the application loses focus and are then released 
* **Other changes and improvements:**
	* screenshots are now saved in a `screenshots/` subdirectory
	* logfiles are now saved in a `logfiles/` subdirectory
	* more helpful "disconnected" overlay in live mode (displays server IP and port)
	* major improvements to the behavior of windows with multi-monitor setups (among other things, dialogs are now displayed relative the the main window as opposed to always being on the primiary monitor)
	* added a "select player" command to the drawing API: `| 3 | 0 | Agent/Team |`
	* the swap buffers command now prints an error when the second byte does not equal 0 instead of ignoring it, making the behavior consistent with the rest of the drawing API
	* added a black outline for the player numbers texts
	* moved `config.txt` out of `resources/` into the root directory for easier access
	* moved build scripts to the `scripts/` subdirectory
	* removed the separate start scripts for Log Mode
	* added a more helpful error message for the drawing API failing to parse float values
	* made sure the GameState Overlay is still rendered after losing the server connection to be able to see the current score
	* added the new rcssserver3d robot body type models (needs [[r395]](http://sourceforge.net/p/simspark/svn/395/))
