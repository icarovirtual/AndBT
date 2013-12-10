package br.pucrs.tcii;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import br.pucrs.tcii.handlers.BluetoothHandler;
import br.pucrs.tcii.utils.Data;
import br.pucrs.tcii.utils.Messages;
import br.pucrs.tcii.utils.What;

public class BluetoothService {
	private static final String TAG = BluetoothService.class.getName();
	private final static int DEFAULT_BUFFER_SIZE = 1024;
	private static final int MAX_BLUETOOTH_CONNECTIONS = 7;

	// Name for the SDP record when creating server socket
	private String appName;
	private static BluetoothAdapter adapterBT;
	private static BluetoothHandler mHandler;
	private AcceptThread acceptThread;
	private ConnectThread connectThread;
	private ConnectedThread connectedThread;
	private boolean isServer = false;
	private static int bufferSize;
	/**
	 * A Bluetooth Piconet can support up to 7 connections. This array holds 7
	 * unique UUIDs. When attempting to make a connection, the UUID on the
	 * client must match one that the server is listening for. When accepting
	 * incoming connections server listens for all 7 UUIDs. When trying to form
	 * an outgoing connection, the client tries each UUID one at a time.
	 */
	private ArrayList<UUID> mUuids = new ArrayList<UUID>() {
		private static final long serialVersionUID = 1L;
		{
			// 7 randomly-generated UUIDs. These must match on both server and
			// client.
			this.add(UUID.fromString("b7746a40-c758-4868-aa19-7ac6b3475dfc"));
			this.add(UUID.fromString("2d64189d-5a2c-4511-a074-77f199fd0834"));
			this.add(UUID.fromString("e442e09a-51f3-4a7b-91cb-f638491d1412"));
			this.add(UUID.fromString("a81d6504-4536-49ee-a475-7d96d09439e4"));
			this.add(UUID.fromString("aa91eab1-d8ad-448e-abdb-95ebba4a9b55"));
			this.add(UUID.fromString("4d34da73-d0a4-4f40-ac38-917e0a9dee97"));
			this.add(UUID.fromString("5e14d4df-9c8a-4db7-81e4-c937564c86e0"));
		}
	};
	private ArrayList<String> mDeviceAddresses = new ArrayList<String>();
	private ArrayList<ConnectedThread> mConnThreads = new ArrayList<ConnectedThread>();
	private ArrayList<BluetoothSocket> mSockets = new ArrayList<BluetoothSocket>();
	private int connectionsLimit;
	// TODO it should start with -1 and get changed when connection is
	// stablished
	private int myUUID = 0;
	
	private boolean isConnected = false;

	public BluetoothService(BluetoothHandler handlerBT, String appName) {
		this(handlerBT, appName, MAX_BLUETOOTH_CONNECTIONS, DEFAULT_BUFFER_SIZE);
	}

	public BluetoothService(BluetoothHandler handlerBT, String appName, int maxConn) {
		this(handlerBT, appName, maxConn, DEFAULT_BUFFER_SIZE);
	}

	/**
	 * Constructor. Prepares a new BluetoothChat session.
	 * 
	 * @param bufferSize
	 * 
	 * @param context
	 *            The UI Activity Context
	 * @param handler
	 *            A Handler to send messages back to the UI Activity
	 */
	public BluetoothService(BluetoothHandler handlerBT, String appName, int maxConn, int bufferSize) {
		this.appName = appName;
		BluetoothService.adapterBT = BluetoothAdapter.getDefaultAdapter();
		BluetoothService.mHandler = handlerBT;
		BluetoothService.bufferSize = bufferSize;
		this.connectionsLimit = maxConn;

		// Remove some UUID's so that we won't have more than expected from
		// the client app
		while (this.mUuids.size() > this.connectionsLimit) {
			this.mUuids.remove(0);
		}
	}

	private void cancelAcceptThread() {
		// Cancel any thread waiting for connection
		if (acceptThread != null) {
			acceptThread.cancel();
			acceptThread = null;
			Log.d(TAG, "Accept Thread canceled");
		}
	}

	private void cancelConnectThread() {
		// Cancel any thread attempting to make a connection
		if (connectThread != null) {
			connectThread.cancel();
			connectThread = null;
			Log.d(TAG, "Connect Thread canceled");
		}
	}

	private void cancelConnectedThread() {
		// TODO should we cancel threads of mConnThreads here?

		// Cancel any thread currently running a connection
		if (connectedThread != null) {
			connectedThread.cancel();
			connectedThread = null;
			Log.d(TAG, "Connected Thread canceled");
		}
	}

	/**
	 * Start the ConnectedThread to begin managing a Bluetooth connection
	 * 
	 * @param socket
	 *            The BluetoothSocket on which the connection was made
	 * @param device
	 *            The BluetoothDevice that has been connected
	 */
	private synchronized void connected(BluetoothSocket socket, BluetoothDevice device) {
		Log.d(TAG, "Connected to device " + device.getAddress());

		// Start the thread to manage the connection and perform transmissions
		connectedThread = new ConnectedThread(socket);
		connectedThread.start();
		// Add each connected thread to an array
		mConnThreads.add(connectedThread);

		// Send the name of the connected device back to the UI Activity
		Message msg = mHandler.obtainMessage(What.CONNECTED_TO);
		Bundle bundle = new Bundle();
		bundle.putString(Data.DEVICE_NAME.v(), device.getName());
		bundle.putFloat(Data.EVENT_TIME.v(), System.currentTimeMillis());
		msg.setData(bundle);
		msg.sendToTarget();
		
		this.isConnected  = true;
	}

	/**
	 * Stop all threads
	 */
	public synchronized void stop() {
		Log.d(TAG, ">> Stopping all threads <<");
		cancelConnectThread();
		cancelConnectedThread();
		cancelAcceptThread();

		Message msg = mHandler.obtainMessage(What.NOTIFICATION);
		Bundle bundle = new Bundle();
		bundle.putInt(Data.MESSAGE.v(), Messages.STOPPING_ALL_THREADS.v());
		bundle.putFloat(Data.EVENT_TIME.v(), System.currentTimeMillis());
		msg.setData(bundle);
		msg.sendToTarget();
	}

	public boolean isConnected() {
		return isConnected;
	}
	
	/**
	 * Write to the ConnectedThread in an unsynchronized manner
	 * 
	 * @param fullData
	 *            The bytes to write
	 * @param rawData 
	 * @param msgType
	 * @see ConnectedThread#write(byte[])
	 */
	public void write(byte[] fullData, byte[] rawData, What msgType) {
		// When writing, try to write out to all connected threads
		this.write(fullData, rawData, msgType, null);
	}

	public void write(byte[] fullData, byte[] rawData, What msgType, String deviceAddress) {
		for (int i = 0; i < mConnThreads.size(); i++) {
			try {
				// Create temporary object
				ConnectedThread connectedThread;
				// Synchronize a copy of the ConnectedThread
				synchronized (this) {
					connectedThread = mConnThreads.get(i);
				}

				Log.d(TAG, "Sending a message " + fullData.length + " bytes long, with msgType " + msgType + " to " + connectedThread.getDeviceAddress());

				// Perform the write unsynchronized
				if (deviceAddress == null)
					connectedThread.write(fullData, rawData, msgType, true);
				else if (!deviceAddress.equals(connectedThread.getDeviceAddress()))
					connectedThread.write(fullData, rawData, msgType, false);
			} catch (Exception e) {
				// TODO notify client app in every log.e
				Log.e(TAG, "Problem while writing a message", e);
			}
		}
	}

	/**
	 * Indicate that the connection attempt failed and notify the UI Activity.
	 */
	private void connectionFailed() {
		// Send a failure message back to the Activity
		Message msg = mHandler.obtainMessage(What.ERROR.v());
		Bundle bundle = new Bundle();
		bundle.putInt(Data.MESSAGE.v(), Messages.CONNECTION_FAILED.v());
		bundle.putFloat(Data.EVENT_TIME.v(), System.currentTimeMillis());
		msg.setData(bundle);
		mHandler.sendMessage(msg);

	}

	/**
	 * This thread runs while listening for incoming connections. It behaves
	 * like a server-side client. It runs until a connection is accepted (or
	 * until cancelled).
	 */
	private class AcceptThread extends Thread {
		BluetoothServerSocket serverSocket = null;

		public AcceptThread() {
		}

		public void run() {
			Log.d(TAG, "BEGIN mAcceptThread" + this);
			setName("AcceptThread");
			BluetoothSocket socket = null;
			try {
				// Listen for all UUIDs
				for (int i = 0; i < mUuids.size(); i++) {
					serverSocket = adapterBT.listenUsingRfcommWithServiceRecord(appName, mUuids.get(i));
					socket = serverSocket.accept();
					if (socket != null) {
						String address = socket.getRemoteDevice().getAddress();
						mSockets.add(socket);
						mDeviceAddresses.add(address);
						connected(socket, socket.getRemoteDevice());
					}
				}
			} catch (IOException e) {
				Log.e(TAG, "accept() failed", e);
			}
			Log.i(TAG, "END mAcceptThread");
		}

		public void cancel() {
			Log.d(TAG, "cancel " + this);
			try {
				serverSocket.close();
			} catch (IOException e) {
				Log.e(TAG, "close() of server failed", e);
			}
		}
	}

	/**
	 * This thread runs while attempting to make an outgoing connection with a
	 * device. It runs straight through; the connection either succeeds or
	 * fails.
	 */
	private class ConnectThread extends Thread {
		private final BluetoothSocket mmSocket;
		private final BluetoothDevice mmDevice;
		private UUID tempUuid;

		public ConnectThread(BluetoothDevice device, UUID uuidToTry) {
			mmDevice = device;
			BluetoothSocket tmp = null;
			tempUuid = uuidToTry;

			// Get a BluetoothSocket for a connection with the
			// given BluetoothDevice
			try {
				tmp = device.createRfcommSocketToServiceRecord(uuidToTry);
			} catch (IOException e) {
				Log.e(TAG, "create() failed", e);
			}
			mmSocket = tmp;
		}

		public void run() {
			Log.i(TAG, "BEGIN mConnectThread");
			setName("ConnectThread");

			// Always cancel discovery because it will slow down a connection
			adapterBT.cancelDiscovery();

			// Make a connection to the BluetoothSocket
			try {
				// This is a blocking call and will only return on a
				// successful connection or an exception
				mmSocket.connect();
			} catch (IOException e) {
				// The last element
				if (mUuids.get(mUuids.size() - 1).equals(tempUuid.toString())) {
					connectionFailed();
				}
				// Close the socket
				try {
					mmSocket.close();
				} catch (IOException e2) {
					Log.e(TAG, "unable to close() socket during connection failure", e2);
				}
				// Start the service over to restart listening mode
				BluetoothService.this.start();
				return;
			}

			// Reset the ConnectThread because we're done
			synchronized (BluetoothService.this) {
				connectThread = null;
			}

			// Start the connected thread
			connected(mmSocket, mmDevice);
		}

		public void cancel() {
			try {
				mmSocket.close();
			} catch (IOException e) {
				Log.e(TAG, "close() of connect socket failed", e);
			}
		}
	}

	/**
	 * This thread runs during a connection with a remote device. It handles all
	 * incoming and outgoing transmissions.
	 */
	private class ConnectedThread extends Thread {
		private final BluetoothSocket mmSocket;
		private final InputStream inStream;
		private final OutputStream outStream;

		public String getDeviceAddress() {
			return mmSocket.getRemoteDevice().getAddress();
		}

		public ConnectedThread(BluetoothSocket socket) {
			Log.d(TAG, "create ConnectedThread");
			mmSocket = socket;
			InputStream tmpIn = null;
			OutputStream tmpOut = null;

			// Get the BluetoothSocket input and output streams
			try {
				tmpIn = socket.getInputStream();
				tmpOut = socket.getOutputStream();
			} catch (IOException e) {
				Log.e(TAG, "temp sockets not created", e);
			}

			inStream = tmpIn;
			outStream = tmpOut;
		}

		public void run() {
			Log.i(TAG, "BEGIN mConnectedThread");
			byte[] fullData;
			int readLength;

			// Keep listening to the InputStream while connected
			while (true) {
				try {
					// Read from the InputStream
					fullData = new byte[bufferSize];
					readLength = inStream.read(fullData);
					// Received WRITE constant, want READ equivalent
					int msgType = fullData[readLength -1] + What.WRITE_READ_DIF.v();
					
					// Received data has Write type in last byte, remove that
					byte[] rawData = new byte[readLength -1];
					System.arraycopy(fullData, 0, rawData, 0, readLength -1);
					
					if (isServer) {
						Log.d(TAG, "I'm the server, so I'm forwarding the message");
						BluetoothService.this.write(fullData, rawData, What.fromInt(msgType), getDeviceAddress());
					}
					
					// Send the obtained bytes to the UI Activity
					mHandler.obtainMessage(msgType, -1, -1, rawData).sendToTarget();
				} catch (IOException e) {
					Log.e(TAG, "disconnected", e);
					connectionLost();
					break;
				}
			}
		}

		/**
		 * Write to the connected OutStream.
		 * 
		 * @param fullData
		 *            The bytes to write
		 * @param msgType 
		 * @param rawData 
		 * @param callHandler
		 * @param peer
		 * 
		 */
		public void write(byte[] fullData, byte[] rawData, What msgType, boolean callHandler) {
			String destin = mmSocket.getRemoteDevice().getAddress();
			try {
				synchronized (outStream) {
					Log.d(TAG, "Writing message to " + destin);

					outStream.write(fullData);

					// Share the sent message back to the UI Activity
					if (callHandler)
						mHandler.obtainMessage(msgType, -1, -1, rawData).sendToTarget();
				}
			} catch (IOException e) {
				Log.e(TAG, "Exception during write - " + "Destin:" + destin, e);
			}
		}

		public void cancel() {
			try {
				mmSocket.close();
			} catch (IOException e) {
				Log.e(TAG, "close() of connect socket failed", e);
			}
		}

	}

	/**
	 * Checks if the Bluetooth adapter is enabled.
	 * 
	 * @return adapter state
	 */
	public boolean isEnabled() {
		return adapterBT.isEnabled();
	}

	/**
	 * Disable Bluetooth.
	 * 
	 * @see BluetoothAdapter#disable()
	 * 
	 * @return true to indicate adapter shutdown has begun, or false on
	 *         immediate error
	 */
	public boolean disableBluetooth() {
		return BluetoothService.adapterBT.disable();
	}

	/**
	 * Get the current Bluetooth scan mode of the local Bluetooth adapter.
	 * 
	 * @see BluetoothAdapter#getScanMode()
	 * 
	 * @return current scan mode
	 */
	public int getScanMode() {
		return BluetoothService.adapterBT.getScanMode();
	}

	/**
	 * Start the remote device discovery process.
	 * 
	 * @see BluetoothAdapter#startDiscovery()
	 * 
	 * @return true on success, false on error
	 */
	public boolean startDiscovery() {
		return BluetoothService.adapterBT.startDiscovery();
	}

	/**
	 * Return true if the local Bluetooth adapter is currently in the device
	 * discovery process.
	 * 
	 * @see BluetoothAdapter#isDiscovering()
	 * 
	 * @return true if discovering
	 */
	public boolean isDiscovering() {
		return BluetoothService.adapterBT.isDiscovering();
	}

	/**
	 * Cancel the current device discovery process.
	 * 
	 * @see BluetoothAdapter#cancelDiscovery()
	 * 
	 * @return true on success, false on error
	 */
	public boolean cancelDiscovery() {
		return BluetoothService.adapterBT.cancelDiscovery();
	}

	/**
	 * Return the set of BluetoothDevice objects that are bonded (paired) to the
	 * local adapter.
	 * 
	 * @see BluetoothAdapter#getBondedDevices()
	 * 
	 * @return unmodifiable set of BluetoothDevice, or null on error
	 */
	public Set<BluetoothDevice> getPairedDevices() {
		return BluetoothService.adapterBT.getBondedDevices();
	}

	/**
	 * Get a BluetoothDevice object for the given Bluetooth hardware address.
	 * 
	 * @see BluetoothAdapter#getRemoteDevice(String)
	 * 
	 * @param address
	 *            valid Bluetooth MAC address
	 * @return BluetoothDevice with the parameter MAC address
	 */
	public BluetoothDevice getRemoteDevice(String address) {
		return BluetoothService.adapterBT.getRemoteDevice(address);
	}

	/**
	 * Start the chat service. Specifically start AcceptThread to begin a
	 * session in listening (server) mode. Called by the Activity onResume()
	 */
	public synchronized void start() {
		Log.d(TAG, "start");

		cancelConnectThread();
		cancelConnectedThread();

		// Start the thread to listen on a BluetoothServerSocket
		if (acceptThread == null) {
			acceptThread = new AcceptThread();
			acceptThread.start();
		}
	}

	/**
	 * Start the ConnectThread to initiate a connection to a remote device.
	 * 
	 * @param device
	 *            The BluetoothDevice to connect
	 */
	public synchronized void connect(BluetoothDevice device) {
		Log.d(TAG, "connect to: " + device);

		cancelConnectThread();
		cancelConnectedThread();

		// Create a new thread and attempt to connect to each UUID one-by-one.
		for (int i = 0; i < mUuids.size(); i++) {
			try {
				UUID uuidToTry = mUuids.get(i);
				connectThread = new ConnectThread(device, uuidToTry);
				connectThread.start();
				myUUID = i;
			} catch (Exception e) {
				Log.d(TAG, "error while connecting", e);
			}
		}
	}

	/**
	 * Indicate that the connection was lost and notify the UI Activity.
	 */
	private void connectionLost() {
		// Send a failure message back to the Activity
		Message msg = mHandler.obtainMessage(What.NOTIFICATION);
		Bundle bundle = new Bundle();
		bundle.putString(Data.MESSAGE.v(), "Device connection was lost");
		msg.setData(bundle);
		mHandler.sendMessage(msg);
	}

	public void setServer(boolean isServer) {
		this.isServer = isServer;
	}

	public boolean isServer() {
		return isServer;
	}

	/**
	 * @return 0 when device is server or the connection ID (based on the UUID
	 *         used to stablish connection)
	 * @throws RuntimeException
	 *             if there's no connection stablished
	 * 
	 * */
	public int getMyUUID() {
		if (isServer) {
			return 0;
		}
		if (myUUID == -1) {
			throw new RuntimeException("UUID not defined yet");
		}
		return myUUID;
	}

	public BluetoothAdapter getBTAdapter() {
		return adapterBT;
	}
}
