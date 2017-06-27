package de.deeps.modules.rules;

/**
 * @author Deeps
 */

public class ValueCondition {

	private Comparator.Type comparator;
	private int index;
	private Object expectedValue;
	private Comparator.SupportedDataType dataType;

	public ValueCondition(Comparator.Type comparator, int index,
			String expectedValue, Comparator.SupportedDataType dataType) {
		this.comparator = comparator;
		this.index = index;
		this.dataType = dataType;
		this.expectedValue = Comparator.convert(dataType, expectedValue);
	}

	public Comparator.Type getComparator() {
		return comparator;
	}

	public int getIndex() {
		return index;
	}

	public Object getExpectedValue() {
		return expectedValue;
	}

	public Comparator.SupportedDataType getDataType() {
		return dataType;
	}

}