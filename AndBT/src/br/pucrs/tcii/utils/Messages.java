package br.pucrs.tcii.utils;

public enum Messages {
		CONNECTION_FAILED(0),
		/** All threads got stopped (accept, connect, connected) */
		STOPPING_ALL_THREADS(1);

	private int value;

	private Messages(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}

	public int v() {
		return this.getValue();
	}
}