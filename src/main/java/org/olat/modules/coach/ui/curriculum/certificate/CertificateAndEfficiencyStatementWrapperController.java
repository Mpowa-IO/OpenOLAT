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
package org.olat.modules.coach.ui.curriculum.certificate;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.Identity;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.event.GenericEventListener;
import org.olat.core.util.resource.OresHelper;
import org.olat.course.certificate.ui.CertificateAndEfficiencyStatementListController;
import org.olat.modules.coach.RoleSecurityCallback;
import org.olat.modules.curriculum.CurriculumModule;
import org.olat.modules.curriculum.CurriculumSecurityCallback;
import org.springframework.beans.factory.annotation.Autowired;

public class CertificateAndEfficiencyStatementWrapperController extends BasicController implements Activateable2, GenericEventListener {

    private static final String WITH_CURRICULUM = "Curriculum";
    private static final String WITHOUT_CURRICULM = "List";

    private final TooledStackedPanel stackPanel;
    private final Identity mentee;
    private final CurriculumSecurityCallback curriculumSecurityCallback;
    private final RoleSecurityCallback roleSecurityCallback;

    private CertificateAndEfficiencyStatementCurriculumListController certificateCurriculumListController;
    private CertificateAndEfficiencyStatementListController certificateListController;

    private Link curriculumShow;
    private Link curriculumHide;
    private boolean showCurriculum;
    private String activeCSS = "o_button_toggle o_on";
    private String inactiveCSS = "o_button_toggle";
    private VelocityContainer content;

    @Autowired
    private CurriculumModule curriculumModule;

    public CertificateAndEfficiencyStatementWrapperController(UserRequest ureq, WindowControl wControl, TooledStackedPanel stackPanel, Identity mentee,
    		CurriculumSecurityCallback curriculumSecurityCallback, RoleSecurityCallback roleSecurityCallback) {
        super(ureq, wControl);

        this.stackPanel = stackPanel;
        this.mentee = mentee;
        this.curriculumSecurityCallback = curriculumSecurityCallback;
        this.roleSecurityCallback = roleSecurityCallback;

        content = createVelocityContainer("certificate_list_wrapper");
        showCurriculum = curriculumModule.isEnabled() && roleSecurityCallback.canViewCoursesAndCurriculum();

        if (showCurriculum) {
            curriculumShow = LinkFactory.createLink("off", content, this);
            curriculumShow.setCustomEnabledLinkCSS(inactiveCSS);
            curriculumShow.setIconLeftCSS("o_icon o_icon_toggle");
            curriculumShow.setIconRightCSS(null);

            curriculumHide = LinkFactory.createLink("on", content, this);
            curriculumHide.setCustomEnabledLinkCSS(activeCSS);
            curriculumHide.setIconLeftCSS(null);
            curriculumHide.setIconRightCSS("o_icon o_icon_toggle");

            showCurriculumStructure(ureq);
        } else {
            hideCurriculumStructure(ureq);
        }

        putInitialPanel(content);
    }

    @Override
    public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
        if(entries != null && !entries.isEmpty()) {
            ContextEntry currentEntry = entries.get(0);
            String cmd = currentEntry.getOLATResourceable().getResourceableTypeName();

            Activateable2 selectedCtrl;

            if (cmd.equals(WITHOUT_CURRICULM)) {
                selectedCtrl = hideCurriculumStructure(ureq);
            } else {
                selectedCtrl = showCurriculumStructure(ureq);
            }

            List<ContextEntry> subEntries = entries.subList(1, entries.size());
            selectedCtrl.activate(ureq, subEntries, currentEntry.getTransientState());
        }
    }

    @Override
    protected void event(UserRequest ureq, Component source, Event event) {
        if (source == curriculumHide) {
            hideCurriculumStructure(ureq);
        } else if (source == curriculumShow) {
            showCurriculumStructure(ureq);
        }
    }

    @Override
    public void event(Event event) {
    	//
    }

    public Activateable2 showCurriculumStructure(UserRequest ureq) {
        showCurriculum = true;

        if (certificateCurriculumListController == null) {
            WindowControl bwControl = addToHistory(ureq, OresHelper.createOLATResourceableType(WITH_CURRICULUM), null);
            certificateCurriculumListController = new CertificateAndEfficiencyStatementCurriculumListController(ureq, bwControl, stackPanel, mentee, curriculumSecurityCallback, roleSecurityCallback);
            listenTo(certificateCurriculumListController);
        }

        content.contextPut("showCurriculum", showCurriculum);
        content.put("content", certificateCurriculumListController.getInitialComponent());
    	addToHistory(ureq, certificateCurriculumListController);
        return certificateCurriculumListController;
    }

    public Activateable2 hideCurriculumStructure(UserRequest ureq) {
        showCurriculum = false;

        if (certificateListController == null) {
            WindowControl bwControl = addToHistory(ureq, OresHelper.createOLATResourceableType(WITHOUT_CURRICULM), null);
            certificateListController = new CertificateAndEfficiencyStatementListController(ureq, bwControl, mentee, false, false, false, false);
            certificateListController.setBreadcrumbPanel(stackPanel);
            listenTo(certificateListController);
        }

        content.contextPut("showCurriculum", showCurriculum);
        content.put("content", certificateListController.getInitialComponent());
    	addToHistory(ureq, certificateListController);
        return certificateListController;
    }
}
