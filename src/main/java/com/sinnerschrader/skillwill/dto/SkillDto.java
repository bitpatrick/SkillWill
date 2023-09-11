package com.sinnerschrader.skillwill.dto;

import java.util.List;
import java.util.Set;

import lombok.Builder;

@Builder
public record SkillDto(String name, List<SuggestionSkillDto> suggestions, Set<String> subSkillNames, boolean hidden, String description) {

}
