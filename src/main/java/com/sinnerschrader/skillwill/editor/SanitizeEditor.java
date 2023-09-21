package com.sinnerschrader.skillwill.editor;

import java.beans.PropertyEditorSupport;

public class SanitizeEditor extends PropertyEditorSupport {

	@Override
	public void setAsText(String text) throws IllegalArgumentException {
		
		if ( text == null ) {
			return;
		}
		
		String name = text.replaceAll("[^\\w]*", "");
		setValue(name);
		super.setAsText(name);
	}

}
