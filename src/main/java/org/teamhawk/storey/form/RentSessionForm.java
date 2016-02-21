package org.teamhawk.storey.form;

import java.util.Date;

public class RentSessionForm {
	private Date startTime;
	private Date endTime;
	private String note;

	private RentSessionForm() {
	}

	public RentSessionForm(Date startTime, Date endTime, String note) {
		this.startTime = startTime;
		this.endTime = endTime;
		this.note = note;
	}

	public Date getStartTime() {
		return startTime;
	}

	public Date getEndTime() {
		return endTime;
	}

	public String getNote() {
		return note;
	}
}
