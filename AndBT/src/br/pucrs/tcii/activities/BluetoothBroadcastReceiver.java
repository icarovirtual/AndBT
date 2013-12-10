package br.pucrs.tcii.activities;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/** Receiver for Bluetooth changes */
public class BluetoothBroadcastReceiver extends BroadcastReceiver {
	/**
	 * Handles the received actions
	 * 
	 * @param context
	 *            current context
	 * @param intent
	 *            current intent
	 */
	@Override
	public void onReceive(final Context context, final Intent intent) {
		final String action = intent.getAction();

		if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
			onACL_CONNECTED(context, intent);
		} else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
			onACL_DISCONNECTED(context, intent);
		} else if (BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED.equals(action)) {
			onACL_DISCONNECT_REQUESTED(context, intent);
		} else if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
			onBOND_STATE_CHANGED(context, intent);
		} else if (BluetoothDevice.ACTION_CLASS_CHANGED.equals(action)) {
			onCLASS_CHANGED(context, intent);
		} else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
			onFOUND(context, intent);
		} else if (BluetoothDevice.ACTION_NAME_CHANGED.equals(action)) {
			onNAME_CHANGED(context, intent);
		} else if (BluetoothDevice.ACTION_UUID.equals(action)) {
			onUUID(context, intent);
		}
	}

	/**
	 * UUID action
	 * 
	 * @param context
	 *            current context
	 * @param intent
	 *            current intent
	 */
	public void onUUID(final Context context, final Intent intent) {
	}

	/**
	 * Name changed action
	 * 
	 * @param context
	 *            current context
	 * @param intent
	 *            current intent
	 */
	public void onNAME_CHANGED(final Context context, final Intent intent) {
	}

	/**
	 * Found action
	 * 
	 * @param context
	 *            current context
	 * @param intent
	 *            current intent
	 */
	public void onFOUND(final Context context, final Intent intent) {
	}

	/**
	 * Class changed action
	 * 
	 * @param context
	 *            current context
	 * @param intent
	 *            current intent
	 */
	public void onCLASS_CHANGED(final Context context, final Intent intent) {
	}

	/**
	 * Bond state changed action
	 * 
	 * @param context
	 *            current context
	 * @param intent
	 *            current intent
	 */
	public void onBOND_STATE_CHANGED(final Context context, final Intent intent) {
	}

	/**
	 * ACL disconnect requested action
	 * 
	 * @param context
	 *            current context
	 * @param intent
	 *            current intent
	 */
	public void onACL_DISCONNECT_REQUESTED(final Context context, final Intent intent) {
	}

	/**
	 * ACL disconnected action
	 * 
	 * @param context
	 *            current context
	 * @param intent
	 *            current intent
	 */
	public void onACL_DISCONNECTED(final Context context, final Intent intent) {
	}

	/**
	 * ACL connected action
	 * 
	 * @param context
	 *            current context
	 * @param intent
	 *            current intent
	 */
	public void onACL_CONNECTED(final Context context, final Intent intent) {
	}

	/**
	 * Discovery finished action
	 * 
	 * @param context
	 *            current context
	 * @param intent
	 *            current intent
	 */
	public void onDISCOVERY_FINISHED(final Context context, final Intent intent) {
	}
}
