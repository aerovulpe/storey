package org.teamhawk.storey.form;

import java.util.Date;

public class StorageForm {
	private String name;
	private String description;
	private String address;
	private Double pricePerDay;
	private Double height;
	private Double width;
	private Double length;
    private Date startDate;
    private Date endDate;

	private StorageForm() {
	}

	public StorageForm(String name, String description, String address, Double price, Double height, Double width,
			Double length, Date startDate, Date endDate) {
		this.name = name;
		this.description = description;
		this.address = address;
		this.pricePerDay = price;
		this.height = height;
		this.width = width;
		this.length = length;
		this.startDate = startDate;
		this.endDate = endDate;
	}

	public String getName() {
		return name;
	}

	public Double getHeight() {
		return height;
	}

	public Double getWidth() {
		return width;
	}

	public String getDescription() {
		return description;
	}

	public String getAddress() {
		return address;
	}

	public Double getPricePerDay() {
		return pricePerDay;
	}

	public Double getLength() {
		return length;
	}

	public Date getStartDate() {
		return startDate;
	}

	public Date getEndDate() {
		return endDate;
	}
}
