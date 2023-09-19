package com.sinnerschrader.skillwill.controllers;

import java.beans.PropertyEditorSupport;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public class SubSkillsEditor extends PropertyEditorSupport {

    @Override
    public void setAsText(String text) throws IllegalArgumentException {
        Set<String> cleanedSubSkills = Arrays.stream(text.split(","))
            .map(String::trim)
            .collect(Collectors.toSet());
        setValue(cleanedSubSkills);
    }
    
}
