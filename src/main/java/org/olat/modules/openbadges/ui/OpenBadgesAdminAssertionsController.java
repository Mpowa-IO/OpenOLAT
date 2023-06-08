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
package org.olat.modules.openbadges.ui;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.modules.openbadges.BadgeAssertion;
import org.olat.modules.openbadges.OpenBadgesManager;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 2023-06-05<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class OpenBadgesAdminAssertionsController extends FormBasicController {

	private AssertionTableModel tableModel;
	private FlexiTableElement tableEl;
	private FormLink addLink;
	private CloseableModalController cmc;
	private EditBadgeAssertionController editAssertionCtrl;
	private DialogBoxController confirmDeleteAssertionCtrl;

	@Autowired
	private OpenBadgesManager openBadgesManager;

	protected OpenBadgesAdminAssertionsController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, "assertions");

		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FlexiTableColumnModel columnModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.recipient.getI18n(), Cols.recipient.ordinal()));
		columnModel.addFlexiColumnModel(new DefaultFlexiColumnModel("edit", translate("edit"), "edit"));
		columnModel.addFlexiColumnModel(new DefaultFlexiColumnModel("delete", translate("delete"), "delete"));

		tableModel = new AssertionTableModel(columnModel);
		tableEl = uifactory.addTableElement(getWindowControl(), "assertions", tableModel, getTranslator(),
				formLayout);
		addLink = uifactory.addFormLink("add", "assertion.add", "assertion.add", formLayout, Link.BUTTON);
		updateUI();
	}

	private void updateUI() {
		List<OpenBadgesManager.BadgeAssertionWithSize> assertionsWithSizes = openBadgesManager.getBadgeAssertionsWithSizes();
		tableModel.setObjects(assertionsWithSizes);
		tableEl.reset();
	}

	private void cleanUp() {
		removeAsListenerAndDispose(cmc);
		removeAsListenerAndDispose(editAssertionCtrl);
		cmc = null;
		editAssertionCtrl = null;
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == editAssertionCtrl) {
			cmc.deactivate();
			cleanUp();
			updateUI();
		} else if (source == confirmDeleteAssertionCtrl) {
			if (DialogBoxUIFactory.isOkEvent(event)) {
				BadgeAssertion badgeAssertion = (BadgeAssertion) confirmDeleteAssertionCtrl.getUserObject();
				doDelete(badgeAssertion);
				updateUI();
			}
		} else if (source == cmc) {
			cleanUp();
		}
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == addLink) {
			doUpload(ureq);
		} else if (source == tableEl) {
			SelectionEvent selectionEvent = (SelectionEvent)event;
			String command = selectionEvent.getCommand();
			BadgeAssertion badgeAssertion = tableModel.getObject(selectionEvent.getIndex()).badgeAssertion();
			if ("edit".equals(command)) {
				doEdit(ureq, badgeAssertion);
			} else if ("delete".equals(command)) {
				doConfirmDelete(ureq, badgeAssertion);
			}
		}
	}

	private void doUpload(UserRequest ureq) {
		editAssertionCtrl = new EditBadgeAssertionController(ureq, getWindowControl(), null);
		listenTo(editAssertionCtrl);

		String title = translate("assertion.add");
		cmc = new CloseableModalController(getWindowControl(), "close",
				editAssertionCtrl.getInitialComponent(), true, title);
		listenTo(cmc);
		cmc.activate();
	}

	private void doEdit(UserRequest ureq, BadgeAssertion badgeAssertion) {
		editAssertionCtrl = new EditBadgeAssertionController(ureq, getWindowControl(), badgeAssertion);
		listenTo(editAssertionCtrl);

		String title = translate("assertion.edit");
		cmc = new CloseableModalController(getWindowControl(), "close",
				editAssertionCtrl.getInitialComponent(), true, title);
		listenTo(cmc);
		cmc.activate();
	}

	private void doConfirmDelete(UserRequest ureq, BadgeAssertion badgeAssertion) {
		String title = translate("confirm.delete.assertion.title", badgeAssertion.getRecipientObject());
		String text = translate("confirm.delete.assertion", badgeAssertion.getRecipientObject());
		confirmDeleteAssertionCtrl = activateOkCancelDialog(ureq, title, text, confirmDeleteAssertionCtrl);
		confirmDeleteAssertionCtrl.setUserObject(badgeAssertion);
	}

	private void doDelete(BadgeAssertion badgeAssertion) {
		openBadgesManager.deleteBadgeAssertion(badgeAssertion);
		updateUI();
	}

	enum Cols {
		recipient("form.recipient");

		Cols(String i18n) {
			this.i18n = i18n;
		}

		private final String i18n;

		public String getI18n() {
			return i18n;
		}
	}

	private static class AssertionTableModel extends DefaultFlexiTableDataModel<OpenBadgesManager.BadgeAssertionWithSize> {
		public AssertionTableModel(FlexiTableColumnModel columnModel) {
			super(columnModel);
		}

		@Override
		public Object getValueAt(int row, int col) {
			BadgeAssertion badgeAssertion = getObject(row).badgeAssertion();
			return switch (Cols.values()[col]) {
				case recipient -> badgeAssertion.getRecipientObject();
			};
		}
	}
}