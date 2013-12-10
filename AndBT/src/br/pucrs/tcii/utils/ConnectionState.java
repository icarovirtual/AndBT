package br.pucrs.tcii.utils;

/**
 * Constants that indicate the current connection state.
 */
public enum ConnectionState {
		NONE(0),
		FAILED(1),
		LOST(3);

	/** Numeric value of the enum */
	private int value;

	/**
	 * Create a new enum.
	 * 
	 * @param value
	 *            enum value
	 */
	private ConnectionState(final int value) {
		this.value = value;
	}

	/**
	 * Get the enum value.
	 * 
	 * @return the enum value
	 */
	public int getValue() {
		return this.value;
	}

	/**
	 * Return an enum from an int value.
	 * 
	 * @param val
	 *            int value
	 * @return int as an enum
	 */
	public static ConnectionState fromInt(final int val) {
		for (final ConnectionState state : ConnectionState.values()) {
			if (state.getValue() == val) {
				return state;
			}
		}
		return NONE;
	}

	/**
	 * Text representation of the enum.
	 * 
	 * @return enum as text
	 */
	@Override
	public String toString() {
		return this.value + " " + this.name();
	}
}
