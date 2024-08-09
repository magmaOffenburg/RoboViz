package org.magmaoffenburg.roboviz.util

import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import org.apache.commons.compress.compressors.CompressorStreamFactory
import java.io.BufferedInputStream
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.InputStream
import java.io.InputStreamReader
import java.util.zip.ZipFile
import kotlin.math.max

fun createBufferedReader(file: File): BufferedReader {
    // Extract suffixes from filename to determine extraction method
    val suffixes = file.name.lowercase().split('.').let { it.subList(max(0, it.size - 2), it.size) }.map {
        // Fix a few common shortened suffixes
        when (it) {
            "tbz2" -> listOf("tar", "bz2")
            "tgz" -> listOf("tar", "gz")
            else -> listOf(it)
        }
    }.flatten()

    var inputStream: InputStream = BufferedInputStream(FileInputStream(file))
    for (suffix in suffixes.asReversed()) {
        when (suffix) {
            "bz2" -> inputStream =
                CompressorStreamFactory().createCompressorInputStream(CompressorStreamFactory.BZIP2, inputStream)

            "gz" -> inputStream =
                CompressorStreamFactory().createCompressorInputStream(CompressorStreamFactory.GZIP, inputStream)

            "tar" -> {
                // Only works for the current layout of tar files
                val tarStream = TarArchiveInputStream(inputStream)
                var entry = tarStream.nextEntry

                // Choose first file when not able to navigate deeper in the directory structure
                while (entry != null && entry.isDirectory) {
                    val entries = entry.directoryEntries
                    entry = if (entries.isNotEmpty()) {
                        entries[0]
                    } else {
                        // empty directory
                        tarStream.nextEntry
                    }
                }

                // Search for proper file
                while (entry != null && !entry.name.endsWith("sparkmonitor.log")) {
                    entry = tarStream.nextEntry
                }

                if (entry == null) {
                    throw FileNotFoundException("tar file does not contain logfile")
                }

                // We have reached the proper position
                inputStream = tarStream
                break
            }

            "zip" -> {
                val zipFile = ZipFile(file)
                if (zipFile.size() != 1) {
                    zipFile.close()
                    throw UnsupportedOperationException("Only support single entry zip files")
                }
                val zipEntry = zipFile.entries().nextElement()
                inputStream = zipFile.getInputStream(zipEntry)
                break
            }

            else -> break
        }
    }
    return BufferedReader(InputStreamReader(inputStream))
}
