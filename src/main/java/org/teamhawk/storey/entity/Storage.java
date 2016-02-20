package org.teamhawk.storey.entity;

public class Storage {

  private User owner;
  private User renter;
  private double height;
  private double width;
  private double length;
  private double price;
  private int daysRented = 0;

  public Storage(User owner, double height, double width, double length, double price) {
    this.owner = owner;
    this.height = height;
    this.width = width;
    this.length = length;
    this.price = price;
  }

  public User getOwner() {
    return owner;
  }

  public double getHeight() {
    return height;
  }

  protected void setHeight(double height) {
    this.height = height;
  }

  public double getWidth() {
    return width;
  }

  protected void setWidth(double width) {
    this.width = width;
  }

  public double getLength() {
    return length;
  }

  protected void setLength(double length) {
    this.length = length;
  }

  public User getRenter() {
    return renter;
  }

  protected void setRenter(User renter) {
    this.renter = renter;
  }

  public int getDaysRented() {
    return daysRented;
  }

  protected void addOneMoreDay() {
    daysRented++;
  }

  protected void resetDaysRented() {
    daysRented = 0;
  }

  public double getPrice() {
    return price;
  }
}