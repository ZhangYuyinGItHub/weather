package com.zhangyy.weather00;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class rayOper {

	private raySQLiteOpenHelper mRayDb;

	private static volatile rayOper singRayObject = null;

	private rayOper(Context context) {
		mRayDb = new raySQLiteOpenHelper(context);
	}

	/**
	 * 获取数据库操作对象
	 * 
	 */
	public static rayOper getSingleRay(Context context) {
		if (singRayObject == null) {
			singRayObject = new rayOper(context);
		}

		return singRayObject;
	}

	/**
	 * 添加一条数据记录
	 * 
	 * @param ray_value
	 * @param time
	 */
	public boolean addItem(String ray_value, String time) {

		boolean ret = false;
		SQLiteDatabase db = mRayDb.getWritableDatabase();
		db.execSQL("insert into ray (rayvalue, time) values (?, ?)",
				new String[] { ray_value, time });

		db.close();
		ret = findItem(ray_value, time);
		return ret;

	}

	/**
	 * 添加一条数据记录
	 * 
	 * @param ray_value
	 * @param time
	 */
	public boolean findItem(String ray_value, String time) {
		boolean ret = false;
		SQLiteDatabase db = mRayDb.getReadableDatabase();
		Cursor cursor = db.rawQuery(
				"select * from ray where rayvalue = ? and time = ?",
				new String[] { ray_value, time });
		if (cursor.getCount() != 0)
			ret = true;
		cursor.close();
		db.close();

		return ret;
	}

	/**
	 * 查找指定时间的记录, 时间格式："2016-12-18 16:17:30"
	 * 
	 * @param month
	 * @param day
	 * @param hour
	 * @param minute
	 */
	public ArrayList<Ray> findItems(String month, String day, String hour,
			String minute) {
		String time = "";
		if ((month == null) || (month.equals("")))
			time = "2016-";
		else if ((day == null) || (day.equals("")))
			time = "2016-" + month;
		else if ((hour == null) || (hour.equals("")))
			time = "2016-" + month + "-" + day;
		else if ((minute == null) || (minute.equals("")))
			time = "2016-" + month + "-" + day + " " + hour;
		else
			time = "2016-" + month + "-" + day + " " + hour + ":" + minute;

		SQLiteDatabase db = mRayDb.getReadableDatabase();// 2016-12-18 16:17:30

		Cursor cursor = db.rawQuery("select * from ray where time like '"
				+ time + "%'", null);
		ArrayList<Ray> arrayRay = new ArrayList<Ray>();

		for (; cursor.moveToNext() == true;) {

			String rayValue = cursor.getString(cursor
					.getColumnIndex("rayvalue"));
			String rayTime = cursor.getString(cursor.getColumnIndex("time"));
			Ray ray = new Ray(rayValue, rayTime);

			String rayId = cursor.getString(cursor.getColumnIndex("id"));
			ray.setRayId(rayId);
			arrayRay.add(ray);
		}
		cursor.close();
		db.close();

		return arrayRay;
	}

	/**
	 * 
	 * @param month
	 * @param day
	 * @param hour
	 * @param minute
	 */
	public void deleteItems(String month, String day, String hour,
			String minute, String second) {
		String time = "";
		if ((month == null) || (month.equals("")))
			time = "2016-";
		else if ((day == null) || (day.equals("")))
			time = "2016-" + month;
		else if ((hour == null) || (hour.equals("")))
			time = "2016-" + month + "-" + day;
		else if ((minute == null) || (minute.equals("")))
			time = "2016-" + month + "-" + day + " " + hour;
		else if ((second == null) || (second.equals("")))
			time = "2016-" + month + "-" + day + " " + hour + ":" + minute;
		else
			time = "2016-" + month + "-" + day + " " + hour + ":" + minute
					+ ":" + second;

		System.out.print("-----" + time + "-----");

		SQLiteDatabase db = mRayDb.getWritableDatabase();

		db.execSQL("delete from ray where time like '" + time + "%'");
		db.close();
	}
}
