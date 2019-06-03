package com.zhangyy.weather00;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.Paint.Style;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout.LayoutParams;

/**
 * 
 * @author zhangyayun1@gmail.com
 * 
 */
@SuppressLint("ViewConstructor")
class ChartView extends View {

	public static final int DATA_SIZE = 48;

	public static final int RECT_SIZE = 10;
	private Point mSelectedPoint;

	private static final int k = 2;
	private static final int b = 30;

	// 鏋氫妇瀹炵幇鍧愭爣妗岄潰鐨勬牱寮忛鏍?
	public static enum Mstyle {
		Line, Curve
	}

	private Mstyle mstyle = Mstyle.Line;
	private Point[] mPoints = new Point[100];

	Context context;
	int bheight = 0;
	HashMap<Double, Double> map;
	ArrayList<Double> dlk;

	int totalvalue = 30;
	int pjvalue = 5;
	String xstr, ystr = "";// 妯旱鍧愭爣鐨勫睘鎬?
	int margint = 15;
	int marginb = 40;
	int c = 0;
	int resid = 0;
	Boolean isylineshow;

	/**
	 * 
	 * @param map
	 * @param totalvalue
	 * @param pjvalue
	 * @param xstr
	 * @param ystr
	 * @param isylineshow
	 */
	public void SetTuView(HashMap<Double, Double> map, int totalvalue,
			int pjvalue, String xstr, String ystr, Boolean isylineshow) {
		this.map = map;
		this.totalvalue = totalvalue;
		this.pjvalue = pjvalue;
		this.xstr = xstr;
		this.ystr = ystr;
		this.isylineshow = isylineshow;

		// 灞忓箷妯悜
		// act.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
	}

	public ChartView(Context ct) {
		super(ct);
		this.context = ct;
	}

	public ChartView(Context ct, AttributeSet attrs) {
		super(ct, attrs);
		this.context = ct;
	}

	public ChartView(Context ct, AttributeSet attrs, int defStyle) {
		super(ct, attrs, defStyle);
		this.context = ct;
	}

	@SuppressLint("DrawAllocation")
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if (c != 0)
			this.setbg(c);
		if (resid != 0)
			this.setBackgroundResource(resid);
		dlk = getintfrommap(map);
		int height = getHeight();
		if (bheight == 0)
			bheight = height - marginb;

		int width = getWidth();
		int blwidh = dip2px(context, 50) - 50;
		int pjsize = totalvalue / pjvalue;// 鐣岄潰甯冨眬鐨勫昂瀵哥殑姣斾緥
		// set up paint
		Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		paint.setColor(Color.WHITE);
		paint.setStrokeWidth(1);
		paint.setStyle(Style.STROKE);

		/* 画纵坐标 */
		for (int i = 0; i < pjsize + 1; i++)//
		{
			if (i == pjsize)/* 最上面的一条线颜色 */
				paint.setColor(Color.WHITE);
			/* 画横线 */
			canvas.drawLine(blwidh, bheight - (bheight / pjsize) * i + margint
					- 20, width, bheight - (bheight / pjsize) * i + margint
					- 20, paint);// --
			/* 画纵坐标标度 */
			drawline(pjvalue * i + ystr, blwidh / 2, bheight
					- (bheight / pjsize) * i + margint, canvas);
		}
		ArrayList<Integer> xlist = new ArrayList<Integer>();//
		//
		paint.setColor(Color.BLUE);
		if (dlk == null)
			return;
		/* 画横坐标 */

		mPoints = getSortPoints(map);
		if (mPoints.length == 0)
			return;
		int leftpoint = getSetionLeftPoint(mPoints[0].x);
		int rightpoint = getSetionRightPoint(mPoints[mPoints.length - 1].x);

		/* 动态设置宽度 */
		WindowManager wm = (WindowManager) getContext().getSystemService(
				Context.WINDOW_SERVICE);
		int width1 = wm.getDefaultDisplay().getWidth()-40;
		int width2 = (rightpoint - leftpoint) * k + blwidh + b;
		if (width1 > width2)
			getLayoutParams().width = width1;
		else
			getLayoutParams().width = width2;
		setLayoutParams(getLayoutParams());
		//
		// System.out.print("----left:" + leftpoint + "----\n");
		// System.out.print("----width1:" + width1 + "----\n");

		/* 获取横坐标的坐标值 */
		int len = rightpoint;
		if ((rightpoint - leftpoint) < width1)
			len = width1 + leftpoint;
		for (int i = leftpoint; i <= len; i += 30) {
			drawline(secToTime(i * 60).substring(0, 5) + xstr,
					(blwidh + (i - leftpoint) * k) + b, bheight + 40 - 20,
					canvas);
			// System.out.print("----secToTime(i):" + secToTime(i) + "----\n");
		}
		/* 获取曲线的点值 */
		for (int i = 0; i < mPoints.length; i++) {

			mPoints[i].x = ((mPoints[i].x - leftpoint) * k + b + blwidh - 10);

			// mPoints[i].y = bheight - mPoints[i].y * pjvalue / (bheight);
			mPoints[i].y = bheight - mPoints[i].y * (bheight)
					/ (pjvalue * pjsize);

			System.out.print("----mPoints[i].x:" + secToTime(mPoints[i].x)
					+ "----\n");
		}

		/* 画曲线 */
		// mPoints = getpoints(dlk, map, xlist, totalvalue, bheight);
		// Point[] ps=getpoints(dlk, map, xlist, totalvalue, bheight);
		// mPoints=ps;

		paint.setColor(Color.GREEN);
		paint.setStyle(Style.STROKE);
		paint.setStrokeWidth(2);

		if (mstyle == Mstyle.Curve)
			drawscrollline(mPoints, canvas, paint);
		else
			drawline(mPoints, canvas, paint);

		paint.setColor(Color.RED);
		paint.setStyle(Style.FILL);
		for (int i = 0; i < mPoints.length; i++) {
			canvas.drawRect(pointToRect(mPoints[i]), paint);
		}
		//canvas.drawRect(pointToRect(new Point(0, bheight + margint)), paint);

	}

	private WindowManager getWindowManager() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			for (int i = 0; i < mPoints.length; i++) {
				if (pointToRect(mPoints[i])
						.contains(event.getX(), event.getY())) {
					System.out.println("-yes-" + i);
					mSelectedPoint = mPoints[i];
				}
			}
			break;
		case MotionEvent.ACTION_MOVE:
			if (null != mSelectedPoint) {
				// mSelectedPoint.x = (int) event.getX();
				mSelectedPoint.y = (int) event.getY();
				// invalidate();
			}
			break;
		case MotionEvent.ACTION_UP:
			mSelectedPoint = null;
			break;
		default:
			break;
		}
		return true;

	}

	private RectF pointToRect(Point p) {
		return new RectF(p.x - RECT_SIZE / 2, p.y - RECT_SIZE / 2, p.x
				+ RECT_SIZE / 2, p.y + RECT_SIZE / 2);
	}

	private void drawscrollline(Point[] ps, Canvas canvas, Paint paint) {
		Point startp = new Point();
		Point endp = new Point();
		for (int i = 0; i < ps.length - 1; i++) {
			startp = ps[i];
			endp = ps[i + 1];
			int wt = (startp.x + endp.x) / 2;
			Point p3 = new Point();
			Point p4 = new Point();
			p3.y = startp.y;
			p3.x = wt;
			p4.y = endp.y;
			p4.x = wt;

			Path path = new Path();
			path.moveTo(startp.x, startp.y);
			path.cubicTo(p3.x, p3.y, p4.x, p4.y, endp.x, endp.y);
			canvas.drawPath(path, paint);

		}
	}

	private void drawline(Point[] ps, Canvas canvas, Paint paint) {
		Point startp = new Point();
		Point endp = new Point();
		for (int i = 0; i < ps.length - 1; i++) {
			startp = ps[i];
			endp = ps[i + 1];
			canvas.drawLine(startp.x, startp.y, endp.x, endp.y, paint);
		}
	}

	/**
	 * 以key键值的大小进行排序
	 * 
	 * @param map
	 * @return
	 */
	private Point[] getSortPoints(HashMap<Double, Double> map) {
		Point[] points = new Point[map.size()];
		int i = 0;
		Object[] keyArr = map.keySet().toArray();
		Arrays.sort(keyArr);
		for (Object key : keyArr) {
			Double key1 = (Double) key;
			Double val = map.get(key);
			points[i++] = new Point(key1.intValue(), val.intValue());
		}
		return points;
	}

	/**
	 * 获取点横坐标x所在区间的左端点
	 * 
	 * @param x
	 * @return
	 */

	private int getSetionLeftPoint(int x) {
		int leftPoint = 0;
		int a = (x) / 60;
		int b = (x) % 60;
		if (b > 30) {
			// b = (b - 30) * 3 / 7;
			leftPoint = a * 60 + 30;
		} else {
			leftPoint = a * 60;
		}

		return leftPoint;
	}

	/**
	 * 获取点横坐标x所在区间的右端点
	 * 
	 * @param x
	 * @return
	 */

	private int getSetionRightPoint(int x) {
		int rightPoint = 0;
		int a = (x) / 60;
		int b = (x) % 60;
		if (b > 30) {
			// b = (b - 30) * 3 / 7;
			rightPoint = (a + 1) * 60;
		} else {
			rightPoint = a * 60 + 30;
		}

		return rightPoint;
	}

	/**
	 * 获取点横坐标x所在区间中所在的位置
	 * 
	 * @param x
	 * @return
	 */

	private int getSetionPoint(int x) {
		int point = 0;
		int b = (x) % 100;
		if (b > 30) {
			point = x + (b - 30) * 3 / 7;
		} else {
			point = x;
		}

		return point;
	}

	private Point[] getpoints(ArrayList<Double> dlk,
			HashMap<Double, Double> map, ArrayList<Integer> xlist, int max,
			int h) {
		Point[] points = new Point[dlk.size()];
		for (int i = 0; i < dlk.size(); i++) {
			int ph = h - (int) (h * (map.get(dlk.get(i)) / max));

			points[i] = new Point(xlist.get(i), ph + margint);

		}
		/* 按键值从大到小的顺序存入点数组 */
		// int i = 0;
		// Object[] keyArr = map.keySet().toArray();
		// Arrays.sort(keyArr);
		// for (Object key : keyArr) {
		// Double key1 = (Double) key;
		// Double val = map.get(key);
		// points[i++] = new Point(key1.intValue(), val.intValue());
		// }

		return points;
	}

	private void drawline(String text, int x, int y, Canvas canvas) {
		Paint p = new Paint();
		p.setAlpha(0x0000ff);
		p.setTextSize(20);
		String familyName = "瀹嬩綋";
		Typeface font = Typeface.create(familyName, Typeface.NORMAL);
		p.setTypeface(font);
		p.setColor(Color.WHITE);
		p.setTextAlign(Paint.Align.CENTER);
		canvas.rotate(45, x, y);
		canvas.drawText(text, x, y + 10, p);
		canvas.rotate(-45, x, y);
	}

	public int dip2px(Context context, float dpValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dpValue * scale + 0.5f);
	}

	public int px2dip(Context context, float pxValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (pxValue / scale + 0.5f);
	}

	@SuppressWarnings("rawtypes")
	public ArrayList<Double> getintfrommap(HashMap<Double, Double> map) {
		ArrayList<Double> dlk = new ArrayList<Double>();
		int position = 0;
		if (map == null)
			return null;
		Set set = map.entrySet();
		Iterator iterator = set.iterator();

		while (iterator.hasNext()) {
			@SuppressWarnings("rawtypes")
			Map.Entry mapentry = (Map.Entry) iterator.next();
			dlk.add((Double) mapentry.getKey());
		}
		for (int i = 0; i < dlk.size(); i++) {
			int j = i + 1;
			position = i;
			Double temp = dlk.get(i);
			for (; j < dlk.size(); j++) {
				if (dlk.get(j) < temp) {
					temp = dlk.get(j);
					position = j;
				}
			}

			dlk.set(position, dlk.get(i));
			dlk.set(i, temp);
		}
		return dlk;

	}

	public void setbg(int c) {
		this.setBackgroundColor(c);
	}

	public HashMap<Double, Double> getMap() {
		return map;
	}

	public void setMap(HashMap<Double, Double> map) {
		this.map = map;
	}

	public int getTotalvalue() {
		return totalvalue;
	}

	public void setTotalvalue(int totalvalue) {
		this.totalvalue = totalvalue;
	}

	public int getPjvalue() {
		return pjvalue;
	}

	public void setPjvalue(int pjvalue) {
		this.pjvalue = pjvalue;
	}

	public String getXstr() {
		return xstr;
	}

	public void setXstr(String xstr) {
		this.xstr = xstr;
	}

	public String getYstr() {
		return ystr;
	}

	public void setYstr(String ystr) {
		this.ystr = ystr;
	}

	public int getMargint() {
		return margint;
	}

	public void setMargint(int margint) {
		this.margint = margint;
	}

	public Boolean getIsylineshow() {
		return isylineshow;
	}

	public void setIsylineshow(Boolean isylineshow) {
		this.isylineshow = isylineshow;
	}

	public int getMarginb() {
		return marginb;
	}

	public void setMarginb(int marginb) {
		this.marginb = marginb;
	}

	public Mstyle getMstyle() {
		return mstyle;
	}

	public void setMstyle(Mstyle mstyle) {
		this.mstyle = mstyle;
	}

	public int getBheight() {
		return bheight;
	}

	public void setBheight(int bheight) {
		this.bheight = bheight;
	}

	public int getC() {
		return c;
	}

	public void setC(int c) {
		this.c = c;
	}

	public int getResid() {
		return resid;
	}

	public void setResid(int resid) {
		this.resid = resid;
	}

	/**
	 * 秒格式化为时间
	 * 
	 * @param time
	 * @return
	 */
	public static String secToTime(int time) {
		String timeStr = null;
		int hour = 0;
		int minute = 0;
		int second = 0;
		if (time <= 0)
			return "00:00";
		else {
			minute = time / 60;
			if (minute < 60) {
				second = time % 60;
				timeStr = unitFormat(minute) + ":" + unitFormat(second);
			} else {
				hour = minute / 60;
				if (hour > 99)
					return "99:59:59";
				minute = minute % 60;
				second = time - hour * 3600 - minute * 60;
				timeStr = unitFormat(hour) + ":" + unitFormat(minute) + ":"
						+ unitFormat(second);
			}
		}
		return timeStr;
	}

	/**
	 * 整型格式化成字符串
	 * 
	 * @param i
	 * @return
	 */
	public static String unitFormat(int i) {
		String retStr = null;
		if (i >= 0 && i < 10)
			retStr = "0" + Integer.toString(i);
		else
			retStr = "" + i;
		return retStr;
	}

}
