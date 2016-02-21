package org.teamhawk.storey.form;

import static org.teamhawk.storey.service.OfyService.ofy;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.teamhawk.storey.entity.Storage;

import com.google.api.server.spi.config.AnnotationBoolean;
import com.google.api.server.spi.config.ApiResourceProperty;
import com.google.common.collect.ImmutableList;
import com.googlecode.objectify.cmd.Query;

public class StorageQueryForm {
	private static final Logger LOG = Logger.getLogger(StorageQueryForm.class.getName());

	/**
	 * Enum representing a field type.
	 */
	public static enum FieldType {
		STRING, INTEGER, FLOATING_POINT
	}

	/**
	 * Enum representing a field.
	 */
	public static enum Field {
		VOLUME("volume", FieldType.FLOATING_POINT), PRICE_PER_DAY("pricePerDay", FieldType.STRING), STARTING_MONTH(
				"startingMonth", FieldType.INTEGER), OWNER_USER_ID("ownerUserId", FieldType.STRING);

		private String fieldName;

		private FieldType fieldType;

		private Field(String fieldName, FieldType fieldType) {
			this.fieldName = fieldName;
			this.fieldType = fieldType;
		}

		private String getFieldName() {
			return this.fieldName;
		}
	}

	/**
	 * Enum representing an operator.
	 */
	public static enum Operator {
		EQ("=="), LT("<"), GT(">"), LTEQ("<="), GTEQ(">="), NE("!=");

		private String queryOperator;

		private Operator(String queryOperator) {
			this.queryOperator = queryOperator;
		}

		private String getQueryOperator() {
			return this.queryOperator;
		}

		private boolean isInequalityFilter() {
			return this.queryOperator.contains("<") || this.queryOperator.contains(">")
					|| this.queryOperator.contains("!");
		}
	}

	/**
	 * A class representing a single filter for the query.
	 */
	public static class Filter {
		private Field field;
		private Operator operator;
		private String value;

		public Filter() {
		}

		public Filter(Field field, Operator operator, String value) {
			this.field = field;
			this.operator = operator;
			this.value = value;
		}

		public Field getField() {
			return field;
		}

		public Operator getOperator() {
			return operator;
		}

		public String getValue() {
			return value;
		}
	}

	/**
	 * A list of query filters.
	 */
	private List<Filter> filters = new ArrayList<>(0);

	/**
	 * Holds the first inequalityFilter for checking the feasibility of the
	 * whole query.
	 */
	@ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
	private Filter inequalityFilter;

	public StorageQueryForm() {
	}

	/**
	 * Checks the feasibility of the whole query.
	 */
	private void checkFilters() {
		for (Filter filter : this.filters) {
			if (filter.operator.isInequalityFilter()) {
				// Only one inequality filter is allowed.
				if (inequalityFilter != null && !inequalityFilter.field.equals(filter.field)) {
					throw new IllegalArgumentException("Inequality filter is allowed on only one field.");
				}
				inequalityFilter = filter;
			}
		}
	}

	/**
	 * Getter for filters.
	 *
	 * @return The List of filters.
	 */
	public List<Filter> getFilters() {
		return ImmutableList.copyOf(filters);
	}

	/**
	 * Adds a query filter.
	 *
	 * @param filter
	 *            A Filter object for the query.
	 * @return this for method chaining.
	 */
	public StorageQueryForm filter(Filter filter) {
		if (filter.operator.isInequalityFilter()) {
			// Only allows inequality filters on a single field.
			if (inequalityFilter != null && !inequalityFilter.field.equals(filter.field)) {
				throw new IllegalArgumentException("Inequality filter is allowed on only one field.");
			}
			inequalityFilter = filter;
		}
		filters.add(filter);
		return this;
	}

	/**
	 * Returns an Objectify Query object for the specified filters.
	 *
	 * @return an Objectify Query.
	 */
	@ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
	public Query<Storage> getQuery() {
		// First check the feasibility of inequality filters.
		checkFilters();
		Query<Storage> query = ofy().load().type(Storage.class);
		if (inequalityFilter == null) {
			// Order by name.
			query = query.order("name");
		} else {
			// If we have any inequality filters, order by the field first.
			query = query.order(inequalityFilter.field.getFieldName());
			query = query.order("name");
		}
		for (Filter filter : this.filters) {
			// Applies filters in order.
			if (filter.field.fieldType == FieldType.STRING) {
				query = query.filter(
						String.format("%s %s", filter.field.getFieldName(), filter.operator.getQueryOperator()),
						filter.value);
			} else if (filter.field.fieldType == FieldType.INTEGER) {
				query = query.filter(
						String.format("%s %s", filter.field.getFieldName(), filter.operator.getQueryOperator()),
						Integer.parseInt(filter.value));
			} else if (filter.field.fieldType == FieldType.FLOATING_POINT) {
				query = query.filter(
						String.format("%s %s", filter.field.getFieldName(), filter.operator.getQueryOperator()),
						Double.parseDouble(filter.value));
			}
			LOG.info(query.toString());
		}
		return query;
	}
}
