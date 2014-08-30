package com.airbus.timer;

import java.util.Calendar;
import java.util.Date;
import java.util.Timer;

public class TimerManager {

	// 时间间隔
	private static final long PERIOD_MINUTE = 60 * 1000;
	private static final long PERIOD_WEEK = 7 * 24 * 60 * 60 * 1000;
	private static final long PERIOD_DAY = 24 * 60 * 60 * 1000;

	public void execute() {
		Calendar calendar = Calendar.getInstance();

		/*** 定制每日的执行时间 ***/

		calendar.set(Calendar.HOUR_OF_DAY, 9);
		calendar.set(Calendar.MINUTE, 30);
		calendar.set(Calendar.SECOND, 0);

		Date date = calendar.getTime(); // 第一次执行定时任务的时间

		// 如果第一次执行定时任务的时间 小于 当前的时间
		// 此时要在 第一次执行定时任务的时间 加一天，以便此任务在下个时间点执行。如果不加一天，任务会立即执行。
		if (date.before(new Date())) {
			date = this.addDay(date, 1);
		}

		Timer timer = new Timer();

		NFDFlightDataTimerTask task = new NFDFlightDataTimerTask();
		// 安排指定的任务在指定的时间开始进行重复的固定延迟执行。
		timer.schedule(task, new Date(), PERIOD_DAY);
	}

	// 增加或减少天数
	private Date addDay(Date date, int num) {
		Calendar startDT = Calendar.getInstance();
		startDT.setTime(date);
		startDT.add(Calendar.DAY_OF_MONTH, num);
		return startDT.getTime();
	}
}
