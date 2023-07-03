/**
 * <a href="http://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.modules.todo;

import java.util.List;

/**
 * 
 * Initial date: 24 Mar 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public enum ToDoStatus {
	
	open,
	inProgress,
	done,
	deleted;
	
	public static final List<ToDoStatus> OPEN_TO_DONE = List.of(ToDoStatus.open, ToDoStatus.inProgress, ToDoStatus.done);
	public static final List<String> OPEN_TO_DONE_NAMES = List.of(ToDoStatus.open.name(), ToDoStatus.inProgress.name(), ToDoStatus.done.name());
	public static final List<ToDoStatus> STATUS_OVERDUE = List.of(ToDoStatus.open, ToDoStatus.inProgress, ToDoStatus.deleted);

}
