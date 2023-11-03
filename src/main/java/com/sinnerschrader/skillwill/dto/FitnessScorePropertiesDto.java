package com.sinnerschrader.skillwill.dto;

import lombok.Builder;

@Builder
public record FitnessScorePropertiesDto(
  double weightAverageSkills,
  double weightAverageWills,
  double weightSpecializationSkills,
  double weightSpecializationWills,
  int maxLevelValue
) {
}
