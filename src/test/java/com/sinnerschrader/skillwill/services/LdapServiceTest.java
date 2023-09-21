package com.sinnerschrader.skillwill.services;

//import static org.junit.Assert.*;
//
//import com.sinnerschrader.skillwill.domain.user.Role;
//import com.sinnerschrader.skillwill.domain.user.UserDetailsImpl;
//import com.sinnerschrader.skillwill.repository.UserRepository;
//import java.util.Optional;
//import org.junit.Before;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

//@RunWith(SpringJUnit4ClassRunner.class)
//@SpringBootTest
public class LdapServiceTest {

//  @Autowired
//  private LdapService ldapService;
//
//  @Autowired
//  private UserRepository userRepo;
//
//  @Before
//  public void setup() {
//    userRepo.deleteAll();
//    userRepo.insert(new UserDetailsImpl("aaaaaa"));
//    userRepo.insert(new UserDetailsImpl("bbbbbb"));
//  }
//
//  @Test
//  public void createUserByMail() {
//    var user = ldapService.createUserByMail("aaa.aaa@example.com");
//    assertEquals("aaaaaa", user.getUsername());
//    assertEquals(Role.USER, user.getLdapDetails().getRole());
//  }
//
//  @Test
//  public void createrAdminByMail() {
//    assertEquals(Role.ADMIN, ldapService.createUserByMail("bbb.bbb@example.com").getLdapDetails().getRole());
//  }
//
//  @Test
//  public void createAdminByInvalidMail() {
//    assertNull(ldapService.createUserByMail("lorem@ipsum.net"));
//  }
//
//  @Test
//  public void syncUser() {
//    var userA = userRepo.findById("aaaaaa").get();
//    assertNull(userA.getLdapDetails());
//    ldapService.syncUser(userA);
//    assertEquals("aaa.aaa@example.com", userRepo.findById("aaaaaa").get().getLdapDetails().getMail());
//  }
//
//  @Test
//  public void syncUsers() {
//    assertNull(userRepo.findById("aaaaaa").get().getLdapDetails());
//    assertNull(userRepo.findById("bbbbbb").get().getLdapDetails());
//
//    ldapService.syncUsers(userRepo.findAll(), true);
//
//    assertEquals("aaa.aaa@example.com", userRepo.findById("aaaaaa").get().getLdapDetails().getMail());
//    assertEquals("bbb.bbb@example.com", userRepo.findById("bbbbbb").get().getLdapDetails().getMail());
//  }
//
//  @Test
//  public void syncUsersRemovesMissing() {
//    userRepo.insert(new UserDetailsImpl("cccccc"));
//    var allUsers = userRepo.findAll();
//    assertEquals(3, allUsers.size());
//    ldapService.syncUsers(allUsers, true);
//    assertFalse(userRepo.findById("cccccc").isPresent());
//
//  }

}
