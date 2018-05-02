package lucene;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.analysis.en.PorterStemFilter;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.LongPoint;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.analysis.core.LowerCaseTokenizer;
import org.apache.lucene.analysis.core.WhitespaceTokenizer;


	
public class IndexingFiles 
{
	private IndexingFiles() {}
	
	
	/** Index all text files under a category. 
	 * @throws FileNotFoundException */
	@SuppressWarnings("unused")
	public static void main(String[] args) throws FileNotFoundException
	{
		String usage = "java org.apache.lucene.demo.IndexFiles"
				+"[-index INDEX_PATH] [-docs DOCS_PATH] [-update]\n\n"
				+"This indexes the documents in DOCS_PATH, creating a Lucene index"
				+"in INDEX_PATH that can be searched with SearchFiles";
		
		
		String indexPath = "index";
		String docsPath = "cacm\\cacm data\\files";
		String stopwords = "cacm\\cacm data\\common_words";
		 
		
		boolean create = true;
		
		if(docsPath == null)
		{
			System.err.println("Usage:" + usage);
			System.exit(1);
		}
		
		final Path docDir = Paths.get(docsPath);
		if(!Files.isReadable(docDir))
		{
			System.out.println("Document directory '" + docDir.toAbsolutePath() + "'does not exist or is not readabble, please check the path");
			System.exit(1);
		}
		
		
		
		
		Date start = new Date();
		
		try {
			System.out.println("Indexing to directory '" + indexPath + "'...");
			readCacm();
			Directory dir = FSDirectory.open(Paths.get(indexPath));
			CustomAnalyzer analyzer =  new CustomAnalyzer();
			// List<String> results = tokenizeString(analyzer,"Everything i hate lololo ti kaalaaaaaa dwdawd dwaad wads");
			//System.out.println(results);
			IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
			
			
			if(create)
			{
				//Create a new index in the directory , removing any previously indexed documents
				iwc.setOpenMode(OpenMode.CREATE);
			}
			else
			{
				//Add new documents to an existing index;
				iwc.setOpenMode(OpenMode.CREATE_OR_APPEND);
			}
			
			/* Optional. For better indexing perfomance
			 * if you are indexing many documents increase the rRRAM buffer. But if you
			 * do this  increase the max heap size to the JVM(eg add -Xmx512m or -Xmx1g):
			 * iwc.setRAMBufferSizeMB(256.0)
			 */
			
			IndexWriter writer = new IndexWriter(dir,iwc);
			indexDocs(writer,docDir);
			
			
			writer.close();
			
			
			
			Date end = new Date();
			System.out.println(end.getTime() - start.getTime() + " total milliseconds");
			
		}catch(IOException e)
		{
			System.out.println("caught a " + e.getClass() + "\n with message: " + e.getMessage());
		}
				
			
	}

	public static List<String> tokenizeString(CustomAnalyzer analyzer, String string) {
	    List<String> result = new ArrayList<String>();
	    try {
	      TokenStream stream  = analyzer.tokenStream(null, new StringReader(string));
	      stream.reset();
	      while (stream.incrementToken()) {
	        result.add(stream.getAttribute(CharTermAttribute.class).toString());
	      }
	    } catch (IOException e) {
	      // not thrown b/c we're using a string reader...
	      throw new RuntimeException(e);
	    }
	    return result;
	  }
	
	static void indexDocs(final IndexWriter writer, Path path) throws IOException {
		if(Files.isDirectory(path))
		{
			
			Files.walkFileTree(path,new SimpleFileVisitor<Path>()
			{
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
					try {
						indexDoc (writer, file, attrs.lastModifiedTime().toMillis());
						
					} catch (IOException ignore){
						
						//don't index files that can't be read
					}
					return FileVisitResult.CONTINUE;
				}
				
				
			});
			
		}
		else
		{
			indexDoc(writer, path, Files.getLastModifiedTime(path).toMillis());
		}
	}
	
	/**Indexes a single document */
	
	static void indexDoc(IndexWriter writer, Path file, long lastModified) throws IOException 
	{
		
		FileReader fr = null;
		BufferedReader br = null;
		
		
		
		String sCurrentLine = "";
		String id = "";
		String title = "";
		String authors = "";
		String content = "";
		boolean inT = false ;
		boolean inW = false;
		boolean inA = false;
		
		
		
		try {
			//make a new , empty document
			fr = new FileReader(file.toString());
			br = new BufferedReader(fr);
			
			while((sCurrentLine = br.readLine()) != null)
			{
				if(sCurrentLine.charAt(0) == '.' && sCurrentLine.charAt(1) == 'I')
				{
					id = sCurrentLine;					
				}
				else if(sCurrentLine.charAt(0) == '.' && sCurrentLine.charAt(1) == 'T')
				{
					inT = true;
					inA = false;
					inW = false;
					
				}
				else if(sCurrentLine.charAt(0) == '.' && sCurrentLine.charAt(1) == 'W')
				{
					inT = false;
					inA = false;
					inW = true;
				}
				else if(sCurrentLine.charAt(0) == '.' && sCurrentLine.charAt(1) == 'B')
				{
					inT = false;
					inW = false;
					inA = false;
					
				}
				else if(sCurrentLine.charAt(0) == '.' && sCurrentLine.charAt(1) == 'A')
				{
					inA = true;
					inW = false;
					inT = false;
					
				}
				else if(sCurrentLine.charAt(0) == '.' && sCurrentLine.charAt(1) == 'N')
				{
					inT = false;
					inA = false;
					inW = false;
				}
				else if(sCurrentLine.charAt(0) == '.' && sCurrentLine.charAt(1) == 'X')
				{
					inT = false;	
					inA = false;
					inW = false;
				}
				
				//Get T
				if(inT && sCurrentLine.charAt(0) != '.')
				{
					title += sCurrentLine;
				}
				
				//Get W
				if(inW  && sCurrentLine.charAt(0) != '.')
				{
					content += sCurrentLine;
				}
				//Get A
				if(inA  && sCurrentLine.charAt(0) != '.')
				{
					authors += sCurrentLine;
				}
				
		
				
				
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
		
		
			System.out.println(id);
			System.out.println(title);
			System.out.println(authors);
			System.out.println(content);
			
			Document doc = new Document();
			
			
			System.out.println(file.toString());
			
			Field pathField = new StringField("path", file.toString(), Field.Store.YES);
			doc.add(pathField);
			
			doc.add(new LongPoint("modified", lastModified));
			
			doc.add(new TextField("articleid", id, Field.Store.YES));
			
			doc.add(new TextField("title", title, Field.Store.YES));
			
			doc.add(new TextField("authors", authors, Field.Store.YES ));
			
			doc.add(new TextField("content", content , Field.Store.YES ));
			
			if(writer.getConfig().getOpenMode() == OpenMode.CREATE)
			{
			
				//New index, we just add the document(no old document can be there)
				System.out.println("adding " + file);
				writer.addDocument(doc);
				System.out.println("doc " + doc);
				
			}
			
			else
			{
				/*Existing index(an old copy of this document may have been indexed)
				 *we use updateDocument instead to replace the old one matching the exact 
				 *path ,if present  
				 */
				//System.out.println("updating" + file);
				//writer.updateDocument(new Term("path", file.toString()), doc);
			}
		}
	
	
	
	static void readCacm()
	{
		final String FILENAME = "cacm\\cacm data\\cacm.all";
		
		
		FileReader fr = null;
		BufferedReader br = null;
		FileWriter fw = null;
		BufferedWriter bw = null;
		
		
		
		String sCurrentLine = "";
		String id = "";
		String title = "";
		String author = "";
		String content = "";
		String nextLine = "";
		String article = null;
		int counter2 = 0;
		boolean firsttime = true;
		
		int counter = 1;
		
		
		File file = null;
		//FileWriter fw;
		
		
		try {

			fr = new FileReader(FILENAME);
			br = new BufferedReader(fr);
			
			

			//br = new BufferedReader(new FileReader(FILENAME));

			while ((sCurrentLine = br.readLine()) != null) 
			{
				
				
				if(sCurrentLine.charAt(0) == '.' && sCurrentLine.charAt(1) == 'I')
				{
					
					if(counter >= 2)
						bw.close();
					
					file = new File("cacm\\cacm data\\files\\" + "File_" +counter+ ".txt");
					file.createNewFile();
					fw = new FileWriter(file.getAbsoluteFile(),true);
					bw = new BufferedWriter(fw);
					
					
					counter++;
					
				}
				
				bw.write(sCurrentLine);
				bw.newLine();
				
				
				
				
				
				//if(sCurrentLine.charAt(0) == '.' && sCurrentLine.charAt(1) == 'I' && !firsttime)
				//{			
						//System.out.println(article + "\n");
						
						//file = new File("C:\\Users\\peira\\Downloads\\Cacm\\files\\" + "File_" +counter+ ".txt");
						//file.createNewFile();
						//fw = new FileWriter(file.getAbsoluteFile());
						//bw = new BufferedWriter(fw);
						//bw.write(article);
						//bw.close();

						//counter++; 
						
						
						/*id = article.charAt(0) + String.valueOf(article.charAt(1) + String.valueOf(3));
						System.out.println(id);
						
						for(char ch : article.toCharArray())
						{
							
						}*/
						//article = "";
						
					//article = article + sCurrentLine;
					//counter++;
				//}
				
				//article = article + sCurrentLine;     //anti na fainetai to id2 sto telos tou 1ou keimenou fainetai sthn arxh tou 2ou
				
				//firsttime = false;
				
				
					
					/*if(sCurrentLine.equals(".I " + String.valueOf(counter)) || !(sCurrentLine.equals(".I " + String.valueOf(counter+1))))
					{			
						article = article + sCurrentLine;								
					}
					
					else
					{
						id = sCurrentLine;
						
						article = id + article;
						
						System.out.println(article);
						System.out.println();
						
						
						counter++;
						
						article = "";
						
						
						
					}*/
				
					
				
				//System.out.println("mpikarevlaka");
				
				
				
				
				
			}
					
					
					/*do
					{
						article += sCurrentLine;
						sCurrentLine = br.readLine();
						
					}while(!sCurrentLine.equals(".I " + (counter+1)));
					
					System.out.println(article);
					*/
					
					/*System.out.println(sCurrentLine);
					
					nextLine = br.readLine();
					
					
					while(!(nextLine = br.readLine()).equals(".B"))
					{
						title += nextLine;
						
						
					}
					System.out.println(title);
					
					
					/*br.readLine();
					title = br.readLine();
					System.out.println();*/
						
				
		

		} catch (IOException e) {

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
		
	}
}

