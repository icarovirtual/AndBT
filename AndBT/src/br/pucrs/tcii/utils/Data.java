package br.pucrs.tcii.utils;

public enum Data {
		DEVICE_NAME("DEVICE_NAME"),
		TOAST("TOAST"),
		MESSAGE("MESSAGE"),
		EVENT_TIME("CONNECTION_TIME");

	private String value;

	private Data(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

	public String v() {
		return this.getValue();
	}
}