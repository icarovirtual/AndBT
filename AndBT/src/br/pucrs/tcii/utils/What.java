package br.pucrs.tcii.utils;

public enum What {
		ERROR(0),
		CONNECTED_TO(1),
		NOTIFICATION(2),

		/** Request connect */
		REQUEST_CONNECT_DEVICE(3),
		/** Request Bluetooth enabling */
		REQUEST_ENABLE_BT(4),

		
		READ_BEGIN(20),
		/** Read any data */
		READ_DATA(21),
		/** Read data for turn handling */
		READ_TURN(22),
		/** Read data for real time handling */
		READ_REAL_TIME(23),
		
		READ_FINISH(24),

		
		WRITE_BEGIN(30),
		/** Write any data */
		WRITE_DATA(31),
		/** Write data for turn handling */
		WRITE_TURN(32),
		/** Write data for real time handling */
		WRITE_REAL_TIME(33),
		
		WRITE_FINISH(34),

		/** Numeric difference between Write and Read constants */
		WRITE_READ_DIF(-10);


	private int value;

	private What(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}

	public int v() {
		return this.getValue();
	}

	public static What fromInt(int val) {
		for (What what : values()) {
			if (what.v() == val) {
				return what;
			}
		}

		// TODO throws an exception?
		return null;
	}
}