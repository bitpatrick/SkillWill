package com.sinnerschrader.skillwill.controllers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.sinnerschrader.skillwill.domain.user.User;
import com.sinnerschrader.skillwill.domain.user.Role;
import com.sinnerschrader.skillwill.domain.skills.KnownSkill;
import com.sinnerschrader.skillwill.misc.EmbeddedLdap;
import com.sinnerschrader.skillwill.repositories.userRepository;
import com.sinnerschrader.skillwill.repositories.SessionRepository;
import com.sinnerschrader.skillwill.repositories.SkillRepository;
import com.sinnerschrader.skillwill.services.LdapService;
import com.sinnerschrader.skillwill.session.Session;
import com.unboundid.ldap.sdk.LDAPException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Integration test for UserController
 *
 * @author torree
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class UserControllerTest {

  @Autowired
  private UserController userController;

  @Autowired
  private userRepository personRepo;

  @Autowired
  private SkillRepository skillRepo;

  @Autowired
  private SessionRepository sessionRepo;

  @Autowired
  private EmbeddedLdap embeddedLdap;

  @Autowired
  private LdapService ldapService;

  @Before
  public void setUp() throws LDAPException, IOException {
    embeddedLdap.reset();
    skillRepo.deleteAll();
    personRepo.deleteAll();
    sessionRepo.deleteAll();

    skillRepo.insert(new KnownSkill("Java"));
    skillRepo.insert(new KnownSkill("hidden", new ArrayList<>(), true, new HashSet<>()));

    User userUser = new User("aaaaaa");
    userUser.addUpdateSkill("Java", 2, 3, false, false);
    userUser.addUpdateSkill("hidden", 0, 1, true, false);
    personRepo.insert(userUser);

    User adminUser = new User("bbbbbb");
    adminUser.setRole(Role.ADMIN);
    personRepo.insert(adminUser);

    ldapService.syncUsers(personRepo.findAll(), true);

    Session userSession = new Session("YWFhLmFhYUBzaW5uZXJzY2hyYWRlci5jb20=|foo|bar");
    sessionRepo.insert(userSession);

    Session adminSession = new Session("YmJiLmJiYkBzaW5uZXJzY2hyYWRlci5jb20=|foo|bar");
    sessionRepo.insert(adminSession);
  }

  @Test
  public void testGetUserValid() throws JSONException {
    ResponseEntity<String> res = userController.getUser("aaaaaa");
    assertEquals(HttpStatus.OK, res.getStatusCode());

    assertTrue(new JSONObject(res.getBody()).has("id"));
    assertTrue(new JSONObject(res.getBody()).get("id").equals("aaaaaa"));

    assertTrue(new JSONObject(res.getBody()).has("firstName"));
    assertTrue(new JSONObject(res.getBody()).get("firstName").equals("Fooberius"));

    assertTrue(new JSONObject(res.getBody()).has("lastName"));
    assertTrue(new JSONObject(res.getBody()).get("lastName").equals("Barblub"));
  }

  @Test
  public void testGetUserInvalid() {
    assertEquals(HttpStatus.NOT_FOUND, userController.getUser("barfoo").getStatusCode());
  }

  @Test
  public void testGetUsersValid() throws JSONException {
    ResponseEntity<String> res = userController.getUsers("Java", "Hamburg");
    assertEquals(HttpStatus.OK, res.getStatusCode());
    assertTrue(new JSONObject(res.getBody()).has("searched"));
    assertEquals("Java", new JSONObject(res.getBody()).getJSONArray("searched").getJSONObject(0).getString("found"));
    assertEquals(1, new JSONObject(res.getBody()).getJSONArray("results").length());
    assertEquals("aaaaaa", new JSONObject(res.getBody()).getJSONArray("results").getJSONObject(0).getString("id"));
    assertTrue(new JSONObject(res.getBody()).getJSONArray("results").getJSONObject(0).has("fitness"));
  }

  @Test
  public void testGetUsersHideHidden() throws JSONException {
    ResponseEntity<String> res = userController.getUsers("Java", "");
    JSONArray skillJson = new JSONObject(res.getBody()).getJSONArray("results").getJSONObject(0).getJSONArray("skills");
    assertEquals(1, skillJson.length());
    assertEquals("Java", skillJson.getJSONObject(0).getString("name"));
  }

  @Test
  public void testGetUsersSkillsEmpty() throws JSONException {
    ResponseEntity<String> res = userController.getUsers("", "Hamburg");
    assertEquals(HttpStatus.OK, res.getStatusCode());
    assertEquals("aaaaaa", new JSONObject(res.getBody()).getJSONArray("results").getJSONObject(0).getString("id"));
    assertFalse(new JSONObject(res.getBody()).getJSONArray("results").getJSONObject(0).has("fitness"));
    assertTrue(new JSONObject(res.getBody()).has("searched"));
    assertEquals(2, new JSONObject(res.getBody()).getJSONArray("results").length());
    assertEquals("aaaaaa", new JSONObject(res.getBody()).getJSONArray("results").getJSONObject(0).getString("id"));
    assertFalse(new JSONObject(res.getBody()).getJSONArray("results").getJSONObject(0).has("fitness"));
  }

  @Test
  public void testGetUsersSkillsNull() throws JSONException {
    ResponseEntity<String> res = userController.getUsers(null, "Hamburg");
    assertEquals(HttpStatus.OK, res.getStatusCode());
    assertTrue(new JSONObject(res.getBody()).has("searched"));
    assertEquals(2, new JSONObject(res.getBody()).getJSONArray("results").length());
    assertEquals("aaaaaa", new JSONObject(res.getBody()).getJSONArray("results").getJSONObject(0).getString("id"));
    assertFalse(new JSONObject(res.getBody()).getJSONArray("results").getJSONObject(0).has("fitness"));
  }

  @Test
  public void testGetUsersSkillUnknownOnly() throws JSONException {
    ResponseEntity<String> res = userController.getUsers("Unknown, More unknown", "");
    assertEquals(HttpStatus.OK, res.getStatusCode());
    assertTrue(new JSONObject(res.getBody()).has("searched"));
    assertEquals(0, new JSONObject(res.getBody()).getJSONArray("searched").length());
    assertTrue(new JSONObject(res.getBody()).has("results"));
    assertEquals(0, new JSONObject(res.getBody()).getJSONArray("results").length());
  }

  @Test
  public void testGetUsersSkillUnknown() throws JSONException {
    ResponseEntity<String> res = userController.getUsers("Unknown,Java", "");
    assertEquals(HttpStatus.OK, res.getStatusCode());
    assertTrue(new JSONObject(res.getBody()).has("searched"));
    assertEquals(1, new JSONObject(res.getBody()).getJSONArray("searched").length());
    assertEquals("Java", new JSONObject(res.getBody()).getJSONArray("searched").getJSONObject(0).getString("found"));
    assertTrue(new JSONObject(res.getBody()).has("results"));
    assertEquals(1, new JSONObject(res.getBody()).getJSONArray("results").length());
    assertEquals("aaaaaa", new JSONObject(res.getBody()).getJSONArray("results").getJSONObject(0).getString("id"));
  }

  @Test
  public void testGetUsersNoFitnessInEmptySearch() throws JSONException {
    ResponseEntity<String> res = userController.getUsers("", "Hamburg");
    assertFalse(new JSONObject(res.getBody()).getJSONArray("results").getJSONObject(0).has("fitness"));
  }

  @Test
  public void testGetUsersLocationEmpty() throws JSONException {
    ResponseEntity<String> res = userController.getUsers("Java", "");
    assertEquals(HttpStatus.OK, res.getStatusCode());
    assertTrue(new JSONObject(res.getBody()).has("searched"));
    assertEquals("Java", new JSONObject(res.getBody()).getJSONArray("searched").getJSONObject(0).getString("found"));
    assertEquals(1, new JSONObject(res.getBody()).getJSONArray("results").length());
    assertEquals("aaaaaa", new JSONObject(res.getBody()).getJSONArray("results").getJSONObject(0).getString("id"));
    assertTrue(new JSONObject(res.getBody()).getJSONArray("results").getJSONObject(0).has("fitness"));
  }

  @Test
  public void testGetUsersSkillsEmptyLocationEmpty() throws JSONException {
    ResponseEntity<String> res = userController.getUsers("", "");
    assertEquals(HttpStatus.OK, res.getStatusCode());
    assertTrue(new JSONObject(res.getBody()).has("searched"));
    assertEquals(2, new JSONObject(res.getBody()).getJSONArray("results").length());
    assertEquals("aaaaaa", new JSONObject(res.getBody()).getJSONArray("results").getJSONObject(0).getString("id"));
    assertFalse(new JSONObject(res.getBody()).getJSONArray("results").getJSONObject(0).has("fitness"));
  }

  @Test
  public void testGetUsersIgnoreSkillCase() throws JSONException {
    ResponseEntity<String> res = userController.getUsers("JaVa", "Hamburg");
    assertEquals(HttpStatus.OK, res.getStatusCode());
    assertTrue(new JSONObject(res.getBody()).has("searched"));
    assertEquals("Java", new JSONObject(res.getBody()).getJSONArray("searched").getJSONObject(0).getString("found"));
    assertEquals(1, new JSONObject(res.getBody()).getJSONArray("results").length());
    assertEquals("aaaaaa", new JSONObject(res.getBody()).getJSONArray("results").getJSONObject(0).getString("id"));
    assertTrue(new JSONObject(res.getBody()).getJSONArray("results").getJSONObject(0).has("fitness"));
  }

  @Test
  public void testGetUsersIgnoreNonAlphanumerics() throws JSONException {
    ResponseEntity<String> res = userController.getUsers("j#a)_V®a", "Hamburg");
    assertEquals(HttpStatus.OK, res.getStatusCode());
    assertTrue(new JSONObject(res.getBody()).has("searched"));
    assertEquals("Java", new JSONObject(res.getBody()).getJSONArray("searched").getJSONObject(0).getString("found"));
    assertEquals(1, new JSONObject(res.getBody()).getJSONArray("results").length());
    assertEquals("aaaaaa", new JSONObject(res.getBody()).getJSONArray("results").getJSONObject(0).getString("id"));
    assertTrue(new JSONObject(res.getBody()).getJSONArray("results").getJSONObject(0).has("fitness"));
  }

  @Test
  public void testGetUsersLocationUnknown() throws JSONException {
    ResponseEntity<String> res = userController.getUsers("Java", "IAmUnknown");
    assertEquals(HttpStatus.OK, res.getStatusCode());
    assertEquals(0, new JSONObject(res.getBody()).getJSONArray("results").length());
  }

  @Test
  public void testModifySkillsValid() {
    ResponseEntity<String> res = userController.updateSkills("aaaaaa", "Java", "3", "0", false, "YWFhLmFhYUBzaW5uZXJzY2hyYWRlci5jb20=|foo|bar");
    assertEquals(HttpStatus.OK, res.getStatusCode());
    assertEquals(3, personRepo.findByIdIgnoreCase("aaaaaa").getSkillsExcludeHidden().get(0).getSkillLevel());
    assertEquals(0, personRepo.findByIdIgnoreCase("aaaaaa").getSkillsExcludeHidden().get(0).getWillLevel());
  }

  @Test
  public void testModifySkillsLevelsZero() {
    ResponseEntity<String> res = userController.updateSkills("aaaaaa", "Java", "0", "0", false, "YWFhLmFhYUBzaW5uZXJzY2hyYWRlci5jb20=|foo|bar");
    assertEquals(HttpStatus.BAD_REQUEST, res.getStatusCode());
  }

  @Test
  public void testModifySkillsLevelOverMax() {
    ResponseEntity<String> res = userController.updateSkills("aaaaaa", "Java", "0", "4", false, "YWFhLmFhYUBzaW5uZXJzY2hyYWRlci5jb20=|foo|bar");
    assertEquals(HttpStatus.BAD_REQUEST, res.getStatusCode());
  }

  @Test
  public void testModifySkillsSessionInvalid() {
    ResponseEntity<String> res = userController.updateSkills("aaaaaa", "Java", "0", "0", false, "InvalidSession");
    assertEquals(HttpStatus.FORBIDDEN, res.getStatusCode());
    assertEquals(2, personRepo.findByIdIgnoreCase("aaaaaa").getSkillsExcludeHidden().get(0).getSkillLevel());
    assertEquals(3, personRepo.findByIdIgnoreCase("aaaaaa").getSkillsExcludeHidden().get(0).getWillLevel());
  }

  @Test
  public void testModifySkillsUserUnknown() {
    sessionRepo.deleteAll();
    Session session = new Session("2342");
    sessionRepo.insert(session);

    ResponseEntity<String> res = userController.updateSkills("IAmUnknown", "Java", "0", "0", false, "2342");
    assertEquals(HttpStatus.FORBIDDEN, res.getStatusCode());
    assertEquals(2, personRepo.findByIdIgnoreCase("aaaaaa").getSkillsExcludeHidden().get(0).getSkillLevel());
    assertEquals(3, personRepo.findByIdIgnoreCase("aaaaaa").getSkillsExcludeHidden().get(0).getWillLevel());
  }

  @Test
  public void testModifySkillsSkillUnknown() {
    ResponseEntity<String> res = userController.updateSkills("aaaaaa", "UnknownSkill", "0", "0", false, "YWFhLmFhYUBzaW5uZXJzY2hyYWRlci5jb20=|foo|bar");
    assertEquals(HttpStatus.BAD_REQUEST, res.getStatusCode());
    assertEquals(2, personRepo.findByIdIgnoreCase("aaaaaa").getSkillsExcludeHidden().get(0).getSkillLevel());
    assertEquals(3, personRepo.findByIdIgnoreCase("aaaaaa").getSkillsExcludeHidden().get(0).getWillLevel());
  }

  @Test
  public void testModifySkillsSkillLevelOutOfRange() {
    ResponseEntity<String> res = userController.updateSkills("aaaaaa", "Java", "5", "0", false, "YWFhLmFhYUBzaW5uZXJzY2hyYWRlci5jb20=|foo|bar");
    assertEquals(HttpStatus.BAD_REQUEST, res.getStatusCode());
    assertEquals(2, personRepo.findByIdIgnoreCase("aaaaaa").getSkillsExcludeHidden().get(0).getSkillLevel());
    assertEquals(3, personRepo.findByIdIgnoreCase("aaaaaa").getSkillsExcludeHidden().get(0).getWillLevel());
  }

  @Test
  public void testModifySkillsWillLevelOutOfRange() {
    ResponseEntity<String> res = userController.updateSkills("aaaaaa", "Java", "0", "5", false, "YWFhLmFhYUBzaW5uZXJzY2hyYWRlci5jb20=|foo|bar");
    assertEquals(HttpStatus.BAD_REQUEST, res.getStatusCode());
    assertEquals(2, personRepo.findByIdIgnoreCase("aaaaaa").getSkillsExcludeHidden().get(0).getSkillLevel());
    assertEquals(3, personRepo.findByIdIgnoreCase("aaaaaa").getSkillsExcludeHidden().get(0).getWillLevel());
  }

  @Test
  public void testModifySkillsHidden() {
    assertEquals(HttpStatus.BAD_REQUEST, userController.updateSkills("aaaaaa", "hidden", "0", "3", false, "YWFhLmFhYUBzaW5uZXJzY2hyYWRlci5jb20=|foo|bar").getStatusCode());
    assertNull(personRepo.findByIdIgnoreCase("aaaaaa").getSkillExcludeHidden("hidden"));
  }

  @Test
  public void testModifiyMentorExisitingSkill() throws JSONException {
    assertEquals("Java", new JSONObject(userController.getUser("aaaaaa").getBody())
      .getJSONArray("skills")
      .getJSONObject(0)
      .getString("name"));

    assertFalse(new JSONObject(userController.getUser("aaaaaa").getBody())
      .getJSONArray("skills")
      .getJSONObject(0)
      .getBoolean("mentor"));

    assertEquals(HttpStatus.OK, userController.updateSkills("aaaaaa", "Java", "3", "3", true, "YWFhLmFhYUBzaW5uZXJzY2hyYWRlci5jb20=|foo|bar").getStatusCode());

    assertTrue(new JSONObject(userController.getUser("aaaaaa").getBody())
      .getJSONArray("skills")
      .getJSONObject(0)
      .getBoolean("mentor"));
  }

  @Test
  public void testSetMentorNewSkill() throws JSONException {
    assertEquals(HttpStatus.OK, userController.removeSkill("aaaaaa", "Java", "YWFhLmFhYUBzaW5uZXJzY2hyYWRlci5jb20=|foo|bar").getStatusCode());

    assertEquals(0, new JSONObject(userController.getUser("aaaaaa").getBody())
      .getJSONArray("skills")
      .length());

    assertEquals(HttpStatus.OK, userController.updateSkills("aaaaaa", "Java", "3", "3", true, "YWFhLmFhYUBzaW5uZXJzY2hyYWRlci5jb20=|foo|bar").getStatusCode());

    assertTrue(new JSONObject(userController.getUser("aaaaaa").getBody())
      .getJSONArray("skills")
      .getJSONObject(0)
      .getBoolean("mentor"));
  }

  @Test
  public void testRemoveSkill() {
    ResponseEntity<String> res = userController.removeSkill("aaaaaa", "Java", "YWFhLmFhYUBzaW5uZXJzY2hyYWRlci5jb20=|foo|bar");
    assertEquals(HttpStatus.OK, res.getStatusCode());
  }

  @Test
  public void testRemoveSkillSkillUnknown() {
    ResponseEntity<String> res = userController.removeSkill("aaaaaa", "UNKNOWN", "YWFhLmFhYUBzaW5uZXJzY2hyYWRlci5jb20=|foo|bar");
    assertEquals(HttpStatus.BAD_REQUEST, res.getStatusCode());
  }

  @Test
  public void testRemoveSkillUserUnknown() {
    ResponseEntity<String> res = userController.removeSkill("IAmUnknown", "Java", "YWFhLmFhYUBzaW5uZXJzY2hyYWRlci5jb20=|foo|bar");
    assertEquals(HttpStatus.FORBIDDEN, res.getStatusCode());
  }

  @Test
  public void testRemoveSkillUserForbidden() {
    ResponseEntity<String> res = userController.removeSkill("aaaaaa", "Java", "IAmUnknown");
    assertEquals(HttpStatus.FORBIDDEN, res.getStatusCode());
  }
  
  @Test
  public void setRoleValid() {
    
  }

  @Test
  public void testGetSimilarUser() throws JSONException {
    User p1 = new User("abc");
    p1.addUpdateSkill("Java", 1, 2, false, false);
    p1.addUpdateSkill(".NET", 3, 2, false, false);
    p1.addUpdateSkill("Text", 1, 3, false, false);
    personRepo.insert(p1);

    User p2 = new User("def");
    p2.addUpdateSkill("Java", 3, 2, false, false);
    personRepo.insert(p2);

    User p3 = new User("ghi");
    p3.addUpdateSkill("Java", 1, 0, false, false);
    p3.addUpdateSkill(".NET", 3, 2, false, false);
    personRepo.insert(p3);

    ResponseEntity<String> res = userController.getSimilar("abc", 1);
    assertEquals(1, new JSONArray(res.getBody()).length());
    assertEquals("ghi", new JSONArray(res.getBody()).getJSONObject(0).getString("id"));
  }

  @Test
  public void testGetSimilarUserNotFound() {
    ResponseEntity<String> res = userController.getSimilar("IAmUnknown", 42);
    assertEquals(HttpStatus.NOT_FOUND, res.getStatusCode());
  }

  @Test
  public void testGetSimilarUserCountNegative() {
    ResponseEntity<String> res = userController.getSimilar("aaaaaa", -1);
    assertEquals(HttpStatus.BAD_REQUEST, res.getStatusCode());
  }

  @Test
  public void testSetRoleValid() {
    ResponseEntity<String> res = userController.updateRole("aaaaaa", "YmJiLmJiYkBzaW5uZXJzY2hyYWRlci5jb20=|foo|bar", "ADMIN");
    assertEquals(HttpStatus.OK, res.getStatusCode());
    assertEquals(Role.ADMIN, personRepo.findByIdIgnoreCase("aaaaaa").getRole());
  }

  @Test
  public void testSetRoleValidIgnoreCase() {
    ResponseEntity<String> res = userController.updateRole("aaaaaa", "YmJiLmJiYkBzaW5uZXJzY2hyYWRlci5jb20=|foo|bar", "aDmiN");
    assertEquals(HttpStatus.OK, res.getStatusCode());
    assertEquals(Role.ADMIN, personRepo.findByIdIgnoreCase("aaaaaa").getRole());
  }

  @Test
  public void testSetRoleInvalid() {
    ResponseEntity<String> res = userController.updateRole("aaaaaa", "YmJiLmJiYkBzaW5uZXJzY2hyYWRlci5jb20=|foo|bar", "unicorn");
    assertEquals(HttpStatus.BAD_REQUEST, res.getStatusCode());
    assertEquals(Role.USER, personRepo.findByIdIgnoreCase("aaaaaa").getRole());
  }

  @Test
  public void testSetRoleNotAdmin() {
    ResponseEntity<String> res = userController.updateRole("aaaaaa", "YWFhLmFhYUBzaW5uZXJzY2hyYWRlci5jb20=|foo|bar", "ADMIN");
    assertEquals(HttpStatus.FORBIDDEN, res.getStatusCode());
    assertEquals(Role.USER, personRepo.findByIdIgnoreCase("aaaaaa").getRole());
  }

  @Test
  public void testSetRoleInvalidSession() {
    ResponseEntity<String> res = userController.updateRole("aaaaaa", "fleischkremistkeinesession", "ADMIN");
    assertEquals(HttpStatus.FORBIDDEN, res.getStatusCode());
    assertEquals(Role.USER, personRepo.findByIdIgnoreCase("aaaaaa").getRole());
  }

  @Test
  public void testSetRoleUnknonwUser() {
    ResponseEntity<String> res = userController.updateRole("dermönchmitderpeitsche", "YmJiLmJiYkBzaW5uZXJzY2hyYWRlci5jb20=|foo|bar", "ADMIN");
    assertEquals(HttpStatus.NOT_FOUND, res.getStatusCode());
  }

}
