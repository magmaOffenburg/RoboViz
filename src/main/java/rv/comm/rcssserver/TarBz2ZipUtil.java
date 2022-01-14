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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author kdorer
 */
public class TarBz2ZipUtil
{
	private static final Logger LOGGER = LogManager.getLogger();

	/**
	 * Creates the reader used for sequential reading
	 *
	 * @return the reader used for sequential reading
	 * @throws FileNotFoundException
	 *             if the logsrc is not found
	 */
	public static BufferedReader createBufferedReader(File file) throws FileNotFoundException
	{
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

	public static Reader getZipStream(File file)
	{
		try (ZipFile zipFile = new ZipFile(file)) {
			if (zipFile.size() != 1) {
				LOGGER.error("Only support single entry zip files");
				zipFile.close();
				return null;
			} else {
				ZipEntry zipEntry = zipFile.entries().nextElement();
				return new InputStreamReader(zipFile.getInputStream(zipEntry));
			}

		} catch (IOException e) {
			// not a zip file
			LOGGER.error("File has zip ending, but seems to be not zip");
			return null;
		}
	}

	public static Reader getTarBZ2InputStream(File file)
	{
		try {
			// only works for the current layout of tar.bz2 files
			InputStream zStream = new BufferedInputStream(new FileInputStream(file));
			CompressorInputStream bz2InputStream =
					new CompressorStreamFactory().createCompressorInputStream(CompressorStreamFactory.BZIP2, zStream);
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
				LOGGER.error("tar file does not contain logfile");
				return null;
			}

			// search for proper file
			while (entry != null && !entry.getName().endsWith("sparkmonitor.log")) {
				entry = tarStream.getNextTarEntry();
			}

			if (entry == null) {
				LOGGER.error("tar file does not contain logfile");
				return null;
			}

			// we have reached the proper position
			return new InputStreamReader(tarStream);

		} catch (IOException e) {
			// not a bz2 file
			LOGGER.error("File has bz2 ending, but seems to be not bz2", e);
		} catch (CompressorException e) {
			LOGGER.error("Error decompressing data", e);
		}
		return null;
	}

	public static Reader getCompressedInputStream(File file, String which)
	{
		try {
			InputStream zStream = new BufferedInputStream(new FileInputStream(file));
			CompressorInputStream bz2InputStream =
					new CompressorStreamFactory().createCompressorInputStream(which, zStream);
			return new InputStreamReader(bz2InputStream);

		} catch (IOException | CompressorException e) {
			LOGGER.error("Error getting compressed input stream", e);
		}
		return null;
	}

	/**
	 * Creates the writer as zip, bz2 or unpacked stream
	 *
	 * @return the writer used for sequential reading
	 */
	public static PrintWriter createPrintWriter(File file) throws IOException
	{
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

	public static Writer getCompressingWriter(File file, String which)
	{
		try {
			// only works for the current layout of tar.bz2 files
			OutputStream zStream = new BufferedOutputStream(new FileOutputStream(file));

			CompressorOutputStream bz2Stream =
					new CompressorStreamFactory().createCompressorOutputStream(which, zStream);
			return new OutputStreamWriter(bz2Stream);

		} catch (IOException | CompressorException e) {
			LOGGER.error("Error getting compressing writer", e);
		}
		return null;
	}

	public static boolean isTarBZ2Ending(File file)
	{
		return endsWith(file, "tar.bz2");
	}

	public static boolean isBZ2Ending(File file)
	{
		return endsWith(file, ".bz2");
	}

	public static boolean isGZipEnding(File file)
	{
		return endsWith(file, ".gz");
	}

	public static boolean isZIPEnding(File file)
	{
		return endsWith(file, "zip");
	}

	private static boolean endsWith(File file, String ending)
	{
		return file.getName().toLowerCase().endsWith(ending);
	}
}
