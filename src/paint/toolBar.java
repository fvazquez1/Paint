/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package paint;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Slider;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.ToolBar;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Pair;

/**
 *
 * @author Francisco Vazquez
 */
public class toolBar extends ToolBar{
    static Stage stage;
    static boolean canDraw,drawnOn;
    static ImageView imageView;
    static Canvas canvas;
    static GraphicsContext gc;
    static WritableImage wim; 
    static Color currentColor;
    static double currentWidth;
    static boolean straightLineSelected;
    static Pair<Double,Double> initialClick;
    static Pane pane;
    static ToggleGroup toggleGroup;
    public toolBar(Stage stage,ImageView imageView){
        this.stage = stage; 
        currentColor = Color.BLACK;
        canDraw = false;
        this.imageView = imageView;
        canvas = new Canvas(600,500);
        gc = canvas.getGraphicsContext2D();
        wim = new WritableImage((int)imageView.getFitWidth()+20,(int)imageView.getFitHeight()+20);
        toggleGroup = new ToggleGroup();
        this.getItems().addAll(addDrawLine(),addFreeDrawLine(),addLineColor(),addLineWidth());
    }
    
    private ToggleButton addDrawLine() { 
        ToggleButton drawLine = new ToggleButton("Draw Line");
        
        drawLine.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event){
                drawnOn = true;
                if((ToggleButton) toggleGroup.getSelectedToggle() != null){
                    canDraw= true;
                    straightLineSelected = true;
                }
                else{
                    canDraw= false;
                    straightLineSelected = false;
                }
                canvas.setOnMousePressed( 
                new EventHandler<MouseEvent>(){

                    @Override
                    public void handle(MouseEvent event) {
                        canvas.setWidth(imageView.getFitWidth());
                        canvas.setHeight(imageView.getFitHeight());
                        if (canDraw && straightLineSelected){
                            initialClick = new Pair(event.getX(),event.getY());
                        }
                    }
                });

                canvas.addEventHandler(MouseEvent.MOUSE_RELEASED, 
                        new EventHandler<MouseEvent>(){

                        @Override
                        public void handle(MouseEvent event) {
                            if (canDraw && straightLineSelected){
                                gc.setLineWidth(currentWidth);
                                gc.setStroke(currentColor);
                                gc.strokeLine(initialClick.getKey(), initialClick.getValue(), event.getX(), event.getY());
                                pane.snapshot(null, wim);
                            }
                        }
                });
                
            }
        });
                
        drawLine.setToggleGroup(toggleGroup);
        
        return drawLine;
    }
    
    private ToggleButton addFreeDrawLine() {
        ToggleButton freeLine = new ToggleButton("Free Draw Line");
        freeLine.setOnAction(new EventHandler<ActionEvent>() {
            @Override // Resets the window to show nothing
            public void handle(ActionEvent event){
                drawnOn = true;
                straightLineSelected = false;
                if((ToggleButton) toggleGroup.getSelectedToggle() != null){
                    canDraw= true;
                }
                else{
                    canDraw= false;
                }
                canvas.setOnMousePressed( 
                new EventHandler<MouseEvent>(){

                    @Override
                    public void handle(MouseEvent event) {
                        canvas.setWidth(imageView.getFitWidth());
                        canvas.setHeight(imageView.getFitHeight());
                        if (canDraw){
                            gc.beginPath();
                            gc.setLineWidth(currentWidth);
                            gc.setStroke(currentColor);
                            gc.moveTo(event.getX(), event.getY());
                            gc.stroke();
                        }
                    }
                });

                canvas.addEventHandler(MouseEvent.MOUSE_DRAGGED, 
                        new EventHandler<MouseEvent>(){

                    @Override
                    public void handle(MouseEvent event) {
                        if (canDraw && !straightLineSelected){
                            gc.setFill(currentColor);
                            gc.lineTo(event.getX(), event.getY());
                            gc.stroke();
                        }
                    }
                });
        
                canvas.addEventHandler(MouseEvent.MOUSE_RELEASED, new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent event) {
                        pane.snapshot(null, wim);
                        //mb.setWim(wim);
                    }
                });
            }
        });

        freeLine.setToggleGroup(toggleGroup);
        
        return freeLine;
    }
    
    private Button addLineColor(){
        Button lineColor = new Button("Select Line Color"); 
        ColorPicker colorpicker = new ColorPicker(Color.BLACK);
        lineColor.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event){
                StackPane stack = new StackPane();
                stack.setPadding(new Insets(25,25,25,25));
                Stage tempstage = new Stage();
                stack.getChildren().add(colorpicker);
                Scene stageScene = new Scene(stack);
                tempstage.setScene(stageScene);
                
                tempstage.show();
                tempstage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                    @Override
                    public void handle(WindowEvent event){
                        currentColor = colorpicker.getValue();
                    }
                });
            }
        });
        return lineColor;
    }
    
    private Button addLineWidth(){
        Button lineWidth = new Button("Set Line Width");
        Slider slider = new Slider(1,20,1);
        slider.setMajorTickUnit(1.0);
        slider.setSnapToTicks(true);
        slider.setShowTickMarks(true);
        slider.setShowTickLabels(true);
        
        lineWidth.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event){
                StackPane stack = new StackPane();
                stack.setPadding(new Insets(25,25,25,25));
                Stage tempstage = new Stage();
                stack.getChildren().add(slider);
                Scene stageScene = new Scene(stack);
                tempstage.setScene(stageScene);
                
                tempstage.show();
                tempstage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                    @Override
                    public void handle(WindowEvent event){
                        currentWidth = slider.getValue();
                    }
                });
            }
        });
        
        return lineWidth;
    }
    
    public void setWim(WritableImage wim){
        this.wim = wim;
    }
    
    public void setPane(Pane pane){
        this.pane = pane;
    }
    
    public Canvas getCanvas(){
        return canvas;
    }
    
    public WritableImage getWim(){
        return wim;
    }
    public Boolean beenDrawnOn(){
        return drawnOn;
    }
}
