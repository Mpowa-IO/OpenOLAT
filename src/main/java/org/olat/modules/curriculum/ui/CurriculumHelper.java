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
package org.olat.modules.curriculum.ui;

import org.olat.core.gui.translator.Translator;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumElement;

/**
 * 
 * Initial date: 3 févr. 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CurriculumHelper {
	
	public static final int AVATAR_MAX_LENGTH = 9;
	
	private CurriculumHelper() {
		//
	}
	
	public static String truncateAvatar(String ref) {
		if(ref != null && ref.length() > AVATAR_MAX_LENGTH) {
			ref = ref.substring(0, AVATAR_MAX_LENGTH);
		}
		return ref;
	}
	
	public static String getCurriculumBusinessPath(Long curriculumKey) {
		return "[CurriculumAdmin:0][Curriculum:" + curriculumKey + "]";
	}
	
	public static String getLabel(CurriculumElement element, Translator translator) {
		Curriculum curriculum = element.getCurriculum();
		CurriculumElement parentElement = element.getParent();
		
		String[] args = new String[] {
			element.getDisplayName(),										// 0
			element.getIdentifier(),										// 1
			parentElement == null ? null : parentElement.getDisplayName(),	// 2
			parentElement == null ? null : parentElement.getIdentifier(),	// 3
			curriculum.getDisplayName(),									// 4
			curriculum.getIdentifier()										// 5
		};

		String i18nKey = parentElement == null ? "select.value.element" : "select.value.element.parent";
		return translator.translate(i18nKey, args);
	}
}
