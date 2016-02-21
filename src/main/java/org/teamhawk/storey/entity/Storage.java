package org.teamhawk.storey.entity;

import static org.teamhawk.storey.service.OfyService.ofy;

import java.util.Calendar;
import java.util.Date;

import org.teamhawk.storey.form.StorageForm;

import com.google.api.server.spi.config.AnnotationBoolean;
import com.google.api.server.spi.config.ApiResourceProperty;
import com.google.common.base.Preconditions;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;
import com.googlecode.objectify.annotation.Parent;
import com.googlecode.objectify.condition.IfNotDefault;

@Entity
@Cache
public class Storage {

	private static final Double DEFAULT_PRICE_PER_DAY = 10.0;

	/**
	 * The id for the datastore key.
	 *
	 * We use automatic id assignment for entities of the Storage class.
	 */
	@Id
	private long id;

	@Index
	private String name;

	private String description;

	/**
	 * Holds Profile key as the parent.
	 */
	@Parent
	@ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
	private Key<Profile> ownerKey;

	/**
	 * The userId of the owner.
	 */
	@ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
	private String ownerUserId;

	@Index(IfNotDefault.class)
	private String address;

	private Double pricePerDay;

	/**
	 * The starting date of the availability of this storage.
	 */
	private Date startDate;

	/**
	 * The ending date of the availability of this storage.
	 */
	private Date endDate;

	/**
	 * Indicating the starting startingMonth derived from startDate.
	 *
	 * We need this for a composite query specifying the starting startingMonth.
	 */
	@Index
	private int startingMonth;

	/**
	 * The maximum capacity of this storage.
	 */
	@Index
	private Double volume;

	private Double height;
	private Double width;
	private Double length;

	private Storage() {
	}

	public Storage(final long id, final String ownerUserId, final StorageForm storageForm) {
		Preconditions.checkNotNull(storageForm.getName(), "The name is required");
		this.id = id;
		this.ownerKey = Key.create(Profile.class, ownerUserId);
		this.ownerUserId = ownerUserId;
		updateWithStorageForm(storageForm);
	}

	private void updateWithStorageForm(StorageForm storageForm) {
		name = storageForm.getName();
		description = storageForm.getDescription();
		address = storageForm.getAddress();
		pricePerDay = storageForm.getPricePerDay() == null ? DEFAULT_PRICE_PER_DAY : storageForm.getPricePerDay();

		Date startDate = storageForm.getStartDate();
		this.startDate = startDate == null ? null : new Date(startDate.getTime());
		Date endDate = storageForm.getEndDate();
		this.endDate = endDate == null ? null : new Date(endDate.getTime());
		if (this.startDate != null) {
			// Getting the starting month for a composite query.
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(this.startDate);
			// Calendar.MONTH is zero based, so adding 1.
			this.startingMonth = calendar.get(calendar.MONTH) + 1;
		}

		height = storageForm.getHeight();
		width = storageForm.getWidth();
		length = storageForm.getLength();
		volume = height * width * length;
	}

	public long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	@ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
	public Key<Profile> getOwnerKey() {
		return ownerKey;
	}

	// Get a String version of the key
	public String getWebsafeKey() {
		return Key.create(ownerKey, Storage.class, id).getString();
	}

	@ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
	public String getOwnerUserId() {
		return ownerUserId;
	}

	/**
	 * Returns owner's display name.
	 *
	 * @return owner's display name. If there is no Profile object, return his/her
	 *         userId.
	 */
	public String getOwnerDisplayName() {
		// Profile owner = ofy().load().key(Key.create(Profile.class,
		// ownerUserId)).now();
		Profile owner = ofy().load().key(getOwnerKey()).now();
		if (owner == null) {
			return ownerUserId;
		} else {
			return owner.getDisplayName();
		}
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

	public double getPricePerDay() {
		return pricePerDay;
	}

	public String getAddress() {
		return address;
	}

	public int getMonth() {
		return startingMonth;
	}

	public Double getVolume() {
		return volume;
	}

	/**
	 * Returns a defensive copy of startDate if not null.
	 * 
	 * @return a defensive copy of startDate if not null.
	 */
	public Date getStartDate() {
		return startDate == null ? null : new Date(startDate.getTime());
	}

	/**
	 * Returns a defensive copy of endDate if not null.
	 * 
	 * @return a defensive copy of endDate if not null.
	 */
	public Date getEndDate() {
		return endDate == null ? null : new Date(endDate.getTime());
	}
}