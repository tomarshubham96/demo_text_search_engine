package com.lucene.sampleApp.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
	
	@GetMapping("/fileContents")
	public String getFileContents(Model model,@RequestParam(value="fileName") String fileName) {
		
		try {
			String content = luceneIndexerService.getFileContents(fileName);
			model.addAttribute("fileContents", content);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "fileContents.html";		
	}

}
