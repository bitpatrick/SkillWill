package com.sinnerschrader.skillwill.mock;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.sinnerschrader.skillwill.domain.skills.Skill;
import com.sinnerschrader.skillwill.domain.user.User;
import com.sinnerschrader.skillwill.jobs.LdapSyncJob;
import com.sinnerschrader.skillwill.repositories.SkillRepository;
import com.sinnerschrader.skillwill.repositories.UserRepository;

/**
 * Reads Mock Data from files specified in application.properties and
 * inserts it into DB. Handle with care, as this could delete all your data.
 *
 * @author torree
 */
@Component
public class MockData {

  private static final Logger logger = LoggerFactory.getLogger(MockData.class);

  @Autowired
  private SkillRepository skillRepo;

  @Autowired
  private UserRepository userRepo;

  @Autowired
  private LdapSyncJob ldapSyncJob;

  @Value("${mockInit}")
  private boolean initmock;

  @Value("${mockSkillFilePath}")
  private String skillsPath;

  @Value("${mockPersonsFilePath}")
  private String personsPath;

  private JSONArray readMockFileToJsonArray(String path) throws IOException {
    InputStreamReader reader = new InputStreamReader(getClass()
      .getResourceAsStream("/mockdata/" + path));

    BufferedReader bufferedReader = new BufferedReader(reader);
    StringBuilder jsonString = new StringBuilder();

    String line = "";
    while ((line = bufferedReader.readLine()) != null) {
      jsonString.append(line);
    }

    return new JSONArray(jsonString.toString());
  }

  private void mockUsers() throws IOException {
    logger.warn("Deleting all users in DB");
    userRepo.deleteAll();

    JSONArray usersJsonArray = readMockFileToJsonArray(personsPath);
    for (int i = 0; i < usersJsonArray.length(); i++) {
      JSONObject userJson = usersJsonArray.getJSONObject(i);
      User user = new User(userJson.getString("id"), "password");

      JSONArray skillsJsonArray = userJson.getJSONArray("skills");
      for (int j = 0; j < skillsJsonArray.length(); j++) {
        JSONObject skillJson = skillsJsonArray.getJSONObject(j);
        user.addUpdateSkill(
          skillJson.getString("name"),
          skillJson.getInt("skillLevel"),
          skillJson.getInt("willLevel"),
          false,
          false
        );
      }

      userRepo.save(user);
    }

    logger.info("Added {} users", usersJsonArray.length());
  }

  private void mockSkills() throws IOException {
    logger.warn("Deleting all skills in DB!");
    skillRepo.deleteAll();

    JSONArray skillsJsonArray = readMockFileToJsonArray(skillsPath);
    for (int i = 0; i < skillsJsonArray.length(); i++) {
      JSONObject skillJson = skillsJsonArray.getJSONObject(i);
      Skill skill = new Skill(skillJson.getString("name"));
      skill.setHidden(skillJson.getBoolean("hidden"));

      try {
    	  
    	  // subskills
          JSONArray subskillsJsonArray = skillJson.getJSONArray("subskills");
          for (int j = 0; j < subskillsJsonArray.length(); j++) {
            skill.addSubSkillName(subskillsJsonArray.getString(j));
          }
          
          // suggestions
          JSONArray suggestionsJsonArray = skillJson.getJSONArray("suggestions");
          for (int k = 0; k < suggestionsJsonArray.length(); k++) {
            JSONObject suggestion = suggestionsJsonArray.getJSONObject(k);
            String name = suggestion.getString("name");
            int count = suggestion.getInt("count");
            skill.addSuggestion(name, count);
          }
    	  
      } catch (JSONException e) {}
      
      skillRepo.save(skill);
    }

    logger.info("Added {} skills", skillsJsonArray.length());
  }

  @EventListener(ApplicationStartedEvent.class)
  public void init() throws IOException {
    if (!initmock) {
      return;
    }

    logger.warn("Mocking is enabled, this will overwrite all data in your DB");
    mockSkills();
    mockUsers();

//    logger.info("Syncing mocked users with LDAP");
//    ldapSyncJob.run();
//    logger.info("Finished mock data setup");
  }

}
