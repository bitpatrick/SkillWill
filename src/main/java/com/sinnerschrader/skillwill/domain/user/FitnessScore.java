package com.sinnerschrader.skillwill.domain.user;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.sinnerschrader.skillwill.domain.skills.Skill;
import com.sinnerschrader.skillwill.domain.skills.UserSkill;
import com.sinnerschrader.skillwill.dto.FitnessScoreDto;
import com.sinnerschrader.skillwill.dto.FitnessScorePropertiesDto;
import com.sinnerschrader.skillwill.dto.SkillDto;
import com.sinnerschrader.skillwill.dto.UserDto;

/**
 * Calculate how well a user fits into a searched skill set.
 * The result can be on a scale from 0 (does not fit at all) to 1 (perfect match)
 *
 * @author torree
 */
public class FitnessScore {

  private final FitnessScoreProperties props;

  private final User userDetailsImpl;

  private final Collection<Skill> searchedSkills;

  private final double value;

  public FitnessScore(User userDetailsImpl, Collection<Skill> searchedSkills, FitnessScoreProperties props) {
    this.userDetailsImpl = userDetailsImpl;
    this.searchedSkills = searchedSkills;
    this.props = props;
    this.value = (double) Math.round(calculateValue() * 10000) / 10000;
  }
  
  public static FitnessScore fromDto(FitnessScoreDto dto) {
	  
	  FitnessScoreProperties properties = FitnessScoreProperties.fromDto(dto.props());
	  User userDetails = User.createUser(dto.user()); // Assumendo che tu abbia un metodo simile in UserDetailsImpl
	  Collection<Skill> skills = dto.searchedSkills().stream()
	                                    .map(Skill::fromDto) // Assumendo che tu abbia un metodo simile in Skill
	                                    .collect(Collectors.toList());

	  return new FitnessScore(userDetails, skills, properties);
     
  }

  
  public FitnessScoreDto toDto() {
	  
	  FitnessScorePropertiesDto props = null;
	  UserDto userDto = null;
	  List<SkillDto> skills = null;
	  
	  if ( this.props != null ) {
		  
		  props = FitnessScorePropertiesDto.builder()
				  .weightAverageSkills(this.props.getWeightAverageSkills())
				  .weightAverageWills(this.props.getWeightAverageWills())
				  .weightSpecializationSkills(this.props.getWeightSpecializationSkills())
				  .weightSpecializationWills(this.props.getWeightSpecializationWills())
				  .maxLevelValue(this.props.getMaxLevelValue())
				  .build();
	  }
	  
	  if ( userDetailsImpl != null) {
		 
		  userDto = this.userDetailsImpl.toUserDto();
	  }
	  
	  if ( this.searchedSkills != null ) {

		  skills = this.searchedSkills.stream().map(Skill::toDto).toList();  
	  }
	  
	  return FitnessScoreDto.builder()
			  .props(props)
			  .user(userDto)
			  .searchedSkills(skills)
			  .value(value)
			  .build();
  }

  public double getValue() {
    return this.value;
  }

  private Set<String> getSearchedSkillNames(Collection<Skill> searchedSkills) {
    return searchedSkills.stream().map(Skill::getName).collect(Collectors.toSet());
  }

  private Set<UserSkill> getSearchedPersonalSkills() {
    return this.userDetailsImpl.getSkills(true).stream()
        .filter(s -> getSearchedSkillNames(this.searchedSkills).contains(s.getName()))
        .collect(Collectors.toSet());
  }

  private Set<UserSkill> getUnsearchedPersonalSkills() {
    var skillset = new HashSet<>(this.userDetailsImpl.getSkills(true));
    skillset.removeAll(getSearchedPersonalSkills());
    return skillset;
  }

  private double getAverageSkillLevelSearched() {
    return getSearchedPersonalSkills().stream()
        .mapToInt(UserSkill::getSkillLevel)
        .average()
        .orElse(0);
  }

  private double getAverageWillLevelSearched() {
    return getSearchedPersonalSkills().stream()
        .mapToInt(UserSkill::getWillLevel)
        .average()
        .orElse(0);
  }

  private double getAverageSkillLevelUnsearched() {
    return getUnsearchedPersonalSkills().stream()
        .mapToInt(UserSkill::getSkillLevel)
        .average()
        .orElse(0);
  }

  private double getAverageWillLevelUnsearched() {
    return this.getUnsearchedPersonalSkills().stream()
        .mapToInt(UserSkill::getWillLevel)
        .average()
        .orElse(0);
  }

  private double getSpecializationSkills() {
    return (props.getMaxLevelValue() + getAverageSkillLevelSearched() - getAverageSkillLevelUnsearched())
        / (2 * props.getMaxLevelValue());
  }

  private double getSpecializationWills() {
    return (props.getMaxLevelValue() + getAverageWillLevelSearched() - getAverageWillLevelUnsearched())
        / (2 * props.getMaxLevelValue());
  }

  private double calculateValue() {
    return (props.getWeightAverageSkills() * getAverageSkillLevelSearched()) / props.getMaxLevelValue() +
        (props.getWeightAverageWills() * getAverageWillLevelSearched()) / props.getMaxLevelValue() +
        (props.getWeightSpecializationSkills() * getSpecializationSkills()) +
        (props.getWeightSpecializationWills() * getSpecializationWills());
  }

}
