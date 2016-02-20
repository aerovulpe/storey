package org.teamhawk.storey.entity;

import java.util.ArrayList;
import java.util.List;

public class User {

  private String emailAddress;
  private String displayName;
  private String firstName;
  private String lastName;
  private List<Storage> availableStorage = new ArrayList<>();
  private List<Storage> rentedStorage = new ArrayList<>();

  public User(String emailAddress, String displayName, String firstName, String lastName) {
    this.emailAddress = emailAddress;
    this.displayName = displayName;
    this.firstName = firstName;
    this.lastName = lastName;
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

  public List<Storage> getAvailableStorage() {
    return availableStorage;
  }

  public List<Storage> getRentedStorage() {
    return rentedStorage;
  }

  protected void addAvailableStorage(double height, double width, double length, double price) {
    availableStorage.add(new Storage(this, height, width, length, price));
  }

  protected void addRentedStorage(Storage storage) {
    rentedStorage.add(storage);
  }

  protected void removeAvailableStorage(Storage storage) {
    if (availableStorage.contains(storage))
      availableStorage.remove(storage);
  }

  protected void removeRentedStorage(Storage storage) {
    if (rentedStorage.contains(storage))
      rentedStorage.remove(storage);
  }
}
