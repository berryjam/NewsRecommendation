package com.airbus.jdbc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class DataBaseUtil {
	private static List<String> URLS;

	private static final String PATH = "/Users/berryjam/git/NewsRecommendation/NewsRecommendation/record.txt";

	private Connection connection = null;

	public void connect(String drive, String url, String username,
			String password) {
		try {
			Class.forName(drive);
			connection = DriverManager.getConnection(url, username, password);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void init() {
		connect(Constants.DRIVE, Constants.URL, Constants.USERNAME,
				Constants.PASSWORD);
		URLS = new ArrayList<String>();
		PreparedStatement statement;
		String url = null;
		try {
			statement = connection.prepareStatement("SELECT url FROM URLS");
			ResultSet dateResultSet = statement.executeQuery();
			while (dateResultSet.next()) {
				url = dateResultSet.getString("url");
				URLS.add(url);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void insertUrls(String url) throws NumberFormatException,
			SQLException, IOException {
		if (isUniqueURL(url)) {
			FileReader fr = new FileReader(new File(PATH));
			BufferedReader br = new BufferedReader(fr);
			String line = br.readLine();
			br.close();
			FileWriter fw = new FileWriter(new File(PATH));
			try {
				int batch = 1;
				if (line == null) {
					fw.write(Integer.toString(batch));
					fw.flush();
				} else {
					batch = Integer.parseInt(line);
					if (isNextBatch())
						++batch;
					fw.write(Integer.toString(batch));
					fw.flush();
				}
				String date = getCurrentDate();
				PreparedStatement statement = connection
						.prepareStatement("INSERT INTO URLS(url,date,batch) VALUES('"
								+ url + "','" + date + "','" + batch + "')");
				statement.executeUpdate();
				fw.close();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public String getLastBatchURLS() {
		StringBuilder sb = new StringBuilder("");
		String date = getLastBatchDate();
		System.out.println("Last Batch Date:" + date);
		if (date == null)
			return sb.toString();
		try {
			PreparedStatement statement = connection
					.prepareStatement("SELECT url FROM URLS WHERE date ='"
							+ date + "'");
			ResultSet rs = statement.executeQuery();
			while (rs.next()) {
				sb.append(rs.getString("url") + "\r\n");
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return sb.toString();
	}

	private String getCurrentDate() {
		SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd");
		Date date = new Date();
		return f.format(date);
	}

	/**
	 * 返回最新一批抓取的链接中的最早日期
	 * 
	 * @return 日期或者为null
	 */
	private String getLastBatchDate() {
		PreparedStatement statement;
		List<String> dates = new ArrayList<String>();
		try {
			statement = connection
					.prepareStatement("SELECT DISTINCT date FROM URLS WHERE batch = (SELECT MAX(batch) FROM URLS)");
			ResultSet dateResultSet = statement.executeQuery();
			String date = null;
			while (dateResultSet.next()) {
				date = dateResultSet.getString("date");
				dates.add(date);
			}
			Collections.sort(dates);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (!dates.isEmpty())
			return dates.get(0);
		else
			return null;
	}

	// 增加或减少天数
	private Date addDay(Date date, int num) {
		Calendar startDT = Calendar.getInstance();
		startDT.setTime(date);
		startDT.add(Calendar.DAY_OF_MONTH, num);
		return startDT.getTime();
	}

	private boolean isNextBatch() {
		String s = getLastBatchDate();
		if (s == null)
			return false;
		SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd");
		Date lastBatchDate = null;
		try {
			lastBatchDate = f.parse(s);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Date nextBatchDate = addDay(lastBatchDate, Constants.PERIOD_DAY);
		System.out.println("nextBatchDate:" + nextBatchDate);
		Date currentDate = new Date();
		String currentTime = f.format(currentDate);
		try {
			currentDate = f.parse(currentTime);
			System.out.println("currentDate:" + currentDate);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (currentDate.before(nextBatchDate))
			return false;
		else
			return true;
	}

	private boolean isUniqueURL(String url) {
		for (String s : URLS) {
			if (s.equals(url))
				return false;
		}
		return true;
	}
}
