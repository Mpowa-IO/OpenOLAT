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
package org.olat.course.nodes.practice.manager;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.TypedQuery;

import org.olat.basesecurity.IdentityRef;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.PersistenceHelper;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.course.nodes.practice.PlayMode;
import org.olat.course.nodes.practice.PracticeFilterRule;
import org.olat.course.nodes.practice.PracticeFilterRule.Type;
import org.olat.course.nodes.practice.model.SearchPracticeItemParameters;
import org.olat.modules.qpool.Pool;
import org.olat.modules.qpool.QuestionItem;
import org.olat.modules.qpool.QuestionItemCollection;
import org.olat.resource.OLATResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 12 mai 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class PracticeQuestionItemQueries {
	
	@Autowired
	private DB dbInstance;
	
	public List<QuestionItem> searchItems(SearchPracticeItemParameters searchParams,
			List<QuestionItemCollection> collections, List<Pool> pools, List<OLATResource> resources,
			IdentityRef identity) {
		
		QueryBuilder sb = new QueryBuilder(4096);
		sb.append("select item")
		  .append(" from questionitem item")
		  .append(" left join fetch item.taxonomyLevel taxonomyLevel");
		appendResources(sb, collections, pools, resources);
		
		boolean needIdentity = appendPlayMode(sb, searchParams.getPlayMode());

		appendSearchParams(sb, searchParams);
		
		TypedQuery<QuestionItem> query = dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), QuestionItem.class);
		
		if(collections != null && !collections.isEmpty()) {
			List<Long> keys = collections.stream()
					.map(QuestionItemCollection::getKey)
					.collect(Collectors.toList());
			query.setParameter("collectionKey", keys);
		}
		if(pools != null && !pools.isEmpty()) {
			List<Long> keys = pools.stream()
					.map(Pool::getKey)
					.collect(Collectors.toList());
			query.setParameter("poolKey", keys);
		}
		if(resources != null && !resources.isEmpty()) {
			List<Long> keys = resources.stream()
					.map(OLATResource::getKey)
					.collect(Collectors.toList());
			query.setParameter("resourceKey", keys);
		}
		appendSearchParams(query, searchParams);
		
		if(needIdentity) {
			query.setParameter("identityKey", identity.getKey());	
		}
		
		return query.getResultList();
	}
	
	private boolean appendPlayMode(QueryBuilder sb, PlayMode playMode) {
		boolean needIdentity = false;
		if(playMode == PlayMode.newQuestions) {
			sb.and()
			  .append(" not exists (select ref.identifier from practiceglobalitemref ref")
			  .append("  where ref.identifier=item.identifier and ref.identity.key=:identityKey")
			  .append(" )");
			needIdentity = true;
		} else if(playMode == PlayMode.incorrectQuestions) {
			sb.and()
			  .append(" exists (select ref.identifier from practiceglobalitemref ref")
			  .append("  where ref.identifier=item.identifier and ref.lastAttemptsPassed=false and ref.identity.key=:identityKey")
			  .append(" )");
			needIdentity = true;
		}
		return needIdentity;
	}
	
	private void appendResources(QueryBuilder sb, List<QuestionItemCollection> collections, List<Pool> pools, List<OLATResource> resources) {
		if(collections != null && !collections.isEmpty()) {
			sb.append(" left join qcollection2item coll2item on (coll2item.item.key=item.key)");
		}
		if(pools != null && !pools.isEmpty()) {
			sb.append(" left join qpool2item pool2item on (pool2item.item.key=item.key)");
		}
		if(resources != null && !resources.isEmpty()) {
			sb.append(" left join qshareitem shareditem on (shareditem.item.key=item.key)");
		}
		
		sb.and().append("(");
		boolean or = false;
		if(collections != null && !collections.isEmpty()) {
			sb.append("coll2item.collection.key in (:collectionKey)");
			or = true;
		}
		if(pools != null && !pools.isEmpty()) {
			if(or) {
				sb.append(" or ");
			}
			sb.append("pool2item.pool.key in (:poolKey)");
			or = true;
		}
		if(resources != null && !resources.isEmpty()) {
			if(or) {
				sb.append(" or ");
			}
			sb.append("shareditem.resource.key in (:resourceKey)");
		}
		sb.append(")");
	}
	
	private void appendSearchParams(QueryBuilder sb, SearchPracticeItemParameters searchParams) {
		if(searchParams.getExactTaxonomyLevelKey() != null) {
			sb.and().append("taxonomyLevel.key =:taxonomyLevelKey");	
		} else if(searchParams.getDescendantsLevels() != null && !searchParams.getDescendantsLevels().isEmpty()) {
			sb.and().append("(");
			for(int i=0; i<searchParams.getDescendantsLevels().size(); i++) {
				if(i > 0) {
					sb.append(" or ");
				}
				sb.append("taxonomyLevel.materializedPathKeys like :taxonomyLevels_" + i);
			}
			sb.append(")");
		}
		
		if(searchParams.getRules() != null && !searchParams.getRules().isEmpty()) {
			Set<Type> types = new HashSet<>();
			int count = 0;
			for(PracticeFilterRule rule: searchParams.getRules()) {
				if(types.contains(rule.getType())) {
					continue;
				}
				types.add(rule.getType());
				
				Type type = rule.getType();
				if(type == Type.assessmentType) {
					sb.and().append(" item.assessmentType in (:assessmentTypes)");
				} else if(type == Type.educationalContextLevel) {
					sb.and().append(" item.educationalContext.key in (:educationalContextKeys)");
				} else if(type == Type.keyword) {
					sb.and().appendFuzzyLike("item.keywords", "keywords_" + (count++));
				} else if(type == Type.language) {
					sb.and().append(" item.language in (:languages)");
				}
			}
		}
	}
	
	private void appendSearchParams(TypedQuery<QuestionItem> query, SearchPracticeItemParameters searchParams) {
		if(searchParams.getExactTaxonomyLevelKey() != null) {
			query.setParameter("taxonomyLevelKey", searchParams.getExactTaxonomyLevelKey());
		} else if(searchParams.getDescendantsLevels() != null && !searchParams.getDescendantsLevels().isEmpty()) {
			for(int i=0; i<searchParams.getDescendantsLevels().size(); i++) {
				query.setParameter("taxonomyLevels_" + i, searchParams.getDescendantsLevels().get(i).getMaterializedPathKeys() + "%");
			}
		}
		
		if(searchParams.getRules() != null && !searchParams.getRules().isEmpty()) {
			List<Long> educationalContextKeys = new ArrayList<>();
			List<String> assessmentTypes = new ArrayList<>();
			List<String> languages = new ArrayList<>();

			int count = 0;
			for(PracticeFilterRule rule: searchParams.getRules()) {
				Type type = rule.getType();
				if(type == Type.assessmentType) {
					assessmentTypes.add(rule.getValue());
				} else if(type == Type.educationalContextLevel) {
					educationalContextKeys.add(Long.valueOf(rule.getValue()));
				} else if(type == Type.keyword) {
					String fuzzyValue = PersistenceHelper.makeFuzzyQueryString(rule.getValue());
					query.setParameter("keywords_" + (count++), fuzzyValue);
				} else if(type == Type.language) {
					languages.add(rule.getValue());
				}
			}
			
			if(!educationalContextKeys.isEmpty()) {
				query.setParameter("educationalContextKeys", educationalContextKeys);
			}
			if(!assessmentTypes.isEmpty()) {
				query.setParameter("assessmentTypes", assessmentTypes);
			}
			if(!languages.isEmpty()) {
				query.setParameter("languages", languages);
			}
		}
	}
}