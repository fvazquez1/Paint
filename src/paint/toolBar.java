/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package paint;

import java.util.Stack;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
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
import javafx.scene.shape.Rectangle;
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
    static Color currentColor, currentFillColor;
    static double currentWidth;
    static boolean straightLineSelected, shapeSelected, colorGrabbed;
    static Pair<Double,Double> initialClick;
    static Pane pane;
    static ToggleGroup toggleGroup;
    static String currentShape = "";
    static Stack<WritableImage> undoStack;
    
    public toolBar(Stage stage,ImageView imageView){
        this.stage = stage; 
        currentColor = Color.BLACK;
        canDraw = false;
        this.imageView = imageView;
        toggleGroup = new ToggleGroup();
        this.getItems().addAll(addDrawLine(),addFreeDrawLine(),addRectangle(),
                addSquare(), addEllipse(), addCircle(),addLineColor(),
                addFillColor(),addColorGrab(),addLineWidth());
        canvas = new Canvas(imageView.getFitWidth(),imageView.getFitHeight());
        gc = canvas.getGraphicsContext2D();
        wim = new WritableImage((int)imageView.getFitWidth()+20,(int)imageView.getFitHeight()+20);
    }
    
    private ToggleButton addDrawLine() { 
        ToggleButton drawLine = new ToggleButton("Draw Line");
        drawLine.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event){
                drawnOn = true;
                shapeSelected = false;
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
                        canvas.setWidth(paint.menuBar.imageView.getFitWidth());
                        canvas.setHeight(paint.menuBar.imageView.getFitHeight());
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
                                undoStack.push(wim);
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
                shapeSelected = false; 
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
                        if (canDraw && !straightLineSelected && !shapeSelected){
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
                        if (canDraw && !straightLineSelected && !shapeSelected){
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
                        undoStack.push(pane.snapshot(null, wim));
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
        if(colorGrabbed){
            colorpicker.setValue(currentColor);
        }
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
    
    private Button addFillColor(){
        Button fillColor = new Button("Select fill Color"); 
        ColorPicker colorpicker = new ColorPicker(Color.TRANSPARENT);
        fillColor.setOnAction(new EventHandler<ActionEvent>() {
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
                        currentFillColor = colorpicker.getValue();
                    }
                });
            }
        });
        return fillColor;
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
    
    private ToggleButton addRectangle(){
        ToggleButton rectangle = new ToggleButton("Draw Rectangle");
        
        rectangle.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event){
                canDraw= true;
                shapeSelected = true;
                straightLineSelected = false;
                currentShape = "rect";
                canvas.setOnMousePressed( 
                    new EventHandler<MouseEvent>(){

                        @Override
                        public void handle(MouseEvent event) {
                            canvas.setWidth(imageView.getFitWidth());
                            canvas.setHeight(imageView.getFitHeight());
                            if (canDraw && !straightLineSelected && shapeSelected){
                                initialClick = new Pair(event.getX(),event.getY());
                            }
                        }
                    });

                    canvas.addEventHandler(MouseEvent.MOUSE_RELEASED, 
                            new EventHandler<MouseEvent>(){

                            @Override
                            public void handle(MouseEvent event) {
                                if (canDraw && !straightLineSelected && shapeSelected && currentShape == "rect"){
                                    gc.setLineWidth(currentWidth);
                                    gc.setStroke(currentColor);
                                    gc.setFill(currentFillColor);
                                    if ((event.getX()-initialClick.getKey() >= 0.0) && (event.getY()-initialClick.getValue()>=0.0)){
                                        if(currentFillColor != null){
                                            gc.fillRect(initialClick.getKey(), initialClick.getValue(), (event.getX()-initialClick.getKey()), (event.getY()-initialClick.getValue()));
                                        }
                                        else{
                                            gc.strokeRect(initialClick.getKey(), initialClick.getValue(), (event.getX()-initialClick.getKey()), (event.getY()-initialClick.getValue()));
                                        }
                                    }
                                    else{
                                        if((event.getX()-initialClick.getKey() < 0) && (event.getY()-initialClick.getValue()>=0)){
                                            double originalX = initialClick.getKey();
                                            double originalY = initialClick.getValue();
                                            initialClick = new Pair(event.getX(),originalY);
                                            if(currentFillColor != null){
                                                gc.fillRect(initialClick.getKey(), initialClick.getValue(), (originalX-initialClick.getKey()), (event.getY()-initialClick.getValue()));
                                            }
                                            else{
                                                gc.strokeRect(initialClick.getKey(), initialClick.getValue(), (originalX-initialClick.getKey()), (event.getY()-initialClick.getValue()));
                                            }
                                        }
                                        else{
                                            if((event.getX()-initialClick.getKey() >= 0) && (event.getY()-initialClick.getValue()<0)){
                                                double originalX = initialClick.getKey();
                                                double originalY = initialClick.getValue();
                                                initialClick = new Pair(originalX,event.getY());
                                                if(currentFillColor != null){
                                                    gc.fillRect(initialClick.getKey(), initialClick.getValue(), (event.getX()-initialClick.getKey()), (originalY-initialClick.getValue()));
                                                }
                                                else{
                                                    gc.strokeRect(initialClick.getKey(), initialClick.getValue(), (event.getX()-initialClick.getKey()), (originalY-initialClick.getValue()));
                                                }
                                            }
                                            else{
                                                if((event.getX()-initialClick.getKey() < 0) && (event.getY()-initialClick.getValue()<0)){
                                                    double originalX = initialClick.getKey();
                                                    double originalY = initialClick.getValue();
                                                    initialClick = new Pair(event.getX(),event.getY());
                                                    if(currentFillColor != null){
                                                        gc.fillRect(initialClick.getKey(), initialClick.getValue(), (originalX-initialClick.getKey()), (originalY-initialClick.getValue()));
                                                    }
                                                    else{
                                                        gc.strokeRect(initialClick.getKey(), initialClick.getValue(), (originalX-initialClick.getKey()), (originalY-initialClick.getValue()));
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    
                                }
                                pane.snapshot(null, wim);
                                undoStack.push(wim);
                            }
                    });
                
            }
        });
        
        rectangle.setToggleGroup(toggleGroup);
        return rectangle;
    }
    
    private ToggleButton addSquare(){
        ToggleButton square = new ToggleButton("Draw Square");
        
        square.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event){
                canDraw= true;
                shapeSelected = true;
                straightLineSelected = false;
                currentShape = "square";
                canvas.setOnMousePressed( 
                        new EventHandler<MouseEvent>(){

                            @Override
                            public void handle(MouseEvent event) {
                                canvas.setWidth(imageView.getFitWidth());
                                canvas.setHeight(imageView.getFitHeight());
                                if (canDraw && !straightLineSelected && shapeSelected){
                                    initialClick = new Pair(event.getX(),event.getY());
                                }
                            }
                        });
                canvas.addEventHandler(MouseEvent.MOUSE_RELEASED, 
                    new EventHandler<MouseEvent>(){

                    @Override
                    public void handle(MouseEvent event) {
                        if (canDraw && !straightLineSelected && shapeSelected&& currentShape == "square"){
                                gc.setLineWidth(currentWidth);
                                gc.setStroke(currentColor);
                                gc.setFill(currentFillColor);
                                if ((event.getX()-initialClick.getKey() >= 0) && (event.getY()-initialClick.getValue()>=0)){
                                    if((event.getX()-initialClick.getKey())>(event.getY()-initialClick.getValue())){
                                        double lesser = event.getY()-initialClick.getValue();
                                        if(currentFillColor != null){
                                            gc.fillRect(initialClick.getKey(), initialClick.getValue(), lesser, lesser);
                                        }
                                        else{
                                            gc.strokeRect(initialClick.getKey(), initialClick.getValue(), lesser, lesser);
                                        }
                                    }
                                    else{
                                        double lesser = event.getX()-initialClick.getValue();
                                        if(currentFillColor != null){
                                            gc.fillRect(initialClick.getKey(), initialClick.getValue(), lesser, lesser);
                                        }
                                        else{
                                            gc.strokeRect(initialClick.getKey(), initialClick.getValue(), lesser, lesser);
                                        }
                                    }
                                }
                                else{
                                    if((event.getX()-initialClick.getKey() < 0) && (event.getY()-initialClick.getValue()>=0)){
                                        double originalX = initialClick.getKey();
                                        double originalY = initialClick.getValue();

                                        double width = Math.abs(originalX - initialClick.getKey());
                                        double height = Math.abs(originalY - initialClick.getValue());

                                        if(width < height){
                                            if(currentFillColor != null){
                                                gc.fillRect(event.getX(), event.getY(), width, width);
                                            }
                                            else{
                                                gc.strokeRect(event.getX(), event.getY(), width, width);
                                            }
                                        }
                                        else{
                                            if(currentFillColor != null){
                                                gc.fillRect(event.getX(), event.getY(), height, height);
                                            }
                                            else{
                                                gc.strokeRect(event.getX(), event.getY(), height, height);
                                            }
                                        }
                                    }    
                                    else{
                                        if((event.getX()-initialClick.getKey() >= 0) && (event.getY()-initialClick.getValue()<0)){
                                            double originalX = initialClick.getKey();
                                            double originalY = initialClick.getValue();
                                            initialClick = new Pair(originalX,event.getY());
                                            double width = Math.abs(originalX - initialClick.getKey());
                                            double height = Math.abs(originalY - initialClick.getValue());

                                            if(width < height){
                                                if(currentFillColor != null){
                                                    gc.fillRect(initialClick.getKey(), initialClick.getValue(), width, width);
                                                }
                                                else{
                                                    gc.strokeRect(initialClick.getKey(), initialClick.getValue(), width, width);
                                                }
                                            }
                                            else{
                                                if(currentFillColor != null){
                                                    gc.fillRect(initialClick.getKey(), initialClick.getValue(), height, height);
                                                }
                                                else{
                                                    gc.strokeRect(initialClick.getKey(), initialClick.getValue(), height, height);
                                                }
                                            }
                                        }
                                        else{
                                            if((event.getX()-initialClick.getKey() < 0) && (event.getY()-initialClick.getValue()<0)){
                                                double originalX = initialClick.getKey();
                                                double originalY = initialClick.getValue();
                                                initialClick = new Pair(event.getX(),event.getY());
                                                double width = Math.abs(originalX - initialClick.getKey());
                                                double height = Math.abs(originalY - initialClick.getValue());

                                                if(width < height){
                                                    gc.strokeRect(initialClick.getKey(), initialClick.getValue(), width, width);
                                                }
                                                else{
                                                    gc.strokeRect(initialClick.getKey(), initialClick.getValue(), height, height);
                                                }
                                            }
                                        }
                                    }
                                }
                                
                                initialClick = new Pair(0,0);
                            }
                            pane.snapshot(null, wim);
                            undoStack.push(wim);
                        }
                });
            }
        });
        
        square.setToggleGroup(toggleGroup);
        return square;
    }
    
   private ToggleButton addEllipse(){
       ToggleButton ellipse = new ToggleButton("Draw Ellipse");
       
       ellipse.setOnAction(new EventHandler<ActionEvent> (){
           @Override
           public void handle(ActionEvent event){
               canDraw= true;
               shapeSelected = true;
               straightLineSelected = false;
               currentShape = "ellipse";
                
               canvas.setOnMousePressed( 
                    new EventHandler<MouseEvent>(){

                        @Override
                        public void handle(MouseEvent event) {
                            canvas.setWidth(imageView.getFitWidth());
                            canvas.setHeight(imageView.getFitHeight());
                            if (canDraw && !straightLineSelected && shapeSelected){
                                initialClick = new Pair(event.getX(),event.getY());
                            }
                        }
                });
               
               canvas.addEventHandler(MouseEvent.MOUSE_RELEASED, 
                        new EventHandler<MouseEvent>(){

                        @Override
                        public void handle(MouseEvent event) {
                            if (canDraw && !straightLineSelected && shapeSelected && currentShape == "ellipse"){
                                gc.setLineWidth(currentWidth);
                                gc.setStroke(currentColor);
                                gc.setFill(currentFillColor);
                                if ((event.getX()-initialClick.getKey() >= 0.0) && (event.getY()-initialClick.getValue()>=0.0)){
                                    if(currentFillColor != null){
                                        gc.fillOval(initialClick.getKey(), initialClick.getValue(), (event.getX()-initialClick.getKey()), (event.getY()-initialClick.getValue()));
                                    }
                                    else{
                                        gc.strokeOval(initialClick.getKey(), initialClick.getValue(), (event.getX()-initialClick.getKey()), (event.getY()-initialClick.getValue()));
                                    }
                                }
                                else{
                                    if((event.getX()-initialClick.getKey() < 0) && (event.getY()-initialClick.getValue()>=0)){
                                        double originalX = initialClick.getKey();
                                        double originalY = initialClick.getValue();
                                        initialClick = new Pair(event.getX(),originalY);
                                        if(currentFillColor != null){
                                            gc.fillOval(initialClick.getKey(), initialClick.getValue(), (originalX-initialClick.getKey()), (event.getY()-initialClick.getValue()));
                                        }
                                        else{
                                            gc.strokeOval(initialClick.getKey(), initialClick.getValue(), (originalX-initialClick.getKey()), (event.getY()-initialClick.getValue()));
                                        }
                                    }
                                    else{
                                        if((event.getX()-initialClick.getKey() >= 0) && (event.getY()-initialClick.getValue()<0)){
                                            double originalX = initialClick.getKey();
                                            double originalY = initialClick.getValue();
                                            initialClick = new Pair(originalX,event.getY());
                                            if(currentFillColor != null){
                                                gc.fillOval(initialClick.getKey(), initialClick.getValue(), (event.getX()-initialClick.getKey()), (originalY-initialClick.getValue()));
                                            }
                                            else{
                                                gc.strokeOval(initialClick.getKey(), initialClick.getValue(), (event.getX()-initialClick.getKey()), (originalY-initialClick.getValue()));
                                            }
                                        }
                                        else{
                                            if((event.getX()-initialClick.getKey() < 0) && (event.getY()-initialClick.getValue()<0)){
                                                double originalX = initialClick.getKey();
                                                double originalY = initialClick.getValue();
                                                initialClick = new Pair(event.getX(),event.getY());
                                                if(currentFillColor != null){
                                                    gc.fillOval(initialClick.getKey(), initialClick.getValue(), (originalX-initialClick.getKey()), (originalY-initialClick.getValue()));
                                                }
                                                else{
                                                    gc.strokeOval(initialClick.getKey(), initialClick.getValue(), (originalX-initialClick.getKey()), (originalY-initialClick.getValue()));
                                                }
                                            }
                                        }
                                    }
                                }
                                
                            }
                            pane.snapshot(null, wim);
                            undoStack.push(wim);
                        }
                    });
           }
       });
       
       
       ellipse.setToggleGroup(toggleGroup);
       return ellipse;
   }
    
   private ToggleButton addCircle(){
        ToggleButton circle = new ToggleButton("Draw Circle");
        
        circle.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event){
                canDraw= true;
                shapeSelected = true;
                straightLineSelected = false;
                currentShape = "circle";
                canvas.setOnMousePressed( 
                        new EventHandler<MouseEvent>(){

                            @Override
                            public void handle(MouseEvent event) {
                                canvas.setWidth(imageView.getFitWidth());
                                canvas.setHeight(imageView.getFitHeight());
                                if (canDraw && !straightLineSelected && shapeSelected){
                                    initialClick = new Pair(event.getX(),event.getY());
                                }
                            }
                        });
                canvas.addEventHandler(MouseEvent.MOUSE_RELEASED, 
                    new EventHandler<MouseEvent>(){

                    @Override
                    public void handle(MouseEvent event) {
                        if (canDraw && !straightLineSelected && shapeSelected&& currentShape == "circle"){
                                gc.setLineWidth(currentWidth);
                                gc.setStroke(currentColor);
                                gc.setFill(currentFillColor);
                                if ((event.getX()-initialClick.getKey() >= 0) && (event.getY()-initialClick.getValue()>=0)){
                                    if((event.getX()-initialClick.getKey())>(event.getY()-initialClick.getValue())){
                                        double lesser = event.getY()-initialClick.getValue();
                                        if(currentFillColor != null){
                                            gc.fillOval(initialClick.getKey(), initialClick.getValue(), lesser, lesser);
                                        }
                                        else{
                                            gc.strokeOval(initialClick.getKey(), initialClick.getValue(), lesser, lesser);
                                        }
                                    }
                                    else{
                                        double lesser = event.getX()-initialClick.getValue();
                                        if(currentFillColor != null){
                                            gc.fillOval(initialClick.getKey(), initialClick.getValue(), lesser, lesser);
                                        }
                                        else{
                                            gc.strokeOval(initialClick.getKey(), initialClick.getValue(), lesser, lesser);
                                        }
                                    }
                                }
                                else{
                                    if((event.getX()-initialClick.getKey() < 0) && (event.getY()-initialClick.getValue()>=0)){
                                        double originalX = initialClick.getKey();
                                        double originalY = initialClick.getValue();

                                        double width = Math.abs(originalX - initialClick.getKey());
                                        double height = Math.abs(originalY - initialClick.getValue());

                                        if(width < height){
                                            if(currentFillColor != null){
                                                gc.fillOval(event.getX(), event.getY(), width, width);
                                            }
                                            else{
                                                gc.strokeOval(event.getX(), event.getY(), width, width);
                                            }
                                        }
                                        else{
                                            if(currentFillColor != null){
                                                gc.fillOval(event.getX(), event.getY(), height, height);
                                            }
                                            else{
                                                gc.strokeOval(event.getX(), event.getY(), height, height);
                                            }
                                        }
                                    }    
                                    else{
                                        if((event.getX()-initialClick.getKey() >= 0) && (event.getY()-initialClick.getValue()<0)){
                                            double originalX = initialClick.getKey();
                                            double originalY = initialClick.getValue();
                                            initialClick = new Pair(originalX,event.getY());
                                            double width = Math.abs(originalX - initialClick.getKey());
                                            double height = Math.abs(originalY - initialClick.getValue());

                                            if(width < height){
                                                if(currentFillColor != null){
                                                    gc.fillOval(initialClick.getKey(), initialClick.getValue(), width, width);
                                                }
                                                else{
                                                    gc.strokeOval(initialClick.getKey(), initialClick.getValue(), width, width);
                                                }
                                            }
                                            else{
                                                if(currentFillColor != null){
                                                    gc.fillOval(initialClick.getKey(), initialClick.getValue(), height, height);
                                                }
                                                else{
                                                    gc.strokeOval(initialClick.getKey(), initialClick.getValue(), height, height);
                                                }
                                            }
                                        }
                                        else{
                                            if((event.getX()-initialClick.getKey() < 0) && (event.getY()-initialClick.getValue()<0)){
                                                double originalX = initialClick.getKey();
                                                double originalY = initialClick.getValue();
                                                initialClick = new Pair(event.getX(),event.getY());
                                                double width = Math.abs(originalX - initialClick.getKey());
                                                double height = Math.abs(originalY - initialClick.getValue());

                                                if(width < height){
                                                    if(currentFillColor != null){
                                                        gc.fillOval(initialClick.getKey(), initialClick.getValue(), width, width);
                                                    }
                                                    else{
                                                        gc.strokeOval(initialClick.getKey(), initialClick.getValue(), width, width);
                                                    }
                                                }
                                                else{
                                                    if(currentFillColor != null){
                                                        gc.fillOval(initialClick.getKey(), initialClick.getValue(), height, height);
                                                    }
                                                    else{
                                                        gc.strokeOval(initialClick.getKey(), initialClick.getValue(), height, height);
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                                
                                initialClick = new Pair(0,0);
                            }
                            pane.snapshot(null, wim);
                            undoStack.push(wim);
                        }
                });
            }
        });
        
        circle.setToggleGroup(toggleGroup);
        return circle;
    }
   
   private ToggleButton addColorGrab(){
       ToggleButton colorGrab = new ToggleButton("Grab Color");
       
       colorGrab.setOnAction(new EventHandler<ActionEvent>() {
           @Override
           public void handle(ActionEvent event){
               canDraw= false;
               canvas.setOnMouseClicked(new EventHandler<MouseEvent>(){
                   @Override 
                   public void handle(MouseEvent event){
                       currentColor = wim.getPixelReader().getColor((int)event.getX(), (int)event.getY());
                       colorGrabbed = true;
                       
                   }
               });
           }
       });
       colorGrab.setToggleGroup(toggleGroup);
       return colorGrab;
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
