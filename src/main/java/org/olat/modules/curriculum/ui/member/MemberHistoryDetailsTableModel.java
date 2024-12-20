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
package org.olat.modules.curriculum.ui.member;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FilterableFlexiTableModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableDateRangeFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableDateRangeFilter.DateRange;
import org.olat.core.util.StringHelper;

/**
 * 
 * Initial date: 26 nov. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class MemberHistoryDetailsTableModel extends DefaultFlexiTableDataModel<MemberHistoryDetailsRow>
implements FilterableFlexiTableModel {
	
	private static final MemberHistoryCols[] COLS = MemberHistoryCols.values();

	private List<MemberHistoryDetailsRow> backups;
	
	public MemberHistoryDetailsTableModel(FlexiTableColumnModel columnModel) {
		super(columnModel);
	}

	@Override
	public void filter(String searchString, List<FlexiTableFilter> filters) {
		if(StringHelper.containsNonWhitespace(searchString) || (filters != null && !filters.isEmpty() && filters.get(0) != null)) {
			DateRange range = getFrom(filters);
			searchString = searchString.toLowerCase();
			
			List<MemberHistoryDetailsRow> filteredRows = new ArrayList<>(backups.size());
			for(MemberHistoryDetailsRow row:backups) {
				boolean accept = accept(row, searchString)
						&& acceptDateRange(row, range);
				if(accept) {
					filteredRows.add(row);
				}
			}
		} else {
			super.setObjects(backups);
		}
	}
	
	private boolean accept(MemberHistoryDetailsRow row, String searchString) {
		if(!StringHelper.containsNonWhitespace(searchString)) {
			return true;
		}
		return row.getUserDisplayName() != null && row.getUserDisplayName().toLowerCase().contains(searchString);
	}
	
	private boolean acceptDateRange(MemberHistoryDetailsRow row, DateRange range) {
		if(range == null) {
			return true;
		}
		return (range.getStart() == null || range.getStart().compareTo(row.getDate()) <= 0)
				&& (range.getEnd() == null || range.getEnd().compareTo(row.getDate()) >= 0);
	}
	
	private DateRange getFrom(List<FlexiTableFilter> filters) {
		FlexiTableFilter dateFilter = FlexiTableFilter.getFilter(filters, MemberHistoryDetailsController.FILTER_DATE);
		if(dateFilter instanceof FlexiTableDateRangeFilter rangeFilter) {
			DateRange range = rangeFilter.getDateRange();
			if(range != null && (range.getStart() != null || range.getEnd() != null)) {
				return range;
			}
		}
		return null;
	}

	@Override
	public Object getValueAt(int row, int col) {
		MemberHistoryDetailsRow detailsRow = getObject(row);
		if(col >= 0 && col < COLS.length) {
			return switch(COLS[col]) {
				case key -> detailsRow.getHistoryKey();
				case creationDate -> detailsRow.getDate();
				case role -> detailsRow.getMembership();
				case activity -> detailsRow.getStatus();
				case originalValue -> "";
				case newValue -> detailsRow.getStatus();
				case note -> null;
				case actor -> detailsRow.getUserDisplayName();
				default -> "ERROR";
			};
		}
		
		return "ERROR";
	}
	
	@Override
	public void setObjects(List<MemberHistoryDetailsRow> objects) {
		backups = new ArrayList<>(objects);
		super.setObjects(objects);
	}
	
	public enum MemberHistoryCols implements FlexiSortableColumnDef {
		key("table.header.key"),
		creationDate("table.header.date"),
		role("table.header.role"),
		activity("table.header.activity"),
		originalValue("table.header.original.value"),
		newValue("table.header.new.value"),
		note("table.header.note"),
		actor("table.header.actor");
		
		private final String i18nKey;
		
		private MemberHistoryCols(String i18nKey) {
			this.i18nKey = i18nKey;
		}
		
		@Override
		public String i18nHeaderKey() {
			return i18nKey;
		}

		@Override
		public boolean sortable() {
			return false;
		}

		@Override
		public String sortKey() {
			return name();
		}
	}
}
