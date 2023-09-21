package com.sinnerschrader.skillwill.job;

import com.sinnerschrader.skillwill.repository.UserRepository;
import com.sinnerschrader.skillwill.service.LdapService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * Scheduled runner syncing all users with the LDAP
 *
 * @author torree
 */
@Service
@EnableScheduling
public class LdapSyncJob {

  private static final Logger logger = LoggerFactory.getLogger(LdapSyncJob.class);

  @Autowired
  private LdapService ldapService;

  @Autowired
  private UserRepository UserRepository;

  @Scheduled(cron = "${ldapSyncCron}")
  public void run() {
    logger.info("Starting regular LDAP sync, this may take a while");
    ldapService.syncUsers(UserRepository.findAll(), true);
    logger.info("Finished regular LDAP sync");
  }

}
