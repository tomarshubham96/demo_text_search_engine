package com.lucene.sampleApp.Controller;

import java.util.ArrayList;
import java.util.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.lucene.sampleApp.Entity.Result;
import com.lucene.sampleApp.Services.LuceneSearcherService;

@Controller
public class LuceneSearchController {

	@Autowired
	private LuceneSearcherService luceneSearchService;
	
	@Value("${lucene.query.default.records}")
	private Integer QUERY_DEFAULT_RECORDS;

	@Value("${lucene.query.max.records}")
	private Integer QUERY_MAX_RECORDS;
	
	private final static Logger logger = Logger.getLogger("LuceneSearchController");
	
	@GetMapping("/search")
	public String searchIndex(Model model,@RequestParam(value="query") String query) {
		
		if (!query.trim().isEmpty()) {
			int count = QUERY_DEFAULT_RECORDS;
			boolean showAvailable = false;
			
    		ArrayList<Result> results = luceneSearchService.searchIndex(query, count, showAvailable);
    		
    		logger.info("Search for '" + query +"' found " + results.size() +
    				" and retrieved " + results.size() + " records");
    		
    		model.addAttribute("searchText", query);
    		model.addAttribute("count", results.size());
    		model.addAttribute("results", results);
    		
    		return "index.html";
    		
    	} else {
    		//throw new InvalidLuceneQueryException(query);
    		System.out.println("ERROR QUERY");
    	}
		return "error.html";
		
	}
}
