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
import javax.swing.filechooser.FileFilter;

public class FileFilterExt extends FileFilter
{
	public final String[] fileTypes;
	private String desc;

	public FileFilterExt(String[] fileTypes)
	{
		this.fileTypes = fileTypes;
		desc = "";
		for (int i = 0; i < fileTypes.length; i++)
			desc += i > 0 ? ", " + fileTypes[i] : fileTypes[i];
	}

	@Override
	public boolean accept(File f)
	{
		if (f.isDirectory())
			return true;

		String extension = getExtension(f);

		if (extension == null)
			return false;

		for (String fileType : fileTypes)
			if (extension.equals(fileType))
				return true;

		return false;
	}

	private static String getExtension(File f)
	{
		String s = f.getName();
		int i = s.lastIndexOf('.');

		if (i > 0 && i < s.length() - 1)
			return s.substring(i + 1).toLowerCase();

		return null;
	}

	@Override
	public String getDescription()
	{
		return desc;
	}
}
