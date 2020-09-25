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
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
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
    static double currentWidth,orgTranslateX, orgTranslateY,orgSceneX, orgSceneY;
    static Pane pane;
    static boolean straightLineSelected;
    static Pair<Double,Double> initialClick;
    static Stack<WritableImage> undoStack,redoStack;
    /**
     * Constructor for menuBar object. This class extends the JavaFX object MenuBar. 
     * 
     * @param stage the Stage that the menu bar will be displayed on.
     * @throws IOException 
     */
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
        undoStack = new Stack<WritableImage>();
        redoStack = new Stack<WritableImage>();
        canvas = paint.menuBar.canvas;
        
        
}
    /**
     * Sets up and adds the File menu to the menuBar. All MenuItems are instantiated,
     * and added to the File menu. The File Menu is then added to the menuBar. 
     */
    public void addFile(){
        // Creating the file option in the menu bar
        final Menu mbFile = new Menu("File");
        // Adding open, save, and clear image options to file menu
        MenuItem open = addOpenImage();
        MenuItem saveAs = addSaveAsImage();
        MenuItem save = addSaveImage();
        MenuItem clearImg = addClearImage();
        MenuItem undo = addUndo();
        MenuItem redo = addRedo();
        MenuItem select = addSelectBox();
        // Adds all the options to the file menu
        mbFile.getItems().addAll(open,saveAs,save,undo,redo,select,clearImg);
        // Adds the file menu to the menu bar
        this.getMenus().addAll(mbFile);
    }
    /**
     * Sets up and adds the Help menu to the menuBar. All the MenuItems are instantiated,
     * and added to the Help menu. The Help Menu is then added to the menuBar.
     * @throws IOException 
     */
    public void addHelp() throws IOException{
        final Menu mbHelp = new Menu("Help");
        
        MenuItem about = addAbout();
        MenuItem toolHelp = addToolHelp();
        mbHelp.getItems().addAll(about,toolHelp);
        this.getMenus().addAll(mbHelp);
    }
    /**
     * MenuItem for "Open Image" is created as well as the corresponding EventHandler. 
     * The EventHandler handles the opening the FileChooser and following opening
     * of the chosen image. 
     * @return "Open Image" MenuItem 
     */
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
                    WritableImage tempWim = new WritableImage((int)imageView.getFitWidth()+20,(int)imageView.getFitHeight()+20);
                    pane.snapshot(null, tempWim);
                    undoStack.push(tempWim);
                } catch (FileNotFoundException ex) {
                    Logger.getLogger(Paint.class.getName()).log(Level.SEVERE, null, ex);
                }
                
            }
        });
        return open;
    }
    /**
     *MenuItem for "Save As.." is created as well as the corresponding EventHandler. 
     * The EventHandler handles the opening the FileChooser as well as the saving
     *  of the image at the specified file location. 
     * @return "Save As.." MenuItem  
     */
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
    /**
     * MenuItem for "Save" is created as well as the corresponding EventHandler. 
     * The EventHandler handles the saving of the current image that is open. 
     * The image is saved at the already existing file location. 
     * @return "Save" MenuItem
     */
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
    /**
     * MenuItem for "Redo" is created as well as the corresponding EventHandler. 
     * The EventHandler handles the call to redo() as well as setting the current 
     * image to what is pushed off of the redo stack.
     * @return "Redo" MenuItem
     */
    private MenuItem addRedo(){
        MenuItem redo = new MenuItem("Redo");
        
        redo.setOnAction(new EventHandler<ActionEvent>() {
           @Override
           public void handle(ActionEvent event){
               paint.toolBar.wim = redo();
           }
        });
        redo.setAccelerator(new KeyCodeCombination(KeyCode.Z,KeyCombination.CONTROL_DOWN,KeyCombination.SHIFT_DOWN));
        return redo;
    }
    /**
     * Manages the redo stack containing images that can potentially be redone. 
     * A stack of WritableImages are stored in a stack, and when this method is
     * called, the last change to an image is redone. 
     * @return WritableImage of the image with the last change redone
     */
    private WritableImage redo(){
        WritableImage recent = new WritableImage((int)imageView.getFitWidth()+20,(int)imageView.getFitHeight()+20);
        
        try{
            recent = redoStack.pop();
            undoStack.push(paint.toolBar.wim);
        }
        catch(Exception e){
            System.out.println("Nothing to undo");
        }
        
        return recent;
    }
    /**
     * MenuItem for "Undo" is created as well as the corresponding EventHandler. 
     * The EventHandler handles the call to undo() as well as setting the current 
     * image to what is pushed off of the undo stack.
     * @return "Undo" MenuItem
     */
    private MenuItem addUndo(){
        MenuItem undo = new MenuItem("Undo");
        
        undo.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event){
                paint.toolBar.wim = undo();
            }
        });
        undo.setAccelerator(new KeyCodeCombination(KeyCode.Z,KeyCombination.CONTROL_DOWN));
        return undo;
    }
    /**
     * Manages the undo stack containing images that can potentially be undone. 
     * A stack of WritableImages are stored in a stack, and when this method is
     * called, the most recent change to an image is undone. 
     * @return WritableImage of the image without the most recent change
     */
    private WritableImage undo(){
        WritableImage last = new WritableImage((int)imageView.getFitWidth()+20,(int)imageView.getFitHeight()+20);
        try{
            last = undoStack.pop();
            redoStack.push(paint.toolBar.wim);
        }
        catch(Exception e){
            System.out.println("Nothing to redo");
        }
        return last;
    }
    /**
     * MenuItem for "Clear Image" is created as well as the corresponding EventHandler. 
     * The EventHandler handles the removal of the current image and all  
     * drawings being displayed.
     * @return "Clear Image" MenuItem
     */
    private MenuItem addClearImage(){
        // Adding clear image option under file
        MenuItem clearImg = new MenuItem("Clear Image");
        clearImg.setOnAction(new EventHandler<ActionEvent>() {
            @Override // Resets the window to show nothing
            public void handle(ActionEvent event){
                imageView.setImage(null);
                paint.toolBar.gc.clearRect(0, 0, 2000, 2000);
            }
        });
        return clearImg;
    }
    /**
     * MenuItem for "Select" is created as well as the corresponding EventHandler. 
     * The EventHandler handles the selection of the desired part of the current image,
     * then opens the desired part of the image in the middle of the canvas. A
     * separate EventHandler is made to handle the movement of the desire part of
     * the image. 
     * @return "Select" MenuItem
     */
    private MenuItem addSelectBox(){
        MenuItem select = new MenuItem("Select");
        
        select.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event){
                
                toolBar.canvas.setOnMousePressed(new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent event){
                        initialClick = new Pair(event.getX(),event.getY());
                    }
                });
                
                toolBar.canvas.setOnMouseReleased(new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent event){
                        try{
                            WritableImage selectedImg = new WritableImage(imageView.getImage().getPixelReader(),(int)Math.round(initialClick.getKey()),(int)Math.round(initialClick.getValue()),Math.abs((int)(event.getX()-initialClick.getKey())),Math.abs((int)(event.getY()-initialClick.getValue())));
                            SnapshotParameters params = new SnapshotParameters();
                            Rectangle2D selectedRect = new Rectangle2D(Math.round(initialClick.getKey())+150, Math.round(initialClick.getValue()),Math.abs(event.getY()-initialClick.getValue()),Math.abs(event.getX()-initialClick.getKey())+150);
                            params.setViewport(selectedRect);
                            
                            pane.snapshot(params, selectedImg);
                            ImageView selectedImgView = new ImageView(selectedImg);
                            selectedImgView.setOnMousePressed(new EventHandler<MouseEvent>(){
                                @Override
                                public void handle(MouseEvent event){
                                    orgSceneX = event.getSceneX();
                                    orgSceneY = event.getSceneY();
                                    orgTranslateX = ((ImageView)(event.getSource())).getTranslateX();
                                    orgTranslateY = ((ImageView)(event.getSource())).getTranslateY();
                                }
                            });
                            selectedImgView.setOnMouseDragged(new EventHandler<MouseEvent>() {
                                @Override
                                public void handle(MouseEvent event){
                                    double offsetX = event.getSceneX() - orgSceneX;
                                    double offsetY = event.getSceneY() - orgSceneY;
                                    double newTranslateX = orgTranslateX + offsetX;
                                    double newTranslateY = orgTranslateY + offsetY;

                                    ((ImageView)(event.getSource())).setTranslateX(newTranslateX);
                                    ((ImageView)(event.getSource())).setTranslateY(newTranslateY);
                                }
                            });
                            pane.getChildren().add(selectedImgView);
                            
                            
                        }
                        catch(Exception e){
                            System.out.println("Almost got an error lol");
                        }

                        gc = paint.toolBar.gc;
                        gc.setFill(Color.WHITE);
                        gc.fillRect((int)Math.round(initialClick.getKey()),(int)Math.round(initialClick.getValue()),Math.abs((int)(event.getX()-initialClick.getKey())),Math.abs((int)(event.getY()-initialClick.getValue())));
                    }
                });
            }
        });
        
        return select;
    }
    /**
     * Returns the ImageView being used by the menuBar.
     * @return ImageView
     */
    public ImageView getImageView(){
        return imageView;
    }
    /**
     * MenuItem for "Release Notes" is created as well as the corresponding EventHandler. 
     * The EventHandler handles the opening of a separate window to display the 
     * most current release notes. 
     * @return "Release Notes" MenuItem
     * @throws IOException 
     */
    private MenuItem addAbout() throws IOException{
        MenuItem about = new MenuItem("Release Notes");

        String text = new String(Files.readAllBytes(Paths.get("C:\\Users\\Francisco Vazquez\\Documents\\cs250\\Paint\\src\\paint\\ReleaseNotes.txt")) );
        Label abtLabel = new Label(text);
        abtLabel.setStyle(" -fx-background-color: white;");

        
        ScrollPane scroll = new ScrollPane();
        Stage tempstage = new Stage();
        scroll.setContent(abtLabel);
        Scene stageScene = new Scene(scroll);
        tempstage.setScene(stageScene);
        tempstage.setMaxHeight(900);
        tempstage.setTitle("Release Notes");
        
        about.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event){
                tempstage.show();
            }
        });
        about.setAccelerator(new KeyCodeCombination(KeyCode.A,KeyCombination.CONTROL_DOWN));
        return about; 
    }
    /**
     * MenuItem for "How to Use Tools" is created as well as the corresponding EventHandler. 
     * The EventHandler handles the opening of a separate window to display the 
     * most current tool help file.  
     * @return "How to Use Tools" MenuItem
     * @throws IOException 
     */
    private MenuItem addToolHelp() throws IOException{
        MenuItem toolHelp = new MenuItem("How to Use Tools");

        String text = new String(Files.readAllBytes(Paths.get("C:\\Users\\Francisco Vazquez\\Documents\\cs250\\Paint\\src\\paint\\toolBar_functionHelp.txt")) );
        Label abtLabel = new Label(text);
        abtLabel.setStyle(" -fx-background-color: white;");


        ScrollPane scroll = new ScrollPane();
        Stage tempstage = new Stage();
        scroll.setContent(abtLabel);
        Scene stageScene = new Scene(scroll);
        tempstage.setScene(stageScene);
        tempstage.setMaxHeight(900);
        tempstage.setTitle("Tool Bar Help");

        toolHelp.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event){
                tempstage.show();
            }
        });
        toolHelp.setAccelerator(new KeyCodeCombination(KeyCode.H,KeyCombination.CONTROL_DOWN));
        return toolHelp; 
    }
    /**
     * Sets a new boolean value to the variable drawnOn that indicates if any 
     * drawing has been done on the image. This variable helps determine how to 
     * save the current image. 
     * @param beenDrawnOn boolean indicating whether the current image has been 
     * drawn on. 
     */
    public void setDrawnOn(boolean beenDrawnOn){
        drawnOn = beenDrawnOn;
    }
    /**
     * Sets the current WritableImage to a new WritableImage. The WritableImage
     * is used in multiple methods, and should only be changed or set when necessary. 
     * @param wim New WritableImage to be used
     */
    public void setWim(WritableImage wim) {
        this.wim = wim;
    }
    /**
     * Sets the current Pane to a new Pane. This Pane is used primarily to save 
     * the current image with any potential changes made to it.
     * @param pane New Pane to be used
     */
    public void setPane(Pane pane){
        this.pane = pane;
    }
}
