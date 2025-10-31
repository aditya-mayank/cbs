package com.example.cycleborrowingsystem;

import com.example.cycleborrowingsystem.models.User;

public final class Session {
	private static User currentUser;
	private Session() {}
	public static void setCurrentUser(User user) { currentUser = user; }
	public static User getCurrentUser() { return currentUser; }
	public static void clear() { currentUser = null; }
}
