package lucene;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.Analyzer.TokenStreamComponents;
import org.apache.lucene.analysis.core.LetterTokenizer;
import org.apache.lucene.analysis.en.PorterStemFilter;

public class CustomAnalyzer extends Analyzer 
{
	String stopWordsPath = "cacm\\cacm data\\common_words";
	String mfield;

	public CustomAnalyzer()
	 {
		 //createComponents(stopWordsPath);
		
	 }
	
	//BufferedReader reader = null;
	@Override
	 protected TokenStreamComponents createComponents(String mField) 
	{
	 /*Tokenizer source = new LowerCaseTokenizer();
      return new TokenStreamComponents(source, new PorterStemFilter(source));*/
		 	
		CharArraySet stopWords = FillCharArraySet();
		Tokenizer source = new LetterTokenizer();              
		TokenStream filter = new LowerCaseFilter(source);                
		filter = new StopFilter(filter, stopWords);                  
		filter = new PorterStemFilter(filter);
		return new TokenStreamComponents(source, filter);
		    
	}
	
	 
	 private CharArraySet FillCharArraySet()
	 {
	 	CharArraySet stopwords = new CharArraySet(429,false);
	 	String sCurrentLine;
	 		
	 	FileReader fr = null;
		BufferedReader br = null;
			
		try
		{
			fr = new FileReader(stopWordsPath);
			//System.out.println(stopWordsPath);
			br = new BufferedReader(fr);
				
			while((sCurrentLine = br.readLine())!= null)
			{
				stopwords.add(sCurrentLine);
			}
		}catch (IOException e) {

			e.printStackTrace();

		} finally {

			try {

				if (br != null)
					br.close();

				if (fr != null)
					fr.close();

			} catch (IOException ex) {

				ex.printStackTrace();

			}
			}	 		
	 	return stopwords;
	 }
}
