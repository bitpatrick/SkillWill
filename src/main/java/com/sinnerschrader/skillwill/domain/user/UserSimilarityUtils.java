package com.sinnerschrader.skillwill.domain.user;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import com.sinnerschrader.skillwill.domain.skills.UserSkill;

/**
 * Get n Persons that are similar to the reference user
 * similar = low jaccard distance between users' skillsets
 *
 * @author torree
 */
public class UserSimilarityUtils {

  public static List<UserDetailsImpl> findSimilar(UserDetailsImpl userDetailsImpl, Collection<UserDetailsImpl> candidates, Integer count) {
    if (count != null && count <= 0) {
      throw new IllegalArgumentException("count must be positive or null");
    }

    return candidates.stream()
      .filter(candidate -> !candidate.getId().equals(userDetailsImpl.getId()))
      .sorted(new JaccardDistanceComparator(userDetailsImpl))
      .limit(count != null ? count : Long.MAX_VALUE)
      .collect(Collectors.toList());
  }

  private static double jaccardDistance(UserDetailsImpl userA, UserDetailsImpl userB) {
    var skillsNamesA = userA.getSkills(true).stream().map(UserSkill::getName).collect(Collectors.toSet());
    var skillsNamesB = userB.getSkills(true).stream().map(UserSkill::getName).collect(Collectors.toSet());

    double intersectionSize = skillsNamesA.stream().filter(skillsNamesB::contains).count();
    double unionSize = skillsNamesA.size() + skillsNamesB.size() - intersectionSize;

    return 1 - intersectionSize / unionSize;
  }

  private static class JaccardDistanceComparator implements Comparator<UserDetailsImpl> {

    private final UserDetailsImpl referenceUser;

    private JaccardDistanceComparator(UserDetailsImpl reference) {
      this.referenceUser = reference;
    }

    @Override
    public int compare(UserDetailsImpl userA, UserDetailsImpl userB) {
      var distanceA = jaccardDistance(userA, referenceUser);
      var distanceB = jaccardDistance(userB, referenceUser);
      return Double.compare(distanceA, distanceB);
    }

  }

}
