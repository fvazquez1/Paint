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
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
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
import javafx.util.Duration;
import javax.imageio.ImageIO;
import static paint.menuBar.drawnOn;
import static paint.menuBar.file;

/**
 * Main class that assembles the Paint application and runs it.
 *
 * @author Francisco Vazquez
 */
public class Paint extends Application {

    int counter = 0;
    List<WritableImage> wims = new ArrayList<>();
    List<Canvas> canvases = new ArrayList<>();
    List<ImageView> imgViews = new ArrayList<>();
    List<GraphicsContext> graphicsContexts = new ArrayList<>();
    List<Boolean> tabSaved = new ArrayList<>();

    @Override
    public void start(Stage primaryStage) throws FileNotFoundException, IOException {
        //Creates the menu bar
        menuBar mb = new menuBar(primaryStage);
        //Creates the tool bar
        toolBar tb = new toolBar(primaryStage, mb.getImageView());
        //Creates scroll pane that will be used for the canvas
        ScrollPane scroll = new ScrollPane();

        scroll.setFitToHeight(true);
        scroll.setFitToWidth(true);
        // Setting up the layout of the window
        GridPane grid = new GridPane();
        StackPane stack = new StackPane();
        WritableImage wim = new WritableImage((int) mb.getImageView().getFitWidth(), (int) mb.getImageView().getFitHeight());
        mb.setWim(wim);
        tb.setWim(wim);
        //Makes sure that the writable image, and the drawn on boolean is the same for the tool bar and menu bar
        tb.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                mb.setWim(tb.getWim());
                mb.setDrawnOn(tb.beenDrawnOn());
            }
        });
        //Creating the Welcome Tab that shows on open
        Tab startingTab = new Tab("WELCOME");

        String text = new String(Files.readAllBytes(Paths.get("C:\\Users\\Francisco Vazquez\\Documents\\cs250\\Paint\\src\\paint\\WelcomeTab.txt")));
        Label abtLabel = new Label(text);

        startingTab.setContent(abtLabel);
        //Adding TabPane to allow for multiple canvases
        TabPane tabPane = new TabPane(startingTab);

        //Creates an extra tab to be clicked on that will allow 
        Tab newtab = new Tab();

        EventHandler<Event> newTabEvent;
        newTabEvent = new EventHandler<Event>() {

            public void handle(Event e) {
                if (newtab.isSelected()) {

                    // create Tab
                    Tab tab = new Tab("Tab_" + (int) (counter));
                    //Create a new canvas that is associated with this specific tab
                    Canvas canvas = new Canvas(1220, 620);
                    canvases.add(canvas);
                    //Makes sure the tool bar is pointing to the correct canvas
                    tb.setCanvas(canvas);
                    //Create a new graphics context using the newly created canvas
                    GraphicsContext gc = canvas.getGraphicsContext2D();
                    graphicsContexts.add(gc);
                    //Makes sure the tool bar is pointing to the correct graphics context
                    tb.setGraphicsContext(gc);
                    //Creates a new writable image 
                    WritableImage tabWim = new WritableImage(1200, 600);
                    wims.add(tabWim);

                    //Makes sure the menu bar and the tool bar are pointing to the same writable image
                    mb.setWim(tabWim);
                    tb.setWim(tabWim);

                    //Creates a new image view for this specific tab
                    ImageView tabImgView = new ImageView();
                    tabImgView.setFitHeight(600);
                    tabImgView.setFitWidth(1200);
                    tabImgView.setPreserveRatio(true);
                    imgViews.add(tabImgView);

                    //Creates boolean to tell if the current tab has been saved
                    Boolean currentTabSaved = tb.beenDrawnOn();
                    tabSaved.add(currentTabSaved);

                    mb.setSaved(currentTabSaved);

                    //Makes sure the menu bar is pointing to the correct image view
                    mb.setImageView(tabImgView);
                    //Stacks the image view and canvas together so they can both be displayed on the tab
                    StackPane tabStack = new StackPane();
                    tabStack.getChildren().addAll(mb.getImageView(), tb.getCanvas());

                    //increment tab counter
                    counter++;

                    // set content of the tab
                    tab.setContent(tabStack);
                    //Setting the Pane to allow for snapshot to be used
                    mb.setPane(tabStack);
                    tb.setPane(tabStack);
                    // create event handler for when an existing tab is selected

                    EventHandler<Event> ExistingTabEvent;
                    ExistingTabEvent = new EventHandler<Event>() {

                        public void handle(Event e) {
                            //Gets the tab index so the pointers can be set correctly
                            String tabIndex = tab.getText().substring(tab.getText().length() - 1);
                            int index = Integer.parseInt(tabIndex);
                            //Sets the pointers to the writable image, canvas, image view and graphics context for the selected tab
                            mb.setWim(wims.get(index));
                            tb.setWim(wims.get(index));
                            tb.setCanvas(canvases.get(index));
                            mb.setImageView(imgViews.get(index));
                            tb.setGraphicsContext(graphicsContexts.get(index));
                            mb.setSaved(tabSaved.get(index));

                        }
                    };
                    //Sets the event handler for the newly created tab
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

        EventHandler autoSaveEvent = new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                drawnOn = paint.toolBar.drawnOn;
                WritableImage wim = paint.toolBar.wim;
                if (paint.menuBar.autoSaveOn){
                    try {
                        if (!drawnOn) {
                            mb.setSaved(true);
                            ImageIO.write(SwingFXUtils.fromFXImage(paint.menuBar.imageView.getImage(), null), "png", file);
                        } else {
                            RenderedImage renderedImage = SwingFXUtils.fromFXImage(wim, null);
                            ImageIO.write(renderedImage, "png", file);
                        }
                    } catch (Exception e) {
                        System.out.println("Invalid selection.");
                    }
                }
                System.out.println(tb.getLogMessage());
                Logger.getGlobal().log(Level.FINE, tb.getLogMessage());
            }
        };

        Timeline autoSave = new Timeline(new KeyFrame(Duration.seconds(25), autoSaveEvent));
        autoSave.setCycleCount(Animation.INDEFINITE);
        autoSave.play();
        // set event handler to the tab 
        newtab.setOnSelectionChanged(newTabEvent);

        // add newtab 
        tabPane.getTabs().add(newtab);

        // Sets up canvas 
        StackPane scrollStack = new StackPane();
        stack.getChildren().addAll(mb.getImageView(), tb.getCanvas());

        //Adding scroll capability (Broken with the implementation of tabs)
        mb.setPane(stack);
        tb.setPane(stack);
        scrollStack.getChildren().add(tabPane);
        scroll.setContent(scrollStack);

        //Adding menu bar and the canvas to the window
        grid.add(mb, 0, 0);
        grid.addRow(1, tb);
        grid.addRow(2, tabPane);
        grid.add(scrollStack, 0, 3);
        // Displays the window with everything that has been created
        Scene scene = new Scene(grid, 1500, 900);
        primaryStage.setTitle("Paint");
        primaryStage.setScene(scene);
        primaryStage.show();
        Platform.setImplicitExit(false);
        //Event Handler for when the window is closed 
        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                event.consume();

                if (!paint.menuBar.saved) {
                    GridPane grid = new GridPane();
                    grid.setPadding(new Insets(25, 25, 25, 25));
                    Stage tempstage = new Stage();
                    Label label = new Label("Do you wish to save?");
                    Button button = new Button("Save");
                    grid.add(label, 0, 0);
                    grid.add(button, 0, 1);
                    Scene stageScene = new Scene(grid);
                    tempstage.setScene(stageScene);
                    tempstage.show();
                    button.setOnAction(new EventHandler<ActionEvent>() {
                        @Override
                        public void handle(ActionEvent event) {

                            try {
                                if (!drawnOn) {
                                    paint.menuBar.saved = true;
                                    ImageIO.write(SwingFXUtils.fromFXImage(paint.menuBar.imageView.getImage(), null), "png", file);
                                } else {
                                    FileChooser sFileChooser = new FileChooser();
                                    sFileChooser.setTitle("Save As..");
                                    sFileChooser.getExtensionFilters().addAll(
                                            new FileChooser.ExtensionFilter("*.png", "*.jpg"));
                                    file = sFileChooser.showSaveDialog(primaryStage);
                                    if (file != null) {
                                        drawnOn = paint.toolBar.drawnOn;
                                        paint.menuBar.wim = paint.toolBar.wim;
                                        paint.menuBar.pane = paint.toolBar.pane;
                                        paint.menuBar.undoStack.push(wim);
                                        try {
                                            if (!drawnOn) {
                                                paint.menuBar.saved = true;
                                                ImageIO.write(SwingFXUtils.fromFXImage(paint.menuBar.imageView.getImage(), null), "png", file);
                                            } else {
                                                RenderedImage renderedImage = SwingFXUtils.fromFXImage(wim, null);
                                                ImageIO.write(renderedImage, "png", file);
                                            }
                                        } catch (Exception e) {
                                            System.out.println("Invalid selection.");
                                        }
                                    }
                                }
                            } catch (Exception e) {
                                System.out.println("Invalid selection.");
                            }

                        }
                    });
                    tempstage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                        @Override
                        public void handle(WindowEvent event) {
                            tempstage.close();
                            primaryStage.close();
                            Platform.exit();
                        }
                    });
                } else {
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
