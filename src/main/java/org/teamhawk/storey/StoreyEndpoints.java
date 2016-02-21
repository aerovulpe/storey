package org.teamhawk.storey;

import com.google.api.server.spi.Constant;
import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.Nullable;
import com.google.api.server.spi.config.ApiMethod.HttpMethod;
import com.google.api.server.spi.response.ConflictException;
import com.google.api.server.spi.response.ForbiddenException;
import com.google.api.server.spi.response.NotFoundException;
import com.google.api.server.spi.response.UnauthorizedException;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.appengine.api.users.User;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.Work;
import com.googlecode.objectify.cmd.Query;

import static org.teamhawk.storey.service.OfyService.ofy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.inject.Named;

import org.teamhawk.storey.entity.Profile;
import org.teamhawk.storey.entity.RentSession;
import org.teamhawk.storey.entity.Storage;
import org.teamhawk.storey.form.ProfileForm;
import org.teamhawk.storey.form.RentSessionForm;
import org.teamhawk.storey.form.StorageForm;
import org.teamhawk.storey.form.StorageQueryForm;
import org.teamhawk.storey.service.OfyService;

@Api(name = "storey", version = "v1", scopes = { Constants.EMAIL_SCOPE }, clientIds = { Constants.WEB_CLIENT_ID,
		Constants.ANDROID_CLIENT_ID, Constant.API_EXPLORER_CLIENT_ID }, audiences = {
				Constants.ANDROID_AUDIENCE }, description = "API for the Storey Backend application.")
public class StoreyEndpoints {
	/*
	 * Get the display name from the user's email. For example, if the email is
	 * lemoncake@example.com, then the display name becomes "lemoncake."
	 */
	private static String extractDefaultDisplayNameFromEmail(String email) {
		return email == null ? null : email.substring(0, email.indexOf("@"));
	}

	/**
	 * Creates or updates a Profile object associated with the given user
	 * object.
	 *
	 * @param user
	 *            A Profile object injected by the cloud endpoints.
	 * @param profileForm
	 *            A ProfileForm object sent from the client form.
	 * @return Profile object just created.
	 * @throws UnauthorizedException
	 *             when the Profile object is null.
	 */

	// Declare this method as a method available externally through Endpoints
	@ApiMethod(name = "saveProfile", path = "profile", httpMethod = HttpMethod.POST)
	// The request that invokes this method should provide data that
	// conforms to the fields defined in ProfileForm
	// Pass the ProfileForm parameter
	// Pass the Profile parameter
	public Profile saveProfile(final User user, ProfileForm profileForm) throws UnauthorizedException {

		String userId = null;
		String mainEmail = null;
		String displayName = "Your name will go here";

		// If the user is not logged in, throw an UnauthorizedException
		if (user == null)
			throw new UnauthorizedException("Authorization required");

		// Get the userId and mainEmail
		userId = user.getUserId();
		mainEmail = user.getEmail();

		// Set the displayName to the value sent by the ProfileForm, if sent
		// otherwise set it to null
		displayName = profileForm.getDisplayName();

		Profile profile = ofy().load().key(Key.create(Profile.class, userId)).now();
		if (profile != null) {
			return profile;
		} else {
			// If the displayName is null, set it to default value based on the
			// user's email
			// by calling extractDefaultDisplayNameFromEmail(...)
			if (displayName == null)
				displayName = extractDefaultDisplayNameFromEmail(user.getEmail());

			// Create a new Profile entity
			profile = new Profile(userId, mainEmail, profileForm);
		}

		// Save the Profile entity in the datastore
		ofy().save().entity(profile).now();

		// Return the profile
		return profile;
	}

	// /**
	// * This is an ugly workaround for null userId for Android clients.
	// *
	// * @param user
	// * A Profile object injected by the cloud endpoints.
	// * @return the App Engine userId for the user.
	// */
	// private static String getUserId(User user) {
	// String userId = user.getUserId();
	// if (userId == null) {
	// AppEngineUser appEngineUser = new AppEngineUser(user);
	// ofy().save().entity(appEngineUser).now();
	// // Begin new session for not using session cache.
	// Objectify objectify = ofy().factory().begin();
	// AppEngineUser savedUser =
	// objectify.load().key(appEngineUser.getKey()).now();
	// userId = savedUser.getUser().getUserId();
	// }
	// return userId;
	// }
	//
	/**
	 * Returns a Profile object associated with the given user object. The cloud
	 * endpoints system automatically inject the Profile object.
	 *
	 * @param user
	 *            A Profile object injected by the cloud endpoints.
	 * @return Profile object.
	 * @throws UnauthorizedException
	 *             when the Profile object is null.
	 */
	@ApiMethod(name = "getProfile", path = "profile", httpMethod = HttpMethod.GET)
	public Profile getProfile(final User user) throws UnauthorizedException {
		if (user == null) {
			throw new UnauthorizedException("Authorization required");
		}

		// load the Profile Entity
		String userId = user.getUserId();
		Key<Profile> key = Key.create(Profile.class, userId);
		Profile profile = (Profile) ofy().load().key(key).now();
		return profile;
	}

	/**
	 * Gets the Profile entity for the current user or creates it if it doesn't
	 * exist
	 *
	 * @param user
	 * @return user's Profile
	 */
	private static Profile getProfileFromUser(User user) {
		// First fetch the user's Profile from the datastore.
		Profile profile = ofy().load().key(Key.create(Profile.class, user.getUserId())).now();
		if (profile == null) {
			// Create a new Profile if it doesn't exist.
			String email = user.getEmail();
			profile = new Profile(user.getUserId(), email,
					new ProfileForm(extractDefaultDisplayNameFromEmail(email), "", ""));
		}
		return profile;
	}

	/**
	 * Creates a new Storage object and stores it to the datastore.
	 *
	 * @param user
	 *            A user who invokes this method, null when the user is not
	 *            signed in.
	 * @param storageForm
	 *            A storageForm object representing user's inputs.
	 * @return A newly created Storage Object.
	 * @throws UnauthorizedException
	 *             when the user is not signed in.
	 */
	@ApiMethod(name = "createStorage", path = "storage", httpMethod = HttpMethod.POST)
	public Storage createStorage(final User user, final StorageForm storageForm) throws UnauthorizedException {
		if (user == null) {
			throw new UnauthorizedException("Authorization required");
		}

		Storage storage = ofy().transact(new Work<Storage>() {
			@Override
			public Storage run() {
				// Get the userId of the logged in Profile
				String userId = user.getUserId();

				// Get the key for the Profile's Profile
				Key<Profile> profileKey = Key.create(Profile.class, userId);

				// Allocate a key for the storage -- let App Engine allocate
				// the ID
				// Don't forget to include the parent Profile in the allocated
				// ID
				final Key<Storage> storageKey = OfyService.factory().allocateId(profileKey, Storage.class);

				// Get the Storage Id from the Key
				final long storageId = storageKey.getId();

				// Get the existing Profile entity for the current user if there
				// is one
				// Otherwise create a new Profile entity with default values
				Profile profile = getProfileFromUser(user);

				// Create a new Storage Entity, specifying the user's Profile
				// entity
				// as the parent of the storage
				Storage storage = new Storage(storageId, userId, storageForm);

				// Save Storage and Profile Entities
				ofy().save().entities(storage, profile).now();

				return storage;
			}
		});

		// QueueFactory.getQueue("email-queue").add(ofy().getTransaction(),
		// TaskOptions.Builder.withUrl("/task/send_confirmation_email").param("email",
		// user.getEmail())
		// .param("storageInfo", storage.toString()));

		return storage;
	}

	@ApiMethod(name = "queryStorages", path = "queryStorages", httpMethod = HttpMethod.POST)
	public List<Storage> queryStorage(StorageQueryForm storageQueryForm) {
		Iterable<Storage> storageIterable = storageQueryForm.getQuery();
		List<Storage> result = new ArrayList<>();
		List<Key<Profile>> ownerKeyList = new ArrayList<>();
		for (Storage storage : storageIterable) {
			ownerKeyList.add(Key.create(Profile.class, storage.getOwnerUserId()));
			result.add(storage);
		}
		// To avoid separate datastore gets for each Storage, pre-fetch the
		// Profiles.
		ofy().load().keys(ownerKeyList);
		return result;
	}

	@ApiMethod(name = "getUserStorages", path = "getUserStorages", httpMethod = HttpMethod.GET)
	public List<Storage> getUserStorages(User user) throws UnauthorizedException {
		if (user == null) {
			throw new UnauthorizedException("Authorization required");
		}
		return ofy().load().type(Storage.class).ancestor(Key.create(Profile.class, user.getUserId())).order("name")
				.list();
	}

	@ApiMethod(name = "rentStorage", path = "storage/{websafeStorageKey}/rent", httpMethod = HttpMethod.POST)
	public WrappedBoolean rentStorage(final User user, @Named("websafeStorageKey") final String websafeStorageKey,
			final RentSessionForm sessionForm)
					throws UnauthorizedException, NotFoundException, ForbiddenException, ConflictException {
		// If not signed in, throw a 401 error.
		if (user == null) {
			throw new UnauthorizedException("Authorization required");
		}

		WrappedBoolean result = ofy().transact(new Work<WrappedBoolean>() {
			@Override
			public WrappedBoolean run() {
				try {
					// Get the user's Profile entity
					Profile profile = getProfileFromUser(user);

					// Get the storage key
					Key<Storage> storageKey = Key.create(websafeStorageKey);

					// Allocate a key for the storage -- let App Engine allocate
					// the ID
					// Don't forget to include the parent Profile in the
					// allocated
					// ID
					final Key<RentSession> rentSessionKey = OfyService.factory().allocateId(storageKey,
							RentSession.class);

					// TODO Has the user already rented this storage space,
					// is the space up for renting?

					RentSession rentSession = new RentSession(rentSessionKey.getId(), storageKey,
							Key.create(Profile.class, user.getUserId()).getString(), sessionForm);
					profile.addRentedStorageSessionKey(rentSessionKey.getString());

					// Save RentSession and Profile Entities
					ofy().save().entities(rentSession, profile).now();

					return new WrappedBoolean(true);

				} catch (Exception e) {
					return new WrappedBoolean(false, "Unknown exception");

				}
			}
		});
		// if result is false
		if (!result.getResult()) {
			if (result.getReason() == "Already registered") {
				throw new ConflictException("You have already registered");
			} else if (result.getReason() == "No seats available") {
				throw new ConflictException("There are no seats available");
			} else {
				throw new ForbiddenException("Unknown exception");
			}
		}
		return result;
	}

	/**
	 * Returns a collection of Storage Objects that the user rented
	 *
	 * @param user
	 *            An user who invokes this method, null when the user is not
	 *            signed in.
	 * @return a Collection of Storages that the user is going to attend.
	 * @throws UnauthorizedException
	 *             when the Profile object is null.
	 */
	@ApiMethod(name = "getStoragesRented", path = "getStoragesRented", httpMethod = HttpMethod.GET)
	public Collection<Storage> getStoragesRented(final User user) throws UnauthorizedException, NotFoundException {
		// If not signed in, throw a 401 error.
		if (user == null) {
			throw new UnauthorizedException("Authorization required");
		}
		Profile profile = ofy().load().key(Key.create(Profile.class, user.getUserId())).now();
		if (profile == null) {
			throw new NotFoundException("Profile doesn't exist.");
		}
		List<String> keyStringsRentedSessions = profile.getRentedStorageSessionKeys();
		List<Key<Storage>> keysToRent = new ArrayList<>();
		for (String keyString : keyStringsRentedSessions) {
			RentSession session = (RentSession) ofy().load().key(Key.create(keyString)).now();
			keysToRent.add(session.getStorageKey());
		}
		return ofy().load().keys(keysToRent).values();
	}

	@ApiMethod(name = "getStorage", path = "storage/{websafeStorageKey}", httpMethod = HttpMethod.GET)
	public Storage getStorage(@Named("websafeStorageKey") final String websafeStorageKey) throws NotFoundException {
		Key<Storage> key = Key.create(websafeStorageKey);
		Storage storage = ofy().load().key(key).now();
		if (storage == null)
			throw new NotFoundException("No Storage with key: " + websafeStorageKey);

		return storage;
	}

	public static class WrappedBoolean {

		private final Boolean result;
		private final String reason;

		public WrappedBoolean(Boolean result) {
			this.result = result;
			this.reason = "";
		}

		public WrappedBoolean(Boolean result, String reason) {
			this.result = result;
			this.reason = reason;
		}

		public Boolean getResult() {
			return result;
		}

		public String getReason() {
			return reason;
		}
	}
}
