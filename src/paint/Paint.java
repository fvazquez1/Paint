/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package paint;


import java.io.FileNotFoundException;
import java.io.IOException;
import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

/**
 *
 * @author Francisco Vazquez
 */
public class Paint extends Application {
    @Override
    public void start(Stage primaryStage) throws FileNotFoundException, IOException{
        //Creates the menu bar
        menuBar mb = new menuBar(primaryStage);
        //Creates scroll pane that will be used for the canvas
        ScrollPane scroll = new ScrollPane();
        
        scroll.setFitToHeight(true);
        scroll.setFitToWidth(true);
        // Setting up the layout of the window
        GridPane grid = new GridPane();
        StackPane stack = new StackPane();
        WritableImage wim = new WritableImage((int)mb.getImageView().getFitHeight(),(int)mb.getImageView().getFitWidth());
        
        // Sets up canvas 

        StackPane scrollStack = new StackPane();
        stack.getChildren().addAll(mb.getImageView(),mb.getCanvas());
        mb.setPane(stack);
        scroll.setContent(stack);
        scrollStack.getChildren().add(scroll);
        //Adding menu bar and the canvas to the window
        grid.add(mb,0,0);
        grid.add(scrollStack,0,2);
        // Displays the window with everything that has been created
        Scene scene = new Scene(grid, 1500, 900);
        primaryStage.setTitle("Paint");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
    
}
