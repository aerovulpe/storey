package org.teamhawk.storey.form;

/**
 * A simple Java object (POJO) representing a Profile form sent from the client.
 */
public class ProfileForm {
	private String displayName;
	private String firstName;
	private String lastName;

	private ProfileForm() {
	}

	public ProfileForm(String displayName, String firstName, String lastName) {
		this.displayName = displayName;
		this.firstName = firstName;
		this.lastName = lastName;
	}

	public String getDisplayName() {
		return displayName;
	}

	public String getFirstName() {
		return firstName;
	}

	public String getLastName() {
		return lastName;
	}
}
