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
package org.olat.course.assessment;

import java.util.List;

import org.olat.basesecurity.IdentityShort;
import org.olat.core.id.Identity;
import org.olat.course.assessment.model.CourseStatistics;
import org.olat.course.assessment.model.SearchAssessedIdentityParams;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.modules.assessment.model.AssessmentEntryStatus;

/**
 * The manager taylored for the assessment tool.
 * 
 * 
 * Initial date: 22.07.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public interface AssessmentToolManager {
	
	public CourseStatistics getStatistics(Identity coach, SearchAssessedIdentityParams params);
	
	public List<Identity> getAssessedIdentities(Identity coach, SearchAssessedIdentityParams params);
	
	public List<IdentityShort> getShortAssessedIdentities(Identity coach, SearchAssessedIdentityParams params, int maxResults);
	
	public List<AssessmentEntry> getAssessmentEntries(Identity coach, SearchAssessedIdentityParams params, AssessmentEntryStatus status);

}
