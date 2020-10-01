package org.magmaoffenburg.roboviz.configuration

import java.io.File

class ConfigParser {

    val argsList = arrayListOf<Pair<String, String>>()
    val fileMap = hashMapOf<String, String>()

    /**
     * parse the args into a ArrayList, delimiter is "="
     */
    fun parseArgs(args: Array<String>): List<Pair<String, String>> {
        argsList.clear()

        args.forEach { arg ->
            if (arg.startsWith("--")) {
                argsList.add(Pair(arg.substring(2).substringBefore("="), arg.substringAfter("=")))
            }
        }

        return argsList
    }

    /**
     * parse the config file into a HashMap, delimiter is ":"
     */
    fun parseFile(path: String): HashMap<String, String> {
        fileMap.clear()

        File(path).forEachLine { line ->
            if (line.trim().isNotEmpty() && !line.startsWith("#")) {
                val pair = when {
                    line.startsWith("Team Color") -> {
                        val superKey = line.substringBefore(":").trim()
                        val key = line.substringBeforeLast(":").replaceBefore(":", "$superKey ")
                        val value = line.substringAfterLast(":")

                        Pair(key.trim(), value.trim())
                    }
                    line.substringBefore(":").trim() == "Server" -> {
                        val key = "${line.substringBefore(":").trim()} : ${line.substringAfter(":").trim()}"
                        val value = line.substringAfter(":").trim()
                        Pair(key, value)
                    }
                    else -> {
                        Pair(line.substringBefore(":").trim(), line.substringAfter(":").trim())
                    }
                }

                fileMap[pair.first] = pair.second
            }
        }

        return fileMap
    }

    /**
     * write the stored variables into the config file
     */
    fun writeFile(path: String) {
        val configFile = File(path)

        // read raw file
        val rawFileList = configFile.readLines().toMutableList()

        fileMap.forEach { pair ->
            when {
                pair.key.startsWith("Team Color") -> {
                    val superKey = pair.key.replace("Team Color", "Team Color".padEnd(20))
                    val index = rawFileList.indexOfFirst { line ->
                        line.startsWith(superKey)
                    }
                    rawFileList[index] = "$superKey:${pair.value}"
                }
                pair.key.startsWith("Server :") -> {
                    // only new servers will be added, existing servers can't be edited since there is no unique ID
                    val newLine = pair.key.replace("Server", "Server".padEnd(20))
                    val index = rawFileList.indexOfFirst { line ->
                        line.startsWith(newLine)
                    }

                    if (index >= 0) {
                        rawFileList[index] = newLine
                    } else {
                        // add a new server
                        val addIndex = rawFileList.indexOfLast { line ->
                            line.startsWith("${"Server".padEnd(20)} :")
                        }
                        rawFileList.add(addIndex + 1, newLine)
                    }
                }
                else -> {
                    val index = rawFileList.indexOfFirst { line ->
                        line.startsWith("${pair.key.padEnd(20)} :")
                    }
                    rawFileList[index] = "${pair.key.padEnd(20)} : ${pair.value}"
                }
            }
        }

        val sb = StringBuilder()
        rawFileList.forEach { line ->
            sb.append(line)
            sb.append("\n")
        }
        configFile.writeText(sb.toString())
    }

    /**
     * get the value of a key
     */
    fun getValue(key: String): String {
        return if (fileMap.containsKey(key)) {
            fileMap[key]!!
        } else {
            System.err.println("The key \"$key\" does not exist!")
            ""
        }
    }

    /**
     * set the value of a key
     */
    fun setValue(key: String, value: String) {
        fileMap[key] = value
    }

    /**
     * get all key value pairs, where the key starts with the superKey
     */
    fun getValueSuperKey(superKey: String): List<Map.Entry<String, String>> {
        return fileMap.filter {
            it.key.startsWith(superKey)
        }.entries.toList()
    }

}