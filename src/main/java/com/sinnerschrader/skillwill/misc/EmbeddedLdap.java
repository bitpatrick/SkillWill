package com.sinnerschrader.skillwill.misc;

import com.unboundid.ldap.listener.InMemoryDirectoryServer;
import com.unboundid.ldap.listener.InMemoryDirectoryServerConfig;
import com.unboundid.ldap.listener.InMemoryListenerConfig;
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.ldif.LDIFReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;


/**
 * Embedded LDAP used for integration testing
 *
 * @author torree
 */
//@Service
public class EmbeddedLdap {

  private static final Logger logger = LoggerFactory.getLogger(EmbeddedLdap.class);

  private InMemoryDirectoryServer dirServer = null;

  public void startup() throws LDAPException {
    logger.warn("Starting embedded LDAP");

    var serverconfig = new InMemoryDirectoryServerConfig("dc=example,dc=com");
    serverconfig.setListenerConfigs(InMemoryListenerConfig.createLDAPConfig(
        "default",
        InetAddress.getLoopbackAddress(), 1338, null
    ));
    serverconfig.setSchema(null);
    dirServer = new InMemoryDirectoryServer(serverconfig);
    dirServer.startListening();
    reset();
  }

  public void reset() throws LDAPException {
    if (dirServer == null) {
      startup();
    }

    logger.warn("Resetting embedded LDAP");
    var ldifInputStream = getClass().getResourceAsStream("/testdata.ldif");
    dirServer.importFromLDIF(true, new LDIFReader(ldifInputStream));
  }

}
