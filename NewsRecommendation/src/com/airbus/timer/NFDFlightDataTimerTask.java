package com.airbus.timer;

import java.util.TimerTask;

import com.airbus.email.MailSenderInfo;
import com.airbus.email.SimpleMailSender;
import com.airbus.jdbc.DataBaseUtil;

public class NFDFlightDataTimerTask extends TimerTask {

	@Override
	public void run() {
		try {
			// look busy (and give MailSenderInfo a chance)
			Thread.sleep(10000);
			// 在这里写你要执行的内容
			// MailSenderInfo主要是设置邮件
			MailSenderInfo mailInfo = new MailSenderInfo();
			mailInfo.setMailServerHost("smtp.126.com");
			mailInfo.setMailServerPort("25");
			mailInfo.setValidate(true);
			mailInfo.setUserName("berry22222");
			mailInfo.setPassword("893131");// 您的邮箱密码
			mailInfo.setFromAddress("berry22222@126.com");
			// FIXME 更改为要接受新闻信息的收件人邮箱
			mailInfo.setToAddress("berryjamcoding@gmail.com");
			mailInfo.setSubject("Airbus 每周新技术相关新闻推荐");
			getUrlsByDate("2014-09-30", "2014-09-30");
			mailInfo.setContent(getUrlsByPeriod());
			// SimpleMailSender要来发送邮件
			SimpleMailSender sms = new SimpleMailSender();
			sms.sendTextMail(mailInfo);// 发送文体格式
			System.out.println("发送邮件！");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static String getUrlsByDate(String startDate, String endDate) {
		DataBaseUtil util = new DataBaseUtil();
		util.init();
		return util.getURLSByDate(startDate, endDate);
	}

	private String getUrlsByPeriod() {
		// 根据需求从数据库读取最近一批抓取到的url
		DataBaseUtil util = new DataBaseUtil();
		util.init();
		return util.getLastBatchURLS();
	}
}
