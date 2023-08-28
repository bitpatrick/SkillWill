package com.sinnerschrader.skillwill.domain.person;

//import static org.junit.Assert.assertEquals;
//
//import org.json.JSONException;
//import org.json.JSONObject;
//import org.junit.Before;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
//
//import com.sinnerschrader.skillwill.domain.user.Role;
//import com.sinnerschrader.skillwill.domain.user.UserDetailsImpl;
//import com.sinnerschrader.skillwill.domain.user.UserLdapDetails;

/**
 * Partial unit tests for User
 *
 * @author torree
 */
//@RunWith(SpringJUnit4ClassRunner.class)
//@SpringBootTest
public class UserTest {

//  private UserDetailsImpl userDetailsImpl;
//
//  @Before
//  public void init() {
//    userDetailsImpl = new UserDetailsImpl("foobar");
//    userDetailsImpl.addUpdateSkill("skillname", 2, 3, false, false);
//  }
//
//  @Test
//  public void testAddUpdateNewSkill() {
//    userDetailsImpl.addUpdateSkill("new skill", 2, 3, false, false);
//    assertEquals(2, userDetailsImpl.getSkills(true).size());
//  }
//
//  @Test
//  public void testAddUpdateKnownSkill() {
//    userDetailsImpl.addUpdateSkill("skillname", 0, 1, false, false);
//    assertEquals(1, userDetailsImpl.getSkills(true).size());
//    assertEquals(0, userDetailsImpl.getSkills(true).get(0).getSkillLevel());
//    assertEquals(1, userDetailsImpl.getSkills(true).get(0).getWillLevel());
//  }
//
//  @Test
//  public void testToJson() throws JSONException {
//    userDetailsImpl.setLdapDetails(
//        new UserLdapDetails(
//            "Fooberius",
//            "Barblub",
//            "fooberius.barblub@sinnerschrader.com",
//            "+49 666 666",
//            "Hamburg",
//            "Senior Web Unicorn",
//            "Firma",
//            Role.ADMIN
//        )
//    );
//    JSONObject obj = userDetailsImpl.toJSON();
//
//    assertEquals("foobar", obj.getString("id"));
//    assertEquals("ADMIN", obj.getString("role"));
//    assertEquals("Fooberius", obj.getString("firstName"));
//    assertEquals("Barblub", obj.getString("lastName"));
//    assertEquals("+49 666 666", obj.getString("phone"));
//    assertEquals("Senior Web Unicorn", obj.getString("title"));
//    assertEquals("Hamburg", obj.getString("location"));
//    assertEquals("fooberius.barblub@sinnerschrader.com", obj.getString("mail"));
//    assertEquals("skillname", obj.getJSONArray("skills").getJSONObject(0).getString("name"));
//    assertEquals(2, obj.getJSONArray("skills").getJSONObject(0).getInt("skillLevel"));
//    assertEquals(3, obj.getJSONArray("skills").getJSONObject(0).getInt("willLevel"));
//  }

}
