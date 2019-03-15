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
package org.olat.core.commons.services.vfs.manager;

import java.util.Date;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.commons.services.vfs.manager.VFSMetadataDAO;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 11 mars 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class VFSMetadataDAOTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private VFSMetadataDAO vfsMetadataDao;
	
	@Test
	public void createMetadata() {
		String uuid = UUID.randomUUID().toString();
		String relativePath = "/bcroot/hello/world/";
		String filename = "image.jpg";
		String uri = "file:///Users/frentix/Documents/bcroot/hello/world/image.jpg";
		String uriProtocol = "file";
		VFSMetadata metadata = vfsMetadataDao.createMetadata(uuid, relativePath, filename, new Date(), 10l, false, uri, uriProtocol, null);
		dbInstance.commitAndCloseSession();
		
		Assert.assertNotNull(metadata);
		Assert.assertNotNull(metadata.getKey());
		Assert.assertNotNull(metadata.getCreationDate());
		Assert.assertNotNull(metadata.getLastModified());
		Assert.assertEquals(uuid, metadata.getUuid());
		Assert.assertEquals(relativePath, metadata.getRelativePath());
		Assert.assertEquals(filename, metadata.getFilename());
		Assert.assertFalse(metadata.isDirectory());
		Assert.assertEquals(uri, metadata.getUri());
		Assert.assertEquals(uriProtocol, metadata.getProtocol());
	}
	
	@Test
	public void getMetadata_uuid() {
		String uuid = UUID.randomUUID().toString();
		String relativePath = "/bcroot/hello/world/";
		String filename = "image.jpg";
		String uri = "file:///Users/frentix/Documents/bcroot/hello/world/image.jpg";
		String uriProtocol = "file";
		VFSMetadata metadata = vfsMetadataDao.createMetadata(uuid, relativePath, filename, new Date(), 15l, false, uri, uriProtocol, null);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(metadata);
		
		VFSMetadata loadedMetadata = vfsMetadataDao.getMetadata(uuid);
		Assert.assertEquals(metadata, loadedMetadata);
		Assert.assertEquals(metadata.getKey(), loadedMetadata.getKey());
		Assert.assertEquals(uuid, metadata.getUuid());
		Assert.assertEquals(relativePath, metadata.getRelativePath());
		Assert.assertEquals(filename, metadata.getFilename());
		Assert.assertFalse(metadata.isDirectory());
		Assert.assertEquals(uri, metadata.getUri());
		Assert.assertEquals(uriProtocol, metadata.getProtocol());
	}
	
	@Test
	public void getMetadata_path() {
		String uuid = UUID.randomUUID().toString();
		String relativePath = "/bcroot/hello/world/";
		String filename = uuid + ".jpg";
		String uri = "file:///Users/frentix/Documents/bcroot/hello/world/image.jpg";
		String uriProtocol = "file";
		VFSMetadata metadata = vfsMetadataDao.createMetadata(uuid, relativePath, filename, new Date(), 18l, false, uri, uriProtocol, null);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(metadata);
		
		VFSMetadata loadedMetadata = vfsMetadataDao.getMetadata(relativePath, filename, false);
		Assert.assertEquals(metadata, loadedMetadata);
		Assert.assertEquals(metadata.getKey(), loadedMetadata.getKey());
		Assert.assertEquals(uuid, metadata.getUuid());
		Assert.assertEquals(relativePath, metadata.getRelativePath());
		Assert.assertEquals(filename, metadata.getFilename());
		Assert.assertEquals(18l, metadata.getFileSize());
		Assert.assertFalse(metadata.isDirectory());
		Assert.assertEquals(uri, metadata.getUri());
		Assert.assertEquals(uriProtocol, metadata.getProtocol());
	}

}
