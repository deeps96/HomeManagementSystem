package de.deeps.modules.rules;

/**
 * @author Deeps
 */

public class Comparator {

	public static enum SupportedDataType {
		String, Integer, Boolean
	};

	public static enum Type {
		EQUALS
	};

	public static Object convert(SupportedDataType dataType, String value) {
		switch (dataType) {
			case Integer:
				return Integer.parseInt(value);
			case Boolean:
				return Boolean.parseBoolean(value);
			default:
				return value;
		}
	}

	public static boolean compare(ValueCondition condition, Object value) {
		switch (condition.getDataType()) {
			case String:
				return compare(
					condition.getComparator(),
					(String) condition.getExpectedValue(),
					(String) value);
			case Integer:
				return compare(
					condition.getComparator(),
					(int) condition.getExpectedValue(),
					(int) value);
			case Boolean:
				return compare(
					condition.getComparator(),
					(boolean) condition.getExpectedValue(),
					(boolean) value);
			default:
				return false;
		}
	}

	private static boolean compare(Type comparator, int expectedValue,
			int value) {
		switch (comparator) {
			case EQUALS:
				return expectedValue == value;
			default:
				return false;
		}
	}

	private static boolean compare(Type comparator, String expectedValue,
			String value) {
		switch (comparator) {
			case EQUALS:
				return expectedValue.equals(value);
			default:
				return false;
		}
	}

	private static boolean compare(Type comparator, boolean expectedValue,
			boolean value) {
		switch (comparator) {
			case EQUALS:
				return expectedValue == value;
			default:
				return false;
		}
	}

}
