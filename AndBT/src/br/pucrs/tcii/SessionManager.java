package br.pucrs.tcii;

import java.util.Set;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import br.pucrs.tcii.activities.BluetoothBroadcastReceiver;
import br.pucrs.tcii.datahandling.Serializer;
import br.pucrs.tcii.exceptions.BluetoothNotConnectedExeption;
import br.pucrs.tcii.exceptions.BluetoothNotSupportedException;
import br.pucrs.tcii.handlers.BluetoothHandler;
import br.pucrs.tcii.models.GameControl;
import br.pucrs.tcii.utils.BluetoothState;
import br.pucrs.tcii.utils.Data;
import br.pucrs.tcii.utils.OnActivityResult;
import br.pucrs.tcii.utils.What;

/**
 * This main class of the framework. Using this, the applications can have
 * access with all the Bluetooth functionalities AndBT offers.
 */

public class SessionManager {
	/** Tag for logging */
	private static final String TAG = SessionManager.class.getName();

	/** Singleton instance */
	private static SessionManager instance;

	/** Bluetooth service */
	private BluetoothService serviceBT = null;

	/** Implementation of turn and real time functions */
	private GameControl gameControl;
	/** Handler to start functions with a delay */
	private Handler handlerRealtime;
	/** Will write the messages in realtime */
	private RealtimeRunnable runnnableRealtime;
	/** Control for real time messaging */
	private boolean isSendingRealtime;

	/** Serializer to convert data to bytes */
	private Serializer serializer;

	/**
	 * Initialize a Bluetooth session using the default JSON serializer.
	 * 
	 * @param handlerBT
	 *            handler to send messages back to the UI
	 * @param appName
	 *            identifier of the application for the Bluetooth services
	 * @param maxPeers
	 *            maximum number of clients that will connect to the Bluetooth
	 *            server</br> minimum of 1, maximum of 7
	 * @throws BluetoothNotSupportedException
	 */
	public static void initialize(final BluetoothHandler handlerBT, final String appName, int maxConn) throws BluetoothNotSupportedException {
		initialize(handlerBT, appName, maxConn, null);
	}

	/**
	 * Initialize a Bluetooth session with a custom serializer.
	 * 
	 * @param handlerBT
	 *            handler for Bluetooth messages
	 * @param appName
	 * @param maxConn
	 *            handler to send messages back to the UI
	 * @param appName
	 *            identifier of the application for the Bluetooth services
	 * @param maxPeers
	 *            maximum number of clients that will connect to the Bluetooth
	 *            server</br> minimum of 1, maximum of 7
	 * @param serializer
	 *            serializer object to convert data
	 * @throws BluetoothNotSupportedException
	 */
	public static void initialize(final BluetoothHandler handlerBT, final String appName, int maxConn, Serializer serializer) throws BluetoothNotSupportedException {
		if (SessionManager.instance != null) {
			throw new IllegalStateException("Instance already initialized!");
		}

		SessionManager.instance = new SessionManager(handlerBT, appName, maxConn, serializer);

	}

	/**
	 * Checks if the class instance was initialized.
	 * 
	 * @return session state
	 */
	public static boolean isInitialized() {
		return SessionManager.instance != null;
	}

	/**
	 * Get the class singleton instance.
	 * 
	 * @return class instance
	 */
	public static SessionManager getInstance() {
		if (SessionManager.instance == null) {
			throw new IllegalStateException("Instance not initialized yet!");
		}

		return SessionManager.instance;
	}

	/**
	 * Private constructor.
	 * 
	 * @param handlerBT
	 *            current state changed handler
	 * @param appName
	 * @param maxConn
	 *            handler to send messages back to the UI
	 * @param appName
	 *            identifier of the application for the Bluetooth services
	 * @param serializer
	 *            serializer object to convert data
	 * @param maxPeers
	 *            maximum number of clients that will connect to the Bluetooth
	 *            server</br> minimum of 1, maximum of 7
	 * @throws BluetoothNotSupportedException
	 */
	private SessionManager(final BluetoothHandler handlerBT, final String appName, final int maxConn, Serializer serializer) throws BluetoothNotSupportedException {
		// Initialize the BluetoothChatService to perform Bluetooth connections
		this.serviceBT = new BluetoothService(handlerBT, appName);

		this.serializer = serializer != null ? serializer : new Serializer() {
		};
	}

	public boolean setGameControl(final GameControl gc) {
		if (gc != null) {
			this.gameControl = gc;
			return true;
		}

		return false;
	}

	/*
	 * ==========================================================================
	 * ====== BLUETOOTH SESSION CONTROL
	 * ==========================================
	 * ======================================
	 */
	/**
	 * Start a new session.
	 * 
	 * @return true on successful creation,<br>
	 *         false if session is already created
	 */
	public boolean startSession() {
		if (this.serviceBT != null) {
			this.serviceBT.start();
			return true;
		}
		return false;
	}

	/**
	 * Finish the current session.
	 * 
	 * @return true on successful termination,<br>
	 *         false if no session is running
	 */
	public boolean finishSession() {
		if (this.serviceBT != null) {
			this.serviceBT.stop();
			return true;
		}

		return false;
	}

	/*
	 * ==========================================================================
	 * ====== BLUETOOTH RECEIVERS CONTROL
	 * ========================================
	 * ========================================
	 */
	/**
	 * Unregister a Bluetooth Broadcast Receiver.
	 * 
	 * @param context
	 *            application context
	 * @param receiver
	 *            the Bluetooth broadcast receiver
	 */
	private void unregisterReceiver(final Activity context, final BluetoothBroadcastReceiver receiver) {
		try {
			context.unregisterReceiver(receiver);
		} catch (final Exception e) {
			Log.d(SessionManager.TAG, "Can't unregister");
		}
	}

	/**
	 * Register a broadcast receiver for discovery actions.
	 * 
	 * @param context
	 *            application context
	 * @param receiver
	 *            the Bluetooth broadcast receiver
	 */
	private void registerDiscoveryReceiver(final Activity context, final BluetoothBroadcastReceiver receiver) {
		// Register for broadcasts when a device is discovered
		final IntentFilter filterFOUND = new IntentFilter(BluetoothDevice.ACTION_FOUND);
		context.registerReceiver(receiver, filterFOUND);

		final IntentFilter filterNAME_CHANGED = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		context.registerReceiver(receiver, filterNAME_CHANGED);
	}

	/*
	 * ==========================================================================
	 * ====== BLUETOOTH ADAPTER CONTROL
	 * ==========================================
	 * ======================================
	 */
	/**
	 * Enable the Bluetooth adapter.
	 * 
	 * @param context
	 *            application context
	 * @return true if Bluetooth is not enabled and ask the user to enable it<br>
	 *         or if Bluetooth is already enabled
	 */
	public boolean enableBluetooth(final Activity context) {
		// If Bluetooth is not on, request that it be enabled.
		if (this.serviceBT.isEnabled()) {
			return true;
		} else {
			final Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			context.startActivityForResult(enableIntent, What.REQUEST_ENABLE_BT.v());
		}
		return true;
	}

	/**
	 * Disable Bluetooth.
	 * 
	 * @return true to indicate adapter shutdown has begun,</br> false on
	 *         immediate error
	 */
	public boolean disableBluetooth() {
		if (!serviceBT.isEnabled()) {
			return false;
		} else {
			return serviceBT.disableBluetooth();
		}
	}

	/**
	 * Checks if Bluetooth is enabled.
	 * 
	 * @return Bluetooth state
	 */
	public boolean isBluetoothEnabled() {
		return this.serviceBT.isEnabled();
	}

	/**
	 * Make the Bluetooth device discoverable.
	 * 
	 * @param context
	 *            application context
	 * @return true on success,</br> false if device is already discoverable
	 */
	public boolean makeDiscoverable(final Activity context) {
		serviceBT.setServer(true);
		if (this.serviceBT.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
			final Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
			discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
			context.startActivity(discoverableIntent);

			return true;
		}

		return false;
	}

	/**
	 * Start the discovery for Bluetooth devices.
	 * 
	 * @param context
	 *            application context
	 * @param receiver
	 *            the Bluetooth broadcast receiver
	 * @return true on success,</br> false on error
	 */
	public boolean startDiscovery(final Activity context, final BluetoothBroadcastReceiver receiver) {
		// If we're already discovering, stop it
		this.cancelDiscovery(context, receiver);

		this.unregisterReceiver(context, receiver);

		this.registerDiscoveryReceiver(context, receiver);

		// Request discover from BluetoothAdapter
		return this.serviceBT.startDiscovery();
	}

	/**
	 * Stop the discovery for Bluetooth devices.
	 * 
	 * @param context
	 *            application context
	 * @param receiver
	 *            the Bluetooth broadcast receiver
	 * @return true on success,</br> false on error
	 */
	public boolean cancelDiscovery(final ContextWrapper context, final BluetoothBroadcastReceiver receiver) {
		try {
			context.unregisterReceiver(receiver);
		} catch (final Exception e) {
			Log.e(TAG, "BroadcastReceiver already unregistered");
		}

		if (this.serviceBT.isDiscovering()) {
			return this.serviceBT.cancelDiscovery();
		}

		// Make sure we're not doing discovery anymore
		final BluetoothAdapter btAdapter = serviceBT.getBTAdapter();
		if (btAdapter != null) {
			if (btAdapter.isDiscovering()) {
				return btAdapter.cancelDiscovery();
			}
		}
		return false;
	}

	/**
	 * Get a set of currently paired devices.
	 * 
	 * @return currently paired devices
	 */
	public Set<BluetoothDevice> getPairedDevices() {
		return this.serviceBT.getPairedDevices();
	}

	/*
	 * ==========================================================================
	 * ====== BLUETOOTH CONNECTION
	 * ==============================================
	 * ==================================
	 */
	/**
	 * Connect to a Bluetooth device.
	 * 
	 * @param address
	 *            device address
	 * @return true
	 */
	public boolean connectDevice(final String address) {
		// Get the BluetoothDevice object
		final BluetoothDevice device = this.serviceBT.getRemoteDevice(address);
		// Attempt to connect to the device
		this.serviceBT.connect(device);

		return true;
	}

	public boolean isConnected() {
		return this.serviceBT.isConnected();
	}
	
	/**
	 * Send data to connected devices.
	 * 
	 * @param data
	 *            on null data to send
	 * @param msgType
	 *            data, turn or realtime message
	 * @return true if data was sent,</br> false if data was not sent
	 * @throws BluetoothNotConnectedExeption
	 */

	private boolean write(final Object data, final What msgType) throws BluetoothNotConnectedExeption {
		if (data != null) {
			/*
			 * Message type needs to be sent along with the message, put it in
			 * the last byte and send
			 */
			final byte[] rawData = this.serializer.fromObject(data);
			final byte[] fullData = new byte[rawData.length + 1];

			// Message type fits in a byte, so we just cast it
			final byte msgByte = (byte) msgType.v();

			System.arraycopy(rawData, 0, fullData, 0, rawData.length);
			fullData[fullData.length - 1] = msgByte;

			// TODO is this the correct var?
			this.serviceBT.write(fullData, rawData, msgType);

			return true;
		}
		return false;
	}

	/**
	 * Send a message that indicates the beginning of the session.
	 * 
	 * @return true on success,</br> false on error
	 * @throws BluetoothNotConnectedExeption
	 */
	public boolean sendBegin() throws BluetoothNotConnectedExeption {
		if (this.gameControl != null) {
			return this.write(this.gameControl.prepareBegin(), What.WRITE_BEGIN);
		}

		return false;
	}

	/**
	 * Send any type of data, at any time.
	 * 
	 * @param data
	 *            object to be sent
	 * @return true on success,</br> false on error
	 * @throws BluetoothNotConnectedExeption
	 */
	public boolean sendData(final Object data) throws BluetoothNotConnectedExeption {
		return this.write(data, What.WRITE_DATA);
	}

	/**
	 * Send specific data to be used in the game turns. This is defined
	 * previously {@link GameControl#prepareTurn()} function.
	 * 
	 * @return true if turn is sent, false if {@link GameControl} is not set
	 * @throws BluetoothNotConnectedExeption
	 */
	public boolean executeTurn() throws BluetoothNotConnectedExeption {
		if (this.gameControl != null) {
			return this.write(gameControl.prepareTurn(), What.WRITE_TURN);
		}

		return false;
	}

	/**
	 * Send specific data to be sent regularly to simulate realtime. This is
	 * defined previously {@link GameControl#prepareRealtime()} function.
	 * 
	 * Realtime message sending uses the {@link Handler} class to send delayed
	 * messages thru the {@link Handler#postDelayed(Runnable, long)} function
	 * that requires a Runnable, in this case a {@link RealtimeRunnable} class
	 * that writes the message and calls
	 * {@link SessionManager#realtimeLoop(long)} to send the next realtime
	 * message.
	 * 
	 * @param delay
	 *            delay in milliseconds to send each realtime message
	 * @return true if real time is started,</br> false if real time is already
	 *         started
	 * @throws BluetoothNotConnectedExeption
	 */
	public boolean executeRealtime(final long delay) throws BluetoothNotConnectedExeption {
		if (!this.isSendingRealtime) {

			if (this.gameControl != null) {
				this.runnnableRealtime = new RealtimeRunnable(delay);
				this.handlerRealtime = new Handler();
				this.isSendingRealtime = true;

				this.realtimeLoop(delay);

				return true;
			}
		}
		return false;
	}

	/**
	 * Checks if AndBT is currently sending realtime messages.
	 * 
	 * @return true if it is sending,</br> false it is not sending
	 */
	public boolean isRealtimeSending() {
		return this.isSendingRealtime;
	}

	/**
	 * Stops sending real time messages.
	 * 
	 * @return true if it's stopped,</br> false if real time is not running
	 */
	public boolean stopRealtime() {
		if (this.isSendingRealtime) {
			this.runnnableRealtime = null;
			this.handlerRealtime = null;

			this.isSendingRealtime = false;

			return true;
		}
		return false;
	}

	/**
	 * Sends realtime messages using a {@link RealtimeRunnable} after a certain
	 * delay.
	 * 
	 * @param delay
	 *            delay in milliseconds to send each realtime message
	 */
	private void realtimeLoop(final long delay) {
		if (this.isSendingRealtime) {
			this.handlerRealtime.postDelayed(this.runnnableRealtime, delay);
		}
	}

	/** This Runnable class is used to send the realtime messages */
	private class RealtimeRunnable implements Runnable {
		/** Realtime delay in milliseconds */
		private final long delay;

		/**
		 * Constructor for the class
		 * 
		 * @param delay
		 *            realtime delay in milliseconds
		 */
		public RealtimeRunnable(final long delay) {
			this.delay = delay;
		}

		/**
		 * Send a message and call for the next message to be sent after a delay
		 */
		@Override
		public void run() {
			try {
				write(gameControl.prepareRealtime(), What.WRITE_REAL_TIME);
				realtimeLoop(delay);
			} catch (final BluetoothNotConnectedExeption e) {
				Log.d(TAG, "Error happend", e);
			}
		}
	}

	public int getConnectionID() {
		return serviceBT.getMyUUID();
	}

	/**
	 * Send a message that indicates the end of the session.
	 * 
	 * @return true on success,</br> false on error
	 * @throws BluetoothNotConnectedExeption
	 */
	public boolean sendFinish() throws BluetoothNotConnectedExeption {
		if (this.gameControl != null) {
			return this.write(this.gameControl.prepareFinish(), What.WRITE_FINISH);
		}

		return false;
	}

	public OnActivityResult processActivityResult(final int requestCode, final int resultCode, final Intent data) {
		switch (What.fromInt(requestCode)) {
		case REQUEST_CONNECT_DEVICE:
			if (resultCode == Activity.RESULT_OK) {
				final String address = data.getExtras().getString(Data.DEVICE_NAME.v());
				connectDevice(address);
				return OnActivityResult.CONNECT_OK;
			} else {
				return OnActivityResult.CONNECT_CANCELLED;
			}
		case REQUEST_ENABLE_BT:
			if (resultCode == Activity.RESULT_OK) {
				return OnActivityResult.ENABLE_BT_OK;
			} else {
				return OnActivityResult.ENABLE_BT_CANCELLED;
			}
		default:
			return OnActivityResult.ERROR;
		}
	}

	public <T> T processHandlerMessage(final BluetoothState state, final Message msg, Class<T> type) {
		return this.serializer.toObject((byte[]) msg.obj, type);
	}
}
