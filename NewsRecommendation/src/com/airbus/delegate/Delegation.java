package com.airbus.delegate;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.archive.crawler.Heritrix;

import com.airbus.email.MailSenderInfo;
import com.airbus.email.SimpleMailSender;
import com.airbus.timer.NFDFlightDataTimerTask;

public class Delegation {
	public static final long PERIOD = (long) (0.25 * 60 * 60 * 1000); // 抓取的间隔时间，单位为毫秒

	public static void main(String[] args) throws Exception {

		Heritrix heritrix = null;
		heritrix = new Heritrix();
		heritrix.start(args);

		// // ---------------如果dates.txt内容包含起始日期和结束日期则查找数据库发送相应数据--------------
		// Delegation delegation = new Delegation();
		// delegation.checkInputDate();
		// // ---------------如果dates.txt内容包含起始日期和结束日期则查找数据库发送相应数据--------------
		//
		// while (true) {
		// Thread.sleep(PERIOD);
		// heritrix.getComponent().stop();
		// heritrix = new Heritrix();
		// heritrix.start(args);
		// }
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
			mailInfo.setUserName("berry33333");// 发送人邮箱
			mailInfo.setPassword("abc893131");// 发送人的邮箱密码
			mailInfo.setFromAddress("berry33333@126.com");
			// FIXME 更改为要接受新闻信息的收件人邮箱
			mailInfo.setToAddress("berryjamcoding@gmail.com");// 接收人邮箱
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

	// 目前通过该功能来模拟在输入栏输入日期返回相应的新闻,如果dates.txt里面内容不为空则会读取里面的日期然后推送邮件
	public void checkInputDate() {
		File file = new File("dates.txt");
		FileReader fr;
		try {
			fr = new FileReader(file);
			BufferedReader br = new BufferedReader(fr);
			String startDate = br.readLine();
			String endDate = br.readLine();
			if (startDate != null && endDate != null) {
				this.sendEmailByDate(startDate, endDate);
			}
			br.close();
			fr.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
