package lucene;


import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.apache.lucene.LucenePackage;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.queryparser.flexible.core.builders.QueryBuilder;
import org.apache.lucene.search.*;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery.Builder;
import org.apache.lucene.store.FSDirectory;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import sun.misc.Version;


/** Simple command-line based search demo. */
public class SearchingFiles {

  public SearchingFiles() {}

	/** Simple command-line based search demo. */
  public void main(String[] args) throws Exception {

    String usage =
      "Usage:\tjava org.apache.lucene.demo.SearchFiles [-index dir] [-field f] [-repeat n] [-queries file] [-query string] [-raw] [-paging hitsPerPage]\n\nSee http://lucene.apache.org/core/4_1_0/demo/ for details.";
    if (args.length > 0 && ("-h".equals(args[0]) || "-help".equals(args[0]))) {
      System.out.println(usage);
      System.exit(0);
    }
    
    /*IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(index)));  //Dimiourgei indexreader gia to index mas
    IndexSearcher searcher = new IndexSearcher(reader); //enan index searcher vasiemseno ston indexreade

	//endregion

	  List<RelativeDocs> relativeMap = LoadRelativeArticles();

	  LoadQueries(relativeMap);

    //region
    /*BufferedReader in = null;  //diavazei tin eisodo apo ton xrhsth
    in = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
    
    //MultiFieldQueryParser parser = new MultiFieldQueryParser(fields, analyzer);
    QueryParser parser1 = new QueryParser(fields[0],analyzer);
    QueryParser parser2 = new QueryParser(fields[1],analyzer);
    while (true) {
      if (queries == null && queryString == null) {                        // prompt the user
        System.out.println("Enter title query : ");
      }

      String line1 = queryString != null ? queryString : in.readLine();

      if (line1 == null || line1.length() == -1) {
        break;
      }

      line1 = line1.trim();
      if (line1.length() == 0) {
        break;
      }
      
      System.out.println("Enter content query : ");
      String line2 = queryString != null ? queryString : in.readLine();
      line2 = line2.trim();
      
      Query query1 = parser1.parse(line1);
      Query query2 = parser2.parse(line2);
      
      
      //final Query
      
      BooleanQuery finalQuery = new BooleanQuery.Builder()
    		  .add(query1, BooleanClause.Occur.SHOULD) // MUST implies that the keyword must occur.
      		  .add(query2, BooleanClause.Occur.SHOULD)
      		  .build();
      
      
      System.out.println("Searching for: " + finalQuery.toString());

      if (repeat > 0) {                           // repeat & time as benchmark
        Date start = new Date();
        for (int i = 0; i < repeat; i++) {
          searcher.search(finalQuery, 100);
        }
        Date end = new Date();
        System.out.println("Time: "+(end.getTime()-start.getTime())+"ms");
      }

      doPagingSearch(in, searcher, finalQuery, hitsPerPage, raw, queries == null && queryString == null);

      if (queryString != null) {
        break;
      }
    }
    reader.clotr();*/
    //endregion
  }



	public List<RelativeDocs> LoadRelativeArticles()
  {
	 //Map<String,String> mymap = new HashMap<>();
	 List<RelativeDocs> relativeList = new ArrayList<>();

	  
	  String qrelsFile = "cacm\\cacm data\\qrels.text";
	  FileReader fr = null;
	  BufferedReader br = null;
	  String sCurrentLine = null; 
	  //String previous_q_id = "01";
	  String q_id;
	  String q_article;
	  
	  try
	  {
	  fr = new FileReader(qrelsFile);
	  br = new BufferedReader(fr);
	  
	  sCurrentLine = br.readLine();
	  
	  do {
		  
		  if(sCurrentLine.trim().length()!=0)
		  {
			  q_id = sCurrentLine.substring(0,2);
			  q_article = sCurrentLine.substring(3,7);
			  
			  System.out.println(q_id + " " + q_article);
			  
			  RelativeDocs bla = new RelativeDocs();
			  bla.setQ_id(q_id);
			  bla.setQ_article(q_article);
			  relativeList.add(bla);
			  sCurrentLine = br.readLine();
		  }	  
		  if(sCurrentLine==null) {
			  break;
		  }
	  }
	  while(!sCurrentLine.isEmpty());
	  
	  }catch (IOException e) {

			e.printStackTrace();

		} finally {

			try {

				if (fr != null)
					fr.close();

			} catch (IOException ex) {

				ex.printStackTrace();

			}

		}
	  
	  
	  return relativeList;
	  
  	}

  
  public Map<String,List<PrecisionRecall>> LoadQueries(List<RelativeDocs> relativeMaps) throws Exception
  {
  	Map<String,List<PrecisionRecall>> integerListMap = new HashMap<>();

  	int hitsperpage = 20;
  	String[] fields =  {"content", "title" , "authors"};
  	String index = "index";
  	IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(index)));  //Dimiourgei indexreader gia to index mas
	IndexSearcher searcher = new IndexSearcher(reader); //enan index searcher vasiemseno ston indexreader
	CustomAnalyzer analyzer = new CustomAnalyzer();

	QueryParser parser1 = new QueryParser(fields[0],analyzer); //periorismos se poio pedio tou eurethriou tha psa3oume to sygekrimeno query
	QueryParser parser2 = new QueryParser(fields[2],analyzer);

	String querysFile = "cacm\\cacm data\\query.text";
	String sCurrentLine = null;
	FileReader fr = null;
	BufferedReader br = null;

	boolean inW = false;
	boolean inA = false;
	boolean firstid = true;

	String id = "";
	String wText = "";
	String aText = "";

	int counter = 1;

	try
	{
		fr = new FileReader(querysFile);
		br = new BufferedReader(fr);

		while((sCurrentLine = br.readLine())!= null && counter<65)
		{
			if(sCurrentLine.trim().length() !=0)
			{
				if(sCurrentLine.charAt(0) == '.' && sCurrentLine.charAt(1) == 'W')
				{
					inW = true;
					inA = false;
				}
				else if(sCurrentLine.charAt(0) == '.' && sCurrentLine.charAt(1) == 'A')
				{
					inW = false;
					inA = true;
				}
				if(inW && sCurrentLine.charAt(0) != '.')
				{

					wText += sCurrentLine + " ";
				}
				if(inA && sCurrentLine.charAt(0) != '.')
				{
					aText += sCurrentLine + " ";
				}
				if(sCurrentLine.charAt(0) == '.' && sCurrentLine.charAt(1) == 'N')
				{
					inW = false;
					inA = false;

					System.out.println("-------------------------------------- Eimaste sthn anazhthsh --------------------------------------" + counter);

					Query query1 = parser1.parse(wText);
					if(aText.isEmpty())
					{
						System.out.println("wText: " + wText);
						List<PrecisionRecall> doPagingSearchobj = doPagingSearch(searcher, query1, hitsperpage,relativeMaps,counter);
						String cnt2;
						if(counter<10)
						{
							cnt2 = "0" + Integer.toString(counter);
						}
						else
						{
							cnt2 = Integer.toString(counter);
						}
						integerListMap.put(cnt2,doPagingSearchobj);
						counter++;
					}
					else
						{
							Query query2 = parser2.parse(aText);
							System.out.println("aText: " + aText);
							System.out.println("wText: " + wText);

							BooleanQuery finalQuery = new BooleanQuery.Builder()
									.add(query1, BooleanClause.Occur.SHOULD) // MUST implies that the keyword must occur.
									.add(query2, BooleanClause.Occur.SHOULD)
									.build();

							List<PrecisionRecall> doPagingSearchobj = doPagingSearch(searcher, finalQuery, hitsperpage,relativeMaps,counter);
							wText = "";
							aText = "";
							String cnt2;
							if(counter<10)
							{
								cnt2 = "0" + Integer.toString(counter);
							}
							else
							{
								cnt2 = Integer.toString(counter);
							}
							integerListMap.put(cnt2,doPagingSearchobj);
							counter++;
						}
				}
			}
		}
	}
	catch (IOException e) {
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
	return integerListMap;
  }
  

/**
   * This demonstrates a typical paging search scenario, where the search engine presents 
   * pages of size n to the user. The user can then go to the next page if interested in
   * the next hits.
   * 
   * When the query is executed for the first time, then only enough results are collected
   * to fill 5 result pages. If the user wants to page beyond this limit, then the query
   * is executed another time and all hits are collected.
 * @throws IOException 
   * 
   */

public static List<PrecisionRecall> doPagingSearch(IndexSearcher searcher, Query query, int hitsPerPage,List<RelativeDocs> relativeMaps2,int cnt) throws IOException
{

	List<PrecisionRecall> prList = new ArrayList<>();

	String cnt2;
	if(cnt<10)
	{
		cnt2 = "0" + Integer.toString(cnt);
	}
	else
	{
		cnt2 = Integer.toString(cnt);
	}

	List<RelativeDocs> relDocs = relativeMaps2.stream().filter(r -> r.getQ_id().equals(cnt2)).collect(Collectors.toList()); //returns the articles with id cnt
	//relDocs.forEach(r->System.out.println(r.getQ_article()));


	int num_of_recalled = 0;				//Paronomasths tou P
	int rels = relDocs.size();  			// Paronomasths tou R
	int true_positives = 0;         		//Arithmiths P kai R
	double precision = 0;
	double recall=0;

	boolean id_found = false;


	TopDocs results = searcher.search(query, hitsPerPage); //
	ScoreDoc hits[] = results.scoreDocs;


	int numTotalHits = (int) results.totalHits;
	System.out.println("Total Hits: "+ numTotalHits);

	//Emfanhzoume ta apotelesmata kai ypologizoume ta R kai P me th vohtheia ths listas pou periexei ta pragmatika synafh
	//if(hits.length < numTotalHits) {
	for(int i = 0;i<hitsPerPage;i++)   //pairnoume ena ena document pou epistrafhke apo thn results.scoreDocs
	{

		PrecisionRecall precisionRecall = new PrecisionRecall();
		Document doc = searcher.doc(hits[i].doc);
		System.out.println("hits[i].doc: "+ hits[i].doc);
		String path = doc.get("path");
		String id = doc.get("articleid");
		String title = doc.get("title");
		final String id2;
		id = id.substring(2,id.length()).trim();

		id2 = id;
		//System.out.println(id2);
		//id_found = relDocs.stream().anyMatch(r -> r.getQ_id().equals(id2));

		for(RelativeDocs item : relDocs){
			if(item.getQ_article().equals(id2))
			{
				id_found = true;
			}
			else
			{
				id_found = false;
			}
		}


		//relDocs.forEach(r->System.out.println(r.getQ_article()));



		num_of_recalled++;
		if(id_found)
		{
			true_positives++;
		}
		if(rels>0)
		{
			precision = (double)true_positives/(double)num_of_recalled;

			precisionRecall.setPrecision(precision);
			recall = (double) true_positives / (double) rels;

			precisionRecall.setRecall(recall);
		}

		System.out.println("true_positives: " + true_positives );
		System.out.println("num_of_recalled: " + num_of_recalled);
		System.out.println("rels: " + rels );

		System.out.println("Precision of result: " + num_of_recalled + " is: "  +precision );
		System.out.println("Recall of result: " + num_of_recalled + " is: "  +recall );

		prList.add(precisionRecall);



		if(path!=null)
		{
			System.out.println("Article's id: " + id + " Article's title: " + title);
		}
		else
		{
			System.out.println("File's path not found!");
		}
	}
	//}


	return prList;

}

public static List<ResultForUser> searchForTerm(String userQuery) throws IOException, ParseException {

	List<ResultForUser> queryResult = new ArrayList<>();
	int hitsperpage = 20;
	String[] fields =  {"content", "title" , "authors"};
	String index = "index";
	IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(index)));  //Dimiourgei indexreader gia to index mas
	IndexSearcher searcher = new IndexSearcher(reader); //enan index searcher vasiemseno ston indexreader
	CustomAnalyzer analyzer = new CustomAnalyzer();
	QueryParser parser1 = new QueryParser(fields[0],analyzer); //periorismos se poio pedio tou eurethriou tha psa3oume to sygekrimeno query
	Query query1 = parser1.parse(userQuery+"*");
	TopDocs hits = searcher.search(query1,hitsperpage);

	for(int i=0;i<hits.scoreDocs.length;i++)
	{
		ResultForUser hitResult = new ResultForUser();
		ScoreDoc scoreDoc = hits.scoreDocs[i];
		Document doc = searcher.doc(scoreDoc.doc);
		String id = doc.get("articleid");
		String title = doc.get("title");
		String content = doc.get("content");
		hitResult.setArticleId(id);
		hitResult.setTitle(title);
		hitResult.setContent(content);
		System.out.println(title);
		queryResult.add(hitResult);
	}

	return queryResult;
}
}

//region
//  public static void doPagingSearch(BufferedReader in, IndexSearcher searcher, Query query, 
//                                     int hitsPerPage, boolean raw, boolean interactive) throws IOException {
// 
//    // Collect enough docs to show 5 pages
//    TopDocs results = searcher.search(query, 5 * hitsPerPage);
//    ScoreDoc[] hits = results.scoreDocs;
//    
//    int numTotalHits = (int) results.totalHits;
//    System.out.println(numTotalHits + " total matching documents");
//
//    int start = 0;
//    int end = Math.min(numTotalHits, hitsPerPage);
//        
//    while (true) {
//      if (end > hits.length) {
//        System.out.println("Only results 1 - " + hits.length +" of " + numTotalHits + " total matching documents collected.");
//        System.out.println("Collect more (y/n) ?");
//        String line = in.readLine();
//        if (line.length() == 0 || line.charAt(0) == 'n') {
//          break;
//        }
//
//        hits = searcher.search(query, numTotalHits).scoreDocs;
//      }
//      
//      end = Math.min(hits.length, start + hitsPerPage);
//      
//     for (int i = start; i < end; i++) {
//        /*if (raw) {                              // output raw format
//          System.out.println("doc="+hits[i].doc+" score="+hits[i].score);
//          continue;
//        }*/
//
//        Document doc = searcher.doc(hits[i].doc);
//        String path = doc.get("path");
//        String id = doc.get("articleid");
//        if (path != null) {
//          System.out.println((i+1) + ". " + path + id);
//          String title = doc.get("title");
//          if (title != null) {
//            System.out.println("   Title: " + doc.get("title"));
//          }
//        } else {
//          System.out.println((i+1) + ". " + "No path for this document");
//        }
//                  
//      }
//
//      /*if (!interactive || end == 0) {
//        break;
//      }*/
//
//      if (numTotalHits >= end) {
//        boolean quit = false;
//        while (true) {
//          System.out.print("Press ");
//          if (start - hitsPerPage >= 0) {
//            System.out.print("(p)revious page, ");  
//          }
//          if (start + hitsPerPage < numTotalHits) {
//            System.out.print("(n)ext page, ");
//          }
//          System.out.println("(q)uit or enter number to jump to a page.");
//          
//          String line = in.readLine();
//          if (line.length() == 0 || line.charAt(0)=='q') {
//            quit = true;
//            break;
//          }
//          if (line.charAt(0) == 'p') {
//            start = Math.max(0, start - hitsPerPage);
//            break;
//          } else if (line.charAt(0) == 'n') {
//            if (start + hitsPerPage < numTotalHits) {
//              start+=hitsPerPage;
//            }
//            break;
//          } else {
//            int page = Integer.parseInt(line);
//            if ((page - 1) * hitsPerPage < numTotalHits) {
//              start = (page - 1) * hitsPerPage;
//              break;
//            } else {
//              System.out.println("No such page");
//            }
//          }
//        }
//        if (quit) break;
//        end = Math.min(numTotalHits, start + hitsPerPage);
//      }
//    }
//  }
	//endregion
