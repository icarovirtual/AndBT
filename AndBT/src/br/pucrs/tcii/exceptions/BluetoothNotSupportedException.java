package br.pucrs.tcii.exceptions;

/** Bluetooth is not supported on the device */
public class BluetoothNotSupportedException extends Exception {
	private static final long serialVersionUID = 1L;

	public BluetoothNotSupportedException(final String string) {
		super(string);
	}
}
