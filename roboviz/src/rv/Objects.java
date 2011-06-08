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

package rv;

/**
 * A collection of utility methods applicable to all objects.
 *
 * @author Drew Noakes
 */
public class Objects {
    /**
     * Determines whether two object references are equal.  If both values are <tt>null</tt>, true is returned.
     */
    public static boolean equals(Object a, Object b) {
        // Note that this method will be included in version 1.7 of the Java class library
        return (a==null && b==null) || (a!=null && a.equals(b));
    }
}
