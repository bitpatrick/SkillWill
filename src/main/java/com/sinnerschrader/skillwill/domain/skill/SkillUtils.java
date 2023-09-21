package com.sinnerschrader.skillwill.domain.skill;

import org.springframework.util.StringUtils;

public class SkillUtils {

  public static String generateStemName(String name) {
    if (name == null) {
      throw new IllegalArgumentException("cannot generate stem from null");
    }
    return name.replaceAll("[^A-Za-z0-9+]", "").toUpperCase();
  }

  public static String sanitizeName(String name) {
	  
	  return StringUtils.isEmpty(name) ? "" : name.trim();
  }
  
  public static String capitalizeFirstLetter(String input) {
	  
	    if (input == null || input.isEmpty()) {
	        return input;
	    }

	    String[] words = input.toLowerCase().split(" ");
	    StringBuilder result = new StringBuilder();

	    for (String word : words) {
	        if (word.length() > 0) {
	            result.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1)).append(" ");
	        }
	    }

	    // Rimuove l'ultimo spazio e restituisce la stringa risultante
	    return result.toString().trim();
	}

}
