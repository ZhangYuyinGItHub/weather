package com.zhangyy.weather00;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.weather00.R;

/**
 * Activity for scanning and displaying available Bluetooth LE devices.
 */

public class Setting extends Activity {

	private LeDeviceListAdapter mLeDeviceListAdapter;
	private BluetoothAdapter mBluetoothAdapter;
	private boolean mScanning;
	private Handler mHandler;

	ListView mDeviceList;
	Button mDeviceScanBtn;
	Button mDeviceConnectBtn;

	List<Ray> mRayList;
	rayOper mRayDb;
	ListView mRayDataListView = null;
	List<Map<String, Object>> mRayDataList = new ArrayList<Map<String, Object>>();
	SimpleAdapter mRayDataAdapter;

	private String mSelectDeviceName = null;
	private String mSelectDeviceAddr = null;
	private TextView mSelectDeviceNameTextView;
	private TextView mSelectDeviceAddrTextView;

	private static final int REQUEST_ENABLE_BT = 1;
	// 10秒后停止查找搜索.
	private static final long SCAN_PERIOD = 10000;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.setting);
		getActionBar().setTitle("设置");
		getActionBar().setIcon(R.drawable.setting);
		mHandler = new Handler();

		/* 设备设置 */
		mDeviceList = (ListView) findViewById(R.id.devicelist);
		mDeviceScanBtn = (Button) findViewById(R.id.device_scan);
		mDeviceConnectBtn = (Button) findViewById(R.id.device_connect);
		mDeviceScanBtn.setOnClickListener(deviceScanListener);
		mDeviceConnectBtn.setOnClickListener(deviceConnectListener);

		mSelectDeviceNameTextView = (TextView) findViewById(R.id.select_device_name);
		mSelectDeviceAddrTextView = (TextView) findViewById(R.id.select_device_addr);

		/* 数据设置 , ray data listview 设置 */
		mRayDataAdapter = new SimpleAdapter(this, mRayDataList,
				R.layout.ray_data_listview, new String[] { "id", "value",
						"time" }, new int[] { R.id.rayid, R.id.rayvalue,
						R.id.raytime });
		mRayDataListView = (ListView) findViewById(R.id.raydatalist);
		mRayDataListView.setAdapter(mRayDataAdapter);
		setRayDataBase();
		mRayDataListView.setOnItemLongClickListener(onlistviewLongClick);

		// 检查当前手机是否支持ble 蓝牙,如果不支持退出程序
		if (!getPackageManager().hasSystemFeature(
				PackageManager.FEATURE_BLUETOOTH_LE)) {
			Toast.makeText(this, "Not Support BLE", Toast.LENGTH_SHORT).show();
			finish();
		}

		// 初始化 Bluetooth adapter, 通过蓝牙管理器得到一个参考蓝牙适配器(API必须在以上android4.3或以上和版本)
		final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
		mBluetoothAdapter = bluetoothManager.getAdapter();

		// 检查设备上是否支持蓝牙
		if (mBluetoothAdapter == null) {
			Toast.makeText(this, "Not Support BLE", Toast.LENGTH_SHORT).show();
			finish();
			return;
		}

		mDeviceList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				final BluetoothDevice device = mLeDeviceListAdapter
						.getDevice(position);
				if (device == null)
					return;

				mSelectDeviceName = device.getName();
				mSelectDeviceAddr = device.getAddress();
				mSelectDeviceNameTextView.setText(mSelectDeviceName);
				mSelectDeviceAddrTextView.setText(mSelectDeviceAddr);

				// final Intent intent = new Intent();
				// intent.putExtra(Weather00.EXTRAS_DEVICE_NAME,
				// device.getName());
				// intent.putExtra(Weather00.EXTRAS_DEVICE_ADDRESS,
				// device.getAddress());
				// if (mScanning) {
				// mBluetoothAdapter.stopLeScan(mLeScanCallback);
				// mScanning = false;
				// }
				// // startActivity(intent);
				// setResult(RESULT_OK, intent);
				// finish();
			}
		});
	}

	private void setRayDataBase() {

		mRayDb = rayOper.getSingleRay(this);
		mRayList = mRayDb.findItems(null, null, null, null);

		for (Ray ray : mRayList) {
			Map<String, Object> listem = new HashMap<String, Object>();
			listem.put("id", ray.getId());
			listem.put("value", ray.getValue());
			listem.put("time", ray.getTime());
			mRayDataList.add(listem);
		}

		mRayDataAdapter.notifyDataSetChanged();
		// setListViewHeightBasedOnChildren(mRayDataListView);
	}

	private OnItemLongClickListener onlistviewLongClick = new OnItemLongClickListener() {

		public boolean onItemLongClick(AdapterView<?> parent, View view,
				int position, long id) {
			// TODO Auto-generated method stub
			mRayDb = rayOper.getSingleRay(Setting.this);
			mRayList = mRayDb.findItems(null, null, null, null);

			for (Ray ray : mRayList) {
				String item = ray.getId() + "  "+ray.getValue()+"  "+ray.getTime()+"\n";
				FileToScard("ray.txt", item);
			}

			return false;
		}
	};

	/**
	 * @author zhangyuyin
	 * @category 导出文件到SD卡
	 * @param filename
	 *            文件名
	 * @param content
	 *            文件的内容
	 */
	public boolean FileToScard(String filename, String content) {
		boolean flag = false;
		FileOutputStream mFileOutputStream = null;
		// 获得SD卡所在的路径
		File mFile = new File(Environment.getExternalStorageDirectory(),
				filename);
		// 判断SD卡是否可用
		if (Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			try {
				/**
				 * FileOutputStream 构造函数中，有两个参数，第一个是文件名；第二个是文件的读写方式，Boolean类型，
				 * 默认是false即清除原来的数据重新写入，若设置为true则选择追加的方式。
				 */
				mFileOutputStream = new FileOutputStream(mFile, true);
				mFileOutputStream.write(content.getBytes());
				Toast.makeText(Setting.this, mFile.getName(),
						Toast.LENGTH_SHORT).show();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				if (mFileOutputStream != null) {
					try {
						mFileOutputStream.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}

		}
		else
		{
			Toast.makeText(Setting.this, "---bukeyong---",
					Toast.LENGTH_SHORT).show();
		}
		return flag;
	}

	private OnClickListener deviceScanListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			if (mScanning == false) {
				mLeDeviceListAdapter.clear();
				scanLeDevice(true);

			} else {
				scanLeDevice(false);

			}
		}
	};
	private OnClickListener deviceConnectListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			// setRayDataBase();
			final Intent intent = new Intent();
			intent.putExtra(Weather00.EXTRAS_DEVICE_NAME, mSelectDeviceName);
			intent.putExtra(Weather00.EXTRAS_DEVICE_ADDRESS, mSelectDeviceAddr);
			if (mScanning) {
				mBluetoothAdapter.stopLeScan(mLeScanCallback);
				mScanning = false;
			}
			// startActivity(intent);
			setResult(RESULT_OK, intent);
			finish();
		}
	};

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		if (!mScanning) {
			menu.findItem(R.id.menu_stop).setVisible(false);
			menu.findItem(R.id.menu_scan).setVisible(false);
			menu.findItem(R.id.menu_refresh).setActionView(null);
			mDeviceScanBtn.setText("扫描");

		} else {
			menu.findItem(R.id.menu_stop).setVisible(false);
			menu.findItem(R.id.menu_scan).setVisible(false);
			menu.findItem(R.id.menu_refresh).setActionView(
					R.layout.actionbar_indeterminate_progress);
			mDeviceScanBtn.setText("停止...");
		}
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_scan:
			mLeDeviceListAdapter.clear();
			scanLeDevice(true);
			break;
		case R.id.menu_stop:
			scanLeDevice(false);
			break;
		}
		return true;
	}

	@Override
	protected void onResume() {
		super.onResume();

		// 为了确保设备上蓝牙能使用, 如果当前蓝牙设备没启用,弹出对话框向用户要求授予权限来启用
		if (!mBluetoothAdapter.isEnabled()) {
			if (!mBluetoothAdapter.isEnabled()) {
				Intent enableBtIntent = new Intent(
						BluetoothAdapter.ACTION_REQUEST_ENABLE);
				startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
			}
		}

		// Initializes list view adapter.
		mLeDeviceListAdapter = new LeDeviceListAdapter();
		mDeviceList.setAdapter(mLeDeviceListAdapter);
		scanLeDevice(true);

		final Intent intent = new Intent();
		intent.putExtra(Weather00.EXTRAS_DEVICE_NAME, "");
		intent.putExtra(Weather00.EXTRAS_DEVICE_ADDRESS, "");
		if (mScanning) {
			mBluetoothAdapter.stopLeScan(mLeScanCallback);
			mScanning = false;
		}
		// startActivity(intent);
		setResult(RESULT_OK, intent);
	}

	// @Override
	// protected void onActivityResult(int requestCode, int resultCode, Intent
	// data) {
	// // User chose not to enable Bluetooth.
	// if (requestCode == REQUEST_ENABLE_BT
	// && resultCode == Activity.RESULT_CANCELED) {
	// finish();
	// return;
	// }
	// super.onActivityResult(requestCode, resultCode, data);
	// }

	@Override
	protected void onPause() {
		super.onPause();
		scanLeDevice(false);
		mLeDeviceListAdapter.clear();
	}

	// private void onListItemClick(ListView l, View v, int position, long id) {
	// System.out.println("==position=="+position);
	// final BluetoothDevice device = mLeDeviceListAdapter.getDevice(position);
	// if (device == null) return;
	// final Intent intent = new Intent(this, DeviceControlActivity.class);
	// intent.putExtra(DeviceControlActivity.EXTRAS_DEVICE_NAME,
	// device.getName());
	// intent.putExtra(DeviceControlActivity.EXTRAS_DEVICE_ADDRESS,
	// device.getAddress());
	// if (mScanning) {
	// mBluetoothAdapter.stopLeScan(mLeScanCallback);
	// mScanning = false;
	// }
	// startActivity(intent);
	// }

	private void scanLeDevice(final boolean enable) {
		if (enable) {
			// Stops scanning after a pre-defined scan period.
			mHandler.postDelayed(new Runnable() {
				@Override
				public void run() {
					mScanning = false;
					mBluetoothAdapter.stopLeScan(mLeScanCallback);
					invalidateOptionsMenu();
				}
			}, SCAN_PERIOD);

			mScanning = true;
			mBluetoothAdapter.startLeScan(mLeScanCallback);
		} else {
			mScanning = false;
			mBluetoothAdapter.stopLeScan(mLeScanCallback);
		}
		invalidateOptionsMenu();
	}

	// Adapter for holding devices found through scanning.
	private class LeDeviceListAdapter extends BaseAdapter {
		private ArrayList<BluetoothDevice> mLeDevices;
		private LayoutInflater mInflator;

		public LeDeviceListAdapter() {
			super();
			mLeDevices = new ArrayList<BluetoothDevice>();
			mInflator = Setting.this.getLayoutInflater();
		}

		public void addDevice(BluetoothDevice device) {
			if (!mLeDevices.contains(device)) {
				mLeDevices.add(device);
			}
		}

		public BluetoothDevice getDevice(int position) {
			return mLeDevices.get(position);
		}

		public void clear() {
			mLeDevices.clear();
		}

		@Override
		public int getCount() {
			return mLeDevices.size();
		}

		@Override
		public Object getItem(int i) {
			return mLeDevices.get(i);
		}

		@Override
		public long getItemId(int i) {
			return i;
		}

		@Override
		public View getView(int i, View view, ViewGroup viewGroup) {
			ViewHolder viewHolder;
			// General ListView optimization code.
			if (view == null) {
				view = mInflator.inflate(R.layout.listitem_device, null);
				viewHolder = new ViewHolder();
				viewHolder.deviceAddress = (TextView) view
						.findViewById(R.id.device_address);
				viewHolder.deviceName = (TextView) view
						.findViewById(R.id.device_name);
				view.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) view.getTag();
			}

			BluetoothDevice device = mLeDevices.get(i);
			final String deviceName = device.getName();
			if (deviceName != null && deviceName.length() > 0)
				viewHolder.deviceName.setText(deviceName);
			else
				viewHolder.deviceName.setText("Unknow Device");
			viewHolder.deviceAddress.setText(device.getAddress());

			return view;
		}
	}

	// Device scan callback.
	private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {

		@Override
		public void onLeScan(final BluetoothDevice device, int rssi,
				byte[] scanRecord) {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					mLeDeviceListAdapter.addDevice(device);
					mLeDeviceListAdapter.notifyDataSetChanged();
				}
			});
		}
	};

	static class ViewHolder {
		TextView deviceName;
		TextView deviceAddress;
	}
}