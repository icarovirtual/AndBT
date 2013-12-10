package com.example.android.BluetoothChat;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import br.pucrs.tcii.SessionManager;
import br.pucrs.tcii.activities.DeviceListActivity;
import br.pucrs.tcii.exceptions.BluetoothNotConnectedExeption;
import br.pucrs.tcii.exceptions.BluetoothNotSupportedException;
import br.pucrs.tcii.handlers.BluetoothHandler;
import br.pucrs.tcii.models.GameControl;
import br.pucrs.tcii.utils.BluetoothState;
import br.pucrs.tcii.utils.What;

/**
 * This is the main Activity that displays the current chat session.
 */
public class BluetoothChat extends Activity {
	private static final String TAG = BluetoothChat.class.getName();
	public static final String APP_NAME = "BluetoothChat";

	// Layout Views
	private ListView mConversationView;
	private EditText mOutEditText;
	private Button mSendButton;

	// Name of the connected device
	private String mConnectedDeviceName = null;
	// Array adapter for the conversation thread
	private ArrayAdapter<String> mConversationArrayAdapter;
	// String buffer for outgoing messages

	private Handler handlerKeyPress;
	private GameControl gc;
	private PressRunnable pressRunnable;
	private boolean isPressing;
	private boolean changeWriting;

	// The action listener for the EditText widget, to listen for the return key
	private final TextView.OnEditorActionListener mWriteListener = new TextView.OnEditorActionListener() {
		@Override
		public boolean onEditorAction(final TextView view, final int actionId, final KeyEvent event) {
			// If the action is a key-up event on the return key, send the
			// message
			if (actionId == EditorInfo.IME_NULL && event.getAction() == KeyEvent.ACTION_UP) {
				final String message = view.getText().toString();
				try {
					SessionManager.getInstance().sendData(message);
				} catch (final BluetoothNotConnectedExeption e) {
					Toast.makeText(BluetoothChat.this, R.string.not_connected, Toast.LENGTH_SHORT).show();
					Log.e(TAG, "Failed while sending message", e);
					return true;
				}
				mOutEditText.setText("");
			}

			Log.i(TAG, "END onEditorAction");
			return true;
		}
	};

	class PressRunnable implements Runnable {
		@Override
		public void run() {
			isPressing = false;
		}
	}

	private final BluetoothHandler stateChangedHandler = new BluetoothHandler() {
		@Override
		public void onWRITE_DATA(final BluetoothState state, final Message msg) {
			final String writeMessage = SessionManager.getInstance().processHandlerMessage(state, msg, String.class);
			mOutEditText.setText("");
			mConversationArrayAdapter.add("Me:  " + writeMessage);
		}

		public void onWRITE_REAL_TIME(BluetoothState state, Message msg) {
		};

		@Override
		public void onREAD_DATA(final BluetoothState state, final Message msg) {
			final String readMessage = SessionManager.getInstance().processHandlerMessage(state, msg, String.class);

			final byte[] readBuf = (byte[]) msg.obj;
			if (readBuf == null) {
				Log.d(TAG, ">> Doing nothing because buffer received is null");
				return;
			}
			mConversationArrayAdapter.add(mConnectedDeviceName + ":  " + readMessage);
		}

		public void onREAD_REAL_TIME(BluetoothState state, Message msg) {
			boolean isPressed = SessionManager.getInstance().processHandlerMessage(state, msg, boolean.class);

			if (isPressed && !changeWriting) {
				changeWriting = true;
				setStatus("connected to " + mConnectedDeviceName + " (writing)");
			} else if (!isPressed && changeWriting) {
				changeWriting = false;
				setStatus(getString(R.string.title_connected_to, mConnectedDeviceName));
			}
		};

		@Override
		public void onCONNECTED_TO(BluetoothState state, Message msg, String deviceAddress, float connectionTime) {
			// save the connected device's name
			BluetoothChat.this.mConnectedDeviceName = deviceAddress;
			// TODO format this timestamp
			Toast.makeText(BluetoothChat.this.getApplicationContext(), "Connected to " + BluetoothChat.this.mConnectedDeviceName + " at " + connectionTime, Toast.LENGTH_SHORT).show();
		};

		@Override
		public void onNOTIFICATION(BluetoothState state, Message msg, int message, float errorTime) {
			switch (state) {
			case CONNECTED:
				BluetoothChat.this.setStatus(BluetoothChat.this.getString(R.string.title_connected_to, BluetoothChat.this.mConnectedDeviceName));
				BluetoothChat.this.mConversationArrayAdapter.clear();
				break;
			case CONNECTING:
				setStatus(R.string.title_connecting);
				break;
			case LISTEN:
			case NONE:
				setStatus(R.string.title_not_connected);
				break;
			}
		}
	};

	@Override
	public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
		switch (SessionManager.getInstance().processActivityResult(requestCode, resultCode, data)) {
		case CONNECT_OK:
			break;
		case CONNECT_CANCELLED:
			break;
		case ENABLE_BT_OK:
			setupChat();
			break;
		case ENABLE_BT_CANCELLED:
			Toast.makeText(this, R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
			finish();
			break;
		default:
			break;
		}
	}

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Get local Bluetooth adapter

		changeWriting = false;

		this.gc = new GameControl() {
			public Object prepareRealtime() {
				return isPressing;
			}
		};

		this.pressRunnable = new PressRunnable();

		try {
			SessionManager.initialize(this.stateChangedHandler, BluetoothChat.APP_NAME, 5);
			this.handlerKeyPress = new Handler();
		} catch (final BluetoothNotSupportedException e) {
			// If the adapter is null, then Bluetooth is not supported
			Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
			finish();
			return;
		}

		SessionManager.getInstance().setGameControl(gc);

		try {
			SessionManager.getInstance().executeRealtime(100);
		} catch (BluetoothNotConnectedExeption e) {
			e.printStackTrace();
		}

		// Set up the window layout
		setContentView(R.layout.main);
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		getMenuInflater().inflate(R.menu.option_menu, menu);
		return true;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		SessionManager.getInstance().finishSession();
		Log.e(TAG, "--- ON DESTROY ---");
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		switch (item.getItemId()) {
		case R.id.connect_scan:
			// Launch the DeviceListActivity to see devices and do scan
			final Intent serverIntent = new Intent(this, DeviceListActivity.class);
			this.startActivityForResult(serverIntent, What.REQUEST_CONNECT_DEVICE.v());
			return true;
		case R.id.discoverable:
			// Ensure this device is discoverable by others
			SessionManager.getInstance().makeDiscoverable(this);
			return true;
		}
		return false;
	}

	@Override
	public void onStart() {
		super.onStart();

		if (SessionManager.getInstance().enableBluetooth(this)) {
			setupChat();
		}
	}

	private final void setStatus(final CharSequence subTitle) {
		final ActionBar actionBar = getActionBar();
		actionBar.setSubtitle(subTitle);
	}

	private final void setStatus(final int resId) {
		final ActionBar actionBar = getActionBar();
		actionBar.setSubtitle(resId);
	}

	private void setupChat() {
		Log.d(TAG, "setupChat()");

		// Initialize the array adapter for the conversation thread
		mConversationArrayAdapter = new ArrayAdapter<String>(this, R.layout.message);
		mConversationView = (ListView) findViewById(R.id.in);
		mConversationView.setAdapter(mConversationArrayAdapter);

		// Initialize the compose field with a listener for the return key
		mOutEditText = (EditText) findViewById(R.id.edit_text_out);
		mOutEditText.setOnEditorActionListener(mWriteListener);
		mOutEditText.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				isPressing = true;
				handlerKeyPress.removeCallbacks(pressRunnable);
				handlerKeyPress.postDelayed(pressRunnable, 500);
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
			}
		});

		// Initialize the send button with a listener that for click events
		mSendButton = (Button) findViewById(R.id.button_send);
		mSendButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(final View v) {
				// Send a message using content of the edit text widget
				final TextView view = (TextView) findViewById(R.id.edit_text_out);
				final String message = view.getText().toString();
				try {
					SessionManager.getInstance().sendData(message);
				} catch (final BluetoothNotConnectedExeption e) {
					Log.e(TAG, "Failed while sending message", e);
					Toast.makeText(BluetoothChat.this, R.string.not_connected, Toast.LENGTH_SHORT).show();
				}
			}
		});
		SessionManager.getInstance().startSession();
	}

}
