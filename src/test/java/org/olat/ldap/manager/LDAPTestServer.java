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
package org.olat.ldap.manager;

import org.apache.logging.log4j.Logger;
import org.junit.ClassRule;
import org.junit.Test;
import org.olat.core.logging.Tracing;
import org.zapodot.junit.ldap.EmbeddedLdapRule;
import org.zapodot.junit.ldap.EmbeddedLdapRuleBuilder;

/**
 * Small LDAP Server with the same data as used by the unit tests:
 * 
 * ldap.ldapUrl=ldap://localhost:1389
 * ldap.ldapSystemDN=uid=test,ou=person,dc=olattest,dc=org
 * ldap.ldapSystemPW=olattest
 * ldap.ldapBases=ou=person,dc=olattest,dc=org
 * ldap.ldapGroupBases=ou=groups,dc=olattest,dc=org
 * 
 * Initial date: 4 oct. 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class LDAPTestServer {

	private static final Logger log = Tracing.createLoggerFor(LDAPTestServer.class);
	
	@ClassRule
	public static final EmbeddedLdapRule embeddedLdapRule = EmbeddedLdapRuleBuilder
	        .newInstance()
	        .usingDomainDsn("dc=olattest,dc=org")
	        .importingLdifs("org/olat/ldap/junittestdata/olattest.ldif")
	        .bindingToAddress("localhost")
	        .bindingToPort(1389)
	        .build();
	
	@Test
	public void startServer() {
		try {
			Thread.sleep(3600 * 1000);
		} catch (InterruptedException e) {
			log.info("I'm done. Goob bye");
		}
	}
}
