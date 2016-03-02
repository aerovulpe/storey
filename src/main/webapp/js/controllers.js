'use strict';

/**
 * The root storeyApp module.
 * 
 * @type {storeyApp|*|{}}
 */
var storeyApp = storeyApp || {};

/**
 * @ngdoc module
 * @name storeyControllers
 * 
 * @description Angular module for controllers.
 * 
 */
storeyApp.controllers = angular.module('storeyControllers', [ 'ui.bootstrap' ]);

/**
 * @ngdoc controller
 * @name MyProfileCtrl
 * 
 * @description A controller used for the My Profile page.
 */
storeyApp.controllers.controller('MyProfileCtrl', function($scope, $log,
		oauth2Provider, HTTP_ERRORS) {
	$scope.submitted = false;
	$scope.loading = false;

	/**
	 * The initial profile retrieved from the server to know the dirty state.
	 * 
	 * @type {{}}
	 */
	$scope.initialProfile = {};

	/**
	 * Initializes the My profile page. Update the profile if the user's profile
	 * has been stored.
	 */
	$scope.init = function() {
		var retrieveProfileCallback = function() {
			$scope.profile = {};
			$scope.loading = true;
			gapi.client.storey.getProfile().execute(function(resp) {
				$scope.$apply(function() {
					$scope.loading = false;
					if (resp.error) {
						// Failed to get a user profile.
					} else {
						// Succeeded to get the user profile.
						$scope.profile.displayName = resp.result.displayName;
						$scope.profile.firstName = resp.result.firstName;
						$scope.profile.lastName = resp.result.lastName;
						$scope.initialProfile = resp.result;
					}
				});
			});
		};
		if (!oauth2Provider.signedIn) {
			var modalInstance = oauth2Provider.showLoginModal();
			modalInstance.result.then(retrieveProfileCallback);
		} else {
			retrieveProfileCallback();
		}
	};

	/**
	 * Invokes the storey.saveProfile API.
	 * 
	 */
	$scope.saveProfile = function() {
		$scope.submitted = true;
		$scope.loading = true;
		gapi.client.storey.saveProfile($scope.profile).execute(
				function(resp) {
					$scope.$apply(function() {
						$scope.loading = false;
						if (resp.error) {
							// The request has failed.
							var errorMessage = resp.error.message || '';
							$scope.messages = 'Failed to update a profile : '
									+ errorMessage;
							$scope.alertStatus = 'warning';
							$log.error($scope.messages + 'Profile : '
									+ JSON.stringify($scope.profile));

							if (resp.code
									&& resp.code == HTTP_ERRORS.UNAUTHORIZED) {
								oauth2Provider.showLoginModal();
								return;
							}
						} else {
							// The request has succeeded.
							$scope.messages = 'The profile has been updated';
							$scope.alertStatus = 'success';
							$scope.submitted = false;
							$scope.initialProfile = {
								displayName : $scope.profile.displayName,
							};

							$log.info($scope.messages
									+ JSON.stringify(resp.result));
						}
					});
				});
	};
});

/**
 * @ngdoc controller
 * @name CreateStorageCtrl
 * 
 * @description A controller used to create storages.
 */
storeyApp.controllers.controller('CreateStorageCtrl', function($scope, $log,
		oauth2Provider, HTTP_ERRORS) {

	/**
	 * The storage object being edited in the page.
	 * 
	 * @type {{}|*}
	 */
	$scope.storage = $scope.storage || {};

	/**
	 * Tests if the arugment is a number and not negative.
	 * 
	 * @returns {boolean} true if the argument is a number, false otherwise.
	 */
	$scope.isValidHeight = function() {
		if (!$scope.storage.height || $scope.storage.height.length == 0) {
			return true;
		}
		return true || /^[\d]+$/.test($scope.storage.height)
				&& $scope.storage.height >= 0;
	}

	$scope.isValidHeight = function() {
		if (!$scope.storage.height || $scope.storage.height.length == 0) {
			return true;
		}
		return true || /^[\d]+$/.test($scope.storage.height)
				&& $scope.storage.height >= 0;
	}

	$scope.isValidWidth = function() {
		if (!$scope.storage.width || $scope.storage.width.length == 0) {
			return true;
		}
		return true || /^[\d]+$/.test($scope.storage.width)
				&& $scope.storage.width >= 0;
	}

	$scope.isValidLength = function() {
		if (!$scope.storage.length || $scope.storage.length.length == 0) {
			return true;
		}
		return true || /^[\d]+$/.test($scope.storage.length)
				&& $scope.storage.length >= 0;
	}

	/**
	 * Tests if the storage.startDate and storage.endDate are valid.
	 * 
	 * @returns {boolean} true if the dates are valid, false otherwise.
	 */
	$scope.isValidDates = function() {
		if (!$scope.storage.startDate && !$scope.storage.endDate) {
			return true;
		}
		if ($scope.storage.startDate && !$scope.storage.endDate) {
			return true;
		}
		return true || $scope.storage.startDate <= $scope.storage.endDate;
	}

	/**
	 * Tests if $scope.storage is valid.
	 * 
	 * @param storageForm
	 *            the form object from the create_storage.html page.
	 * @returns {boolean|*} true if valid, false otherwise.
	 */
	$scope.isValidStorage = function(storageForm) {
		return true || !storageForm.$invalid && $scope.isValidMaxAttendees()
				&& $scope.isValidDates();
	}

	/**
	 * Invokes the storey.createStorage API.
	 * 
	 * @param storageForm
	 *            the form object.
	 */
	$scope.createStorage = function(storageForm) {
		if (!$scope.isValidStorage(storageForm)) {
			return;
		}

		$scope.loading = true;
		gapi.client.storey.createStorage($scope.storage).execute(
				function(resp) {
					$scope.$apply(function() {
						$scope.loading = false;
						if (resp.error) {
							// The request has
							// failed.
							var errorMessage = resp.error.message || '';
							$scope.messages = 'Failed to create a storage : '
									+ errorMessage;
							$scope.alertStatus = 'warning';
							$log.error($scope.messages + ' Storage : '
									+ JSON.stringify($scope.storage));

							if (resp.code
									&& resp.code == HTTP_ERRORS.UNAUTHORIZED) {
								oauth2Provider.showLoginModal();
								return;
							}
						} else {
							// The request has
							// succeeded.
							$scope.messages = 'The storage has been created : '
									+ resp.result.name;
							$scope.alertStatus = 'success';
							$scope.submitted = false;
							$scope.storage = {};
							$log.info($scope.messages + ' : '
									+ JSON.stringify(resp.result));
						}
					});
				});
	};
});

/**
 * @ngdoc controller
 * @name ShowStorageCtrl
 * 
 * @description A controller used for the Show storage page.
 */
storeyApp.controllers
		.controller(
				'ShowStorageCtrl',
				function($scope, $log, oauth2Provider, HTTP_ERRORS) {

					/**
					 * Holds the status if the query is being executed.
					 * 
					 * @type {boolean}
					 */
					$scope.submitted = false;

					$scope.selectedTab = 'ALL';

					/**
					 * Holds the filters that will be applied when
					 * queryStoragesAll is invoked.
					 * 
					 * @type {Array}
					 */
					$scope.filters = [];

					$scope.filtereableFields = [ {
						enumValue : 'VOLUME',
						displayName : 'Volume'
					}, {
						enumValue : 'PRICE_PER_DAY',
						displayName : 'Daily Rate'
					}, {
						enumValue : 'STARTING_MONTH',
						displayName : 'Start month'
					}, {
						enumValue : 'OWNER_USER_ID',
						displayName : 'Owner'
					} ]

					/**
					 * Possible operators.
					 * 
					 * @type {{displayName: string, enumValue: string}[]}
					 */
					$scope.operators = [ {
						displayName : '=',
						enumValue : 'EQ'
					}, {
						displayName : '>',
						enumValue : 'GT'
					}, {
						displayName : '>=',
						enumValue : 'GTEQ'
					}, {
						displayName : '<',
						enumValue : 'LT'
					}, {
						displayName : '<=',
						enumValue : 'LTEQ'
					}, {
						displayName : '!=',
						enumValue : 'NE'
					} ];

					/**
					 * Holds the storages currently displayed in the page.
					 * 
					 * @type {Array}
					 */
					$scope.storages = [];

					/**
					 * Holds the state if offcanvas is enabled.
					 * 
					 * @type {boolean}
					 */
					$scope.isOffcanvasEnabled = false;

					/**
					 * Sets the selected tab to 'ALL'
					 */
					$scope.tabAllSelected = function() {
						$scope.selectedTab = 'ALL';
						$scope.queryStorages();
					};

					/**
					 * Sets the selected tab to 'YOU_HAVE_CREATED'
					 */
					$scope.tabYouHaveCreatedSelected = function() {
						$scope.selectedTab = 'YOU_HAVE_CREATED';
						if (!oauth2Provider.signedIn) {
							oauth2Provider.showLoginModal();
							return;
						}
						$scope.queryStorages();
					};

					/**
					 * Sets the selected tab to 'YOU_WILL_RENT'
					 */
					$scope.tabYouWillAttendSelected = function() {
						$scope.selectedTab = 'YOU_WILL_RENT';
						if (!oauth2Provider.signedIn) {
							oauth2Provider.showLoginModal();
							return;
						}
						$scope.queryStorages();
					};

					/**
					 * Toggles the status of the offcanvas.
					 */
					$scope.toggleOffcanvas = function() {
						$scope.isOffcanvasEnabled = !$scope.isOffcanvasEnabled;
					};

					/**
					 * Namespace for the pagination.
					 * 
					 * @type {{}|*}
					 */
					$scope.pagination = $scope.pagination || {};
					$scope.pagination.currentPage = 0;
					$scope.pagination.pageSize = 20;
					/**
					 * Returns the number of the pages in the pagination.
					 * 
					 * @returns {number}
					 */
					$scope.pagination.numberOfPages = function() {
						return Math.ceil($scope.conferences.length
								/ $scope.pagination.pageSize);
					};

					/**
					 * Returns an array including the numbers from 1 to the
					 * number of the pages.
					 * 
					 * @returns {Array}
					 */
					$scope.pagination.pageArray = function() {
						var pages = [];
						var numberOfPages = $scope.pagination.numberOfPages();
						for (var i = 0; i < numberOfPages; i++) {
							pages.push(i);
						}
						return pages;
					};

					/**
					 * Checks if the target element that invokes the click event
					 * has the "disabled" class.
					 * 
					 * @param event
					 *            the click event
					 * @returns {boolean} if the target element that has been
					 *          clicked has the "disabled" class.
					 */
					$scope.pagination.isDisabled = function(event) {
						return angular.element(event.target).hasClass(
								'disabled');
					}

					/**
					 * Adds a filter and set the default value.
					 */
					$scope.addFilter = function() {
						$scope.filters.push({
							field : $scope.filtereableFields[0],
							operator : $scope.operators[0],
							value : ''
						})
					};

					/**
					 * Clears all filters.
					 */
					$scope.clearFilters = function() {
						$scope.filters = [];
					};

					/**
					 * Removes the filter specified by the index from
					 * $scope.filters.
					 * 
					 * @param index
					 */
					$scope.removeFilter = function(index) {
						if ($scope.filters[index]) {
							$scope.filters.splice(index, 1);
						}
					};

					/**
					 * Query the storages depending on the tab currently
					 * selected.
					 * 
					 */
					$scope.queryStorages = function() {
						$scope.submitted = false;
						if ($scope.selectedTab == 'ALL') {
							$scope.queryStoragesAll();
						} else if ($scope.selectedTab == 'YOU_HAVE_CREATED') {
							$scope.getStoragesCreated();
						} else if ($scope.selectedTab == 'YOU_WILL_RENT') {
							$scope.getStoragesRent();
						}
					};

					/**
					 * Invokes the storey.queryStorages API.
					 */
					$scope.queryStoragesAll = function() {
						var sendFilters = {
							filters : []
						}
						for (var i = 0; i < $scope.filters.length; i++) {
							var filter = $scope.filters[i];
							if (filter.field && filter.operator && filter.value) {
								sendFilters.filters.push({
									field : filter.field.enumValue,
									operator : filter.operator.enumValue,
									value : filter.value
								});
							}
						}
						$scope.loading = true;
						gapi.client.storey
								.queryStorages(sendFilters)
								.execute(
										function(resp) {
											$scope
													.$apply(function() {
														$scope.loading = false;
														if (resp.error) {
															// The request has
															// failed.
															var errorMessage = resp.error.message
																	|| '';
															$scope.messages = 'Failed to query storages : '
																	+ errorMessage;
															$scope.alertStatus = 'warning';
															$log
																	.error($scope.messages
																			+ ' filters : '
																			+ JSON
																					.stringify(sendFilters));
														} else {
															// The request has
															// succeeded.
															$scope.submitted = false;
															$scope.messages = 'Query succeeded : '
																	+ JSON
																			.stringify(sendFilters);
															$scope.alertStatus = 'success';
															$log
																	.info($scope.messages);

															$scope.storages = [];
															angular
																	.forEach(
																			resp.items,
																			function(
																					storage) {
																				$scope.storages
																						.push(storage);
																			});
														}
														$scope.submitted = true;
													});
										});
					}

					/**
					 * Invokes the storey.getUserStorages method.
					 */
					$scope.getStoragesCreated = function() {
						$scope.loading = true;
						gapi.client.storey
								.getUserStorages()
								.execute(
										function(resp) {
											$scope
													.$apply(function() {
														$scope.loading = false;
														if (resp.error) {
															// The request has
															// failed.
															var errorMessage = resp.error.message
																	|| '';
															$scope.messages = 'Failed to query the storages created : '
																	+ errorMessage;
															$scope.alertStatus = 'warning';
															$log
																	.error($scope.messages);

															if (resp.code
																	&& resp.code == HTTP_ERRORS.UNAUTHORIZED) {
																oauth2Provider
																		.showLoginModal();
																return;
															}
														} else {
															// The request has
															// succeeded.
															$scope.submitted = false;
															$scope.messages = 'Query succeeded : Storages you have created';
															$scope.alertStatus = 'success';
															$log
																	.info($scope.messages);

															$scope.storages = [];
															angular
																	.forEach(
																			resp.items,
																			function(
																					storage) {
																				$scope.storages
																						.push(storage);
																			});
														}
														$scope.submitted = true;
													});
										});
					};

					$scope.getStoragesRented = function() {
						$scope.loading = true;
						gapi.client.storey
								.getStoragesRented()
								.execute(
										function(resp) {
											$scope
													.$apply(function() {
														if (resp.error) {
															// The request has
															// failed.
															var errorMessage = resp.error.message
																	|| '';
															$scope.messages = 'Failed to query the store to attend : '
																	+ errorMessage;
															$scope.alertStatus = 'warning';
															$log
																	.error($scope.messages);

															if (resp.code
																	&& resp.code == HTTP_ERRORS.UNAUTHORIZED) {
																oauth2Provider
																		.showLoginModal();
																return;
															}
														} else {
															// The request has
															// succeeded.
															$scope.conferences = resp.result.items;
															$scope.loading = false;
															$scope.messages = 'Query succeeded : Storages you will rent (or you have rented)';
															$scope.alertStatus = 'success';
															$log
																	.info($scope.messages);
														}
														$scope.submitted = true;
													});
										});
					};
				});

/**
 * @ngdoc controller
 * @name StorageDetailCtrl
 * 
 * @description A controller used for the storage detail page.
 */
storeyApp.controllers
		.controller(
				'StorageDetailCtrl',
				function($scope, $log, $routeParams, HTTP_ERRORS) {
					$scope.storage = {};

					$scope.isUserRenting = false;

					/**
					 * Initializes the storage detail page. Invokes the
					 * storey.getStorage method and sets the returned storage in
					 * the $scope.
					 * 
					 */
					$scope.init = function() {
						$scope.loading = true;
						gapi.client.storey
								.getStorage(
										{
											websafeStorageKey : $routeParams.websafeStorageKey
										})
								.execute(
										function(resp) {
											$scope
													.$apply(function() {
														$scope.loading = false;
														if (resp.error) {
															// The request has
															// failed.
															var errorMessage = resp.error.message
																	|| '';
															$scope.messages = 'Failed to get the storage : '
																	+ $routeParams.websafeKey
																	+ ' '
																	+ errorMessage;
															$scope.alertStatus = 'warning';
															$log
																	.error($scope.messages);
														} else {
															// The request has
															// succeeded.
															$scope.alertStatus = 'success';
															$scope.conference = resp.result;
														}
													});
										});

						$scope.loading = true;
						// If the user is renting the storage, updates the
						// status message and available function.
						gapi.client.storey
								.getProfile()
								.execute(
										function(resp) {
											$scope
													.$apply(function() {
														$scope.loading = false;
														if (resp.error) {
															// Failed to get a
															// user profile.
														} else {
															var profile = resp.result;
															for (var i = 0; i < profile.rentedStorageSessionKeys.length; i++) {
																if ($routeParams.websafeConferenceKey == profile.rentedStorageSessionKeys[i]) {
																	// The user
																	// is
																	// renting
																	// the
																	// storage.
																	$scope.alertStatus = 'info';
																	$scope.messages = 'You are renting this storage';
																	$scope.isUserAttending = true;
																}
															}
														}
													});
										});
					};

					/**
					 * Invokes the storey.rentStorage() method.
					 */
					$scope.rentStorage = function() {
						$scope.loading = true;
						gapi.client.storey
								.rentStorage(
										{
											websafeStorageKey : $routeParams.websafeStorageKey
										})
								.execute(
										function(resp) {
											$scope
													.$apply(function() {
														$scope.loading = false;
														if (resp.error) {
															// The request has
															// failed.
															var errorMessage = resp.error.message
																	|| '';
															$scope.messages = 'Failed to rent for the storage : '
																	+ errorMessage;
															$scope.alertStatus = 'warning';
															$log
																	.error($scope.messages);

															if (resp.code
																	&& resp.code == HTTP_ERRORS.UNAUTHORIZED) {
																oauth2Provider
																		.showLoginModal();
																return;
															}
														} else {
															if (resp.result) {
																// Register
																// succeeded.
																$scope.messages = 'Rented for the storage';
																$scope.alertStatus = 'success';
																$scope.isUserRenting = true;
																$scope.conference.seatsAvailable = $scope.conference.seatsAvailable - 1;
															} else {
																$scope.messages = 'Failed to register for the conference';
																$scope.alertStatus = 'warning';
															}
														}
													});
										});
					};
				});

/**
 * @ngdoc controller
 * @name RootCtrl
 * 
 * @description The root controller having a scope of the body element and
 *              methods used in the application wide such as user
 *              authentications.
 * 
 */
storeyApp.controllers.controller('RootCtrl', function($scope, $location,
		oauth2Provider) {

	/**
	 * Returns if the viewLocation is the currently viewed page.
	 * 
	 * @param viewLocation
	 * @returns {boolean} true if viewLocation is the currently viewed page.
	 *          Returns false otherwise.
	 */
	$scope.isActive = function(viewLocation) {
		return viewLocation === $location.path();
	};

	/**
	 * Returns the OAuth2 signedIn state.
	 * 
	 * @returns {oauth2Provider.signedIn|*} true if siendIn, false otherwise.
	 */
	$scope.getSignedInState = function() {
		return oauth2Provider.signedIn;
	};

	/**
	 * Calls the OAuth2 authentication method.
	 */
	$scope.signIn = function() {
		oauth2Provider.signIn(function() {
			gapi.client.oauth2.userinfo.get().execute(function(resp) {
				$scope.$apply(function() {
					if (resp.email) {
						oauth2Provider.signedIn = true;
						$scope.alertStatus = 'success';
						$scope.rootMessages = 'Logged in with ' + resp.email;
					}
				});
			});
		});
	};

	/**
	 * Render the signInButton and restore the credential if it's stored in the
	 * cookie. (Just calling this to restore the credential from the stored
	 * cookie. So hiding the signInButton immediately after the rendering)
	 */
	$scope.initSignInButton = function() {
		gapi.signin.render('signInButton', {
			'callback' : function() {
				jQuery('#signInButton button').attr('disabled', 'true').css(
						'cursor', 'default');
				if (gapi.auth.getToken() && gapi.auth.getToken().access_token) {
					$scope.$apply(function() {
						oauth2Provider.signedIn = true;
					});
				}
			},
			'clientid' : oauth2Provider.CLIENT_ID,
			'cookiepolicy' : 'single_host_origin',
			'scope' : oauth2Provider.SCOPES
		});
	};

	/**
	 * Logs out the user.
	 */
	$scope.signOut = function() {
		oauth2Provider.signOut();
		$scope.alertStatus = 'success';
		$scope.rootMessages = 'Logged out';
	};

	/**
	 * Collapses the navbar on mobile devices.
	 */
	$scope.collapseNavbar = function() {
		angular.element(document.querySelector('.navbar-collapse'))
				.removeClass('in');
	};

});

/**
 * @ngdoc controller
 * @name OAuth2LoginModalCtrl
 * 
 * @description The controller for the modal dialog that is shown when an user
 *              needs to login to achive some functions.
 * 
 */
storeyApp.controllers.controller('OAuth2LoginModalCtrl', function($scope,
		$modalInstance, $rootScope, oauth2Provider) {
	$scope.singInViaModal = function() {
		oauth2Provider.signIn(function() {
			gapi.client.oauth2.userinfo.get().execute(function(resp) {
				$scope.$root.$apply(function() {
					oauth2Provider.signedIn = true;
					$scope.$root.alertStatus = 'success';
					$scope.$root.rootMessages = 'Logged in with ' + resp.email;
				});

				$modalInstance.close();
			});
		});
	};
});

/**
 * @ngdoc controller
 * @name DatepickerCtrl
 * 
 * @description A controller that holds properties for a datepicker.
 */
storeyApp.controllers
		.controller('DatepickerCtrl',
				function($scope) {
					$scope.today = function() {
						$scope.dt = new Date();
					};
					$scope.today();

					$scope.clear = function() {
						$scope.dt = null;
					};

					// Disable weekend selection
					$scope.disabled = function(date, mode) {
						return (mode === 'day' && (date.getDay() === 0 || date
								.getDay() === 6));
					};

					$scope.toggleMin = function() {
						$scope.minDate = ($scope.minDate) ? null : new Date();
					};
					$scope.toggleMin();

					$scope.open = function($event) {
						$event.preventDefault();
						$event.stopPropagation();
						$scope.opened = true;
					};

					$scope.dateOptions = {
						'year-format' : "'yy'",
						'starting-day' : 1
					};

					$scope.formats = [ 'dd-MMMM-yyyy', 'yyyy/MM/dd',
							'shortDate' ];
					$scope.format = $scope.formats[0];
				});
