package br.pucrs.tcii.handlers;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import br.pucrs.tcii.utils.BluetoothState;
import br.pucrs.tcii.utils.Data;
import br.pucrs.tcii.utils.What;

/**
 * Handles Bluetooth related messages processing.
 */
public abstract class BluetoothHandler extends Handler {
	/** Logging tag for the class */
	private static final String TAG = BluetoothHandler.class.getName();

	@Override
	public void handleMessage(final Message msg) {
		final BluetoothState state = BluetoothState.fromInt(msg.what);
		Bundle data = null;
		if (msg != null) {
			data = msg.getData();
		}

		What what = What.fromInt(msg.what);
		switch (what) {
		case CONNECTED_TO: {
			String deviceAddress = data.getString(Data.DEVICE_NAME.v());
			float connectionTime = data.getFloat(Data.EVENT_TIME.v());
			onCONNECTED_TO(state, msg, deviceAddress, connectionTime);
			break;
		}
		case ERROR: {
			String deviceAddress = data.getString(Data.DEVICE_NAME.v());
			float errorTime = data.getFloat(Data.EVENT_TIME.v());
			int errorMessage = data.getInt(Data.MESSAGE.v());
			onERROR(state, msg, errorMessage, deviceAddress, errorTime);
			break;
		}
		case NOTIFICATION: {
			float errorTime = data.getFloat(Data.EVENT_TIME.v());
			int message = data.getInt(Data.MESSAGE.v());
			onNOTIFICATION(state, msg, message, errorTime);
			break;
		}
		case READ_DATA:
			onREAD_DATA(state, msg);
			break;
		case READ_BEGIN:
			onREAD_BEGIN(state, msg);
			break;
		case READ_TURN:
			onREAD_TURN(state, msg);
			break;
		case READ_REAL_TIME:
			onREAD_REAL_TIME(state, msg);
			break;
		case READ_FINISH:
			onREAD_FINISH(state, msg);
			break;
		case WRITE_BEGIN:
			onWRITE_BEGIN(state, msg);
			break;
		case WRITE_DATA:
			onWRITE_DATA(state, msg);
			break;
		case WRITE_TURN:
			onWRITE_TURN(state, msg);
			break;
		case WRITE_REAL_TIME:
			onWRITE_REAL_TIME(state, msg);
			break;
		case WRITE_FINISH:
			onWRITE_FINISH(state, msg);
			break;
		default:
			// TODO error unexpected message arrived
			break;
		}
	}

	// //////////////////////////////////////////////
	// Methods to be implemented by the client app
	// //////////////////////////////////////////////

	public void onNOTIFICATION(BluetoothState state, Message msg, int message, float errorTime) {
	}

	public void onERROR(BluetoothState state, Message msg, int errorMessage, String deviceAddress, float errorTime) {
	}

	public void onCONNECTED_TO(BluetoothState state, Message msg, String deviceAddress, float connectionTime) {
	}

	/**
	 * Received beginning of the session.
	 * 
	 * @param state
	 *            Bluetooth state
	 * @param msg
	 *            received message
	 */
	public void onWRITE_BEGIN(BluetoothState state, Message msg) {
	}

	/**
	 * Sent end of the session.
	 * 
	 * @param state
	 *            Bluetooth state
	 * @param msg
	 *            received message
	 */
	public void onWRITE_FINISH(BluetoothState state, Message msg) {
	}

	/**
	 * Received beginning of the session.
	 * 
	 * @param state
	 *            Bluetooth state
	 * @param msg
	 *            received message
	 */
	public void onREAD_BEGIN(BluetoothState state, Message msg) {
	}

	/**
	 * Received end of the session.
	 * 
	 * @param state
	 *            Bluetooth state
	 * @param msg
	 *            received message
	 */
	public void onREAD_FINISH(BluetoothState state, Message msg) {
	}

	/**
	 * Data is being sent.
	 * 
	 * @param state
	 *            Bluetooth state
	 * @param msg
	 *            received message
	 */
	public void onWRITE_DATA(final BluetoothState state, final Message msg) {
	}

	/**
	 * Turn data is being sent.
	 * 
	 * @param state
	 * @param msg
	 */
	public void onWRITE_TURN(BluetoothState state, Message msg) {
	}

	/**
	 * Real time data is being sent.
	 * 
	 * @param state
	 * @param msg
	 */
	public void onWRITE_REAL_TIME(BluetoothState state, Message msg) {
	}

	/**
	 * Message is being received.
	 * 
	 * @param data
	 *            data received
	 */
	public void onREAD_DATA(BluetoothState state, Message msg) {
	}

	/**
	 * Turn data is being read.
	 * 
	 * @param data
	 *            data received
	 */
	public void onREAD_TURN(BluetoothState state, Message msg) {
		throw new UnsupportedOperationException(TAG + ": onREAD_TURN not implemented");
	}

	/**
	 * Real time data is being read.
	 * 
	 * @param state
	 * @param msg
	 */
	public void onREAD_REAL_TIME(BluetoothState state, Message msg) {
		throw new UnsupportedOperationException(TAG + ": onREAD_REAL_TIME not implemented");
	}

	public Message obtainMessage(What notification) {
		return super.obtainMessage(notification.v());
	}

	public Message obtainMessage(What readData, int bytes, int i, byte[] buffer) {
		return super.obtainMessage(readData.v(), bytes, i, buffer);
	}

}
