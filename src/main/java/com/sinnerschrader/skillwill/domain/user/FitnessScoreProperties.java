package com.sinnerschrader.skillwill.domain.user;

import com.sinnerschrader.skillwill.dto.FitnessScorePropertiesDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Wrapper to load weights for fitness Scores
 */
@Component
public class FitnessScoreProperties {

  @Value("${weightAverageSkills}")
  private double weightAverageSkills;

  @Value("${weightAverageWills}")
  private double weightAverageWills;

  @Value("${weightSpecializationSkills}")
  private double weightSpecializationSkills;

  @Value("${weightSpecializationWills}")
  private double weightSpecializationWills;

  @Value("${maxLevelValue}")
  private int maxLevelValue;
  
  public static FitnessScoreProperties fromDto(FitnessScorePropertiesDto dto) {
      FitnessScoreProperties properties = new FitnessScoreProperties();

      properties.weightAverageSkills = dto.weightAverageSkills();
      properties.weightAverageWills = dto.weightAverageWills();
      properties.weightSpecializationSkills = dto.weightSpecializationSkills();
      properties.weightSpecializationWills = dto.weightSpecializationWills();
      properties.maxLevelValue = dto.maxLevelValue();

      return properties;
  }

  double getWeightAverageSkills() {
    return weightAverageSkills;
  }

  double getWeightAverageWills() {
    return weightAverageWills;
  }

  double getWeightSpecializationSkills() {
    return weightSpecializationSkills;
  }

  double getWeightSpecializationWills() {
    return weightSpecializationWills;
  }

  int getMaxLevelValue() {
    return maxLevelValue;
  }

}
