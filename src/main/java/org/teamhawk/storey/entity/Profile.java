package org.teamhawk.storey.entity;

import java.util.ArrayList;

import com.google.common.collect.ImmutableList;
import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import java.util.List;

import org.teamhawk.storey.form.ProfileForm;

@Entity
@Cache
public class Profile {

	private String emailAddress;
	private String displayName;
	private String firstName;
	private String lastName;
	private List<String> rentedStorageSessionKeys = new ArrayList<>();

	@Id
	private String userId;

	private Profile() {
	}

	public Profile(String userId, String emailAddress, ProfileForm profileForm) {
		this.userId = userId;
		this.emailAddress = emailAddress;
		displayName = profileForm.getDisplayName();
		firstName = profileForm.getFirstName();
		lastName = profileForm.getLastName();
	}

	public String getEmailAddress() {
		return emailAddress;
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

	public String getUserId() {
		return userId;
	}

	public List<String> getRentedStorageSessionKeys() {
		return ImmutableList.copyOf(rentedStorageSessionKeys);
	}

	public void addRentedStorageSessionKey(String storageSessionKey) {
		rentedStorageSessionKeys.add(storageSessionKey);
	}

	public void removeRentedStorageSessionKey(String storageSessionKey) {
		if (rentedStorageSessionKeys.contains(storageSessionKey))
			rentedStorageSessionKeys.remove(storageSessionKey);
	}
}
