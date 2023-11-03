package com.sinnerschrader.skillwill.editor;

import com.sinnerschrader.skillwill.domain.skill.SkillUtils;
import org.springframework.util.StringUtils;

import java.beans.PropertyEditorSupport;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

public class SubSkillsEditor extends PropertyEditorSupport {

	@Override
	public void setAsText(String text) throws IllegalArgumentException {

		Set<String> rawSubSkills = StringUtils.hasText(text) ? Set.of(text.split("\\s*,\\s*")) : Collections.emptySet();

		Set<String> cleanedSubSkills = rawSubSkills.stream().map(item -> item.replaceAll("[^\\w]", "")).map(SkillUtils::capitalizeFirstLetter).collect(Collectors.toSet());

		setValue(cleanedSubSkills);
	}

}
