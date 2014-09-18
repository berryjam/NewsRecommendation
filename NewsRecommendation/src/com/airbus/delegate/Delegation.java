package com.airbus.delegate;

import org.archive.crawler.Heritrix;

public class Delegation {
	public static long PERIOD = (long) (5 * 60 * 60 * 1000); // 抓取的间隔时间

	public static void main(String[] args) throws Exception {
		Heritrix heritrix = null;
		heritrix = new Heritrix();
		heritrix.start(args);
		while (true) {
			Thread.sleep(PERIOD);
			heritrix.getComponent().stop();
			heritrix = new Heritrix();
			heritrix.start(args);
		}
	}
}
