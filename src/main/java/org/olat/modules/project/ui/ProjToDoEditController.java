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
package org.olat.modules.project.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.modules.project.ProjToDo;
import org.olat.modules.project.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 26. Mar 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ProjToDoEditController extends FormBasicController {

	private ProjToDoContentEditController contentCtrl;
	private ProjArtefactReferencesController referenceCtrl;
	private ProjArtefactMetadataController metadataCtrl;

	private final ProjToDo toDo;
	private final boolean firstEdit;
	private final boolean withOpenInSameWindow;
	private Boolean referenceOpen = Boolean.FALSE;
	private Boolean metadataOpen = Boolean.FALSE;
	
	@Autowired
	private ProjectService projectService;

	public ProjToDoEditController(UserRequest ureq, WindowControl wControl, ProjToDo toDo, boolean firstEdit,
			boolean withOpenInSameWindow) {
		super(ureq, wControl, "edit");
		this.toDo = toDo;
		this.firstEdit = firstEdit;
		this.withOpenInSameWindow = withOpenInSameWindow;
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		contentCtrl = new ProjToDoContentEditController(ureq, getWindowControl(), mainForm, toDo);
		listenTo(contentCtrl);
		formLayout.add("content", contentCtrl.getInitialFormItem());
		
		referenceCtrl = new ProjArtefactReferencesController(ureq, getWindowControl(), mainForm, toDo.getArtefact(),
				false, withOpenInSameWindow);
		listenTo(referenceCtrl);
		formLayout.add("reference", referenceCtrl.getInitialFormItem());
		flc.contextPut("referenceOpen", referenceOpen);
		
		if (!firstEdit) {
			metadataCtrl = new ProjArtefactMetadataController(ureq, getWindowControl(), mainForm, toDo.getArtefact());
			listenTo(metadataCtrl);
			formLayout.add("metadata", metadataCtrl.getInitialFormItem());
			flc.contextPut("metadataOpen", metadataOpen);
		}
		
		FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add("buttons", buttonLayout);
		uifactory.addFormSubmitButton("save", buttonLayout);
		if (firstEdit) {
			uifactory.addFormCancelButton("remove", buttonLayout, ureq, getWindowControl());
		} else {
			uifactory.addFormCancelButton("cancel", buttonLayout, ureq, getWindowControl());
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == referenceCtrl) {
			fireEvent(ureq, event);
		}
		super.event(ureq, source, event);
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if ("ONCLICK".equals(event.getCommand())) {
			String referenceOpenVal = ureq.getParameter("referenceOpen");
			if (StringHelper.containsNonWhitespace(referenceOpenVal)) {
				referenceOpen = Boolean.valueOf(referenceOpenVal);
				flc.contextPut("referenceOpen", referenceOpen);
			}
			String metadataOpenVal = ureq.getParameter("metadataOpen");
			if (StringHelper.containsNonWhitespace(metadataOpenVal)) {
				metadataOpen = Boolean.valueOf(metadataOpenVal);
				flc.contextPut("metadataOpen", metadataOpen);
			}
		}
		super.event(ureq, source, event);
	}
	
	@Override
	protected void formCancelled(UserRequest ureq) {
		if (firstEdit) {
			projectService.deleteToDoPermanent(toDo);
		}
		fireEvent(ureq, FormEvent.CANCELLED_EVENT);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		contentCtrl.formOK(ureq);
		fireEvent(ureq, FormEvent.DONE_EVENT);
	}

}