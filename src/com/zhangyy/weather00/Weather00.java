/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zhangyy.weather00;

import android.app.Activity;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import com.example.weather00.R;

/**
 * For a given BLE device, this Activity provides the user interface to connect,
 * display data, and display GATT services and characteristics supported by the
 * device. The Activity communicates with {@code BluetoothLeService}, which in
 * turn interacts with the Bluetooth LE API.
 */
public class Weather00 extends Activity {
	private final static String TAG = Weather00.class.getSimpleName();

	public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
	public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";

	private TextView mConnectionState;
	// private TextView mDataField;
	private String mDeviceNameStr = null;
	private String mDeviceAddressStr = null;
	private BluetoothLeService mBluetoothLeService;
	private boolean mServiceConnected = false;
	private boolean mDeviceConnected = false;
	private BluetoothGattCharacteristic mRayCharacteristic;

	Button mSettingBtn;
	Button mTestBtn;
	Button mEnableBtn;
	Button mDisableBtn;
	Button mLeftArrayBtn;
	Button mRightArrayBtn;
	TextView mRayValue;
	TextView mDeviceAddressTextView;
	TextView mDeviceNameTextView;
	TextView mRayGradeTextView;

	TextView mData;
	String mDateStr;

	ChartView mChartView;
	HashMap<Double, Double> map;

	rayOper mRayDb;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.control);

		final Intent intent = getIntent();
		mDeviceNameStr = intent.getStringExtra(EXTRAS_DEVICE_NAME);
		mDeviceAddressStr = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);

		mRayValue = (TextView) findViewById(R.id.ray_text);
		Typeface typeFace = Typeface.createFromAsset(getAssets(),
				"fonts/Helvetica_LT_35_Thin.ttf");
		mRayValue.setTypeface(typeFace);

		mDeviceAddressTextView = (TextView) findViewById(R.id.device_address);
		mDeviceNameTextView = (TextView) findViewById(R.id.device_name);
		mSettingBtn = (Button) findViewById(R.id.button0);
		mSettingBtn.setOnClickListener(settingClickListener);

		mEnableBtn = (Button) findViewById(R.id.notifyenable);
		mEnableBtn.setOnClickListener(notifylistener);
		mDisableBtn = (Button) findViewById(R.id.notifydisable);
		mDisableBtn.setOnClickListener(disablelistener);

		mLeftArrayBtn = (Button) findViewById(R.id.leftarray);
		mLeftArrayBtn.setOnClickListener(leftArrClickListener);
		mRightArrayBtn = (Button) findViewById(R.id.rightarray);
		mRightArrayBtn.setOnClickListener(rightArrClickListener);
		mData = (TextView) findViewById(R.id.mdisplaydate);
		mRayGradeTextView = (TextView) findViewById(R.id.ray_grade);
		// mData.setTypeface(typeFace);

		/* chart view */
		mChartView = (ChartView) findViewById(R.id.chart_view);
		// chartPaint();
		mChartView.SetTuView(map, 50, 10, "", "", false);
		map = new HashMap<Double, Double>();

		/* 创建数据库 */
		mRayDb = rayOper.getSingleRay(this);
		// mRayDb.deleteItems(null, null, null, null, null);
		mRayDb.addItem("000005DC", "2016-08-03 13:14:30");
		mRayDb.addItem("000004C3", "2016-08-03 15:16:30");
		mRayDb.addItem("00000121", "2016-08-03 16:17:30");
		mRayDb.addItem("000001DC", "2016-08-02 13:14:30");
		mRayDb.addItem("000002C3", "2016-08-02 15:16:30");
		mRayDb.addItem("00000321", "2016-08-02 16:17:30");
		mRayDb.addItem("00000321", "2016-08-01 16:17:30");
		setDisplayCurve(getCurrentDate());

		mConnectionState = (TextView) findViewById(R.id.device_state);
		// mDataField = (TextView) findViewById(R.id.data_value);

		// getActionBar().setTitle(mDeviceName);
		// getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().hide();
		Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
		bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
	}

	private Button.OnClickListener leftArrClickListener = new Button.OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			String currentDate = mDateStr;// getCurrentDate();
			String leftDate = getLeftDate(currentDate);
			setDisplayCurve(leftDate);

		}
	};
	private Button.OnClickListener rightArrClickListener = new Button.OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub

			String currentDate = mDateStr;// getCurrentDate();
			String rightDate = getRightDate(currentDate);
			setDisplayCurve(rightDate);

		}
	};

	private Button.OnClickListener settingClickListener = new Button.OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub

			Intent writeIntent = new Intent();
			writeIntent.setClass(Weather00.this, Setting.class);
			startActivityForResult(writeIntent, 1);

			if (mDeviceConnected == false)
				unbindService(mServiceConnection);

		}
	};
	private Button.OnClickListener notifylistener = new Button.OnClickListener() {

		@Override
		public void onClick(View v) {
			if (mRayCharacteristic != null) {
				// mBluetoothLeService.readCharacteristic(mRayCharacteristic);
				mBluetoothLeService.setCharacteristicNotification(
						mRayCharacteristic, true);
				// Toast.makeText(Weather00.this, "--enable click--.",
				// Toast.LENGTH_SHORT).show();
				mEnableBtn.setVisibility(View.GONE);
				mDisableBtn.setVisibility(View.VISIBLE);
			} else
				Toast.makeText(Weather00.this, "no characteristic!!!",
						Toast.LENGTH_SHORT).show();
		}
	};
	private Button.OnClickListener disablelistener = new Button.OnClickListener() {

		@Override
		public void onClick(View v) {
			if (mRayCharacteristic != null) {
				// mBluetoothLeService.readCharacteristic(mRayCharacteristic);
				mBluetoothLeService.setCharacteristicNotification(
						mRayCharacteristic, false);

			}
			mEnableBtn.setVisibility(View.VISIBLE);
			mDisableBtn.setVisibility(View.GONE);
		}
	};

	/**
	 * 显示指定日期的数据曲线
	 * 
	 * @param date
	 */
	void setDisplayRayValue(String revData) {// 2014-06-30 15:43:12
		mRayValue.setText(Integer.parseInt(revData, 16) + "");
		mRayGradeTextView.setText(getRayGrade(Integer.parseInt(revData, 16)));
	}

	String getCurrentDate() {

		SimpleDateFormat sDateFormat = new SimpleDateFormat(
				"yyyy-MM-dd HH:mm:ss");
		String date = sDateFormat.format(new Date());

		String datestr = date;
		// Toast.makeText(Weather00.this, datestr, Toast.LENGTH_SHORT).show();
		mDateStr = datestr;
		return datestr;
	}

	String getRayGrade(int rayValue) {
		String grade = 0 + "";
		if (rayValue <= 50)
			grade = 0 + "";
		else if (rayValue <= 227)
			grade = 1 + "";
		else if (rayValue <= 318)
			grade = 2 + "";
		else if (rayValue <= 408)
			grade = 3 + "";
		else if (rayValue <= 503)
			grade = 4 + "";
		else if (rayValue <= 606)
			grade = 5 + "";
		else if (rayValue <= 696)
			grade = 6 + "";
		else if (rayValue <= 795)
			grade = 7 + "";
		else if (rayValue <= 881)
			grade = 8 + "";
		else if (rayValue <= 976)
			grade = 9 + "";
		else if (rayValue <= 1079)
			grade = 10 + "";
		else if (rayValue <= 1170)
			grade = 11 + "";
		else if (rayValue > 1170) {
			grade = 11 + "";
		}
		return grade;
	}

	/**
	 * 获取当前日期的上一天
	 * 
	 * @param date
	 */
	String getLeftDate(String currentDate) {// 2014-06-30 15:43:12
		String date = "";
		// int monthInt = Integer.parseInt(date.substring(5, 7));
		int dayInt = Integer.parseInt(currentDate.substring(8, 10));

		if (dayInt == 1) {
			date = "2016-08-01 00:00:00";
		} else {
			dayInt = dayInt - 1;
			date = "2016-" + "08-" + dayInt / 10 + dayInt % 10 + " 00:00:00";
		}
		mDateStr = date;
		// Toast.makeText(Weather00.this, mDateStr, Toast.LENGTH_SHORT).show();
		return date;
	}

	/**
	 * 获取当前日期的上一天
	 * 
	 * @param date
	 */
	String getRightDate(String currentDate) {// 2014-06-30 15:43:12
		String date = "";
		// int monthInt = Integer.parseInt(date.substring(5, 7));
		int dayInt = Integer.parseInt(currentDate.substring(8, 10));

		if (dayInt == 31) {
			date = "2016-08-31 00:00:00";
		} else {
			dayInt = dayInt + 1;
			date = "2016-" + "08-" + dayInt / 10 + dayInt % 10 + " 00:00:00";
		}
		mDateStr = date;
		return date;
	}

	/**
	 * 显示指定日期的数据曲线
	 * 
	 * @param date
	 */
	void setDisplayCurve(String date) {// 2014-06-30 15:43:12

		mDateStr = date.substring(0, 10);
		mData.setText(mDateStr);
		String month = date.substring(5, 7);
		String day = date.substring(8, 10);

		map.clear();

		List<Ray> arrRay = mRayDb.findItems(month, day, null, null);//

		if (arrRay != null) {
			// HashMap<Double, Double> map = new HashMap<Double, Double>();
			for (Ray ray : arrRay) {

				String value = ray.getValue();
				String hour = ray.getTime().substring(11, 13);
				String minute = ray.getTime().substring(14, 16);
				int hourInt = Integer.parseInt(hour);
				int minuteInt = Integer.parseInt(minute);
				int valueInt = (Integer.valueOf(Integer.parseInt(value, 16))
						.intValue());
				if ((valueInt <= 4500.0) && (hourInt < 24) && (minuteInt < 60)) {
					map.put(hourInt * 60 + minuteInt + 0.0,// 转换成分钟数
							valueInt + 0.0);// 十六进制-->十进制-->double
				} else {
					Toast.makeText(Weather00.this,
							valueInt + "," + ray.getTime(), Toast.LENGTH_SHORT)
							.show();
				}
				// Toast.makeText(Weather00.this, value, Toast.LENGTH_SHORT)
				// .show();

			}

		} else {
			Toast.makeText(Weather00.this, "--array is null--.",
					Toast.LENGTH_SHORT).show();
		}
		mChartView.setTotalvalue(1500);
		mChartView.setPjvalue(300);
		mChartView.setMap(map);
		mChartView.setMargint(20);
		mChartView.setMarginb(50);
		mChartView.setMstyle(ChartView.Mstyle.Curve);
		mChartView.invalidate();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		if ((data == null) || (requestCode != 1))
			return;

		mDeviceAddressStr = data.getExtras().getString(
				Weather00.EXTRAS_DEVICE_ADDRESS);
		mDeviceNameStr = data.getExtras().getString(
				Weather00.EXTRAS_DEVICE_NAME);

		Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
		bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);

	};

	// Code to manage Service lifecycle.
	private final ServiceConnection mServiceConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName componentName,
				IBinder service) {
			mBluetoothLeService = ((BluetoothLeService.LocalBinder) service)
					.getService();
			if (!mBluetoothLeService.initialize()) {
				Log.e(TAG, "Unable to initialize Bluetooth");
				finish();
			}
			// Automatically connects to the device upon successful start-up
			// initialization.
			// mBluetoothLeService.connect(mDeviceAddress);

			if (mDeviceAddressStr != null) {
				if (mDeviceAddressStr.length() != 17)
					return;
				mBluetoothLeService.connect(mDeviceAddressStr);
				Toast.makeText(Weather00.this, mDeviceAddressStr,
						Toast.LENGTH_SHORT).show();
			}

			mDeviceAddressTextView.setText(mDeviceAddressStr);
			mDeviceNameTextView.setText(mDeviceNameStr);
			Toast.makeText(Weather00.this, "Service connected.",
					Toast.LENGTH_SHORT).show();

			mServiceConnected = true;
		}

		@Override
		public void onServiceDisconnected(ComponentName componentName) {
			mBluetoothLeService = null;
			mServiceConnected = false;
		}
	};

	// Handles various events fired by the Service.
	// ACTION_GATT_CONNECTED: connected to a GATT server.
	// ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
	// ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
	// ACTION_DATA_AVAILABLE: received data from the device. This can be a
	// result of read
	// or notification operations.
	private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();
			if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
				mDeviceConnected = true;
				updateConnectionState(R.string.connected);
				// invalidateOptionsMenu();
			} else if (BluetoothLeService.ACTION_GATT_DISCONNECTED
					.equals(action)) {
				mDeviceConnected = false;
				updateConnectionState(R.string.disconnected);
				// invalidateOptionsMenu();
				clearUI();
			} else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED
					.equals(action)) {
				displayGattServices(mBluetoothLeService
						.getSupportedGattServices());
			} else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
				displayData(intent
						.getStringExtra(BluetoothLeService.EXTRA_DATA));
				Toast.makeText(Weather00.this, "data available.",
						Toast.LENGTH_SHORT).show();
			} else if (BluetoothLeService.ACTION_DATA_READ.equals(action)) {
				displayData(intent
						.getStringExtra(BluetoothLeService.EXTRA_DATA));
				Toast.makeText(Weather00.this, "data available.",
						Toast.LENGTH_SHORT).show();
			} else if (BluetoothLeService.ACTION_DATA_NOTIFICATION
					.equals(action)) {
				setDisplayCurve(mDateStr);
				Toast.makeText(Weather00.this, "data receive.",
						Toast.LENGTH_SHORT).show();
				setDisplayRayValue(intent
						.getStringExtra(BluetoothLeService.EXTRA_DATA));
			}
		}
	};

	private void clearUI() {
		// mGattServicesList.setAdapter((SimpleExpandableListAdapter) null);
		// mDataField.setText(R.string.no_data);
	}

	@Override
	protected void onResume() {
		super.onResume();
		registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
		if (mBluetoothLeService != null) {
			// final boolean result =
			// mBluetoothLeService.connect(mDeviceAddress);
			// Log.d(TAG, "Connect request result=" + result);
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		unregisterReceiver(mGattUpdateReceiver);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unbindService(mServiceConnection);
		mBluetoothLeService = null;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.gatt_services, menu);
		if (mServiceConnected) {
			menu.findItem(R.id.menu_connect).setVisible(false);
			menu.findItem(R.id.menu_disconnect).setVisible(true);
		} else {
			menu.findItem(R.id.menu_connect).setVisible(true);
			menu.findItem(R.id.menu_disconnect).setVisible(false);
		}
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_connect:
			// mBluetoothLeService.connect(mDeviceAddress);
			return true;
		case R.id.menu_disconnect:
			mBluetoothLeService.disconnect();
			return true;
		case android.R.id.home:
			onBackPressed();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private void updateConnectionState(final int resourceId) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				mConnectionState.setText(resourceId);
			}
		});
	}

	private void displayData(String data) {
		if (data != null) {
			mDeviceNameTextView.setText(data);
		}
	}

	// Demonstrates how to iterate through the supported GATT
	// Services/Characteristics.
	// In this sample, we populate the data structure that is bound to the
	// ExpandableListView
	// on the UI.
	private void displayGattServices(List<BluetoothGattService> gattServices) {

		if (gattServices == null) {
			Toast.makeText(Weather00.this, "gattServices = null.",
					Toast.LENGTH_SHORT).show();
			return;
		}
		String uuid = null;

		// Loops through available GATT Services.
		for (BluetoothGattService gattService : gattServices) {
			uuid = gattService.getUuid().toString();
			List<BluetoothGattCharacteristic> gattCharacteristics = gattService
					.getCharacteristics();
			ArrayList<BluetoothGattCharacteristic> charas = new ArrayList<BluetoothGattCharacteristic>();

			// Loops through available Characteristics.
			for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
				charas.add(gattCharacteristic);
				uuid = gattCharacteristic.getUuid().toString();
				if (uuid.toString().substring(4, 8).equals("ffd3")) {
					Toast.makeText(Weather00.this, "ray characteristic.",
							Toast.LENGTH_SHORT).show();
					mRayCharacteristic = gattCharacteristic;

				}
			}
		}
	}

	private static IntentFilter makeGattUpdateIntentFilter() {
		final IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
		intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
		intentFilter
				.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
		intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
		intentFilter.addAction(BluetoothLeService.ACTION_DATA_READ);
		intentFilter.addAction(BluetoothLeService.ACTION_DATA_NOTIFICATION);
		return intentFilter;
	}
}
