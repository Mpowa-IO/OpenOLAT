package org.olat.course.archiver.webdav;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.olat.basesecurity.IdentityRef;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.services.export.ExportManager;
import org.olat.core.commons.services.export.model.ExportInfos;
import org.olat.core.commons.services.export.model.SearchExportMetadataParameters;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.vfs.MergeSource;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSManager;
import org.olat.core.util.vfs.filters.VFSItemFilter;

/**
 * 
 * Initial date: 26 févr. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class MyArchivesWebDAVSource extends MergeSource {

	private static final Logger log = Tracing.createLoggerFor(CourseArchiveWebDAVSource.class);
	
	private List<VFSItem> exports;
	private boolean initialized = false;
	private final IdentityRef identity;
	
	public MyArchivesWebDAVSource(IdentityRef identity) {
		super(null, "mycoursearchives");
		this.identity = identity;
	}
	
	public boolean isEmpty() {
		if(!initialized) {
			init();
		}
		return exports == null || exports.isEmpty();
	}

	@Override
	public VFSItem resolve(String path) {
		if(!initialized) {
			init();
		}
		
		VFSItem item = super.resolve(path);
		if(item == null) {
			path = VFSManager.sanitizePath(path);
			String childName = VFSManager.extractChild(path);
			for(VFSItem export:exports) {
				if(export.getName().equals(childName)) {
					item = export;
					break;
				}
			}
		}
		return item;
	}

	@Override
	public List<VFSItem> getItems(VFSItemFilter filter) {
		if(!initialized) {
			init();
		}
		return exports;
	}
	
	@Override
	protected void init() {
		if(!initialized) {
			exports = getExports();
			initialized = true;
		}
		super.init();
	}
	
	private List<VFSItem> getExports() {
		try {
			ExportManager exportManager = CoreSpringFactory.getImpl(ExportManager.class);
			SearchExportMetadataParameters params = new SearchExportMetadataParameters();
			params.setCreator(identity);
			List<ExportInfos> exportsList = exportManager.getResultsExport(params);
			List<VFSItem> items = new ArrayList<>(exportsList.size());
			for(ExportInfos export:exportsList) {
				if(export.isNew() || export.isRunning() || export.isCancelled()) {
					continue;
				}
				
				if(export.getExportMetadata() != null
						&& StringHelper.containsNonWhitespace(export.getExportMetadata().getFilePath())) {
					VFSLeaf leaf = VFSManager.olatRootLeaf(export.getExportMetadata().getFilePath());
					items.add(leaf);
				}
			}
			return items;
		} catch (Exception e) {
			log.error("", e);
			return new ArrayList<>();
		}
	}
}