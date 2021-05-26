package com.lucene.sampleApp.Services;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.LongPoint;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternUtils;
import org.springframework.stereotype.Service;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

@Service
public class LuceneIndexerService {
	
	@Value("${lucene.index.location}")
	private String INDEX_PATH;
	
	@Value("${lucene.docsToIndex.location}")
	private String DOCS_PATH;
	
	@Autowired
	private ResourceLoader resourceLoader;

	public void createIndex() throws Exception{
		
		Resource[] resources;      
        
        try {
            resources = ResourcePatternUtils.getResourcePatternResolver(resourceLoader).getResources("classpath:" + DOCS_PATH + "/*.*");              
            
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        
        Date start = new Date();
        try {
          System.out.println("Indexing to directory '" + INDEX_PATH + "'...");

          Directory dir = FSDirectory.open(Paths.get(INDEX_PATH));
          CharArraySet stopWordsOverride = new CharArraySet(Collections.emptySet(), true);
          Analyzer analyzer = new StandardAnalyzer(stopWordsOverride);
          IndexWriterConfig iwc = new IndexWriterConfig(analyzer);

          
            // Add new documents to an existing index:
            iwc.setOpenMode(OpenMode.CREATE_OR_APPEND);

          // Optional: for better indexing performance, if you
          // are indexing many documents, increase the RAM
          // buffer.  But if you do this, increase the max heap
          // size to the JVM (eg add -Xmx512m or -Xmx1g):
          //
           iwc.setRAMBufferSizeMB(256.0);

          IndexWriter writer = new IndexWriter(dir, iwc);
          indexDocs(writer, resources);
          

          // NOTE: if you want to maximize search performance,
          // you can optionally call forceMerge here.  This can be
          // a terribly costly operation, so generally it's only
          // worth it when your index is relatively static (ie
          // you're done adding documents to it):
          //
          // writer.forceMerge(1);

          writer.close();

          Date end = new Date();
          System.out.println(end.getTime() - start.getTime() + " total milliseconds");

        } catch (IOException e) {
          System.out.println(" caught a " + e.getClass() +
           "\n with message: " + e.getMessage());
        }
		
	}
	
	static void indexDocs(final IndexWriter writer, Resource[] resources) throws IOException {
		for (int i = 0; i < resources.length; i++) {
            
			indexDoc(writer, resources[i]);
            
        } 
      }

      /** Indexes a single document */
      static void indexDoc(IndexWriter writer, Resource resource) throws IOException {
    	  Date start = new Date();
        try (InputStream stream = resource.getInputStream()) {
          // make a new, empty document
          Document doc = new Document();
          doc.clear();
          Metadata metadata = new Metadata();
			ContentHandler handler = new BodyContentHandler();
			ParseContext context = new ParseContext();
			Parser parser = new AutoDetectParser();
			try {
				parser.parse(stream, handler, metadata, context);
			}
			catch (TikaException e) {
				e.printStackTrace();
			} catch (SAXException e) {
				e.printStackTrace();
			}
			finally {
				stream.close();
			}
			
			String text = handler.toString();
			
			doc.add(new StringField("path", resource.getFilename(), Field.Store.YES));
			
			doc.add(new TextField("contents", text, Store.YES));
          
          
            System.out.println("updating " + resource.getFilename());
            writer.updateDocument(new Term("path", resource.getFilename()), doc);
          
        }
        Date end = new Date();
        System.out.println(end.getTime() - start.getTime() + " total milliseconds");
//        writer.commit();
//		writer.deleteUnusedFiles();
      }
}
