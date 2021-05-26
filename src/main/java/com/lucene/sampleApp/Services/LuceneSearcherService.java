package com.lucene.sampleApp.Services;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.annotation.PreDestroy;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.TotalHitCountCollector;
import org.apache.lucene.search.highlight.Formatter;
import org.apache.lucene.search.highlight.Fragmenter;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.apache.lucene.search.highlight.SimpleSpanFragmenter;
import org.apache.lucene.search.highlight.TokenSources;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.lucene.sampleApp.Entity.Result;

@Service
public class LuceneSearcherService {
	
	private Directory indexDirectory;
	private QueryParser queryParser;
	private final static Logger logger = Logger.getLogger("LuceneSearcherService");
	
	@Value("${lucene.index.location}")
	private String INDEX_PATH;
	
	
	public static List<String> stopWords = Arrays.asList("a", "a's", "able", "about", "above", "according", "accordingly", "across",
			"actually", "after", "afterwards", "again", "against", "ain't", "all", "allow", "allows", "almost", "alone",
			"along", "already", "also", "although", "always", "am", "among", "amongst", "an", "and", "another", "any",
			"anybody", "anyhow", "anyone", "anything", "anyway", "anyways", "anywhere", "apart", "appear", "appreciate",
			"appropriate", "are", "aren't", "around", "as", "aside", "ask", "asking", "associated", "at", "available",
			"away", "awfully", "b", "be", "became", "because", "become", "becomes", "becoming", "been", "before",
			"beforehand", "behind", "being", "believe", "below", "beside", "besides", "best", "better", "between",
			"beyond", "both", "brief", "but", "by", "c", "c'mon", "c's", "came", "can", "can't", "cannot", "cant",
			"cause", "causes", "certain", "certainly", "changes", "clearly", "co", "com", "come", "comes", "concerning",
			"consequently", "consider", "considering", "contain", "containing", "contains", "corresponding", "could",
			"couldn't", "course", "currently", "d", "definitely", "described", "despite", "did", "didn't", "different",
			"do", "does", "doesn't", "doing", "don't", "done", "down", "downwards", "during", "e", "each", "edu", "eg",
			"eight", "either", "else", "elsewhere", "enough", "entirely", "especially", "et", "etc", "even", "ever",
			"every", "everybody", "everyone", "everything", "everywhere", "ex", "exactly", "example", "except", "f",
			"far", "few", "fifth", "first", "five", "followed", "following", "follows", "for", "former", "formerly",
			"forth", "four", "from", "further", "furthermore", "g", "get", "gets", "getting", "given", "gives", "go",
			"goes", "going", "gone", "got", "gotten", "greetings", "h", "had", "hadn't", "happens", "hardly", "has",
			"hasn't", "have", "haven't", "having", "he", "he's", "hello", "help", "hence", "her", "here", "here's",
			"hereafter", "hereby", "herein", "hereupon", "hers", "herself", "hi", "him", "himself", "his", "hither",
			"hopefully", "how", "howbeit", "however", "i", "i'd", "i'll", "i'm", "i've", "ie", "if", "ignored",
			"immediate", "in", "inasmuch", "inc", "indeed", "indicate", "indicated", "indicates", "inner", "insofar",
			"instead", "into", "inward", "is", "isn't", "it", "it'd", "it'll", "it's", "its", "itself", "j", "just",
			"k", "keep", "keeps", "kept", "know", "known", "knows", "l", "last", "lately", "later", "latter",
			"latterly", "least", "less", "lest", "let", "let's", "like", "liked", "likely", "little", "look", "looking",
			"looks", "ltd", "m", "mainly", "many", "may", "maybe", "me", "mean", "meanwhile", "merely", "might", "more",
			"moreover", "most", "mostly", "much", "must", "my", "myself", "n", "name", "namely", "nd", "near", "nearly",
			"necessary", "need", "needs", "neither", "never", "nevertheless", "new", "next", "nine", "no", "nobody",
			"non", "none", "noone", "nor", "normally", "not", "nothing", "novel", "now", "nowhere", "o", "obviously",
			"of", "off", "often", "oh", "ok", "okay", "old", "on", "once", "one", "ones", "only", "onto", "or", "other",
			"others", "otherwise", "ought", "our", "ours", "ourselves", "out", "outside", "over", "overall", "own", "p",
			"particular", "particularly", "per", "perhaps", "placed", "please", "plus", "possible", "presumably",
			"probably", "provides", "q", "que", "quite", "qv", "r", "rather", "rd", "re", "really", "reasonably",
			"regarding", "regardless", "regards", "relatively", "respectively", "right", "s", "said", "same", "saw",
			"say", "saying", "says", "second", "secondly", "see", "seeing", "seem", "seemed", "seeming", "seems",
			"seen", "self", "selves", "sensible", "sent", "serious", "seriously", "seven", "several", "shall", "she",
			"should", "shouldn't", "since", "six", "so", "some", "somebody", "somehow", "someone", "something",
			"sometime", "sometimes", "somewhat", "somewhere", "soon", "sorry", "specified", "specify", "specifying",
			"still", "sub", "such", "sup", "sure", "t", "t's", "take", "taken", "tell", "tends", "th", "than", "thank",
			"thanks", "thanx", "that", "that's", "thats", "the", "their", "theirs", "them", "themselves", "then",
			"thence", "there", "there's", "thereafter", "thereby", "therefore", "therein", "theres", "thereupon",
			"these", "they", "they'd", "they'll", "they're", "they've", "think", "third", "this", "thorough",
			"thoroughly", "those", "though", "three", "through", "throughout", "thru", "thus", "to", "together", "too",
			"took", "toward", "towards", "tried", "tries", "truly", "try", "trying", "twice", "two", "u", "un", "under",
			"unfortunately", "unless", "unlikely", "until", "unto", "up", "upon", "us", "use", "used", "useful", "uses",
			"using", "usually", "uucp", "v", "value", "various", "very", "via", "viz", "vs", "w", "want", "wants",
			"was", "wasn't", "way", "we", "we'd", "we'll", "we're", "we've", "welcome", "well", "went", "were",
			"weren't", "what", "what's", "whatever", "when", "whence", "whenever", "where", "where's", "whereafter",
			"whereas", "whereby", "wherein", "whereupon", "wherever", "whether", "which", "while", "whither", "who",
			"who's", "whoever", "whole", "whom", "whose", "why", "will", "willing", "wish", "with", "within", "without",
			"won't", "wonder", "would", "wouldn't", "x", "y", "yes", "yet", "you", "you'd", "you'll", "you're",
			"you've", "your", "yours", "yourself", "yourselves", "z", "zero");
	
	public static CharArraySet stopSet = new CharArraySet(stopWords, false);

	public ArrayList<Result> searchIndex(String querystring, int numRecords, boolean showAvailable) {
		IndexReader reader = null;
		IndexSearcher indexSearcher = null;
		TopDocs documents;
		TotalHitCountCollector collector = null;
		
		ArrayList<Result> searchResultList = new ArrayList<Result>();
		
		try {
			Path index = Paths.get(INDEX_PATH);
			indexDirectory = FSDirectory.open(index);
			reader = DirectoryReader.open(indexDirectory);
			indexSearcher = new IndexSearcher(reader);
			
			
			Analyzer analyzer = new StandardAnalyzer(stopSet);
			queryParser = new QueryParser("contents", analyzer);
			
			Query query = null;
	        // Handling Conjunction, Disjunction and Negation
	        if(querystring.toLowerCase().contains("not")) {
	        	String[] split = querystring.toLowerCase().split("not");
	        	Query query1 = queryParser.parse(split[0]);
	        	Query query2 = queryParser.parse(split[1]);
	        	query = new BooleanQuery.Builder().add(query1, BooleanClause.Occur.MUST)
	    				.add(query2, BooleanClause.Occur.MUST_NOT).build();
	        }else {
	        	if(querystring.toLowerCase().contains("and")) {
	            	String[] split = querystring.toLowerCase().split("and");
	            	if(split.length>0){
	            		Query query1 = queryParser.parse(split[0]);
	                	Query query2 = queryParser.parse(split[1]);
	                	query = new BooleanQuery.Builder().add(query1, BooleanClause.Occur.MUST)
	            				.add(query2, BooleanClause.Occur.MUST).build();
	            	}
	            
	            }else {
	            	if(querystring.toLowerCase().contains("or")) {
	                	String[] split = querystring.toLowerCase().split("or");
	                	Query query1 = queryParser.parse(split[0]);
	                	Query query2 = queryParser.parse(split[1]);
	                	query = new BooleanQuery.Builder().add(query1, BooleanClause.Occur.SHOULD)
	            				.add(query2, BooleanClause.Occur.SHOULD).build();
	                }else {
	                	query = queryParser.parse(querystring);
	                }
	            }
	        }
			
			
			logger.info("'" + querystring + "' ==> '" + query.toString() + "'");
			if(showAvailable){
				collector = new TotalHitCountCollector();
				indexSearcher.search(query, collector);
			}
			documents = indexSearcher.search(query, numRecords);
			
			
	        Formatter formatter = new SimpleHTMLFormatter();
	         
	       
	        QueryScorer scorer = new QueryScorer(query);
	         
	        
	        Highlighter highlighter = new Highlighter(formatter, scorer);
	         
	        
	        Fragmenter fragmenter = new SimpleSpanFragmenter(scorer, 10);
	         
	        //breaks text up into same-size fragments with no concerns over spotting sentence boundaries.
	        //Fragmenter fragmenter = new SimpleFragmenter(10);
	         
	        
	        highlighter.setTextFragmenter(fragmenter);
	        
	        for (int i = 0; i < documents.scoreDocs.length; i++) 
	        {
	            int docid = documents.scoreDocs[i].doc;
	            Document doc = indexSearcher.doc(docid);
	            String title = doc.get("path");
	             
	            //Printing - to which document result belongs
	            System.out.println("Path " + " : " + title);
	             
	            //Get stored text from found document
	            String text = doc.get("contents");
	 
	            //Create token stream
	            TokenStream stream = TokenSources.getAnyTokenStream(reader, docid, "contents", analyzer);
	             
	            System.out.println("(" + documents.scoreDocs[i].score + ")");
	            //i added
	            Result giveResult = new Result();
	            //giveResult.setHits(documents.totalHits);
	            giveResult.setScore(documents.scoreDocs[i].score);
	            giveResult.setDocPath(doc.get("path"));
	            
	            //i added
	            
	            //Get highlighted text fragments
	            String[] frags = highlighter.getBestFragments(stream, text, 30);
	            giveResult.setHighlightedText(frags);
				
	            searchResultList.add(giveResult);
	        }
	        indexDirectory.close();
	        return searchResultList;
	 
		} catch (ParseException pe) {
			//throw new InvalidLuceneQueryException(pe.getMessage());
			System.out.println("ERROR QUERY");
		} catch (Exception e) {
			//throw new LuceneSearcherException(e.getMessage());
			System.out.println("ERROR QUERY");
		} finally {
			try {
				if (reader != null) {
					reader.close();
				}
			}
			catch (IOException ioe) {
				logger.warning("Could not close IndexReader: "+ioe.getMessage()); 
			}
		}
		return null;
	}
}
