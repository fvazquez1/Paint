/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package paint;
import java.awt.image.RenderedImage;
import javafx.scene.paint.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Line;
import javafx.stage.FileChooser;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Pair;
import javax.imageio.ImageIO;
/**
 *
 * @author Francisco Vazquez
 */
public class menuBar extends MenuBar{
    static ImageView imageView;
    static Stage primaryStage;
    static File file;
    static Canvas canvas;
    static WritableImage wim;
    static GraphicsContext gc;
    static boolean drawnOn,saved;
    static Color currentColor;
    static double currentWidth;
    static Pane pane;
    static boolean straightLineSelected;
    static Pair<Double,Double> initialClick;
    static Stack<WritableImage> undoStack;
    public menuBar(Stage stage) throws IOException{
        // Set up menu bar
        imageView = new ImageView();
        imageView.setFitHeight(600);
        imageView.setFitWidth(1200);
        imageView.setPreserveRatio(true);
        wim = new WritableImage((int)imageView.getFitWidth()+20,(int)imageView.getFitHeight()+20);
        primaryStage = stage;
        this.addFile();
        this.addHelp();
        saved = false;
        
        
}
    public void addFile(){
        // Creating the file option in the menu bar
        final Menu mbFile = new Menu("File");
        // Adding open, save, and clear image options to file menu
        MenuItem open = addOpenImage();
        MenuItem saveAs = addSaveAsImage();
        MenuItem save = addSaveImage();
        MenuItem clearImg = addClearImage();
        MenuItem undo = addUndo();
        // Adds all the options to the file menu
        mbFile.getItems().addAll(open,saveAs,save,undo,clearImg);
        // Adds the file menu to the menu bar
        this.getMenus().addAll(mbFile);
    }
    
    public void addHelp() throws IOException{
        final Menu mbHelp = new Menu("Help");
        
        MenuItem about = addAbout();
        mbHelp.getItems().add(about);
        this.getMenus().addAll(mbHelp);
    }
    
    private MenuItem addOpenImage(){
        final FileChooser fileChooser = new FileChooser();
        // Adding open image option to the file menu
        MenuItem open = new MenuItem("Open Image");
        open.setOnAction(new EventHandler<ActionEvent>() {
            @Override // Opens up a file chooser winder, and allows for user to choose an image to display
            public void handle(ActionEvent event) {
                fileChooser.setTitle("Choose Image");
                fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.gif"));
                file = fileChooser.showOpenDialog(primaryStage);
                try {
                    Image image = new Image(new FileInputStream(file));
                    imageView.setImage(image);
                    System.out.println("Pls print");
                } catch (FileNotFoundException ex) {
                    Logger.getLogger(Paint.class.getName()).log(Level.SEVERE, null, ex);
                }
                
            }
        });
        return open;
    }
    private MenuItem addSaveAsImage(){
        // Adding save image option under file menu
        MenuItem saveAs = new MenuItem("Save As...");
        saveAs.setOnAction(new EventHandler<ActionEvent>(){
            @Override // Opens a file saving window and allows user to name the file and choose the file destination
            public void handle(ActionEvent event){
                FileChooser sFileChooser = new FileChooser();
                sFileChooser.setTitle("Save As..");
                sFileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("*.png","*.jpg"));
                file = sFileChooser.showSaveDialog(primaryStage);
                if (file != null) {
                    drawnOn = paint.toolBar.drawnOn;
                    wim = paint.toolBar.wim;
                    pane = paint.toolBar.pane;
                    undoStack.push(wim);
                    try {
                        if(!drawnOn){
                            saved = true;
                            ImageIO.write(SwingFXUtils.fromFXImage(imageView.getImage(), null),"png",file);
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
        });
        saveAs.setAccelerator(new KeyCodeCombination(KeyCode.S,KeyCombination.CONTROL_DOWN,KeyCombination.SHIFT_DOWN));
        return saveAs;
    }
    private MenuItem addSaveImage(){
        MenuItem save = new MenuItem("Save");
        save.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event){
                drawnOn = paint.toolBar.drawnOn;
                wim = paint.toolBar.wim;
                undoStack.push(wim);
                try {
                        if(!drawnOn){
                            saved = true;
                            ImageIO.write(SwingFXUtils.fromFXImage(imageView.getImage(), null),"png",file);
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
        });
        save.setAccelerator(new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN));
        
        return save;
    }
    
    private MenuItem addUndo(){
        MenuItem undo = new MenuItem("Undo");
        
        undo.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event){
                undo();
            }
        });
        undo.setAccelerator(new KeyCodeCombination(KeyCode.U,KeyCombination.CONTROL_DOWN));
        return undo;
    }
    
    private WritableImage undo(){
        WritableImage last = wim;
        undoStack = paint.toolBar.undoStack;
        try{
            last = undoStack.pop();
        }
        catch(Exception e){
            System.out.println("Nothing to undo");
        }
        return last;
    }
    
    private MenuItem addClearImage(){
        // Adding clear image option under file
        MenuItem clearImg = new MenuItem("Clear Image");
        clearImg.setOnAction(new EventHandler<ActionEvent>() {
            @Override // Resets the window to show nothing
            public void handle(ActionEvent event){
                imageView.setImage(null);
            }
        });
        return clearImg;
    }
    public ImageView getImageView(){
        return imageView;
    }
    
    private MenuItem addAbout() throws IOException{
        MenuItem about = new MenuItem("About");
        Popup popup = new Popup();
        String text = new String(Files.readAllBytes(Paths.get("C:\\Users\\Francisco Vazquez\\Documents\\cs250\\Paint\\src\\paint\\ReleaseNotes.txt")) );
        Label abtLabel = new Label(text);
        abtLabel.setStyle(" -fx-background-color: white;");
        popup.getContent().add(abtLabel);
        popup.setAutoHide(true);
        about.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event){
                if (!popup.isShowing()) 
                    popup.show(primaryStage);
            }
        });
        about.setAccelerator(new KeyCodeCombination(KeyCode.H,KeyCombination.CONTROL_DOWN));
        return about; 
    }

    public void setDrawnOn(boolean beenDrawnOn){
        drawnOn = beenDrawnOn;
    }
    
    public void setWim(WritableImage wim) {
        this.wim = wim;
    }
    
    public void setPane(Pane pane){
        this.pane = pane;
    }
}
