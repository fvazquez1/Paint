/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package paint;


import java.awt.image.RenderedImage;
import java.io.FileNotFoundException;
import java.io.IOException;
import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;

import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javax.imageio.ImageIO;
import static paint.menuBar.drawnOn;
import static paint.menuBar.file;

/**
 *
 * @author Francisco Vazquez
 */
public class Paint extends Application {
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
        // Sets up canvas 
        StackPane scrollStack = new StackPane();
        stack.getChildren().addAll(mb.getImageView(),tb.getCanvas());
        mb.setPane(stack);
        tb.setPane(stack);
        scroll.setContent(stack);
        scrollStack.getChildren().add(scroll);
        //Adding menu bar and the canvas to the window
        grid.add(mb,0,0);
        grid.addRow(1, tb);
        grid.add(scrollStack,0,2);
        // Displays the window with everything that has been created
        Scene scene = new Scene(grid, 1500, 900);
        primaryStage.setTitle("Paint");
        primaryStage.setScene(scene);
        primaryStage.show();
        
        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>(){
            @Override
            public void handle(WindowEvent event){
                primaryStage.show();
                if(!paint.menuBar.saved){
                    GridPane grid = new GridPane();
                    grid.setPadding(new Insets(25,25,25,25));
                    Stage tempstage = new Stage();
                    Label label = new Label("Do you wish to save?");
                    Button button = new Button("Save");
                    grid.add(label, 0, 0);
                    grid.add(button,0,1);
                    Scene stageScene = new Scene(grid);
                    primaryStage.setScene(stageScene);
                    primaryStage.showAndWait();
                    button.setOnAction(new EventHandler<ActionEvent>() {
                        @Override
                        public void handle(ActionEvent event){
                            
                            try {
                                    if(!drawnOn){
                                        paint.menuBar.saved = true;
                                        ImageIO.write(SwingFXUtils.fromFXImage(paint.menuBar.imageView.getImage(), null),"png",file);
                                    }
                                    else{
                                       RenderedImage renderedImage = SwingFXUtils.fromFXImage(paint.menuBar.wim, null);
                                       ImageIO.write(renderedImage, "png", file);  
                                    }
                                }    
                                    catch (Exception e) {
                                        System.out.println("Invalid selection.");
                                    }

                            }
                        });
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
