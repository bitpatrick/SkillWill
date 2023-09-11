package com.sinnerschrader.skillwill.domain.skills;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

/**
 * A suggestable skill used by Skill
 *
 * @author torree
 */
@Data
@AllArgsConstructor
public class SuggestionSkill {

	private String name;
	volatile private int count;

	public SuggestionSkill() {
		this(null, 0);
	}

	public void incrementCount() {
		this.count += 1;
	}

	public void incrementCount(int add) {
		this.count += add;
	}

}
