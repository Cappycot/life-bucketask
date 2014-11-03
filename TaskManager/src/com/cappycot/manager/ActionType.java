package com.cappycot.manager;

public enum ActionType {

	NONE, FILE, URL, MAIL, NOTE;
	
	public static ActionType get(String arg) {
		switch(arg.toUpperCase()) {
		case "FILE": return FILE;
		case "URL": return URL;
		case "MAIL": return MAIL;
		case "NOTE": return NOTE;
		default: return NONE;
		}
	}

}
