/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.curriculum.ui;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.resource.OresHelper;
import org.olat.modules.curriculum.CurriculumSecurityCallback;
import org.olat.modules.curriculum.ui.event.ActivateEvent;
import org.olat.modules.curriculum.ui.widgets.LectureBlocksWidgetController;
import org.olat.modules.lecture.LectureModule;
import org.olat.modules.lecture.ui.LecturesSecurityCallback;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 14 oct. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CurriculumManagerRootController extends BasicController implements Activateable2 {

	private final Link lecturesBlocksLink;
	private final Link curriculumsLink;
	private final Link implementationsLink;
	private final VelocityContainer mainVC;
	private final TooledStackedPanel toolbarPanel;
	private final CurriculumSecurityCallback secCallback;
	private final LecturesSecurityCallback lecturesSecCallback;

	private CurriculumDashboardController overviewCtrl;
	private CurriculumManagerEventsController eventsCtrl;
	private CurriculumSearchManagerController searchCtrl;
	private CurriculumComposerController implementationsCtrl;
	private CurriculumListManagerController curriculumListCtrl;
	private final CurriculumSearchHeaderController searchFieldCtrl;
	private LectureBlocksWidgetController lectureBlocksWidgetCtrl; 

	@Autowired
	private LectureModule lectureModule;
	
	public CurriculumManagerRootController(UserRequest ureq, WindowControl wControl, TooledStackedPanel toolbarPanel,
			CurriculumSecurityCallback secCallback, LecturesSecurityCallback lecturesSecCallback) {
		super(ureq, wControl);
		this.secCallback = secCallback;
		this.toolbarPanel = toolbarPanel;
		this.lecturesSecCallback = lecturesSecCallback;
		
		mainVC = createVelocityContainer("manager_overview");
		
		searchFieldCtrl = new CurriculumSearchHeaderController(ureq, getWindowControl());
		listenTo(searchFieldCtrl);
		mainVC.put("searchField", searchFieldCtrl.getInitialComponent());

		curriculumsLink = LinkFactory.createLink("curriculum.browser", "curriculums", getTranslator(), mainVC, this, Link.LINK_CUSTOM_CSS);
		curriculumsLink.setIconLeftCSS("o_icon o_icon-xl o_icon_curriculum");
		curriculumsLink.setElementCssClass("btn btn-default o_button_mega o_sel_cur_browser");
		
		implementationsLink = LinkFactory.createLink("curriculum.implementations", "implementations", getTranslator(), mainVC, this, Link.LINK_CUSTOM_CSS);
		implementationsLink.setIconLeftCSS("o_icon o_icon-xl o_icon_curriculum_implementations");
		implementationsLink.setElementCssClass("btn btn-default o_button_mega o_sel_cur_implementations");
		
		lecturesBlocksLink = LinkFactory.createLink("curriculum.lectures", "lecturesblocks", getTranslator(), mainVC, this, Link.LINK_CUSTOM_CSS);
		lecturesBlocksLink.setIconLeftCSS("o_icon o_icon-xl o_icon_calendar_day");
		lecturesBlocksLink.setElementCssClass("btn btn-default o_button_mega o_sel_cur_lectures");
		
		initDashboard(ureq);
		putInitialPanel(mainVC);
	}
	
	private void initDashboard(UserRequest ureq) {
		overviewCtrl = new CurriculumDashboardController(ureq, getWindowControl());
		listenTo(overviewCtrl);
		
		if(lectureModule.isEnabled()) {
			lectureBlocksWidgetCtrl = new LectureBlocksWidgetController(ureq, getWindowControl(), lecturesSecCallback);
			listenTo(lectureBlocksWidgetCtrl);
			overviewCtrl.addWidget("lectures", lectureBlocksWidgetCtrl);
		}
		
		mainVC.put("dashboard", overviewCtrl.getInitialComponent());
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) return;
		
		String type = entries.get(0).getOLATResourceable().getResourceableTypeName();
		if("Curriculum".equalsIgnoreCase(type)) {
			doOpenCurriculumsList(ureq).activate(ureq, entries, state);
		} else if("All".equalsIgnoreCase(type) || "Curriculums".equalsIgnoreCase(type)) {
			List<ContextEntry> subEntries = entries.subList(1, entries.size());
			doOpenCurriculumsList(ureq).activate(ureq, subEntries, entries.get(0).getTransientState());
		} else if("Implementations".equalsIgnoreCase(type)) {
			List<ContextEntry> subEntries = entries.subList(1, entries.size());
			if(subEntries.isEmpty()) {
				doOpenImplementationsAllFilter(ureq);
			} else {
				doOpenImplementations(ureq).activate(ureq, subEntries, entries.get(0).getTransientState());
			}
		} else if("Lectures".equalsIgnoreCase(type) || "Events".equalsIgnoreCase(type)) {
			List<ContextEntry> subEntries = entries.subList(1, entries.size());
			doOpenLecturesBlocks(ureq).activate(ureq, subEntries, entries.get(0).getTransientState());
		} 
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(searchFieldCtrl == source) {
			if(event == Event.DONE_EVENT) {
				doSearch(ureq, searchFieldCtrl.getSearchString());
			}
		} else if(lectureBlocksWidgetCtrl == source) {
			if(event instanceof ActivateEvent ae) {
				doOpenLecturesBlocks(ureq).activate(ureq, ae.getEntries(), null);
			}
		}
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source == curriculumsLink) {
			doOpenCurriculumsList(ureq);
		} else if (source == implementationsLink){
			doOpenImplementationsAllFilter(ureq);
		} else if (source == lecturesBlocksLink) {
			doOpenLecturesBlocks(ureq);
		}
	}
	
	private void doSearch(UserRequest ureq, String searchString) {
		toolbarPanel.popUpToRootController(ureq);
		removeAsListenerAndDispose(searchCtrl);

		WindowControl subControl = addToHistory(ureq, OresHelper.createOLATResourceableInstance("Search", 0l), null);
		searchCtrl = new CurriculumSearchManagerController(ureq, subControl, toolbarPanel, searchString,
				secCallback, lecturesSecCallback);
		listenTo(searchCtrl);
		toolbarPanel.pushController(translate("curriculum.search.results"), searchCtrl);
	}
	
	private CurriculumListManagerController doOpenCurriculumsList(UserRequest ureq) {
		toolbarPanel.popUpToRootController(ureq);
		removeAsListenerAndDispose(curriculumListCtrl);
		
		WindowControl subControl = addToHistory(ureq, OresHelper.createOLATResourceableInstance("Curriculums", 0l), null);
		curriculumListCtrl = new CurriculumListManagerController(ureq, subControl, toolbarPanel,
				secCallback, lecturesSecCallback);
		listenTo(curriculumListCtrl);
		toolbarPanel.pushController(translate("toolbar.curriculums"), curriculumListCtrl);
		return curriculumListCtrl;
	}
	
	private void doOpenImplementationsAllFilter(UserRequest ureq) {
		// Load "All" filter preset
		List<ContextEntry> all = BusinessControlFactory.getInstance().createCEListFromString("[All:0]");
		doOpenImplementations(ureq).activate(ureq, all, null);
	}
	
	private CurriculumComposerController doOpenImplementations(UserRequest ureq) {
		toolbarPanel.popUpToRootController(ureq);
		removeAsListenerAndDispose(implementationsCtrl);
		
		CurriculumComposerConfig config = CurriculumComposerConfig.implementationsView();
		config.setTitle(translate("curriculum.implementations"), 2, "o_icon_curriculum_implementations");
		WindowControl subControl = addToHistory(ureq, OresHelper.createOLATResourceableInstance("Implementations", 0l), null);
		implementationsCtrl = new CurriculumComposerController(ureq, subControl, toolbarPanel,
				null, null, config , secCallback, lecturesSecCallback);
		listenTo(implementationsCtrl);
		toolbarPanel.pushController(translate("toolbar.implementations"), implementationsCtrl);
		return implementationsCtrl;
	}
	
	private CurriculumManagerEventsController doOpenLecturesBlocks(UserRequest ureq) {
		toolbarPanel.popUpToRootController(ureq);
		removeAsListenerAndDispose(eventsCtrl);
		
		eventsCtrl = new CurriculumManagerEventsController(ureq, getWindowControl(), lecturesSecCallback);
		listenTo(eventsCtrl);
		toolbarPanel.pushController(translate("curriculum.lectures"), eventsCtrl);
		return eventsCtrl;
	}
}
