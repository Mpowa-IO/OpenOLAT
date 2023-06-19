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
package org.olat.modules.openbadges;

import java.io.File;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.olat.core.commons.services.image.Size;
import org.olat.core.commons.services.tag.TagInfo;
import org.olat.core.id.Identity;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.repository.RepositoryEntry;

/**
 * Initial date: 2023-05-08<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public interface OpenBadgesManager {

	//
	// Template
	//

	BadgeTemplate createTemplate(String name, File templateFile, String targetFileName, String description,
								 Collection<String> scopes, Identity savedBy);

	List<BadgeTemplate> getTemplates();

	List<TemplateWithSize> getTemplatesWithSizes();

	BadgeTemplate getTemplate(Long key);

	VFSLeaf getTemplateVfsLeaf(String templateImage);

	void updateTemplate(BadgeTemplate template);

	void deleteTemplate(BadgeTemplate template);


	//
	// Class
	//

	BadgeClass createBadgeClass(String uuid, String version, String language, File sourceFile, String targetFileName,
								String name, String description, String criteria, String salt, String issuer,
								Identity savedBy);

	List<BadgeClass> getBadgeClasses(RepositoryEntry entry);

	List<BadgeClassWithSize> getBadgeClassesWithSizes(RepositoryEntry entry);

	BadgeClass getBadgeClass(String uuid);

	VFSLeaf getBadgeClassVfsLeaf(String classFile);

	BadgeClass updateBadgeClass(BadgeClass badgeClass);

	void deleteBadgeClass(BadgeClass badgeClass);

	//
	// Assertion
	//

	BadgeAssertion createBadgeAssertion(String uuid, BadgeClass badgeClass, Date issuedOn,
										Identity recipient, Identity savedBy);

	List<BadgeAssertion> getBadgeAssertions();

	List<BadgeAssertionWithSize> getBadgeAssertionsWithSizes();

	BadgeAssertion getBadgeAssertion(String uuid);

	void updateBadgeAssertion(BadgeAssertion badgeAssertion);

	void deleteBadgeAssertion(BadgeAssertion badgeAssertion);

	//
	// Category
	//

	List<? extends TagInfo> getCategories(BadgeTemplate badgeTemplate, BadgeClass badgeClass);

	void updateCategories(BadgeTemplate badgeTemplate, BadgeClass badgeClass, List<String> displayNames);

	//
	// Entry Configuration
	//
	BadgeEntryConfiguration getConfiguration(RepositoryEntry entry);

	BadgeEntryConfiguration updateConfiguration(BadgeEntryConfiguration configuration);

	boolean isEnabled();

	//
	// Types
	//

	enum FileType {
		png,
		svg
	}

	record TemplateWithSize (BadgeTemplate template, Size size) {
		public Size fitIn(int width, int height) {
			double sourceAspectRatio = (double) size.getWidth() / (double) size.getHeight();
			double targetAspectRatio = (double) width / (double) height;
			if (sourceAspectRatio > targetAspectRatio) {
				return new Size(width, (int) Math.round(width / sourceAspectRatio), false);
			} else {
				return new Size((int) Math.round(height * sourceAspectRatio), height, false);
			}
		}
	}

	record BadgeClassWithSize (BadgeClass badgeClass, Size size) {
		public Size fitIn(int width, int height) {
			double sourceAspectRatio = (double) size.getWidth() / (double) size.getHeight();
			double targetAspectRatio = (double) width / (double) height;
			if (sourceAspectRatio > targetAspectRatio) {
				return new Size(width, (int) Math.round(width / sourceAspectRatio), false);
			} else {
				return new Size((int) Math.round(height * sourceAspectRatio), height, false);
			}
		}
	}

	record BadgeAssertionWithSize (BadgeAssertion badgeAssertion, Size size) {
		public Size fitIn(int width, int height) {
			double sourceAspectRatio = (double) size.getWidth() / (double) size.getHeight();
			double targetAspectRatio = (double) width / (double) height;
			if (sourceAspectRatio > targetAspectRatio) {
				return new Size(width, (int) Math.round(width / sourceAspectRatio), false);
			} else {
				return new Size((int) Math.round(height * sourceAspectRatio), height, false);
			}
		}
	}
}
