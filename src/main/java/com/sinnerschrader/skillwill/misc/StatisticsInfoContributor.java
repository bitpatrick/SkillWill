package com.sinnerschrader.skillwill.misc;

import java.util.HashMap;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.stereotype.Component;

import com.sinnerschrader.skillwill.domain.skills.Skill;
import com.sinnerschrader.skillwill.domain.skills.UserSkill;
import com.sinnerschrader.skillwill.domain.user.UserDetailsImpl;
import com.sinnerschrader.skillwill.repositories.SkillRepository;
import com.sinnerschrader.skillwill.repositories.UserRepository;

@Component
public class StatisticsInfoContributor implements InfoContributor {

  private final UserRepository UserRepository;

  private final SkillRepository skillRepository;

  @Autowired
  public StatisticsInfoContributor(UserRepository UserRepository, SkillRepository skillRepository) {
    this.UserRepository = UserRepository;
    this.skillRepository = skillRepository;
  }

  private void contributeUserCount(Info.Builder builder) {
    builder.withDetail("users_total", UserRepository.count());
  }

  private void contributeSkillCount(Info.Builder builder) {
    builder.withDetail("skills_total", skillRepository.count());
  }

  private void contributeUsedSkillCount(List<UserDetailsImpl> userDetailsImpls, Info.Builder builder) {
    var usedSkillCount = (int) userDetailsImpls.stream()
      .flatMap(user -> user.getSkills(true).stream())
      .map(UserSkill::getName)
      .distinct()
      .count();
    builder.withDetail("skills_used", usedSkillCount);
  }

  private void contributeHiddenSkillCount(Info.Builder builder) {
    builder.withDetail(
      "skills_hidden",
      skillRepository.findAll().stream().filter(Skill::isHidden).count()
    );
  }

  private void contributeUserSkills(List<UserDetailsImpl> userDetailsImpls, Info.Builder builder) {
    var stats = userDetailsImpls.stream()
      .mapToInt(user -> user.getSkills(true).size())
      .summaryStatistics();
    var details = new HashMap<String, Double>();

    details.put("total", (double) stats.getSum());
    details.put("min", (double) stats.getMin());
    details.put("max", (double) stats.getMax());
    details.put("average", stats.getAverage());

    builder.withDetail("personal_skills", details);
  }

  @Override
  public void contribute(Info.Builder builder) {
    var users = UserRepository.findAll();

    contributeUserCount(builder);
    contributeSkillCount(builder);
    contributeUsedSkillCount(users, builder);
    contributeHiddenSkillCount(builder);
    contributeUserSkills(users, builder);
  }

}
