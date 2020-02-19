/*
 *  Copyright 2011 Justin Stoecker
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

package jsgl.io;

import java.io.File;

/**
 * IO utility routines
 *
 * @author Justin
 */
public class IOUtil
{
	/**
	 * Returns a file in the same directory as the parameter file with a
	 * different extension
	 *
	 * @param file
	 *           - original file path (ex. C:\Users\Justin\hello.txt)
	 * @param ext
	 *           - extension for desired file (ex. .hdr will return
	 *           C:\Users\Justin\hello.hdr)
	 */
	public static File getFileByExt(File file, String ext)
	{
		String path = file.getAbsolutePath();
		String hdrPath = path.substring(0, path.lastIndexOf(".")) + ext;
		return new File(hdrPath);
	}
}
