package com.airbus.lucene;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.InvalidTokenOffsetsException;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.filters.NodeClassFilter;
import org.htmlparser.lexer.Lexer;
import org.htmlparser.lexer.Page;
import org.htmlparser.tags.HeadingTag;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.tags.MetaTag;
import org.htmlparser.tags.ParagraphTag;
import org.htmlparser.tags.TitleTag;
import org.htmlparser.util.DefaultParserFeedback;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;
import org.wltea.analyzer.lucene.IKAnalyzer;

public class IndexTools {
	private static final String PATH = "keywords.txt";

	private static IndexWriter indexWriter = null;

	private float curScore = 0.0f;

	private static List<String> keywords = new ArrayList<String>();

	private BooleanQuery query = null;

	// private String[] keyWords = new String[] { "Composites Technologies",
	// "Metallic Technologies", "Surface Engineering",
	// "Structure Engineering", "Aeromechanics", "Sensors Electronics",
	// "Systems Integration", "Physics", "IT", "Security Services",
	// "Simulation", "Energy", "Propulsion", "Biofuel", "复合材料", "表面工程",
	// "重金属技术", "结构工程", "航空", "电子传感器", "生物燃料", "安全服务", "新能源", "仿真", "推进力" };

	public IndexTools() {
		initiate();
	}

	/**
	 * 获得indexwriter对象
	 * 
	 * @param dir
	 * @return
	 * @throws IOException
	 * @throws Exception
	 */
	private IndexWriter getIndexWriter(Directory dir, Analyzer analyzer)
			throws IOException {
		IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_47,
				analyzer);
		IndexWriter iw = new IndexWriter(dir, iwc);
		iw.deleteAll();
		return iw;
	}

	/**
	 * 关闭indexwriter对象
	 * 
	 * @throws IOException
	 * 
	 * @throws Exception
	 */
	private void closeWriter() throws IOException {
		if (indexWriter != null) {
			indexWriter.close();
			indexWriter = null;
		}
	}

	/**
	 * 创建索引
	 * 
	 * @throws InvalidTokenOffsetsException
	 */
	public void createIndex() throws InvalidTokenOffsetsException {
		String indexPath = "luceneindex"; // 建立索引文件的目录
		// 默认IKAnalyzer()-false:实现最细粒度切分算法,true:分词器采用智能切分
		Analyzer analyzer = new IKAnalyzer(true);
		Directory directory = null;
		try {
			directory = FSDirectory.open(new File(indexPath));
			indexWriter = getIndexWriter(directory, analyzer);
		} catch (Exception e) {
			System.out.println("索引打开异常！");
		}
		// 添加索引
		try {
			Document document = new Document();
			document.add(new TextField("filename", "标题:起点", Store.YES));
			document.add(new TextField("content",
					"内容：北京航空航天大学是一所工科大学，我是一名程序员。", Store.YES));
			indexWriter.addDocument(document);
			Document document1 = new Document();
			document1.add(new TextField("filename", "标题:终点", Store.YES));
			document1.add(new TextField("content",
					"内容：清华大学也有航空航天系，也是一所工科大学，我再也不是程序员。", Store.YES));
			indexWriter.addDocument(document1);
			indexWriter.commit();
		} catch (IOException e1) {
			System.out.println("索引创建异常！");
		}
		try {
			closeWriter();
		} catch (Exception e) {
			System.out.println("索引关闭异常！");
		}
	}

	/**
	 * 搜索
	 * 
	 * @throws ParseException
	 * @throws IOException
	 * @throws InvalidTokenOffsetsException
	 */
	public void searchIndex() throws ParseException, IOException,
			InvalidTokenOffsetsException {
		String indexPath = "luceneindex"; // 建立索引文件的目录
		// 默认IKAnalyzer()-false:实现最细粒度切分算法,true:分词器采用智能切分
		Directory directory = null;
		try {
			directory = FSDirectory.open(new File(indexPath));
		} catch (Exception e) {
			e.printStackTrace();
		}
		DirectoryReader ireader = null;
		IndexSearcher isearcher = null;
		try {
			ireader = DirectoryReader.open(directory);
		} catch (IOException e) {
			e.printStackTrace();
		}
		isearcher = new IndexSearcher(ireader);
		// 使用QueryParser查询分析器构造Query对象
		// eg:单个字段查询
		// String fieldName = "content";
		// QueryParser qp = new QueryParser(Version.LUCENE_40, fieldName,
		// analyzer);

		float totalScore = 0.0f;

		BooleanQuery.setMaxClauseCount(Integer.MAX_VALUE);

		// 搜索相似度最高的10条记录
		TopDocs topDocs = isearcher.search(query, 10);
		System.out.println("命中：" + topDocs.totalHits);
		// 输出结果
		ScoreDoc[] scoreDocs = topDocs.scoreDocs;
		boolean hasMustKeywords = false;
		for (int i = 0; i < topDocs.scoreDocs.length; i++) {
			Document targetDoc = isearcher.doc(scoreDocs[i].doc);
			System.out.println("内容：" + targetDoc.toString());
			String[] scoreExplain = null;
			// scoreExplain可以显示文档的得分详情，这里用split截取总分
			scoreExplain = isearcher.explain(query, scoreDocs[i].doc)
					.toString().split(" ", 2);
			String scores = scoreExplain[0];
			totalScore += Float.parseFloat(scores);
			// assertEquals("This is the text to be indexed.",
			// hitDoc.get("fieldname"));
			if (checkMustKeywords(targetDoc.toString()))
				hasMustKeywords = true;
			System.out.println(targetDoc.get("filename") + "\n*score* "
					+ scores);
		}
		if (hasMustKeywords)
			curScore = totalScore;
		else
			curScore = 0;
		System.out.println("===============");
	}

	/**
	 * 分页，高亮显示
	 * 
	 * @param analyzer
	 * @param isearcher
	 * @param query
	 * @param topDocs
	 * @throws IOException
	 * @throws InvalidTokenOffsetsException
	 */
	public void higherIndex(Analyzer analyzer, IndexSearcher isearcher,
			Query query, TopDocs topDocs) throws IOException,
			InvalidTokenOffsetsException {
		TopScoreDocCollector results = TopScoreDocCollector.create(
				topDocs.totalHits, false);
		isearcher.search(query, results);
		// 分页取出指定的doc(开始条数, 取几条)
		ScoreDoc[] docs = results.topDocs(1, 2).scoreDocs;
		for (int i = 0; i < docs.length; i++) {
			Document targetDoc = isearcher.doc(docs[i].doc);
			System.out.println("内容：" + targetDoc.toString());
		}
		// 关键字高亮显示的html标签，需要导入lucene-highlighter-3.5.0.jar
		SimpleHTMLFormatter simpleHTMLFormatter = new SimpleHTMLFormatter(
				"<font color='red'>", "</font>");
		Highlighter highlighter = new Highlighter(simpleHTMLFormatter,
				new QueryScorer(query));
		for (int i = 0; i < docs.length; i++) {
			Document doc = isearcher.doc(docs[i].doc);
			// 标题增加高亮显示
			TokenStream tokenStream1 = analyzer.tokenStream("filename",
					new StringReader(doc.get("filename")));
			String title = highlighter.getBestFragment(tokenStream1,
					doc.get("filename"));
			// 内容增加高亮显示
			TokenStream tokenStream2 = analyzer.tokenStream("content",
					new StringReader(doc.get("content")));
			String content = highlighter.getBestFragment(tokenStream2,
					doc.get("content"));
			System.out.println(doc.get("filename") + " : " + title + " : "
					+ content);
		}
	}

	public void createIndexByURI(String uri) {
		String indexPath = "luceneindex"; // 建立索引文件的目录
		// 默认IKAnalyzer()-false:实现最细粒度切分算法,true:分词器采用智能切分
		Analyzer analyzer = new IKAnalyzer(true);
		Directory directory = null;
		Document document = null;
		try {
			directory = FSDirectory.open(new File(indexPath));
			if (indexWriter == null)
				indexWriter = getIndexWriter(directory, analyzer);

			String curString;
			Pattern p = null;
			Matcher m = null;

			// title tag <title>
			NodeFilter titleFilter = new NodeClassFilter(TitleTag.class);
			Parser titleParser = new Parser(uri);
			titleParser.setEncoding(titleParser.getEncoding());
			NodeList titleList = titleParser.parse(titleFilter);
			for (int i = 0; i < titleList.size(); ++i) {
				TitleTag titleTag = (TitleTag) titleList.elementAt(i);
				curString = titleTag.getTitle();
				document = new Document();
				document.add(new TextField("content", curString, Store.YES));
				indexWriter.addDocument(document);
			}

			// meta tag <meta>
			Parser metaParser = new Parser(uri);
			metaParser.setEncoding(metaParser.getEncoding());
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
					document = new Document();
					document.add(new TextField("content", curString, Store.YES));
					indexWriter.addDocument(document);
				} else {
					curString = metaTag.getMetaContent();
					document = new Document();
					document.add(new TextField("content", curString, Store.YES));
					indexWriter.addDocument(document);
				}
			}

			// heading tag <h*>
			Parser headingParser = new Parser(uri);
			headingParser.setEncoding(headingParser.getEncoding());
			NodeFilter headingFilter = new NodeClassFilter(HeadingTag.class);
			NodeList headingList = headingParser.parse(headingFilter);
			for (int i = 0; i < headingList.size(); ++i) {
				HeadingTag headingTag = (HeadingTag) headingList.elementAt(i);
				curString = headingTag.toPlainTextString();// 得到<h*>标签中的纯文本
				if (curString == null)
					continue;
				document = new Document();
				document.add(new TextField("content", curString, Store.YES));
				indexWriter.addDocument(document);
			}

			// paragraph tag <p>
			Parser paraParser = new Parser(uri);
			paraParser.setEncoding(paraParser.getEncoding());
			NodeFilter paraFilter = new NodeClassFilter(ParagraphTag.class);
			NodeList paraList = paraParser.parse(paraFilter);
			for (int i = 0; i < paraList.size(); ++i) {
				ParagraphTag paraTag = (ParagraphTag) paraList.elementAt(i);
				curString = paraTag.toPlainTextString();
				if (curString == null)
					continue;
				document = new Document();
				document.add(new TextField("content", curString, Store.YES));
				indexWriter.addDocument(document);
			}

			// link tag <a>
			Parser linkParser = new Parser(uri);
			linkParser.setEncoding(linkParser.getEncoding());
			NodeFilter linkFilter = new NodeClassFilter(LinkTag.class);
			NodeList linkList = linkParser.parse(linkFilter);
			for (int i = 0; i < linkList.size(); ++i) {
				LinkTag linkTag = (LinkTag) linkList.elementAt(i);
				curString = linkTag.toPlainTextString();
				if (curString == null)
					continue;
				document = new Document();
				document.add(new TextField("content", curString, Store.YES));
				indexWriter.addDocument(document);
			}
			// 添加索引
			indexWriter.addDocument(document);
			indexWriter.commit();
			closeWriter();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParserException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (indexWriter != null)
				try {
					closeWriter();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
	}

	public static Parser createParser(String inputHTML) {
		Lexer mLexer = new Lexer(new Page(inputHTML));
		return new Parser(mLexer, new DefaultParserFeedback(
				DefaultParserFeedback.QUIET));
	}

	public float getCurrentScore() {
		return this.curScore;
	}

	private void initiate() {
		File f = new File(PATH);
		try {
			FileReader fr = new FileReader(f);
			BufferedReader br = new BufferedReader(fr);
			String s = null;
			while ((s = br.readLine()) != null) {
				keywords.add(s);
			}

			Analyzer analyzer = new IKAnalyzer(true);

			String[] fields = { "content" };

			QueryParser qp = new MultiFieldQueryParser(Version.LUCENE_47,
					fields, analyzer);
			qp.setDefaultOperator(QueryParser.OR_OPERATOR);

			query = new BooleanQuery();
			for (String keyword : keywords) {
				query.add(new TermQuery(new Term("content", keyword)),
						Occur.SHOULD);
			}

			// query = MultiFieldQueryParser.parse(Version.LUCENE_47, array,
			// fields, analyzer);
			// for (int i = 0; i < keywords.size(); ++i) {
			// Query query = qp.parse(keywords.get(i));
			// booleanQuery.add(query, Occur.SHOULD);
			// }

			br.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private boolean checkMustKeywords(String s) {
		if (s.contains("航空航天") || s.contains("航空") || s.contains("航天"))
			return true;
		else
			return false;
	}
}