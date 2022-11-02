package com.example.herokudemo;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.Collator;
import java.util.Locale;
import java.util.StringTokenizer;
import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.Span;

import com.example.herokudemo.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.example.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.herokudemo.NameFinder;



import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;



@RestController
public class IndexController {

	public static final String VERSION = "1.0";
	
	TokenReqModel token = new TokenReqModel();
	TokenResponseModel TokenResponse = new TokenResponseModel();

    @GetMapping("/")
    public String index() {
        return "Hello there! I'm running.";
    }

	@RequestMapping(value = "/tokenizer/"+VERSION, consumes = MediaType.APPLICATION_JSON_VALUE, produces = { "application/json" },method = RequestMethod.POST)
	public @ResponseBody String[] celeb(@RequestBody TokenReqModel details) throws IOException {
		StringTokenizer st = new StringTokenizer(details.getSent());
		String[] processed_tok = new String[st.countTokens()];

		int i = 0;
		while (st.hasMoreTokens()) {
			String token = st.nextToken();
			 
			processed_tok[i]=(String) token;
		i++;
		}
		TokenResponse.setToken(processed_tok);
	
		return TokenResponse.getToken();	
	
	}
	
	@RequestMapping(value = "/breaksentence/"+VERSION, consumes = MediaType.APPLICATION_JSON_VALUE, produces = { "application/json" },method = RequestMethod.POST)
	public @ResponseBody String[] breakSentence(@RequestBody TokenReqModel details) throws IOException {

		String paragraph = details.getSent();
		
		InputStream is = getClass().getResourceAsStream("/en-sent.bin");
		SentenceModel model = new SentenceModel(is);

		SentenceDetectorME sdetector = new SentenceDetectorME(model);

		String sentences[] = sdetector.sentDetect(paragraph);
		
		return sentences;
}
	
	@RequestMapping(value = "/split/"+VERSION, consumes = MediaType.APPLICATION_JSON_VALUE, produces = { "application/json" },method = RequestMethod.POST)
	public @ResponseBody String[] splitAsPerTheToken(@RequestBody TokenReqModel details) throws IOException {

		String sentence = details.getSent();

		String simple = "[.?!]";
		String[] splitString = (sentence.split(simple));
		
		return splitString;

	}
	
	@RequestMapping(value = "/findlocation/"+VERSION, consumes = MediaType.APPLICATION_JSON_VALUE, produces = { "application/json" },method = RequestMethod.POST)
	public @ResponseBody String[] locationFinder(@RequestBody TokenReqModel details) throws IOException {

		InputStream inputStreamTokenizer = getClass().getResourceAsStream("/en-token.bin");
		TokenizerModel tokenModel = new TokenizerModel(inputStreamTokenizer);
		String paragraph = details.getSent();

		// Instantiating the TokenizerME class
		TokenizerME tokenizer = new TokenizerME(tokenModel);
		String tokens[] = tokenizer.tokenize(paragraph);

		// Loading the NER-location moodel
		InputStream inputStreamNameFinder = getClass().getResourceAsStream("/en-ner-location.bin");
		TokenNameFinderModel model = new TokenNameFinderModel(inputStreamNameFinder);

		// Instantiating the NameFinderME class
		NameFinderME nameFinder = new NameFinderME(model);

		// Finding the names of a location
		Span nameSpans[] = nameFinder.find(tokens);
		// Printing the spans of the locations in the sentence

		String [] locationvalue = new String[nameSpans.length];
		int i=0;
		for (Span s : nameSpans) {
		 locationvalue[i]=tokens[s.getStart()];
		}

		return locationvalue;

	}

	@RequestMapping(value = "/namefinder/"+VERSION, consumes = MediaType.APPLICATION_JSON_VALUE, produces = { "application/json" },method = RequestMethod.POST)
	public @ResponseBody NameFinder nameFinder(@RequestBody TokenReqModel details) throws IOException {
		String sentence = details.getSent();
	
		InputStream modelInToken = null;
		InputStream modelIn = null;

		// 1. convert sentence into tokens
		modelInToken = getClass().getResourceAsStream("/en-token.bin");
		TokenizerModel modelToken = new TokenizerModel(modelInToken);
		Tokenizer tokenizer = new TokenizerME(modelToken);
		String tokens[] = tokenizer.tokenize(sentence);

		// 2. find names
		modelIn = getClass().getResourceAsStream("/en-ner-person.bin");
		TokenNameFinderModel model = new TokenNameFinderModel(modelIn);
		NameFinderME nameFinder = new NameFinderME(model);

		Span nameSpans[] = nameFinder.find(tokens);

		// find probabilities for names
		double[] spanProbs = nameFinder.probs(nameSpans);

		NameFinder nameFinder2 =  new NameFinder();
		String[] namelist= new String[nameSpans.length];
		int i =0;
		double [] probability = new double[spanProbs.length];
		for(Span s : nameSpans)
		{
			namelist[i]= tokens[nameSpans[i].getStart()] ;
			probability[i]= spanProbs[i];
			i++;
		}
		nameFinder2.setName(namelist);
		nameFinder2.setPorbability(probability);
		return nameFinder2;

	}
	
	@RequestMapping(value = "/checkString/"+VERSION, consumes = MediaType.APPLICATION_JSON_VALUE, produces = { "application/json" },method = RequestMethod.POST)
	public @ResponseBody int checkString(@RequestBody TwoString details) throws IOException {

		Collator myCollator = Collator.getInstance(new Locale("en", "US"));

		// -1 "abc" is less than "def"
		// 0 the two strings are equal
		// 1 "xyz" is greater than "abc"
		int result = myCollator.compare(details.getString1(), details.getString2());
		return result;

	}

	@RequestMapping(value = "/intent/"+VERSION, consumes = MediaType.APPLICATION_JSON_VALUE, produces = { "application/json" },method = RequestMethod.POST)
	public @ResponseBody ResponsePoJo intentRequest(@RequestBody TokenReqModel details) throws IOException {

	String keyword = "BATHE YOGITA SUDHIR";
	String query = "Who is " + keyword +" ?";
	byte[] jsonData = Files.readAllBytes(Paths.get(details.getSent()+"."+"txt"));
			
			//create ObjectMapper instance
			ObjectMapper objectMapper = new ObjectMapper();
			
			//convert json string to object
			ResponsePoJo intent = objectMapper.readValue(jsonData, ResponsePoJo.class);
			
			
			//convert Object to json string
			ResponsePoJo intent1 = createIntent(intent);
			//configure Object mapper for pretty print
			System.out.println("*************************************");
			objectMapper.configure(SerializationFeature.INDENT_OUTPUT, true);
			
			//writing to console, can write to any output stream such as file
			StringWriter stringEmp = new StringWriter();
			objectMapper.writeValue(stringEmp, intent1);
			System.out.println("YOur result is"+stringEmp);
		/*	
			//read JSON like DOM Parser
			JsonNode rootNode = objectMapper.readTree(jsonData);
			JsonNode idNode = rootNode.path("id");
			System.out.println("id = "+idNode.asInt());*/
			return intent1;
			

		}
		
		public static  ResponsePoJo createIntent(ResponsePoJo intent) {
			ResponsePoJo rep = new ResponsePoJo();
			rep.setId(intent.getId());
			rep.setName(intent.getName());
			rep.setResponse(intent.getResponse());
			return rep;
		}
			

	



	
}