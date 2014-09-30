package com.airbus.delegate;

import org.archive.crawler.Heritrix;

import com.airbus.email.MailSenderInfo;
import com.airbus.email.SimpleMailSender;
import com.airbus.timer.NFDFlightDataTimerTask;

public class Delegation {
	public static long PERIOD = (long) (0.25 * 60 * 60 * 1000); // 抓取的间隔时间

	public static void main(String[] args) throws Exception {
		Heritrix heritrix = null;
		heritrix = new Heritrix();
		heritrix.start(args);
		Delegation d = new Delegation();
		d.sendEmailByDate("2014-09-20", "2014-09-30");
		while (true) {
			Thread.sleep(PERIOD);
			heritrix.getComponent().stop();
			heritrix = new Heritrix();
			heritrix.start(args);
		}
	}

	/**
	 * 通过设置查询的起始和终止日期，发送这个时间段的新闻网址到接收邮箱
	 * 
	 * @param startDate
	 *            查询的起始日期 格式为 xxxx-xx-xx e.g：2014-09-30
	 * @param endDate
	 *            查询的终止日期 格式为 xxxx-xx-xx e.g：2014-10-01
	 */
	public void sendEmailByDate(String startDate, String endDate) {
		// look busy (and give MailSenderInfo a chance)
		try {
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
			mailInfo.setContent(NFDFlightDataTimerTask.getUrlsByDate(startDate,
					endDate));
			// SimpleMailSender要来发送邮件
			SimpleMailSender sms = new SimpleMailSender();
			sms.sendTextMail(mailInfo);// 发送文体格式
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
