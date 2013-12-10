package br.pucrs.tcii.benchmark;

import java.util.Arrays;
import java.util.Random;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Toast;
import android.widget.ToggleButton;
import br.pucrs.tcii.SessionManager;
import br.pucrs.tcii.activities.DeviceListActivity;
import br.pucrs.tcii.datahandling.Serializer;
import br.pucrs.tcii.exceptions.BluetoothNotConnectedExeption;
import br.pucrs.tcii.exceptions.BluetoothNotSupportedException;
import br.pucrs.tcii.handlers.BluetoothHandler;
import br.pucrs.tcii.models.GameControl;
import br.pucrs.tcii.utils.BluetoothState;
import br.pucrs.tcii.utils.What;

import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;

public class BenchmarkActivity extends Activity {
	private static final String TAG_RT_SEND = "RT-SEND";
	private static final String TAG_RT_RECV = "RT-RECV";
	private static final String TAG_DATA_SEND = "DATA-SEND";
	private static final String TAG_DATA_RECV = "DATA-RECV";
	private static final String TAG_SERIAL_ARRAY = "SER-ARRAY";
	private static final String TAG_DESERIAL_ARRAY = "DESER-ARRAY";
	private static final String TAG_SERIAL_CLASS = "SER-CLASS";
	private static final String TAG_DESERIAL_CLASS = "DESER-CLASS";

	/** Array de caracteres vai quadruplicar o tamanho (total 512) */
	private final int DATA_LEN_REALTIME = 120;

	private static final int RT_100 = 100;
	private static final int RT_50 = 50;
	private static final int RT_10 = 10;

	private GameControl gc;
	private BluetoothHandler handler;

	private char[] sendData;

	private String connDevice;

	private boolean imSender;
	private boolean benchDelay;

	protected int curId;

	private BenchClass classBench;
	private int[] arrayBench;
	private boolean isRunning;

	private Serializer serializer;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		isRunning = false;
		benchDelay = true;

		this.serializer = new Serializer() {
			@Override
			public byte[] fromObject(Object data) {
				Log.i("SERIALIZER", "Serializing...");
				return super.fromObject(data);
			}

			@Override
			public <T> T toObject(byte[] data, Class<T> type) throws JsonSyntaxException, JsonParseException {
				Log.i("SERIALIZER", "Deserializing...");
				return super.toObject(data, type);
			}
		};

		ToggleButton but_server = (ToggleButton) findViewById(R.id.but_server);
		ToggleButton but_client = (ToggleButton) findViewById(R.id.but_client);

		but_server.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked) {
					checkButtons(R.id.but_server);
					imSender = true;

					try {
						configAndBT();
					} catch (BluetoothNotConnectedExeption e) {
						e.printStackTrace();
					}
				} else {
					SessionManager.getInstance().finishSession();
				}
			}
		});

		but_client.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked) {
					checkButtons(R.id.but_client);
					imSender = false;

					try {
						configAndBT();
					} catch (BluetoothNotConnectedExeption e) {
						e.printStackTrace();
					}
				} else {
					SessionManager.getInstance().finishSession();
				}
			}
		});

		Button but_bt_state = (Button) findViewById(R.id.but_bt_state);
		Button but_bt_discov = (Button) findViewById(R.id.but_bt_discov);
		Button but_bt_device = (Button) findViewById(R.id.but_bt_device);

		but_bt_state.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				checkButtons(R.id.but_bt_state);
				if (SessionManager.isInitialized())
					SessionManager.getInstance().disableBluetooth();
				else
					Toast.makeText(getApplicationContext(), "Instance not initiated yet!", Toast.LENGTH_SHORT).show();
			}
		});

		but_bt_discov.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (SessionManager.isInitialized())
					SessionManager.getInstance().makeDiscoverable(BenchmarkActivity.this);
				else
					Toast.makeText(getApplicationContext(), "Instance not initiated yet!", Toast.LENGTH_SHORT).show();
			}
		});

		but_bt_device.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (SessionManager.isInitialized())
					startActivityForResult(new Intent(BenchmarkActivity.this, DeviceListActivity.class), What.REQUEST_CONNECT_DEVICE.v());
				else
					Toast.makeText(getApplicationContext(), "Instance not initiated yet!", Toast.LENGTH_SHORT).show();
			}
		});

		final ToggleButton but_realtime_stop_delay = (ToggleButton) findViewById(R.id.but_realtime_stop_delay);

		but_realtime_stop_delay.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked) {
				} else {
					if (SessionManager.isInitialized()) {
						SessionManager.getInstance().stopRealtime();
						try {
							SessionManager.getInstance().sendFinish();
						} catch (BluetoothNotConnectedExeption e) {
							Log.e("OFF", "Could not sendFinish");
						}
					}
				}
			}
		});

		Button but_realtime_100 = (Button) findViewById(R.id.but_realtime_100);
		Button but_realtime_50 = (Button) findViewById(R.id.but_realtime_50);
		Button but_realtime_10 = (Button) findViewById(R.id.but_realtime_10);

		but_realtime_100.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (SessionManager.isInitialized()) {
					curId = 0;
					benchDelay = true;

					checkButtons(R.id.but_realtime_stop_delay);
					SessionManager.getInstance().stopRealtime();
					try {
						sendData = new char[DATA_LEN_REALTIME];
						Arrays.fill(sendData, 'a');
						SessionManager.getInstance().executeRealtime(RT_100);
					} catch (BluetoothNotConnectedExeption e) {
						e.printStackTrace();
					}
				} else
					Toast.makeText(getApplicationContext(), "Instance not initiated yet!", Toast.LENGTH_SHORT).show();
			}
		});

		but_realtime_50.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (SessionManager.isInitialized()) {
					curId = 0;
					benchDelay = true;

					checkButtons(R.id.but_realtime_stop_delay);
					SessionManager.getInstance().stopRealtime();
					try {
						sendData = new char[DATA_LEN_REALTIME];
						Arrays.fill(sendData, 'a');
						SessionManager.getInstance().executeRealtime(RT_50);
					} catch (BluetoothNotConnectedExeption e) {
						e.printStackTrace();
					}
				} else
					Toast.makeText(getApplicationContext(), "Instance not initiated yet!", Toast.LENGTH_SHORT).show();
			}
		});

		but_realtime_10.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (SessionManager.isInitialized()) {
					curId = 0;
					benchDelay = true;

					checkButtons(R.id.but_realtime_stop_delay);
					SessionManager.getInstance().stopRealtime();
					try {
						sendData = new char[DATA_LEN_REALTIME];
						Arrays.fill(sendData, 'a');
						SessionManager.getInstance().executeRealtime(RT_10);
					} catch (BluetoothNotConnectedExeption e) {
						e.printStackTrace();
					}
				} else
					Toast.makeText(getApplicationContext(), "Instance not initiated yet!", Toast.LENGTH_SHORT).show();
			}
		});

		Button but_realtime_class = (Button) findViewById(R.id.but_realtime_class);
		Button but_realtime_array = (Button) findViewById(R.id.but_realtime_array);

		but_realtime_class.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (SessionManager.isInitialized()) {
					curId = 0;
					benchDelay = false;

					try {
						if (classBench == null)
							classBench = new BenchClass();

						long time = System.currentTimeMillis();
						Log.v(TAG_DATA_SEND, "PRE;" + time);

						SessionManager.getInstance().sendData(classBench);

						time = System.currentTimeMillis();
						Log.v(TAG_DATA_SEND, "POS;" + time);
					} catch (BluetoothNotConnectedExeption e) {
						e.printStackTrace();
					}
				} else
					Toast.makeText(getApplicationContext(), "Instance not initiated yet!", Toast.LENGTH_SHORT).show();
			}
		});

		but_realtime_array.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (SessionManager.isInitialized()) {
					try {
						if (arrayBench == null)
							arrayBench = makeArray();

						long time = System.currentTimeMillis();
						Log.v(TAG_DATA_SEND, "PRE;" + time);

						SessionManager.getInstance().sendData(arrayBench);

						time = System.currentTimeMillis();
						Log.v(TAG_DATA_SEND, "POS;" + time);
					} catch (BluetoothNotConnectedExeption e) {
						e.printStackTrace();
					}
				} else
					Toast.makeText(getApplicationContext(), "Instance not initiated yet!", Toast.LENGTH_SHORT).show();
			}
		});

		Button but_serial_class = (Button) findViewById(R.id.but_serial_class);
		Button but_serial_array = (Button) findViewById(R.id.but_serial_array);

		but_serial_array.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!isRunning) {
					isRunning = true;

					int[] data;
					byte[] serialData;
					int qtyTests = 100;
					int qtyMults = 1;
					int initSize = 1;
					int currSize = initSize;
					for (int i = 1; i < qtyTests + 1; i++) {
						for (int x = 0; x < qtyMults; x++) {
							// Hundred between 100 and 900
							int hundred = (new Random(System.currentTimeMillis()).nextInt(10) + 1) * 100;

							data = new int[currSize];
							for (int y = 0; y < currSize; y++) {
								data[y] = hundred + y;
							}

							long time = System.currentTimeMillis();
							Log.v(TAG_SERIAL_ARRAY, i + ";" + x + ";PRE;" + data.length + ";" + time);
							serialData = serializer.fromObject(data);
							time = System.currentTimeMillis();
							Log.v(TAG_SERIAL_ARRAY, i + ";" + x + ";POS;" + serialData.length + ";" + time);

							time = System.currentTimeMillis();
							Log.v(TAG_DESERIAL_ARRAY, i + ";" + x + ";PRE;" + serialData.length + ";" + time);
							data = serializer.toObject(serialData, int[].class);
							time = System.currentTimeMillis();
							Log.v(TAG_DESERIAL_ARRAY, i + ";" + x + ";POS;" + data.length + ";" + time);

							data = null;
							serialData = null;
							currSize *= 10;
						}
						currSize = initSize;
					}

					isRunning = false;
				}
			}
		});

		but_serial_class.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!isRunning) {
					isRunning = true;

					BenchClassList clazz;
					byte[] serialData;
					int qtyTests = 100;
					int qtyMults = 1;
					int initSize = 1;
					int currSize = initSize;
					for (int i = 1; i < qtyTests + 1; i++) {
						for (int x = 0; x < qtyMults; x++) {
							clazz = new BenchClassList(currSize);

							long time = System.currentTimeMillis();
							Log.v(TAG_SERIAL_CLASS, i + ";" + x + ";PRE;" + currSize + ";" + time);
							serialData = serializer.fromObject(clazz);
							time = System.currentTimeMillis();
							Log.v(TAG_SERIAL_CLASS, i + ";" + x + ";POS;" + serialData.length + ";" + time);

							time = System.currentTimeMillis();
							Log.v(TAG_DESERIAL_CLASS, i + ";" + x + ";PRE;" + serialData.length + ";" + time);
							serializer.toObject(serialData, BenchClassList.class);
							time = System.currentTimeMillis();
							Log.v(TAG_DESERIAL_CLASS, i + ";" + x + ";POS;" + currSize + ";" + time);

							clazz = null;
							serialData = null;
							currSize *= 10;
						}
						currSize = initSize;
					}

					isRunning = false;
				}
			}
		});
	}

	protected int[] makeArray() {
		int hundred = (new Random(System.currentTimeMillis()).nextInt(10) + 1) * 100;

		int[] array = new int[99];
		for (int x = 0; x < array.length; x++) {
			array[x] = hundred + x;
		}

		return array;
	}

	private void checkButtons(int id) {
		switch (id) {
		case R.id.but_server: {
			ToggleButton but_client = (ToggleButton) findViewById(R.id.but_client);
			if (but_client.isChecked()) {
				but_client.performClick();
			}
			break;
		}
		case R.id.but_client: {
			ToggleButton but_server = (ToggleButton) findViewById(R.id.but_server);
			if (but_server.isChecked()) {
				but_server.performClick();
			}
			break;
		}
		case R.id.but_bt_state: {
			ToggleButton but_client = (ToggleButton) findViewById(R.id.but_client);
			if (but_client.isChecked()) {
				but_client.performClick();
			}
			ToggleButton but_server = (ToggleButton) findViewById(R.id.but_server);
			if (but_server.isChecked()) {
				but_server.performClick();
			}
			break;
		}
		case R.id.but_realtime_stop_delay: {
			ToggleButton but_delay = (ToggleButton) findViewById(R.id.but_realtime_stop_delay);
			but_delay.setChecked(true);
			break;
		}
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (SessionManager.getInstance().processActivityResult(requestCode, resultCode, data)) {
		case CONNECT_OK:
			break;
		case CONNECT_CANCELLED:
			break;
		case ENABLE_BT_OK:
			break;
		case ENABLE_BT_CANCELLED:
			break;
		case ERROR:
		default:
			// TODO do something here
			break;
		}
	}

	private void configAndBT() throws BluetoothNotConnectedExeption {
		gc = new GameControl() {
			@Override
			public Object prepareTurn() {
				return null;
			}

			@Override
			public Object prepareRealtime() {
				curId += 1;

				long sendTime = System.currentTimeMillis();
				Log.v(TAG_RT_SEND, "Sent;" + curId + ";" + sendTime);

				if (curId == 100) {
					SessionManager.getInstance().stopRealtime();
				}

				return new BenchPackage(curId, sendData);
			}

			@Override
			public Object prepareBegin() {
				return new String("Let's begin!");
			}

			@Override
			public Object prepareFinish() {
				return new String("Done!");
			}
		};

		handler = new BluetoothHandler() {
			@Override
			public void onREAD_DATA(BluetoothState state, Message msg) {
				if (imSender && benchDelay) {
					final BenchPackage recvData = SessionManager.getInstance().processHandlerMessage(state, msg, BenchPackage.class);

					long recvTime = System.currentTimeMillis();
					Log.v(TAG_RT_RECV, "Recv;" + recvData.id + ";" + recvTime);
				} else {
					long timeInit = System.currentTimeMillis();
					long timeEnd = System.currentTimeMillis();

					BenchClass recvClass;
					try {
						recvClass = SessionManager.getInstance().processHandlerMessage(state, msg, BenchClass.class);
						timeEnd = System.currentTimeMillis();
					} catch (Exception e) {
						recvClass = null;
					}

					if (recvClass == null) {
						timeInit = System.currentTimeMillis();
						SessionManager.getInstance().processHandlerMessage(state, msg, int[].class);
						timeEnd = System.currentTimeMillis();
					}

					Log.v(TAG_DATA_RECV, "PRE;" + timeInit);
					Log.v(TAG_DATA_RECV, "POS;" + timeEnd);
				}
			}

			@Override
			public void onREAD_BEGIN(BluetoothState state, Message msg) {
				Toast.makeText(getApplicationContext(), SessionManager.getInstance().processHandlerMessage(state, msg, String.class), Toast.LENGTH_SHORT).show();
			}

			@Override
			public void onREAD_FINISH(BluetoothState state, Message msg) {
				Toast.makeText(getApplicationContext(), SessionManager.getInstance().processHandlerMessage(state, msg, String.class), Toast.LENGTH_SHORT).show();
			}

			@Override
			public void onWRITE_BEGIN(BluetoothState state, Message msg) {
				Toast.makeText(getApplicationContext(), SessionManager.getInstance().processHandlerMessage(state, msg, String.class), Toast.LENGTH_SHORT).show();
			}

			@Override
			public void onWRITE_FINISH(BluetoothState state, Message msg) {
				Toast.makeText(getApplicationContext(), SessionManager.getInstance().processHandlerMessage(state, msg, String.class), Toast.LENGTH_SHORT).show();
			}

			@Override
			public void onREAD_TURN(BluetoothState state, Message msg) {
			}

			@Override
			public void onREAD_REAL_TIME(BluetoothState state, Message msg) {
				if (!imSender && benchDelay) {
					final BenchPackage recvData = SessionManager.getInstance().processHandlerMessage(state, msg, BenchPackage.class);

					long recvTime = System.currentTimeMillis();
					Log.v(TAG_RT_RECV, "Recv;" + recvData.id + ";" + recvTime);

					try {
						SessionManager.getInstance().sendData(recvData);
					} catch (BluetoothNotConnectedExeption e) {
						e.printStackTrace();
					}
				}
			}

			@Override
			public void onWRITE_TURN(BluetoothState state, Message msg) {
			}

			@Override
			public void onCONNECTED_TO(BluetoothState state, Message msg, String deviceAddress, float connectionTime) {
				super.onCONNECTED_TO(state, msg, deviceAddress, connectionTime);
				connDevice = deviceAddress;
				Toast.makeText(getApplicationContext(), "Connected to " + connDevice, Toast.LENGTH_SHORT).show();
			}

			@Override
			public void onNOTIFICATION(BluetoothState state, Message msg, int message, float errorTime) {
				super.onNOTIFICATION(state, msg, message, errorTime);
				switch (state) {

				case CONNECTED:
					setStatus("Connected to " + connDevice);
					try {
						SessionManager.getInstance().sendBegin();
					} catch (BluetoothNotConnectedExeption e) {
						Log.e("onActivityResult", "Could not sendBegin");
					}
					break;

				case CONNECTING:
					setStatus("Connecting...");
					break;

				default:
					setStatus("Not connected");
					break;
				}
			}

		};

		try {
			if (!SessionManager.isInitialized()) {
				SessionManager.initialize(handler, "BenchmarkActivity", 2);
			}
			if (!SessionManager.getInstance().isBluetoothEnabled()) {
				SessionManager.getInstance().enableBluetooth(BenchmarkActivity.this);
			} else {
				SessionManager.getInstance().startSession();
			}
		} catch (BluetoothNotSupportedException e) {
			e.printStackTrace();
		}

		SessionManager.getInstance().setGameControl(gc);
	}

	private final void setStatus(final String status) {
		final ActionBar actionBar = getActionBar();
		actionBar.setSubtitle(status);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
}
