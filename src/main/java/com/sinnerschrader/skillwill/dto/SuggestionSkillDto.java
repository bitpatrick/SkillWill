package com.sinnerschrader.skillwill.dto;

import lombok.Builder;

@Builder
public record SuggestionSkillDto(String name, int count) {
}
