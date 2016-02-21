package org.teamhawk.storey.entity;

import static org.teamhawk.storey.service.OfyService.ofy;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.teamhawk.storey.form.RentSessionForm;

import com.google.api.server.spi.config.AnnotationBoolean;
import com.google.api.server.spi.config.ApiResourceProperty;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;
import com.googlecode.objectify.annotation.Parent;

@Entity
public class RentSession {
	@Id
	private long id;

	/**
	 * Holds Storage key as the parent.
	 */
	@Parent
	@ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
	private Key<Storage> storageKey;
	private String renterKey;
	private Date startTime;
	private Date endTime;
	@Index
	private Double price;
	@Index
	private Integer rating;
	private String note;

	private RentSession() {
	}

	public RentSession(final long id, Key<Storage> storageKey, String renterKey, RentSessionForm sessionForm) {
		this.id = id;
		this.storageKey = storageKey;
		this.renterKey = renterKey;
		startTime = sessionForm.getStartTime();
		endTime = sessionForm.getEndTime();
		note = sessionForm.getNote();

		Storage storage = ofy().load().key(storageKey).now();
		Calendar day1 = new GregorianCalendar();
		Calendar day2 = new GregorianCalendar();
		day1.setTime(startTime);
		day2.setTime(endTime);

		price = storage.getPricePerDay() * daysBetween(day1, day2);
	}

	public long getId() {
		return id;
	}

	@ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
	public Key<Storage> getStorageKey() {
		return storageKey;
	}

	public String getRenterKey() {
		return renterKey;
	}

	public Date getStartTime() {
		return startTime;
	}

	public Date getEndTime() {
		return endTime;
	}

	public Double getPrice() {
		return price;
	}

	public Integer getRating() {
		return rating;
	}

	private static int daysBetween(Calendar day1, Calendar day2) {
		Calendar dayOne = (Calendar) day1.clone(), dayTwo = (Calendar) day2.clone();

		if (dayOne.get(Calendar.YEAR) == dayTwo.get(Calendar.YEAR)) {
			return Math.abs(dayOne.get(Calendar.DAY_OF_YEAR) - dayTwo.get(Calendar.DAY_OF_YEAR));
		} else {
			if (dayTwo.get(Calendar.YEAR) > dayOne.get(Calendar.YEAR)) {
				// swap them
				Calendar temp = dayOne;
				dayOne = dayTwo;
				dayTwo = temp;
			}
			int extraDays = 0;

			int dayOneOriginalYearDays = dayOne.get(Calendar.DAY_OF_YEAR);

			while (dayOne.get(Calendar.YEAR) > dayTwo.get(Calendar.YEAR)) {
				dayOne.add(Calendar.YEAR, -1);
				// getActualMaximum() important for leap years
				extraDays += dayOne.getActualMaximum(Calendar.DAY_OF_YEAR);
			}

			return extraDays - dayTwo.get(Calendar.DAY_OF_YEAR) + dayOneOriginalYearDays;
		}
	}
}
