package com.sinnerschrader.skillwill.domain.person;

import com.sinnerschrader.skillwill.domain.skills.PersonalSkill;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Get n Persons that are similar to the reference person
 * similar = high jaccard index when comparing skill sets
 *
 * @author torree
 */
public class JaccardFilter {

  private final Person reference;

  public JaccardFilter(Person reference) {
    this.reference = reference;
  }

  private static double getJaccardIndex(Person a, Person b) {
    Collection<PersonalSkill> aSkills = a.getSkillsExcludeHidden();
    Collection<PersonalSkill> bSkills = b.getSkillsExcludeHidden();

    double intersectionCount = aSkills.stream().filter(s -> b.getSkill(s.getName()) != null).count();
    double unionCount = aSkills.size() + bSkills.size() - intersectionCount;

    return intersectionCount / unionCount;
  }

  public List<Person> getFrom(List<Person> all, int max) {
    return all.stream()
        .sorted(new JaccardIndexComparator(reference))
        .limit(max)
        .collect(Collectors.toList());
  }

  private static class JaccardIndexComparator implements Comparator<Person> {

    private final Person reference;

    public JaccardIndexComparator(Person reference) {
      this.reference = reference;
    }

    @Override
    public int compare(Person a, Person b) {
      return -1 * Double.compare(getJaccardIndex(a, reference), getJaccardIndex(b, reference));
    }
  }

}