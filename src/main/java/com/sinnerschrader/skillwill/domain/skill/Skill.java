package com.sinnerschrader.skillwill.domain.skill;

import static com.sinnerschrader.skillwill.domain.skill.SkillUtils.generateStemName;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;

import com.sinnerschrader.skillwill.dto.SkillDto;
import com.sinnerschrader.skillwill.dto.SuggestionSkillDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * A skill known to the system including a list of suggestable skills
 */
@AllArgsConstructor
@EqualsAndHashCode(of = {"nameStem"})
@Builder
@Data
public class Skill {
	
	@Id
	private String nameStem;

	private String name;

	private Set<SuggestionSkill> suggestions;

	private Set<String> subSkillNames;

	private boolean hidden;

	private String description;

	@Version
	private Long version;

	public Skill(String name, String description, Set<SuggestionSkill> suggestions, boolean hidden, Set<String> subSkillNames) {
		this.nameStem = generateStemName(name);
		this.name = name;
		this.description = description;
		this.suggestions = suggestions;
		this.subSkillNames = subSkillNames;
		this.hidden = hidden;
	}

	public Skill(String name) {
		this(name, "", new HashSet<>(), false, new HashSet<>());
	}

	public Skill() {
		this("", "", new HashSet<>(), false, new HashSet<>());
	}

	public void setName(String name) {
		this.name = name;
		this.nameStem = generateStemName(name);
	}

	public void setSuggestions(Set<SuggestionSkill> suggestions) {
		this.suggestions = suggestions;
	}

	private SuggestionSkill getSuggestionByName(String name) {
		return this.suggestions.stream().filter(s -> s.getName().equals(name)).findFirst().orElse(null);
	}

	public void renameSuggestion(String oldName, String newName) {
		SuggestionSkill suggestion = getSuggestionByName(oldName);

		if (suggestion == null) {
			// no suggestion to rename
			return;
		}

		suggestion.setName(newName);
	}

	public void incrementSuggestion(String name) {
		SuggestionSkill suggestion = getSuggestionByName(name);

		if (suggestion != null) {
			suggestion.incrementCount();
		} else {
			suggestions.add(new SuggestionSkill(name, 1));
		}
	}

	public void deleteSuggestion(String name) {
		SuggestionSkill suggestion = getSuggestionByName(name);

		if (suggestion == null) {
			// no suggestion to rename
			return;
		}

		this.suggestions.remove(suggestion);
	}

	public void addSubSkillName(String name) {
		this.subSkillNames.add(name);
	}

	public void addSuggestion(String name, int count) {

		SuggestionSkill suggestionSkill = new SuggestionSkill(name, count);

		if (this.suggestions.contains(suggestionSkill)) {
			return;
		}

		this.suggestions.add(suggestionSkill);
	}

	public void removeSubSkillName(String name) {
		this.subSkillNames.remove(name);
	}

	public void renameSubSkill(String oldName, String newName) {
		this.removeSubSkillName(oldName);
		this.addSubSkillName(newName);
	}

	public JSONObject toJSON() {
		JSONObject obj = new JSONObject();
		obj.put("name", this.name);
		obj.put("hidden", this.hidden);
		obj.put("subskills", new JSONArray(this.subSkillNames));
		obj.put("description", this.description);
		return obj;
	}

	public static Skill fromDto(SkillDto dto) {

		Set<SuggestionSkill> suggestionSkills = dto.suggestions().stream().map(SuggestionSkill::fromDto)
				.collect(Collectors.toSet());

		return Skill.builder().name(dto.name()).description(dto.description()).suggestions(suggestionSkills)
				.subSkillNames(dto.subSkillNames()).hidden(dto.hidden()).build();
	}
	
	public SkillDto toDto() {

		List<SuggestionSkillDto> suggestionsDto = null;

		if (suggestions != null) {

			suggestionsDto = this.suggestions.stream().map(SuggestionSkill::toDto).toList();

		}

		return SkillDto.builder().name(name).suggestions(suggestionsDto).subSkillNames(subSkillNames).hidden(hidden)
				.description(description).build();
	}

//	@Override
//	public boolean equals(Object o) {
//		if (this == o) {
//			return true;
//		}
//		if (o == null || getClass() != o.getClass()) {
//			return false;
//		}
//		Skill skill = (Skill) o;
//		return hidden == skill.hidden && Objects.equals(name, skill.name) && Objects.equals(nameStem, skill.nameStem)
//				&& Objects.equals(suggestions, skill.suggestions) && Objects.equals(subSkillNames, skill.subSkillNames)
//				&& Objects.equals(description, skill.description);
//	}

//	@Override
//	public int hashCode() {
//
//		return Objects.hash(name, nameStem, suggestions, subSkillNames, hidden, description);
//	}

}
