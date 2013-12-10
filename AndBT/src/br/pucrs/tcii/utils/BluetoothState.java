package br.pucrs.tcii.utils;

/**
 * Constants that indicate the current connection state.
 */
public enum BluetoothState {
		/** We're doing nothing */
		NONE(0),
		/** Listening for incoming */
		LISTEN(1),
		/** Initiating an outgoing */
		CONNECTING(2),
		/** Connected to a remote device */
		CONNECTED(3);

	/** Numeric value of the enum */
	private int value;

	/**
	 * Create a new enum.
	 * 
	 * @param value
	 *            enum value
	 */
	private BluetoothState(final int value) {
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
	public static BluetoothState fromInt(final int val) {
		for (final BluetoothState state : BluetoothState.values()) {
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

	public int v() {
		return this.getValue();
	}
}
