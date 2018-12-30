package com.yourbigday.api.entity;

public class FormMappingConfig {
	
	private String htmlElementName;
	
	private String arrayName;
	
	private int arrayIndex;

	public String getHtmlElementName() {
		return htmlElementName;
	}

	public void setHtmlElementName(String htmlElementName) {
		this.htmlElementName = htmlElementName;
	}

	public String getArrayName() {
		return arrayName;
	}

	public void setArrayName(String arrayName) {
		this.arrayName = arrayName;
	}

	public int getArrayIndex() {
		return arrayIndex;
	}

	public void setArrayIndex(int arrayIndex) {
		this.arrayIndex = arrayIndex;
	}
}
