/* Copyright 2009 Hochschule Offenburg
 * Klaus Dorer, Mathias Ehret, Stefan Glaser, Thomas Huber,
 * Simon Raffeiner, Srinivasa Ragavan, Thomas Rinklin,
 * Joachim Schilling, Rajit Shahi
 *
 * This file is part of magmaOffenburg.
 *
 * magmaOffenburg is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * magmaOffenburg is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with magmaOffenburg. If not, see <http://www.gnu.org/licenses/>.
 */

package rv.comm.rcssserver;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorInputStream;
import org.apache.commons.compress.compressors.CompressorOutputStream;
import org.apache.commons.compress.compressors.CompressorStreamFactory;

/**
 * 
 * @author kdorer
 */
public class TarBz2ZipUtil {
    /**
     * Creates the reader used for sequential reading
     * 
     * @return the reader used for sequential reading
     * @throws FileNotFoundException
     *             if the logsrc is not found
     */
    public static BufferedReader createBufferedReader(File file) throws FileNotFoundException {
        Reader reader = null;
        if (isTarBZ2Ending(file)) {
            reader = getTarBZ2InputStream(file);

        } else if (isBZ2Ending(file)) {
            reader = getCompressedInputStream(file, CompressorStreamFactory.BZIP2);

        } else if (isGZipEnding(file)) {
            reader = getCompressedInputStream(file, CompressorStreamFactory.GZIP);

        } else if (isZIPEnding(file)) {
            reader = getZipStream(file);
        } else {
            reader = new FileReader(file);
        }
        return new BufferedReader(reader);
    }

    public static Reader getZipStream(File file) {
        try {
            ZipFile zipFile = new ZipFile(file);
            if (zipFile.size() != 1) {
                System.out.println("Only support single entry zip files");
                zipFile.close();
                return null;
            } else {
                ZipEntry zipEntry = zipFile.entries().nextElement();
                Reader reader = new InputStreamReader(zipFile.getInputStream(zipEntry));
                return reader;
            }

        } catch (IOException e) {
            // not a zip file
            System.out.println("File has zip ending, but seems to be not zip");
            return null;
        }
    }

    public static Reader getTarBZ2InputStream(File file) {
        try {
            // only works for the current layout of tar.bz2 files
            InputStream zStream = new BufferedInputStream(new FileInputStream(file));
            CompressorInputStream bz2InputStream = new CompressorStreamFactory()
                    .createCompressorInputStream(CompressorStreamFactory.BZIP2, zStream);
            TarArchiveInputStream tarStream = new TarArchiveInputStream(bz2InputStream);
            TarArchiveEntry entry = tarStream.getNextTarEntry();

            // step into deepest directory
            while (entry != null && entry.isDirectory()) {
                TarArchiveEntry[] entries = entry.getDirectoryEntries();
                if (entries.length > 0) {
                    entry = entries[0];
                } else {
                    // empty directory
                    entry = tarStream.getNextTarEntry();
                }
            }
            if (entry == null) {
                System.out.println("tar file does not contain logfile");
                return null;
            }

            // search for proper file
            while (entry != null && !entry.getName().endsWith("sparkmonitor.log")) {
                entry = tarStream.getNextTarEntry();
            }

            if (entry == null) {
                System.out.println("tar file does not contain logfile");
                return null;
            }

            // we have reached the proper position
            return new InputStreamReader(tarStream);

        } catch (IOException e) {
            // not a bz2 file
            System.out.println("File has bz2 ending, but seems to be not bz2");
            e.printStackTrace();
        } catch (CompressorException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Reader getCompressedInputStream(File file, String which) {
        try {
            InputStream zStream = new BufferedInputStream(new FileInputStream(file));
            CompressorInputStream bz2InputStream = new CompressorStreamFactory()
                    .createCompressorInputStream(which, zStream);
            return new InputStreamReader(bz2InputStream);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (CompressorException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Creates the writer as zip, bz2 or unpacked stream
     * 
     * @return the writer used for sequential reading
     * @throws IOException
     */
    public static PrintWriter createPrintWriter(File file) throws IOException {
        Writer writer = null;
        if (isTarBZ2Ending(file)) {
            // TODO: add support for tar writing
            writer = getCompressingWriter(file, CompressorStreamFactory.BZIP2);

        } else if (isBZ2Ending(file)) {
            writer = getCompressingWriter(file, CompressorStreamFactory.BZIP2);

        } else if (isGZipEnding(file)) {
            writer = getCompressingWriter(file, CompressorStreamFactory.GZIP);

        } else {
            writer = new FileWriter(file);
        }

        return new PrintWriter(new BufferedWriter(writer));
    }

    public static Writer getCompressingWriter(File file, String which) {
        try {
            // only works for the current layout of tar.bz2 files
            OutputStream zStream = new BufferedOutputStream(new FileOutputStream(file));

            CompressorOutputStream bz2Stream = new CompressorStreamFactory()
                    .createCompressorOutputStream(which, zStream);
            // TarArchiveOutputStream tarStream = new TarArchiveOutputStream(
            // bz2Stream);
            // TarArchiveEntry entry = new TarArchiveEntry("spark.log");
            // tarStream.putArchiveEntry(entry);
            return new OutputStreamWriter(bz2Stream);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (CompressorException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean isTarBZ2Ending(File file) {
        if (file.getName().toLowerCase().endsWith("tar.bz2")) {
            return true;
        }
        return false;
    }

    public static boolean isBZ2Ending(File file) {
        if (file.getName().toLowerCase().endsWith(".bz2")) {
            return true;
        }
        return false;
    }

    public static boolean isGZipEnding(File file) {
        if (file.getName().toLowerCase().endsWith(".gz")) {
            return true;
        }
        return false;
    }

    public static boolean isZIPEnding(File file) {
        if (file.getName().toLowerCase().endsWith("zip")) {
            return true;
        }
        return false;
    }
}
