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
package org.olat.course.nodes;

import org.olat.core.util.nodes.INode;
import org.olat.course.tree.CourseEditorTreeNode;

/**
 * 
 * Initial date: 22 Jul 2021<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class CourseNodeHelper {
	
	/**
	 * Gets the CourseNode from an iNode. If the iNode is a CourseNode, the node is
	 * casted to the CourseNode. If the node is a CourseEditorTreeNode, the node
	 * is casted to the CourseEditorTreeNode an the CourseNode of the
	 * CourseEditorTreeNode is returned.
	 *
	 * @param node a CourseNode or CourseEditorTreeNode
	 * @return
	 */
	public static CourseNode getCourseNode(INode node) {
		if (node instanceof CourseNode) {
			return (CourseNode)node;
		} else if (node instanceof CourseEditorTreeNode) {
			return ((CourseEditorTreeNode)node).getCourseNode();
		}
		return null;
	}

}
