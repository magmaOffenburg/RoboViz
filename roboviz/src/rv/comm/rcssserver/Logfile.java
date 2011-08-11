/*
 *  Copyright 2011 RoboViz
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package rv.comm.rcssserver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.apache.tools.bzip2.CBZip2InputStream;
import com.ice.tar.TarEntry;
import com.ice.tar.TarInputStream;

/**
 * Abstraction for a log that can be viewed frame by frame. Supports unpacked, single file zipped
 * and tar.bz2 files.
 * 
 * @author justin
 */
public class Logfile implements ILogfileReader {

    /** used for sequentially playing frames */
    private BufferedReader br;

    /** the file to read from */
    private File           logsrc;

    /** index of the frame that is currently buffered */
    private int            curFramePtr;

    /** the number of frames in the logfile, initially estimated */
    private int            numFrames;

    /** stores the server message at the current frame position */
    private String         curFrameMsg;

    /**
     * Default constructor
     * 
     * @param file
     *            the logfile to open
     * @throws Exception
     *             if the logfile can not be opened
     */
    public Logfile(File file) throws Exception {
        this.logsrc = file;
        numFrames = 1700;
        open();
    }

    /**
     * Opens the file for buffered reading
     * 
     * @throws FileNotFoundException
     */
    private void open() throws IOException {
        br = createBufferedReader();
        if (br != null) {
            curFrameMsg = br.readLine();
        }
        curFramePtr = 0;
    }

    @Override
    public boolean isValid() {
        return br != null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see rv.comm.rcssserver.ILogfileReader#isAtEndOfLog()
     */
    @Override
    public boolean isAtEndOfLog() {
        return curFrameMsg == null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see rv.comm.rcssserver.ILogfileReader#getNumFrames()
     */
    @Override
    public int getNumFrames() {
        return numFrames;
    }

    /*
     * (non-Javadoc)
     * 
     * @see rv.comm.rcssserver.ILogfileReader#getCurrentFrame()
     */
    @Override
    public int getCurrentFrame() {
        return curFramePtr;
    }

    /*
     * (non-Javadoc)
     * 
     * @see rv.comm.rcssserver.ILogfileReader#getCurrrentFrameMessage()
     */
    @Override
    public String getCurrrentFrameMessage() {
        return curFrameMsg;
    }

    /*
     * (non-Javadoc)
     * 
     * @see rv.comm.rcssserver.ILogfileReader#setCurrentFrame(int)
     */
    private String setCurrentFrame(int frame) throws IOException {

        if (frame < curFramePtr) {
            // we have a sequential reader, for stepping backwards we have to start from beginning
            close();
            open();
        }

        String line = curFrameMsg;
        while (curFramePtr < frame && line != null) {
            line = stepForward();
        }
        return line;
    }

    /*
     * (non-Javadoc)
     * 
     * @see rv.comm.rcssserver.ILogfileReader#rewind()
     */
    @Override
    public void rewind() throws IOException {
        close();
        open();
    }

    /*
     * (non-Javadoc)
     * 
     * @see rv.comm.rcssserver.ILogfileReader#close()
     */
    @Override
    public void close() {
        try {
            br.close();
        } catch (Exception ex) {
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see rv.comm.rcssserver.ILogfileReader#stepForward()
     */
    @Override
    public String stepForward() throws IOException {
        if (isAtEndOfLog())
            return null;

        curFrameMsg = br.readLine();
        curFramePtr++;
        if (curFramePtr >= numFrames) {
            // the number of frames was estimated too low
            numFrames++;
        } else if (curFrameMsg == null) {
            // the number of frames was estimated too high
            numFrames = curFramePtr + 1;
        }
        return curFrameMsg;
    }

    /*
     * (non-Javadoc)
     * 
     * @see rv.comm.rcssserver.ILogfileReader#stepBackward()
     */
    @Override
    public void stepBackward() throws IOException {
        if (curFramePtr > 0) {
            setCurrentFrame(curFramePtr - 1);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see rv.comm.rcssserver.ILogfileReader#stepAnywhere(int)
     */
    @Override
    public void stepAnywhere(int frame) throws IOException {
        if (frame < 0) {
            frame = 0;
        }
        setCurrentFrame(frame);
    }

    /**
     * Creates the reader used for sequential reading
     * 
     * @return the reader used for sequential reading
     * @throws FileNotFoundException
     *             if the logsrc is not found
     */
    private BufferedReader createBufferedReader() throws FileNotFoundException {

        if (isBZ2Ending()) {
            return getBZ2Stream();

        } else if (isZIPEnding()) {
            return getZipSteam();
        }
        return new BufferedReader(new FileReader(logsrc));
    }

    private BufferedReader getZipSteam() {
        try {
            ZipFile zipFile = new ZipFile(logsrc);
            if (zipFile.size() != 1) {
                System.out.println("Only support single entry zip files");
                return null;
            } else {
                ZipEntry zipEntry = zipFile.entries().nextElement();
                return new BufferedReader(new InputStreamReader(zipFile.getInputStream(zipEntry)));
            }

        } catch (IOException e) {
            // not a zip file
            System.out.println("File has zip ending, but seems to be not zip");
            return null;
        }
    }

    private BufferedReader getBZ2Stream() {
        try {
            // only works for the current layout of tar.bz2 files
            FileInputStream zStream = new FileInputStream(logsrc);
            // for whatever reasons the CBZip2InputStream assumes that 2 bytes have been consumed on
            // the stream
            byte[] header = new byte[2];
            zStream.read(header);
            if (header[0] != 'B' || header[1] != 'Z') {
                System.out.println("Not a bz2 file, but bz2 ending");
                return null;
            }

            CBZip2InputStream bz2InputStream = new CBZip2InputStream(zStream);
            TarInputStream tarStream = new TarInputStream(bz2InputStream);
            TarEntry entry = tarStream.getNextEntry();

            // step into deepest directory
            while (entry != null && entry.isDirectory()) {
                TarEntry[] entries = entry.getDirectoryEntries();
                if (entries.length > 0) {
                    entry = entries[0];
                } else {
                    // empty directory
                    entry = tarStream.getNextEntry();
                }
            }
            if (entry == null) {
                System.out.println("tar file does not contain logfile");
                return null;
            }

            // search for proper file
            while (entry != null && !entry.getName().endsWith("sparkmonitor.log")) {
                entry = tarStream.getNextEntry();
            }

            if (entry == null) {
                System.out.println("tar file does not contain logfile");
                return null;
            }

            // we have reached the proper position
            return new BufferedReader(new InputStreamReader(tarStream));

        } catch (IOException e) {
            // not a bz2 file
            System.out.println("File has bz2 ending, but seems to be not bz2");
            return null;
        }
    }

    private boolean isBZ2Ending() {
        if (logsrc.getName().endsWith("tar.bz2")) {
            return true;
        }
        return false;
    }

    private boolean isZIPEnding() {
        if (logsrc.getName().endsWith("zip")) {
            return true;
        }
        if (logsrc.getName().endsWith("ZIP")) {
            return true;
        }
        return false;
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        close();
    }
}
