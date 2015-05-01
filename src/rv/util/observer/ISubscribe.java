/* Copyright 2009,2010,2011 Hochschule Offenburg
 * Klaus Dorer, Mathias Ehret, Stefan Glaser, Thomas Huber, Fabian Korak,
 * Simon Raffeiner, Srinivasa Ragavan, Thomas Rinklin,
 * Joachim Schilling, Ingo Schindler, Rajit Shahi
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
package rv.util.observer;

/**
 * Basic subscribe interface for all applications which want to allow objects to
 * subscribe themselves to another object and be informed about changes
 * 
 * @author Klaus Dorer
 * @param <T> Data type transported in updates
 */
public interface ISubscribe<T>
{
	/**
	 * Adds an observer to the list of observers if not already in the list
	 * @param observer the observer that wants to be informed
	 */
	void attach(IObserver<T> observer);
}