package com.sinnerschrader.skillwill.domain.user;

import com.sinnerschrader.skillwill.domain.skill.UserSkill;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Get n Persons that are similar to the reference user
 * similar = low jaccard distance between users' skillsets
 *
 * @author torree
 */
public class UserSimilarityUtils {

  public static List<User> findSimilar(User userDetailsImpl, Collection<User> candidates, Integer count) {

    // check
    if (count != null && count <= 0) {
      throw new IllegalArgumentException("count must be positive or null");
    }

    return candidates.stream()
      .filter(candidate -> !candidate.getUsername().equals(userDetailsImpl.getUsername()))
      .sorted(new JaccardDistanceComparator(userDetailsImpl))
      .limit(count != null ? count : Long.MAX_VALUE)
      .collect(Collectors.toList());
  }

  private static double jaccardDistance(User userA, User userB) {
    var skillsNamesA = userA.getSkills(true).stream().map(UserSkill::getName).collect(Collectors.toSet());
    var skillsNamesB = userB.getSkills(true).stream().map(UserSkill::getName).collect(Collectors.toSet());

    double intersectionSize = skillsNamesA.stream().filter(skillsNamesB::contains).count();
    double unionSize = skillsNamesA.size() + skillsNamesB.size() - intersectionSize;

    return 1 - intersectionSize / unionSize;
  }

  private static class JaccardDistanceComparator implements Comparator<User> {

    private final User referenceUser;

    private JaccardDistanceComparator(User reference) {
      this.referenceUser = reference;
    }

    @Override
    public int compare(User userA, User userB) {
      var distanceA = jaccardDistance(userA, referenceUser);
      var distanceB = jaccardDistance(userB, referenceUser);
      return Double.compare(distanceA, distanceB);
    }

  }

}
