/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
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
package org.olat.modules.catalog.ui;

import static org.olat.modules.catalog.CatalogRepositoryEntrySearchParams.KEY_LAUNCHER;

import java.util.ArrayList;
import java.util.List;

import org.olat.NewControllerFactory;
import org.olat.core.dispatcher.DispatcherModule;
import org.olat.core.dispatcher.mapper.MapperService;
import org.olat.core.dispatcher.mapper.manager.MapperKey;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableExtendedFilter;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DateFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponentDelegate;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableRendererType;
import org.olat.core.gui.components.link.ExternalLinkItem;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.stack.BreadcrumbedStackedPanel;
import org.olat.core.gui.components.stack.PopEvent;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.gui.control.generic.lightbox.LightboxController;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.resource.OresHelper;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.course.CorruptedCourseException;
import org.olat.login.LoginProcessEvent;
import org.olat.login.LoginProcessController;
import org.olat.modules.catalog.CatalogFilter;
import org.olat.modules.catalog.CatalogFilterHandler;
import org.olat.modules.catalog.CatalogFilterSearchParams;
import org.olat.modules.catalog.CatalogRepositoryEntrySearchParams;
import org.olat.modules.catalog.CatalogSearchTerm;
import org.olat.modules.catalog.CatalogV2Module;
import org.olat.modules.catalog.CatalogV2Service;
import org.olat.modules.catalog.ui.CatalogRepositoryEntryDataModel.CatalogRepositoryEntryCols;
import org.olat.modules.catalog.ui.CatalogRepositoryEntryDataSource.CatalogRepositoryEntryRowItemCreator;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.modules.taxonomy.manager.TaxonomyLevelDAO;
import org.olat.modules.taxonomy.model.TaxonomyLevelNamePath;
import org.olat.modules.taxonomy.ui.TaxonomyLevelTeaserImageMapper;
import org.olat.modules.taxonomy.ui.TaxonomyUIFactory;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryModule;
import org.olat.repository.RepositoryService;
import org.olat.repository.ui.RepositoryEntryImageMapper;
import org.olat.repository.ui.author.ACRenderer;
import org.olat.repository.ui.author.EducationalTypeRenderer;
import org.olat.repository.ui.author.TypeRenderer;
import org.olat.resource.accesscontrol.ACService;
import org.olat.resource.accesscontrol.AccessResult;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 24 May 2022<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class CatalogRepositoryEntryListController extends FormBasicController implements Activateable2, FlexiTableComponentDelegate, CatalogRepositoryEntryRowItemCreator {
	
	private BreadcrumbedStackedPanel stackPanel;
	private FlexiTableElement tableEl;
	private CatalogRepositoryEntryDataModel dataModel;
	private final CatalogRepositoryEntryDataSource dataSource;
	private final CatalogRepositoryEntrySearchParams searchParams;
	
	private CatalogRepositoryEntryInfosController infosCtrl;
	private LightboxController lightboxCtrl;
	private WebCatalogAuthController authCtrl;
	
	private final boolean withSearch;
	private final MapperKey mapperThumbnailKey;
	private final TaxonomyLevelTeaserImageMapper teaserImageMapper;
	private final MapperKey teaserImageMapperKey;

	private final TaxonomyLevel taxonomyLevel;
	
	@Autowired
	private CatalogV2Module catalogModule;
	@Autowired
	private CatalogV2Service catalogService;
	@Autowired
	private RepositoryModule repositoryModule;
	@Autowired
	private RepositoryManager repositoryManager;
	@Autowired
	private ACService acService;
	@Autowired
	private TaxonomyLevelDAO taxonomyLevelDao;
	@Autowired
	private MapperService mapperService;

	public CatalogRepositoryEntryListController(UserRequest ureq, WindowControl wControl, BreadcrumbedStackedPanel stackPanel, 
			CatalogRepositoryEntrySearchParams searchParams, boolean withSearch) {
		super(ureq, wControl, "entry_list");
		// Order of the translators matters.
		setTranslator(Util.createPackageTranslator(RepositoryService.class, getLocale()));
		setTranslator(Util.createPackageTranslator(CatalogRepositoryEntryListController.class, getLocale(), getTranslator()));
		setTranslator(Util.createPackageTranslator(TaxonomyUIFactory.class, getLocale(), getTranslator()));
		this.stackPanel = stackPanel;
		stackPanel.addListener(this);
		this.searchParams = searchParams;
		this.withSearch = withSearch;
		this.taxonomyLevel = searchParams.getIdentToTaxonomyLevels().containsKey(KEY_LAUNCHER)
				? searchParams.getIdentToTaxonomyLevels().get(KEY_LAUNCHER).get(0)
				: null;
		this.dataSource = new CatalogRepositoryEntryDataSource(searchParams, withSearch, this, getLocale());
		this.mapperThumbnailKey = mapperService.register(null, "repositoryentryImage", new RepositoryEntryImageMapper());
		this.teaserImageMapper = new TaxonomyLevelTeaserImageMapper();
		this.teaserImageMapperKey = mapperService.register(null, "taxonomyLevelTeaserImage", teaserImageMapper);
		
		initForm(ureq);
		tableEl.reloadData();
		setWindowTitle();
	}

	public TaxonomyLevel getTaxonomyLevel() {
		return taxonomyLevel;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if (taxonomyLevel != null) {
			flc.contextPut("square", CatalogV2Module.TAXONOMY_LEVEL_LAUNCHER_STYLE_SQUARE.equals(catalogModule.getLauncherTaxonomyLevelStyle()));
			flc.contextPut("description", TaxonomyUIFactory.translateDescription(getTranslator(), taxonomyLevel));
			
			List<TaxonomyLevel> taxonomyLevels = taxonomyLevelDao.getChildren(taxonomyLevel);
			catalogService.excludeLevelsWithoutOffers(taxonomyLevels, searchParams);
			taxonomyLevels.sort(CatalogV2UIFactory.getTaxonomyLevelComparator(getTranslator()));
			List<TaxonomyItem> items = new ArrayList<>(taxonomyLevels.size());
			for (TaxonomyLevel child : taxonomyLevels) {
				String displayName = TaxonomyUIFactory.translateDisplayName(getTranslator(), child);
				
				String selectLinkName = "o_tl_" + child.getKey();
				FormLink selectLink = uifactory.addFormLink(selectLinkName, "select_tax", displayName, null, formLayout, Link.NONTRANSLATED);
				selectLink.setUserObject(child.getKey());
				
				TaxonomyItem item = new TaxonomyItem(child.getKey(), displayName, selectLinkName);
				
				String imageUrl = teaserImageMapper.getImageUrl(child);
				if (StringHelper.containsNonWhitespace(imageUrl)) {
					item.setThumbnailRelPath(teaserImageMapperKey.getUrl() + "/" + imageUrl);
				}
				
				items.add(item);
			}
			flc.contextPut("taxonomyLevels", items);
		}
		
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, CatalogRepositoryEntryCols.key));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CatalogRepositoryEntryCols.type, new TypeRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CatalogRepositoryEntryCols.displayName));
		if (repositoryModule.isManagedRepositoryEntries()) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, CatalogRepositoryEntryCols.externalId));
		}
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, CatalogRepositoryEntryCols.externalRef));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CatalogRepositoryEntryCols.lifecycleLabel));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CatalogRepositoryEntryCols.lifecycleSoftkey));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CatalogRepositoryEntryCols.lifecycleStart, new DateFlexiCellRenderer(getLocale())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CatalogRepositoryEntryCols.lifecycleEnd, new DateFlexiCellRenderer(getLocale())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CatalogRepositoryEntryCols.location));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, CatalogRepositoryEntryCols.educationalType, new EducationalTypeRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CatalogRepositoryEntryCols.offers, new ACRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CatalogRepositoryEntryCols.detailsSmall));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CatalogRepositoryEntryCols.startSmall));

		dataModel = new CatalogRepositoryEntryDataModel(dataSource, columnsModel);
		tableEl = uifactory.addTableElement(getWindowControl(), "table", dataModel, 20, false, getTranslator(), formLayout);
		tableEl.setAvailableRendererTypes(FlexiTableRendererType.custom, FlexiTableRendererType.classic);
		tableEl.setRendererType(FlexiTableRendererType.custom);
		tableEl.setSearchEnabled(withSearch);
		tableEl.setCustomizeColumns(true);
		tableEl.setEmptyTableSettings("table.search.empty", "table.search.empty.hint", "o_CourseModule_icon");
		tableEl.setElementCssClass("o_coursetable");
		// Is (more or less) the same visualization as row_1.html
		VelocityContainer row = createVelocityContainer("catalog_repository_entry_row");
		row.setDomReplacementWrapperRequired(false);
		tableEl.setRowRenderer(row, this);
		
		initFilters();
		tableEl.setAndLoadPersistedPreferences(ureq, "catalog-v2-relist");
	}
	
	private void initFilters() {
		CatalogFilterSearchParams filterSearchParams = new CatalogFilterSearchParams();
		filterSearchParams.setEnabled(Boolean.TRUE);
		List<CatalogFilter> catalogFilters = catalogService.getCatalogFilters(filterSearchParams);
		List<FlexiTableExtendedFilter> flexiTableFilters = new ArrayList<>(catalogFilters.size());
		for (CatalogFilter catalogFilter : catalogFilters) {
			CatalogFilterHandler handler = catalogService.getCatalogFilterHandler(catalogFilter.getType());
			if (handler != null && handler.isEnabled(searchParams.isGuestOnly())) {
				FlexiTableExtendedFilter flexiTableFilter = handler.createFlexiTableFilter(getTranslator(), searchParams, catalogFilter);
				if (flexiTableFilter != null) {
					flexiTableFilters.add(flexiTableFilter);
				}
			}
		}
		if (!flexiTableFilters.isEmpty()) {
			tableEl.setFilters(true, flexiTableFilters, false, false);
			tableEl.expandFilters(true);
		}
	}

	@Override
	public void forgeSelectLink(CatalogRepositoryEntryRow row) {
		String displayName = StringHelper.escapeHtml(row.getDisplayName());
		FormLink selectLink = uifactory.addFormLink("select_" + row.getKey(), "select", displayName, null, null, Link.NONTRANSLATED);
		if(row.isClosed()) {
			selectLink.setIconLeftCSS("o_icon o_CourseModule_icon_closed");
		}
		if(row.isMember()) {
			String businessPath = "[RepositoryEntry:" + row.getKey() + "]";
			selectLink.setUrl(BusinessControlFactory.getInstance()
				.getAuthenticatedURLFromBusinessPathString(businessPath));
		}
		selectLink.setUserObject(row);
		row.setSelectLink(selectLink);
	}

	@Override
	public void forgeStartLink(CatalogRepositoryEntryRow row) {
		String businessPath = "[RepositoryEntry:" + row.getKey() + "]";
		String url = BusinessControlFactory.getInstance().getURLFromBusinessPathString(businessPath);
		
		String cmd = "start";
		String label = "start";
		String css = "btn btn-sm btn-primary o_start";
		String cssSmall = "btn btn-xs btn-primary o_start";
		if (searchParams.isWebPublish() && row.isGuestAccess()) {
			url += "?guest=true";
			
			ExternalLinkItem link = uifactory.addExternalLink("start_" + row.getKey(), url, "_self", null);
			link.setCssClass(css);
			link.setIconRightCSS("o_icon o_icon_start");
			link.setName(translate("start.guest"));
			row.setStartLink(link);
			
			ExternalLinkItem linkSmall = uifactory.addExternalLink("starts_" + row.getKey(), url, "_self", null);
			linkSmall.setCssClass(cssSmall);
			linkSmall.setIconRightCSS("o_icon o_icon_start");
			linkSmall.setName(translate("start.guest"));
			row.setStartSmallLink(linkSmall);
			return;
		}
		
		if (!row.isMember() && row.isPublicVisible() && !row.isOpenAccess() && row.getAccessTypes() != null && !row.getAccessTypes().isEmpty()) {
			cmd = "book";
			label = "book";
			css = "btn btn-sm btn-primary o_book";
			cssSmall = "btn btn-xs btn-primary o_book";
		}
		
		FormLink link = uifactory.addFormLink("start_" + row.getKey(), cmd, label, null, flc, Link.LINK);
		link.setUserObject(row);
		link.setCustomEnabledLinkCSS(css);
		link.setIconRightCSS("o_icon o_icon_start");
		link.setTitle(label);
		link.setUrl(url);
		row.setStartLink(link);
		
		FormLink linkSmall = uifactory.addFormLink("starts_" + row.getKey(), cmd, label, null, null, Link.LINK);
		linkSmall.setUserObject(row);
		linkSmall.setCustomEnabledLinkCSS(cssSmall);
		linkSmall.setIconRightCSS("o_icon o_icon_start");
		linkSmall.setTitle(label);
		linkSmall.setUrl(url);
		row.setStartSmallLink(linkSmall);
	}

	@Override
	public void forgeDetailsLink(CatalogRepositoryEntryRow row) {
		String url = CatalogBCFactory.get(searchParams.isWebPublish()).getInfosUrl(row::getKey);
		
		FormLink detailsLink = uifactory.addFormLink("details_" + row.getKey(), "details", "details", null, flc, Link.LINK);
		detailsLink.setIconRightCSS("o_icon o_icon_details");
		detailsLink.setCustomEnabledLinkCSS("btn btn-sm btn-default o_details");
		detailsLink.setTitle("details");
		detailsLink.setUrl(url);
		detailsLink.setUserObject(row);
		row.setDetailsLink(detailsLink);
		
		FormLink detailsSmallLink = uifactory.addFormLink("details_small_" + row.getKey(), "details", "details", null, null, Link.LINK);
		detailsSmallLink.setCustomEnabledLinkCSS("btn btn-xs btn-default o_details");
		detailsSmallLink.setTitle("details");
		detailsSmallLink.setUrl(url);
		detailsSmallLink.setUserObject(row);
		
		row.setDetailsSmallLink(detailsSmallLink);
	}
	
	@Override
	public void forgeThumbnail(CatalogRepositoryEntryRow row) {
		VFSLeaf image = repositoryManager.getImage(row.getKey(), row.getOlatResource());
		if (image != null) {
			row.setThumbnailRelPath(RepositoryEntryImageMapper.getImageUrl(mapperThumbnailKey.getUrl() , image));
		}
	}
	
	@Override
	public void forgeTaxonomyLevels(CatalogRepositoryEntryRow row) {
		List<TaxonomyLevelNamePath> taxonomyLevels = TaxonomyUIFactory.getNamePaths(getTranslator(), row.getTaxonomyLevels());
		row.setTaxonomyLevelNamePaths(taxonomyLevels);
	}

	private void setWindowTitle() {
		String windowTitle = taxonomyLevel != null
				? translate("window.title.taxonomy", TaxonomyUIFactory.translateDisplayName(getTranslator(), taxonomyLevel))
				: translate("window.title.main");
		getWindow().setTitle(windowTitle);
	}

	@Override
	public Iterable<Component> getComponents(int row, Object rowObject) {
		List<Component> cmps = null;
		if (rowObject instanceof CatalogRepositoryEntryRow catalogRow) {
			cmps = new ArrayList<>(2);
			if (catalogRow.getDetailsLink() != null) {
				cmps.add(catalogRow.getDetailsLink().getComponent());
			}
			if (catalogRow.getStartLink() != null) {
				cmps.add(catalogRow.getStartLink().getComponent());
			}
		}
		return cmps;
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if (entries == null || entries.isEmpty()) return;
		
		ContextEntry entry = entries.get(0);
		if (CatalogBCFactory.isInfosType(entry.getOLATResourceable())) {
			Long key = entry.getOLATResourceable().getResourceableId();
			tableEl.resetSearch(ureq);
			dataModel.clear();
			dataModel.load(null, null, 0, -1);
			CatalogRepositoryEntryRow row = dataModel.getObjectByKey(key);
			if (row != null) {
				int index = dataModel.getObjects().indexOf(row);
				if (index >= 1 && tableEl.getPageSize() > 1) {
					int page = index / tableEl.getPageSize();
					tableEl.setPage(page);
				}
				doOpenDetails(ureq, row.getKey());
				if (infosCtrl != null) {
					List<ContextEntry> subEntries = entries.subList(1, entries.size());
					infosCtrl.activate(ureq, subEntries, entries.get(0).getTransientState());
				}
			}
		}
	}

	public void search(UserRequest ureq, String searchString, boolean reset) {
		if (StringHelper.containsNonWhitespace(searchString)) {
			List<CatalogSearchTerm> searchTerms = catalogService.getSearchTerms(searchString, getLocale());
			searchParams.setSearchTerms(searchTerms);
		} else {
			searchParams.setSearchTerms(null);
		}
		if (reset) {
			tableEl.resetSearch(ureq);
		} else {
			tableEl.reset(true, true, true);
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == infosCtrl) {
			if (event instanceof BookedEvent bookedEvent) {
				Long repositoryEntryKey = bookedEvent.getRepositoryEntry().getKey();
				doBooked(ureq, repositoryEntryKey);
			}
		} else if (authCtrl == source) {
			lightboxCtrl.deactivate();
			cleanUp();
			if (event instanceof LoginProcessEvent) {
				LoginProcessController loginProcessEventCtrl = new LoginProcessController(ureq, getWindowControl(), stackPanel, null);
				if (event == LoginProcessEvent.REGISTER_EVENT) {
					loginProcessEventCtrl.doOpenRegistration(ureq);
				}
			}
		} else if (lightboxCtrl == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(authCtrl);
		removeAsListenerAndDispose(lightboxCtrl);
		authCtrl = null;
		lightboxCtrl = null;
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if ("ONCLICK".equals(event.getCommand())) {
			String key = ureq.getParameter("select");
			if (StringHelper.containsNonWhitespace(key) && StringHelper.isLong(key)) {
				doOpenDetails(ureq, Long.valueOf(key));
			}
			
			key = ureq.getParameter("select_taxonomy");
			if (StringHelper.containsNonWhitespace(key)) {
				fireEvent(ureq, new OpenTaxonomyEvent(
						Long.valueOf(key),
						searchParams.getIdentToEducationalTypeKeys().get(KEY_LAUNCHER),
						searchParams.getIdentToResourceTypes().get(KEY_LAUNCHER)));
				return;
			}
		} else if (source == stackPanel) {
			if (event instanceof PopEvent) {
				if (stackPanel.getLastController() == this) {
					setWindowTitle();
				}
			}
		}
		super.event(ureq, source, event);
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source instanceof FormLink link) {
			String cmd = link.getCmd();
			if ("start".equals(cmd)){
				CatalogRepositoryEntryRow row = (CatalogRepositoryEntryRow)link.getUserObject();
				doStart(ureq, row.getKey());
			} else if ("startGuest".equals(cmd)){
				CatalogRepositoryEntryRow row = (CatalogRepositoryEntryRow)link.getUserObject();
				doStartGuest(ureq, row.getKey());
			} else if ("book".equals(cmd)){
				CatalogRepositoryEntryRow row = (CatalogRepositoryEntryRow)link.getUserObject();
				doBook(ureq, row);
			} else if ("details".equals(cmd) || "select".equals(cmd)){
				CatalogRepositoryEntryRow row = (CatalogRepositoryEntryRow)link.getUserObject();
				doOpenDetails(ureq, row.getKey());
			} else if ("select_tax".equals(cmd)){
				Long key = (Long)link.getUserObject();
				fireEvent(ureq, new OpenTaxonomyEvent(
						Long.valueOf(key),
						searchParams.getIdentToEducationalTypeKeys().get(KEY_LAUNCHER),
						searchParams.getIdentToResourceTypes().get(KEY_LAUNCHER)));
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	public synchronized void dispose() {
		if (stackPanel != null) {
			stackPanel.removeListener(this);
		}
		super.dispose();
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	private void doStart(UserRequest ureq, Long repositoryEntryKey) {
		if (searchParams.isWebPublish()) {
			doLogin(ureq, repositoryEntryKey);
		} else {
			try {
				String businessPath = "[RepositoryEntry:" + repositoryEntryKey + "]";
				NewControllerFactory.getInstance().launch(businessPath, ureq, getWindowControl());
			} catch (CorruptedCourseException e) {
				showError("error.corrupted");
			}
		}
	}
	
	private void doStartGuest(UserRequest ureq, Long key) {
		String businessPath = "[RepositoryEntry:" + key + "]";
		String url = BusinessControlFactory.getInstance().getAuthenticatedURLFromBusinessPathString(businessPath) + "?guest=true";
		DispatcherModule.redirectTo(ureq.getHttpResp(), url);
		
	}
	
	protected void doOpenDetails(UserRequest ureq, Long repositoryEntryKey) {
		RepositoryEntry entry = repositoryManager.lookupRepositoryEntry(repositoryEntryKey);
		doOpenDetails(ureq, entry);
	}

	private void doOpenDetails(UserRequest ureq, RepositoryEntry entry) {
		if (entry != null) {
			removeAsListenerAndDispose(infosCtrl);
			OLATResourceable ores = CatalogBCFactory.createInfosOres(entry);
			WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(ores, null, getWindowControl());
			
			infosCtrl = new CatalogRepositoryEntryInfosController(ureq, bwControl, stackPanel, entry);
			listenTo(infosCtrl);
			addToHistory(ureq, infosCtrl);
			
			String displayName = entry.getDisplayname();
			stackPanel.pushController(displayName, infosCtrl);
			
			String windowTitle = translate("window.title.infos", entry.getDisplayname());
			getWindow().setTitle(windowTitle);
		} else {
			tableEl.reloadData();
		}
	}

	private void doBook(UserRequest ureq, CatalogRepositoryEntryRow row) {
		RepositoryEntry entry = repositoryManager.lookupRepositoryEntry(row.getKey());
		if (entry != null) {
			if (searchParams.isWebPublish()) {
				doLogin(ureq, row.getKey());
			} else {
				AccessResult acResult = acService.isAccessible(entry, getIdentity(), row.isMember(), searchParams.isGuestOnly(), null, false);
				if (acResult.isAccessible() || acService.tryAutoBooking(getIdentity(), entry, acResult)) {
					doStart(ureq, row.getKey());
				} else {
					doOpenDetails(ureq, row.getKey());
					if (infosCtrl != null) {
						OLATResourceable ores = OresHelper.createOLATResourceableType("Offers");
						ContextEntry contextEntry = BusinessControlFactory.getInstance().createContextEntry(ores);
						infosCtrl.activate(ureq, List.of(contextEntry), null);
					}
				}
			}
		} else {
			tableEl.reloadData();
		}
	}

	private void doLogin(UserRequest ureq, Long repositoryEntryKey) {
		if (guardModalController(authCtrl)) return;
		RepositoryEntry entry = repositoryManager.lookupRepositoryEntry(repositoryEntryKey);
		authCtrl = new WebCatalogAuthController(ureq, getWindowControl(), entry);
		listenTo(authCtrl);
		
		lightboxCtrl = new LightboxController(ureq, getWindowControl(), authCtrl);
		listenTo(lightboxCtrl);
		lightboxCtrl.activate();
	}

	private void doBooked(UserRequest ureq, Long repositoryEntryKey) {
		stackPanel.popUpToController(this);
		RepositoryEntry entry = repositoryManager.lookupRepositoryEntry(repositoryEntryKey);
		if (entry != null) {
			doOpenDetails(ureq, entry);
			if (repositoryManager.isAllowed(ureq, entry).canLaunch()) {
				doStart(ureq, entry.getKey());
			}
		}
	}
	
	public static final class TaxonomyItem {
		
		private final Long key;
		private final String displayName;
		private final String selectLinkName;
		private String thumbnailRelPath;
		
		public TaxonomyItem(Long key, String displayName, String selectLinkName) {
			this.key = key;
			this.displayName = displayName;
			this.selectLinkName = selectLinkName;
		}

		public Long getKey() {
			return key;
		}

		public String getDisplayName() {
			return displayName;
		}

		public String getSelectLinkName() {
			return selectLinkName;
		}

		public String getThumbnailRelPath() {
			return thumbnailRelPath;
		}

		public void setThumbnailRelPath(String thumbnailRelPath) {
			this.thumbnailRelPath = thumbnailRelPath;
		}
		
	}

}
