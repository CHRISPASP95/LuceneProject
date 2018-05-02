package lucene;



import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import lucene.PrecisionRecall;
import lucene.SearchingFiles;
import org.apache.lucene.queryparser.classic.ParseException;


import javax.swing.*;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class ShowDiagrams extends Application {

    SearchingFiles searchingFiles = new SearchingFiles();
    Map<String,List<PrecisionRecall>> myMap = new HashMap<>();
    ListView<String> listView,resultsListView;
    Button showDiagrams,searchBtn,systemPerfBtn,backBtn;
    TextField searchField;
    VBox layoutSearch;
    Label labelArticleId;
    TextArea labelContent,labelTitle;
    String currentSearch;


    public static void main(String[] args)
    {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        //Initializing Basic UI Objects
        showDiagrams = new Button("View Question(s) Diagram(s)");
        searchBtn = new Button("Search");
        systemPerfBtn = new Button("System performance");
        backBtn = new Button("Back to Results");
        searchField = new TextField();
        labelArticleId = new Label();
        labelTitle = new TextArea();
        labelContent = new TextArea();

        labelContent.setWrapText(true);
        labelContent.setEditable(false);

        labelTitle.setWrapText(true);
        labelTitle.setEditable(false);
        labelTitle.setPrefHeight(15);



        //Main Window
        layoutSearch = new VBox();
        HBox hbox = new HBox(searchBtn,systemPerfBtn);
        Stage stageSearch = new Stage();
        stageSearch.setTitle("Pink Duck Search");
        layoutSearch.setPadding(new Insets(20,20,20,20));
        layoutSearch.getChildren().addAll(searchField,hbox);
        stageSearch.setScene(new Scene(layoutSearch, 300, 500));
        stageSearch.show();


        //Performance Window
        VBox layout = new VBox();
        Stage primaryStage1 = new Stage();
        primaryStage1.setTitle("PR Diagrams");

        //(1)Running SearchFiles
        List<RelativeDocs> relativeMap = searchingFiles.LoadRelativeArticles();
        myMap = searchingFiles.LoadQueries(relativeMap);

        //List<PrecisionRecall> myL = myMap.get("32");
        //myL.forEach(r->System.out.println(r.getPrecision() + " " + r.getRecall()));

        //(2)Sorting question IDs
        List<String> keys = new ArrayList<>(myMap.keySet());
        Collections.sort(keys);

        //(3)Creating listview and setting up Performance Window layout
        listView = new ListView<>();
        listView.getItems().addAll(keys);
        listView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        layout.setPadding(new Insets(20,20,20,20));
        layout.getChildren().addAll(listView,showDiagrams);
        primaryStage1.setScene(new Scene(layout, 300, 500));


        //Button listener on Performance Window, if button showDiagrams gets clicked all the selected questions' PR diagrams are loaded.
        showDiagrams.setOnAction(e -> buttonClicked());

        //Button listener on Main Window, if button systemPerf gets clicked show Performance Window
        systemPerfBtn.setOnAction(e -> primaryStage1.show());
        //Button listener on Main Window, if button search_btn gets clicked the list of articles(CACM) is searched with the query of the textfield searchField above.
        searchBtn.setOnAction(e -> {
            try {
                String query = searchField.getText().trim();
                searchCACM(query);
            } catch (IOException e1) {
                e1.printStackTrace();
            } catch (ParseException e1) {
                e1.printStackTrace();
            }
        });



        backBtn.setOnAction(e->
        {
            layoutSearch.getChildren().remove(labelArticleId);
            layoutSearch.getChildren().remove(labelTitle);
            layoutSearch.getChildren().remove(labelContent);
            try {
                searchCACM(currentSearch);
            } catch (IOException e1) {
                e1.printStackTrace();
            } catch (ParseException e1) {
                e1.printStackTrace();
            }
            layoutSearch.getChildren().remove(backBtn);
        });


    }

    private void buttonClicked() {
        ObservableList<String> ids;
        ids = listView.getSelectionModel().getSelectedItems();

        for(String i: ids)
        {
            init(i);

        }

    }


    private void init(String id) {

        List<PrecisionRecall> myL = myMap.get(id);

        Stage primaryStage = new Stage();
        HBox root = new HBox();
        Scene scene = new Scene(root, 450, 330);

        NumberAxis xAxis = new NumberAxis();
        xAxis.setLabel("Recall");

        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Precision");

        LineChart<Number,Number> lineChart = new LineChart<Number, Number>(xAxis,yAxis);
        lineChart.setTitle("Precision-Recall Diagram");

        List<XYChart.Data<Number,Number>> loulou = new ArrayList<>();
        for (PrecisionRecall pr: myL) {
            loulou.add(new XYChart.Data<>(pr.getRecall(),pr.getPrecision()));

        }

        XYChart.Series<Number,Number> data = new XYChart.Series<>();
        data.setName("Question x");

            data.getData().addAll(loulou);
            //System.out.println(pr.getRecall()+ " " + pr.getPrecision());
            //loulou.forEach(r->System.out.println(r.getXValue() + " " + r.getYValue()));





        lineChart.getData().add(data);

        root.getChildren().add(lineChart);

        primaryStage.setTitle("Diagram of question: " + id);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void searchCACM(String query) throws IOException, ParseException {
        if(resultsListView!=null) {
            layoutSearch.getChildren().remove(resultsListView);
        }
        currentSearch = query;
        //System.out.println(query);

        List<ResultForUser> results = searchingFiles.searchForTerm(query);//.collect(Collectors.toList())
        List<String> resultsTitles = results.stream().map(ResultForUser::getTitle).collect(Collectors.toList());

        resultsListView = new ListView<>();
        resultsListView.getItems().addAll(resultsTitles);
        resultsListView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        resultsListView.setOnMouseClicked(e-> {
            //System.out.println("clicked on " + resultsListView.getSelectionModel().getSelectedItem());
            String title = resultsListView.getSelectionModel().getSelectedItem();
            int index = resultsTitles.indexOf(title);
            String articleId = results.get(index).getArticleId();
            String content =  results.get(index).getContent();
            labelArticleId.setText(articleId);
            labelTitle.setText(title);
            labelContent.setText(content);

            layoutSearch.getChildren().remove(resultsListView);
            //resultsListView.setVisible(false);
            layoutSearch.getChildren().addAll(labelArticleId,labelTitle,labelContent,backBtn);

        });

        layoutSearch.getChildren().addAll(resultsListView);

    }



}
