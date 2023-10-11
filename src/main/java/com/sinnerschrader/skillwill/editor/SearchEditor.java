package com.sinnerschrader.skillwill.editor;

import java.beans.PropertyEditorSupport;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.util.StringUtils;

import com.sinnerschrader.skillwill.domain.skill.SkillUtils;

public class SearchEditor extends PropertyEditorSupport {

	@Override
	public void setAsText(String text) throws IllegalArgumentException {

		List<String> rawSearchItems = StringUtils.hasText(text) ? List.of(text.split("\\s*,\\s*")): Collections.emptyList();

		List<String> cleanedSearchItems = rawSearchItems.stream()
			    .map(item -> item.replaceAll("[^\\w]", "")).map(SkillUtils::capitalizeFirstLetter)
			    .collect(Collectors.toList());
		
		setValue(cleanedSearchItems);
	}

}