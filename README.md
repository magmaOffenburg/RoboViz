![Logo](src/main/resources/images/icon.png) RoboViz
==================

![Build](https://github.com/magmaOffenburg/RoboViz/workflows/Build/badge.svg)

RoboViz is a monitor and visualization tool for the [RoboCup 3D Soccer Simulation League](https://ssim.robocup.org/3d-simulation/). This is a fork of the original version by Justin Stoecker [hosted on SourceForge](https://sourceforge.net/projects/rcroboviz/). Compared to the original version, major improvements have been made as can be seen in detail in the [changelog](CHANGELOG.md).

Java 1.8 is required to build and run RoboViz. Pre-built binaries for Windows, Linux and Mac are available [here](https://github.com/magmaOffenburg/RoboViz/releases). You can also build it from source using [`scripts/build.sh`](scripts/build.sh) or [`scripts/build.bat`](scripts/build.bat).

![](images/video.gif)

Except for the available shortcuts, the information on the [original website](https://sites.google.com/site/umroboviz) is still largely accurate. A complete list of shortcuts is available via a help window opened with the `F1` hotkey. Alternatively, it's also available as a `.html` file in `resources/help/controls.html`.

## Command line arguments

| Argument           | Description                                                               |
|--------------------|---------------------------------------------------------------------------|
| `--logMode`        | Start RoboViz in log instead of live mode.                                |
| `--logFile=`       | Opens the log file at the specified path right away. Implies `--logMode`. |
| `--serverHost=`    | Overrides the server host specified in `config.txt`.                      |
| `--serverPort=`    | Overrides the server port specified in `config.txt`.                      |
| `--drawingFilter=` | The initial filter used in the drawings panel - default is `.*`.          |

## Contributing

Contributions of any form are welcome. That includes:
- Bug reports and feature suggestions by [opening an issue](https://github.com/magmaOffenburg/RoboViz/issues/new).
- Code contributions by creating a [pull request](https://github.com/magmaOffenburg/RoboViz/pulls?q=is%3Aopen+is%3Apr).

Some basic contribution guidelines can be found [here](CONTRIBUTING.md).
