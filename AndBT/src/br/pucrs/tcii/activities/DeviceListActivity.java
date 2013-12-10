package br.pucrs.tcii.activities;

import java.util.Set;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import br.pucrs.tcii.R;
import br.pucrs.tcii.SessionManager;
import br.pucrs.tcii.utils.Data;

/**
 * This Activity appears as a dialog. It lists any paired devices and devices
 * detected in the area after discovery. When a device is chosen by the user,
 * the MAC address of the device is sent back to the parent Activity in the
 * result Intent.
 */
public class DeviceListActivity extends Activity {
	/** Return Intent extra */
	public static String EXTRA_DEVICE_ADDRESS = "device_address";

	/** Already paired devices */
	private ArrayAdapter<String> mPairedDevicesArrayAdapter;
	/** Devices to be discovered */
	private ArrayAdapter<String> mNewDevicesArrayAdapter;

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Setup the window
		this.requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		this.setContentView(R.layout.device_list);

		// Set result CANCELED in case the user backs out
		this.setResult(Activity.RESULT_CANCELED);

		// Initialize the button to perform device discovery
		final Button scanButton = (Button) this.findViewById(R.id.button_scan);
		scanButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(final View v) {
				DeviceListActivity.this.doDiscovery();
				v.setVisibility(View.GONE);
			}
		});

		/*
		 * Initialize array adapters. One for already paired devices and one for
		 * newly discovered devices
		 */
		this.mPairedDevicesArrayAdapter = new ArrayAdapter<String>(this, R.layout.device_name);
		this.mNewDevicesArrayAdapter = new ArrayAdapter<String>(this, R.layout.device_name);

		// Find and set up the ListView for paired devices
		final ListView pairedListView = (ListView) this.findViewById(R.id.paired_devices);
		pairedListView.setAdapter(this.mPairedDevicesArrayAdapter);
		pairedListView.setOnItemClickListener(this.mDeviceClickListener);

		// Find and set up the ListView for newly discovered devices
		final ListView newDevicesListView = (ListView) this.findViewById(R.id.new_devices);
		newDevicesListView.setAdapter(this.mNewDevicesArrayAdapter);
		newDevicesListView.setOnItemClickListener(this.mDeviceClickListener);

		// Get a set of currently paired devices
		final Set<BluetoothDevice> pairedDevices = SessionManager.getInstance().getPairedDevices();

		// If there are paired devices, add each one to the ArrayAdapter
		if (pairedDevices.size() > 0) {
			this.findViewById(R.id.title_paired_devices).setVisibility(View.VISIBLE);
			for (final BluetoothDevice device : pairedDevices) {
				this.mPairedDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
			}
		} else {
			final String noDevices = this.getResources().getText(R.string.none_paired).toString();
			this.mPairedDevicesArrayAdapter.add(noDevices);
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		SessionManager.getInstance().cancelDiscovery(this, this.mReceiver);
	}

	/** Start device discover with the BluetoothAdapter */
	private void doDiscovery() {
		// Indicate scanning in the title
		this.setProgressBarIndeterminateVisibility(true);
		this.setTitle(R.string.scanning);

		// Turn on sub-title for new devices
		this.findViewById(R.id.title_new_devices).setVisibility(View.VISIBLE);
		SessionManager.getInstance().startDiscovery(this, this.mReceiver);
	}

	// The on-click listener for all devices in the ListViews
	private final OnItemClickListener mDeviceClickListener = new OnItemClickListener() {
		@Override
		public void onItemClick(final AdapterView<?> av, final View v, final int arg2, final long arg3) {
			// Cancel discovery because it's costly and we're about to connect
			SessionManager.getInstance().cancelDiscovery(DeviceListActivity.this, DeviceListActivity.this.mReceiver);

			// Get the device MAC address, which is the last 17 chars in the
			// View
			final String info = ((TextView) v).getText().toString();
			final String address = info.substring(info.length() - 17);

			// Create the result Intent and include the MAC address
			final Intent intent = new Intent();
			intent.putExtra(Data.DEVICE_NAME.v(), address);

			// Set result and finish this Activity
			DeviceListActivity.this.setResult(Activity.RESULT_OK, intent);
			DeviceListActivity.this.finish();
		}
	};

	/*
	 * The BroadcastReceiver that listens for discovered devices and changes the
	 * title when discovery is finished
	 */
	private final BluetoothBroadcastReceiver mReceiver = new BluetoothBroadcastReceiver() {

		@Override
		public void onFOUND(final Context context, final Intent intent) {
			// Get the BluetoothDevice object from the Intent
			final BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
			// If it's already paired, skip it, because it's been listed
			// already
			if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
				DeviceListActivity.this.mNewDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
			}
		}

		@Override
		public void onDISCOVERY_FINISHED(final Context context, final Intent intent) {
			DeviceListActivity.this.setProgressBarIndeterminateVisibility(false);
			DeviceListActivity.this.setTitle(R.string.select_device);
			if (DeviceListActivity.this.mNewDevicesArrayAdapter.getCount() == 0) {
				final String noDevices = DeviceListActivity.this.getResources().getText(R.string.none_found).toString();
				DeviceListActivity.this.mNewDevicesArrayAdapter.add(noDevices);
			}
		}
	};
}
