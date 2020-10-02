/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package paint;


import java.awt.image.RenderedImage;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;

import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javax.imageio.ImageIO;
import static paint.menuBar.drawnOn;
import static paint.menuBar.file;

/**
 * Main class that assembles the Paint application and runs it.
 * @author Francisco Vazquez
 */
public class Paint extends Application {
    int counter = 0;
    List<WritableImage> wims = new ArrayList<>();
    List<Canvas> canvases = new ArrayList<>();
    List<ImageView> imgViews = new ArrayList<>();
    List<GraphicsContext> graphicsContexts = new ArrayList<>();
    
    @Override
    public void start(Stage primaryStage) throws FileNotFoundException, IOException{
        //Creates the menu bar
        menuBar mb = new menuBar(primaryStage);
        //Creates the tool bar
        toolBar tb = new toolBar(primaryStage,mb.getImageView());
        //Creates scroll pane that will be used for the canvas
        ScrollPane scroll = new ScrollPane();
        
        scroll.setFitToHeight(true);
        scroll.setFitToWidth(true);
        // Setting up the layout of the window
        GridPane grid = new GridPane();
        StackPane stack = new StackPane();
        WritableImage wim = new WritableImage((int)mb.getImageView().getFitWidth(),(int)mb.getImageView().getFitHeight());
        mb.setWim(wim);
        tb.setWim(wim);
        
        tb.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event){
                mb.setWim(tb.getWim());
                mb.setDrawnOn(tb.beenDrawnOn());
            }
        });
        //Adding TabPane to allow for multiple canvases
        Tab startingTab = new Tab("WELCOME");
        
        String text = new String(Files.readAllBytes(Paths.get("C:\\Users\\Francisco Vazquez\\Documents\\cs250\\Paint\\src\\paint\\WelcomeTab.txt")) );
        Label abtLabel = new Label(text);
        //abtLabel.setStyle(" -fx-background-color: white;");
        
        startingTab.setContent(abtLabel);
        
        TabPane tabPane = new TabPane(startingTab);
        
        
        
        //Creates an extra tab to be clicked on that will allow 
        Tab newtab = new Tab();
        
        EventHandler<Event> newTabEvent;
        newTabEvent = new EventHandler<Event>() { 
            
            public void handle(Event e)
            {
                if (newtab.isSelected())
                {
                    
                    // create Tab
                    Tab tab = new Tab("Tab_" + (int)(counter));
                    
                    Canvas canvas = new Canvas(1220,620);
                    canvases.add(canvas);

                    tb.setCanvas(canvas);
                    
                    GraphicsContext gc = canvas.getGraphicsContext2D();
                    graphicsContexts.add(gc);

                    tb.setGraphicsContext(gc);
                    
                    WritableImage tabWim = new WritableImage(1200,600);
                    wims.add(tabWim);

                    
                    mb.setWim(tabWim);
                    tb.setWim(tabWim);
                    
                    
                    ImageView tabImgView = new ImageView();
                    tabImgView.setFitHeight(600);
                    tabImgView.setFitWidth(1200);
                    tabImgView.setPreserveRatio(true);
                    imgViews.add(tabImgView);

                    
                    mb.setImageView(tabImgView);
                    
                    StackPane tabStack = new StackPane();
                    tabStack.getChildren().addAll(mb.getImageView(),tb.getCanvas());
                    
                    counter++;
                    
                    // set content of the tab
                    tab.setContent(tabStack);
                    
                    // create event handler for when an existing tab is selected
                    
                    EventHandler<Event> ExistingTabEvent;
                    ExistingTabEvent = new EventHandler<Event>() { 

                        public void handle(Event e)
                        {
                            String tabIndex = tab.getText().substring(tab.getText().length()-1);
                            System.out.println("Tab Index: "+ tabIndex);
                            System.out.println("Counter: " + counter);
                            int index = Integer.parseInt(tabIndex);
                            mb.setWim(wims.get(index));
                            tb.setWim(wims.get(index));
                            tb.setCanvas(canvases.get(index));
                            mb.setImageView(imgViews.get(index));
                            tb.setGraphicsContext(graphicsContexts.get(index));
                        }
                    };
                    
                    tab.setOnSelectionChanged(ExistingTabEvent);
                    
                    // add tab
                    tabPane.getTabs().add(
                            tabPane.getTabs().size() - 1, tab);
                    
                    // select the last tab
                    tabPane.getSelectionModel().select(
                            tabPane.getTabs().size() - 2);
                }
            }
        }; 

        
        
        
        // set event handler to the tab 
        newtab.setOnSelectionChanged(newTabEvent); 

        // add newtab 
        tabPane.getTabs().add(newtab); 
        
        // Sets up canvas 
        StackPane scrollStack = new StackPane();
        stack.getChildren().addAll(mb.getImageView(),tb.getCanvas());
        
        //startingTab.setContent(stack);
        
        mb.setPane(stack);
        tb.setPane(stack);
        scrollStack.getChildren().add(tabPane);
        scroll.setContent(scrollStack);
        //Adding menu bar and the canvas to the window
        //grid.add(tabPane,0,0);
        grid.add(mb,0,0);
        grid.addRow(1,tb);
        grid.addRow(2, tabPane);
        grid.add(scrollStack,0,3);
        // Displays the window with everything that has been created
        Scene scene = new Scene(grid, 1500, 900);
        primaryStage.setTitle("Paint");
        primaryStage.setScene(scene);
        primaryStage.show();
        Platform.setImplicitExit(false);
        
        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>(){
            @Override
            public void handle(WindowEvent event){
                event.consume();
                if(!paint.menuBar.saved){
                    GridPane grid = new GridPane();
                    grid.setPadding(new Insets(25,25,25,25));
                    Stage tempstage = new Stage();
                    Label label = new Label("Do you wish to save?");
                    Button button = new Button("Save");
                    grid.add(label, 0, 0);
                    grid.add(button,0,1);
                    Scene stageScene = new Scene(grid);
                    tempstage.setScene(stageScene);
                    tempstage.show();
                    button.setOnAction(new EventHandler<ActionEvent>() {
                        @Override
                        public void handle(ActionEvent event){
                            
                            try {
                                if(!drawnOn){
                                    paint.menuBar.saved = true;
                                    ImageIO.write(SwingFXUtils.fromFXImage(paint.menuBar.imageView.getImage(), null),"png",file);
                                }
                                else{
                                    FileChooser sFileChooser = new FileChooser();
                                    sFileChooser.setTitle("Save As..");
                                    sFileChooser.getExtensionFilters().addAll(
                                        new FileChooser.ExtensionFilter("*.png","*.jpg"));
                                        file = sFileChooser.showSaveDialog(primaryStage);
                                        if (file != null) {
                                            drawnOn = paint.toolBar.drawnOn;
                                            paint.menuBar.wim = paint.toolBar.wim;
                                            paint.menuBar.pane = paint.toolBar.pane;
                                            paint.menuBar.undoStack.push(wim);
                                            try {
                                                if(!drawnOn){
                                                    paint.menuBar.saved = true;
                                                    ImageIO.write(SwingFXUtils.fromFXImage(paint.menuBar.imageView.getImage(), null),"png",file);
                                                }
                                                else{
                                                   RenderedImage renderedImage = SwingFXUtils.fromFXImage(wim, null);
                                                   ImageIO.write(renderedImage, "png", file);  
                                                }
                                            }    
                                            catch (Exception e) {
                                                System.out.println("Invalid selection.");
                                            }  
                                        }       
                                }
                            }
                            catch (Exception e) {
                                System.out.println("Invalid selection.");
                            }

                        }
                        });
                    tempstage.setOnCloseRequest(new EventHandler<WindowEvent>(){
                        @Override
                        public void handle(WindowEvent event){
                            tempstage.close();
                            primaryStage.close();
                            Platform.exit();
                        }
                    });
                }
                else{
                    primaryStage.close();
                    Platform.exit();
                }
            }
        });
    }
    

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
   
    
}
