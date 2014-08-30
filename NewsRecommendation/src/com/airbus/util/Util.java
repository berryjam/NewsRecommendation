package com.airbus.util;

import java.io.FileWriter;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.htmlparser.Node;
import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.filters.NodeClassFilter;
import org.htmlparser.filters.OrFilter;
import org.htmlparser.nodes.TextNode;
import org.htmlparser.tags.HeadingTag;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.tags.MetaTag;
import org.htmlparser.tags.ParagraphTag;
import org.htmlparser.tags.TitleTag;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;

public class Util {

	private String[] keyWords = new String[] { "Composites Technologies",
			"Metallic Technologies", "Surface Engineering",
			"Structure Engineering", "Aeromechanics", "Sensors Electronics",
			"Systems Integration", "Physics", "IT", "Security Services",
			"Simulation", "Energy", "Propulsion", "Biofuel", "复合材料", "表面工程",
			"重金属技术", "结构工程", "航空", "电子传感器", "生物燃料", "安全服务", "能源", "仿真", "推进力" };

	public static void saveLine(String url, String filePath) throws IOException {
		FileWriter fw = new FileWriter(filePath);
		fw.write(url);
	}

	public static void extractKeyWordText(String url, String keyword) {
		try {
			Parser parser = new Parser(url);

			NodeFilter textFilter = new NodeClassFilter(TextNode.class);
			NodeFilter titleFilter = new NodeClassFilter(TitleTag.class);
			OrFilter lastFilter = new OrFilter();
			lastFilter
					.setPredicates(new NodeFilter[] { textFilter, titleFilter });

			NodeList nodelist = parser.parse(lastFilter);
			Node[] nodes = nodelist.toNodeArray();
			String line = "";
			for (int i = 0; i < nodes.length; i++) {
				Node node = nodes[i];
				if (node instanceof TextNode) {
					TextNode textnode = (TextNode) node;
					line = textnode.getText();
				} else if (node instanceof TitleTag) {
					TitleTag titlenode = (TitleTag) node;
					line = titlenode.getTitle();
				}
				if (isTrimEmpty(line))
					continue;
				if (line.contains("汽车")) {
					System.out.println(line);
				}
			}

		} catch (ParserException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static boolean isTrimEmpty(String astr) {
		if ((null == astr) || (astr.length() == 0)) {
			return true;
		}
		if (isBlank(astr.trim())) {
			return true;
		}
		return false;
	}

	public static boolean isBlank(String astr) {
		if ((null == astr) || (astr.length() == 0)) {
			return true;
		} else {
			return false;
		}
	}

	public float getRelateGrade(String uri, String charset) {
		String curString;
		int curWordWei = 1;
		float curTagWei = 0;
		float totalGra = 0;
		Pattern p = null;
		Matcher m = null;
		try {
			// title tag <title>
			curTagWei = 5;
			NodeFilter titleFilter = new NodeClassFilter(TitleTag.class);
			Parser titleParser = new Parser(uri);
			titleParser.setEncoding("utf-8");
			NodeList titleList = titleParser.parse(titleFilter);
			for (int i = 0; i < titleList.size(); ++i) {
				TitleTag titleTag = (TitleTag) titleList.elementAt(i);
				curString = titleTag.getTitle();
				totalGra = totalGra + curTagWei
						* countsKeywordsInString(curString);
			}

			// meta tag <meta>
			curTagWei = 4;
			Parser metaParser = new Parser(uri);
			metaParser.setEncoding("utf-8");
			NodeFilter metaFilter = new NodeClassFilter(MetaTag.class);
			NodeList metaList = metaParser.parse(metaFilter);
			p = Pattern.compile("\\b(description|keywords)\\b",
					Pattern.CASE_INSENSITIVE);
			for (int i = 0; i < metaList.size(); ++i) {
				MetaTag metaTag = (MetaTag) metaList.elementAt(i);
				curString = metaTag.getMetaTagName();
				if (curString == null)
					continue;
				m = p.matcher(curString);
				if (m.find()) {
					curString = metaTag.getMetaContent();
					totalGra = totalGra + curTagWei
							* countsKeywordsInString(curString);
				} else {
					curString = metaTag.getMetaContent();
					totalGra = totalGra + countsKeywordsInString(curString) * 2;
				}
			}

			// heading tag <h*>
			curTagWei = 3;
			Parser headingParser = new Parser(uri);
			headingParser.setEncoding("utf-8");
			NodeFilter headingFilter = new NodeClassFilter(HeadingTag.class);
			NodeList headingList = headingParser.parse(headingFilter);
			for (int i = 0; i < headingList.size(); ++i) {
				HeadingTag headingTag = (HeadingTag) headingList.elementAt(i);
				curString = headingTag.toPlainTextString();// 得到<h*>标签中的纯文本
				if (curString == null)
					continue;
				totalGra = totalGra + curTagWei
						* countsKeywordsInString(curString);
			}

			// paragraph tag <p>
			curTagWei = (float) 2.5;
			Parser paraParser = new Parser(uri);
			paraParser.setEncoding("utf-8");
			NodeFilter paraFilter = new NodeClassFilter(ParagraphTag.class);
			NodeList paraList = paraParser.parse(paraFilter);
			for (int i = 0; i < paraList.size(); ++i) {
				ParagraphTag paraTag = (ParagraphTag) paraList.elementAt(i);
				curString = paraTag.toPlainTextString();
				if (curString == null)
					continue;
				totalGra = totalGra + countsKeywordsInString(curString);
			}

			// link tag <a>
			curTagWei = (float) 0.25;
			Parser linkParser = new Parser(uri);
			linkParser.setEncoding("utf-8");
			NodeFilter linkFilter = new NodeClassFilter(LinkTag.class);
			NodeList linkList = linkParser.parse(linkFilter);
			for (int i = 0; i < linkList.size(); ++i) {
				LinkTag linkTag = (LinkTag) linkList.elementAt(i);
				curString = linkTag.toPlainTextString();
				if (curString == null)
					continue;
				totalGra = totalGra + curTagWei
						* countsKeywordsInString(curString);
			}
		} catch (ParserException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return totalGra;
	}

	// 计算当前字符串中出现指定关键字的次数
	public int countsKeywordsInString(String s) {
		int counts = 0;
		for (int i = 0; i < keyWords.length; ++i) {
			while (s.indexOf(keyWords[i]) != -1) {
				++counts;
				s = s.replaceFirst(keyWords[i], "");
			}
		}
		return counts;
	}
}
