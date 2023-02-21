package org.magmaoffenburg.roboviz.configuration

import org.apache.logging.log4j.kotlin.logger
import java.io.File

class ConfigParser {
    private val logger = logger()

    val argsList = arrayListOf<Pair<String, String>>()
    private val fileMap = hashMapOf<String, String>()
    private val filePairMap = hashMapOf<String, MutableList<Pair<String, String>>>()

    private val pairKeys = listOf("Team Color", "Server")

    /**
     * parse the args into a ArrayList, delimiter is "="
     */
    fun parseArgs(args: Array<String>): List<Pair<String, String>> {
        argsList.clear()

        args.forEach { arg ->
            if (arg.startsWith("--") && arg.contains("=")) {
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
            if (line.trim().isEmpty() || line.startsWith("#")) {
                return@forEachLine
            }

            val key = line.substringBefore(":").trim()
            when {
                pairKeys.contains(key) -> {
                    val value1 = line.substringBeforeLast(":").substringAfter(":").trim()
                    val value2 = line.substringAfterLast(":").trim()

                    if (filePairMap[key] != null) {
                        filePairMap[key]?.add(Pair(value1, value2))
                    } else {
                        filePairMap[key] = mutableListOf(Pair(value1, value2))
                    }
                }
                else -> {
                    fileMap[key] = line.substringAfter(":").trim()
                }
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

        val matchKey = { line: String, key: String -> line.substringBefore(":").trim() == key }

        fileMap.forEach { pair ->
            val index = rawFileList.indexOfFirst { matchKey(it, pair.key) }
            val updatedLine = "${pair.key.padEnd(20)} : ${pair.value}"
            if (index == -1) {
                // Key not yet present in config file
                // Add it to the bottom of the file
                rawFileList.add(updatedLine)
            } else {
                rawFileList[index] = updatedLine
            }
        }
        filePairMap.forEach { pairList ->
            val firstIndex = rawFileList.indexOfFirst { matchKey(it, pairList.key) }
            rawFileList.removeIf { matchKey(it, pairList.key) }
            pairList.value.forEachIndexed { i, pair ->
                rawFileList.add(firstIndex + i, "${pairList.key.padEnd(20)} : ${pair.first}:${pair.second}")
            }
        }

        // rawFileList to String with StringBuilder
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
    fun getValue(key: String) = fileMap[key] ?: null.also {
        logger.warn("The key \"$key\" does not exist!")
    }


    /**
     * get the value pair list of a key
     */
    fun getValuePairList(key: String) = filePairMap[key] ?: emptyList<Pair<String, String>>().also {
        logger.warn("The key \"$key\" does not exist!")
    }

    /**
     * set the value of a key
     */
    fun setValue(key: String, value: String) {
        fileMap[key] = value
    }

    /**
     * set the value pair list of a key
     */
    fun setValuePairList(key: String, value: List<Pair<String, String>>) {
        filePairMap[key] = value.toMutableList()
    }
}
