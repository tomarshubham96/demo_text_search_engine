package com.lucene.sampleApp.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lucene.sampleApp.Services.LuceneIndexerService;

@Controller
public class LuceneIndexController {
	
	@Autowired
	private LuceneIndexerService luceneIndexerService;
	
	@GetMapping("/")
	public String checkService() {
		try {
			luceneIndexerService.createIndex();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "index.html";
	}

}
