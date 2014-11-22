package com.qunar;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.io.FileUtils;
import org.htmlparser.Node;
import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.filters.HasAttributeFilter;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;

public class UserImageSaver {
	private String url;

	private static String PATH = "./counts_of_image.txt";

	public UserImageSaver(String url) {
		this.url = url;
	}

	public void saveImage() {
		Parser parser;
		try {
			parser = new Parser(url);
			parser.setEncoding(parser.getEncoding());

			NodeFilter filter3 = new HasAttributeFilter("class",
					"tie-author-column");
			NodeList nodes = parser.extractAllNodesThatMatch(filter3);

			if (nodes != null) {
				for (int i = 0; i < nodes.size(); i++) {
					Node textnode = (Node) nodes.elementAt(i);

					String content = textnode.getChildren().elementAt(1)
							.getChildren().elementAt(1).getText();
					String[] array = content.split(" ");
					String src = array[3];
					String imageUrl = src.substring(5, src.length() - 1);

					FileReader fr = new FileReader(new File(PATH));
					BufferedReader br = new BufferedReader(fr);
					String line = br.readLine();
					br.close();
					FileWriter fw = new FileWriter(new File(PATH));

					int batch = 1;
					if (line == null) {
						if (downloadImage(imageUrl))
							++batch;
						fw.write(Integer.toString(batch));
						fw.flush();
					} else {
						batch = Integer.parseInt(line);
						if (batch < 1000) {
							if (downloadImage(imageUrl))
								++batch;
						} else {
							System.exit(0);
						}
						fw.write(Integer.toString(batch));
						fw.flush();
					}
					fw.close();
				}
			}
		} catch (ParserException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private boolean downloadImage(String url) {
		try {
			URL httpurl = new URL(url);
			String fileName = getFileNameFromUrl(url);
			File f = new File("UserImage/" + fileName);
			if (!f.exists())
				f.createNewFile();
			FileUtils.copyURLToFile(httpurl, f);
			return true;
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

	private String getFileNameFromUrl(String url) {
		int index = url.lastIndexOf("/");
		String name = url.substring(index + 1) + ".jpg";
		return name;
	}
}
