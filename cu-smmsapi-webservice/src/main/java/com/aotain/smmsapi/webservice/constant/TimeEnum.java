package com.aotain.smmsapi.webservice.constant;

public enum TimeEnum {

	FIRST_FOUND_TIME("firstFoundTime"), FIRST_FOUND_TIME_START("firstFoundTimeStart"), FIRST_FOUND_TIME_END("firstFoundTimeEnd"), 
	LAST_FOUND_TIME("lastFoundTime"), LAST_FOUND_TIME_START("lastFoundTimeStart"), LAST_FOUND_TIME_END("lastFoundTimeEnd");
	private String timeName;

	private TimeEnum(String timeName) {
		this.timeName = timeName;
	}

	public String getTimeName() {
		return timeName;
	}

	public void setTimeName(String timeName) {
		this.timeName = timeName;
	}

}
