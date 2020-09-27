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
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.ToolBar;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Pair;

/**
 * Custom ToolBar class for Paint.java
 * <P>
 * Anything housed in the ToolBar is found in this class. All ToggleButtons, 
 * necessary EventHandlers, and necessary variables are in this class. The toolBar
 * handles all drawing capabilities.
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
    static boolean straightLineSelected, shapeSelected, colorGrabbed, eraserSelected;
    static Pair<Double,Double> initialClick;
    static Pane pane;
    static ToggleGroup toggleGroup;
    static String currentShape = "";
    static String currentText = "";
    static Stack<WritableImage> undoStack;
    static Integer nSides;
    /**
     * Constructor for toolBar object. 
     * @param stage Stage to display the toolBar on
     * @param imageView ImageView being used by the stage
     */
    public toolBar(Stage stage,ImageView imageView){
        this.stage = stage; 
        currentColor = Color.BLACK;
        canDraw = false;
        this.imageView = imageView;
        toggleGroup = new ToggleGroup();
        this.getItems().addAll(addEraser(), addDrawLine(),addFreeDrawLine(),addRectangle(),
                addTriangle(), addSquare(), addEllipse(), addCircle(),addPolygon(),
                addLineColor(),addFillColor(),addColorGrab(),addLineWidth(), 
                addTextBox());
        canvas = new Canvas(imageView.getFitWidth(),imageView.getFitHeight());
        gc = canvas.getGraphicsContext2D();
        wim = new WritableImage((int)imageView.getFitWidth()+20,(int)imageView.getFitHeight()+20);
    }
    public toolBar(){
        
    }
    /**
     * ToggleButton for "Draw Line" is created as well as the corresponding EventHandler.
     * The EventHandler handles the drawing of a straight line on the canvas. 
     * @return "Draw Line" ToggleButton to be added to the toolBar
     */
    private ToggleButton addDrawLine() { 
        ToggleButton drawLine = new ToggleButton("Draw Line");
        drawLine.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event){
                drawnOn = true;
                shapeSelected = false;
                eraserSelected = false;
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
                        gc.setStroke(currentColor);
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
                                gc = paint.toolBar.gc;
                                gc.strokeLine(initialClick.getKey(), initialClick.getValue(), event.getX(), event.getY());
                                pane.snapshot(null, wim);
                                
                                //undoStack.push(wim);
                            }
                        }
                });
                
            }
        });
                
        drawLine.setToggleGroup(toggleGroup);
        
        return drawLine;
    }
    /**
     * ToggleButton for "Free Draw Line" is created as well as the corresponding EventHandler.
     * The EventHandler handles the free drawing of a line on the canvas. The line
     * drawing starts from the point where the mouse is clicked, and is drawn 
     * to the position of the mouse as long as it is being held down. 
     * @return "Free Draw Line" ToggleButton to be added to the toolBar
     */
    private ToggleButton addFreeDrawLine() {
        ToggleButton freeLine = new ToggleButton("Free Draw Line");
        freeLine.setOnAction(new EventHandler<ActionEvent>() {
            @Override // Resets the window to show nothing
            public void handle(ActionEvent event){
                drawnOn = true;
                straightLineSelected = false;
                shapeSelected = false;
                eraserSelected = false;
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
                        //undoStack.push(pane.snapshot(null, wim));
                        //mb.setWim(wim);
                    }
                });
            }
        });

        freeLine.setToggleGroup(toggleGroup);
        
        return freeLine;
    }
    /**
     * Button for "Select Line Color" is created as well as the corresponding EventHandler.
     * The EventHandler handles the changing of the line color by opening a
     * separate window with a ColorChooser. The color is only saved when the 
     * window that was opened is closed.
     * @return "Select Line Color" ToggleButton to be added to the toolBar
     */
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
    /**
     * Button for "Select Fill Color" is created as well as the corresponding EventHandler.
     * The EventHandler handles the changing of the fill color by opening a
     * separate window with a ColorChooser. The color is only saved when the 
     * window that was opened is closed.
     * @return "Select Fill Color" ToggleButton to be added to the toolBar
     */
    private Button addFillColor(){
        Button fillColor = new Button("Select Fill Color"); 
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
    /**
     * Button for "Set Line Width" is created as well as the corresponding EventHandler.
     * The EventHandler handles the changing of the line width by opening a
     * separate window with a Slider. The color is only saved when the 
     * window that was opened is closed.
     * @return "Select Line Color" ToggleButton to be added to the toolBar
     */
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
                stack.setPadding(new Insets(15,15,15,15));
                Stage tempstage = new Stage();
                stack.getChildren().add(slider);
                Scene stageScene = new Scene(stack);
                tempstage.setWidth(425);
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
    /**
     * ToggleButton for "Insert Text" is created as well as the corresponding EventHandler.
     * The EventHandler handles the setting of the desired text to be printed by 
     * opening a separate window with a TextArea for the user to input their desired
     * text. The text is only saved when the window that was opened is closed.
     * @return "Insert Text" ToggleButton to be added to the toolBar
     */
    private ToggleButton addTextBox(){
        ToggleButton txtBox = new ToggleButton("Insert Text");
        
        txtBox.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event){
                canDraw = false;
                shapeSelected = false;
                straightLineSelected = false;
                
                Stage tempstage = new Stage();
                tempstage.setTitle("Text To Be Written");

                TextArea textArea = new TextArea();
                VBox vbox = new VBox(textArea);

                Scene stageScene = new Scene(vbox);
                tempstage.setScene(stageScene);

                tempstage.show();

                tempstage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                    @Override
                    public void handle(WindowEvent event){
                        currentText = textArea.getText();
                    }
                });
                
                canvas.setOnMousePressed(new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent event){
                        canvas.setWidth(imageView.getFitWidth());
                        canvas.setHeight(imageView.getFitHeight());
                        initialClick = new Pair(event.getX(),event.getY());
                        
                        
                        gc.setStroke(currentColor);
                        gc.setFont(Font.font(currentWidth+12));
                        gc.strokeText(currentText, initialClick.getKey(), initialClick.getValue());
                    }
                });
                
            }
        });
        
        txtBox.setToggleGroup(toggleGroup);
        
        return txtBox;
    }
    /**
     * ToggleButton for "Draw Rectangle" is created as well as the corresponding EventHandler.
     * The EventHandler handles the drawing of a rectangle on the canvas. The 
     * rectangle is drawn by clicking and dragging. The rectangle will be drawn 
     * staring where the mouse is clicked, then drawn to the point where the mouse 
     * is released. 
     * @return "Draw Rectangle" ToggleButton to be added to the toolBar
     */
    private ToggleButton addRectangle(){
        ToggleButton rectangle = new ToggleButton("Draw Rectangle");
        
        rectangle.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event){
                canDraw= true;
                shapeSelected = true;
                straightLineSelected = false;
                currentShape = "rect";
                eraserSelected = false;
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
                                //undoStack.push(wim);
                            }
                    });
                
            }
        });
        
        rectangle.setToggleGroup(toggleGroup);
        return rectangle;
    }
    /**
     * ToggleButton for "Eraser" is created as well as the corresponding EventHandler.
     * The EventHandler handles the erasing of anything that was drawn. The eraser
     * works similarly to the Free Draw Line. 
     * @return "Eraser" ToggleButton to be added to the toolBar
     * @see addFreeDrawLine()
     */
    private ToggleButton addEraser(){
        ToggleButton eraser = new ToggleButton("Eraser");
        
        eraser.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event){
                drawnOn = true;
                straightLineSelected = false;
                shapeSelected = false; 
                eraserSelected = true;
                canDraw = false;
                canvas.setOnMousePressed( 
                new EventHandler<MouseEvent>(){
                    @Override
                    public void handle(MouseEvent event) {
                        canvas.setWidth(imageView.getFitWidth());
                        canvas.setHeight(imageView.getFitHeight());
                        if (eraserSelected){
                            gc.clearRect(event.getX(), event.getY(), currentWidth, currentWidth);
                        }
                    }
                });

                canvas.addEventHandler(MouseEvent.MOUSE_DRAGGED, 
                        new EventHandler<MouseEvent>(){
                        @Override
                        public void handle(MouseEvent event) {
                            if (eraserSelected){
                                gc.clearRect(event.getX(), event.getY(), currentWidth, currentWidth);
                            }
                        }
                    });
        
                canvas.addEventHandler(MouseEvent.MOUSE_RELEASED, new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent event) {
                        gc.clearRect(event.getX(), event.getY(), currentWidth, currentWidth);
                        pane.snapshot(null, wim);
                    }
                });
            }
        });
        
        eraser.setToggleGroup(toggleGroup);
        return eraser;
    }
    /**
     * ToggleButton for "Draw Triangle" is created as well as the corresponding EventHandler.
     * The EventHandler handles the drawing of an isosceles triangle on the canvas. The 
     * triangle is drawn by clicking and dragging. The triangle will be drawn 
     * staring where the mouse is clicked, then one of the two equal sides is 
     * drawn to the point where the mouse is released. The triangle is then symmetrically 
     * drawn from the peak or trough of the triangle. 
     * @return "Draw Triangle" ToggleButton to be added to the toolBar
     */
    private ToggleButton addTriangle(){
        ToggleButton triangle = new ToggleButton("Draw Triangle");
        
        triangle.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event){
                canDraw= true;
                shapeSelected = true;
                straightLineSelected = false;
                currentShape = "tri";
                eraserSelected = false;
                canvas.setOnMousePressed( 
                    new EventHandler<MouseEvent>(){

                        @Override
                        public void handle(MouseEvent event) {
                            canvas.setWidth(imageView.getFitWidth());
                            canvas.setHeight(imageView.getFitHeight());
                            gc.setLineWidth(currentWidth);
                            gc.setStroke(currentColor);
                            gc.setFill(currentFillColor);
                            
                            if (canDraw && !straightLineSelected && shapeSelected){
                                initialClick = new Pair(event.getX(),event.getY());
                            }
                            
                        }
                });

                canvas.addEventHandler(MouseEvent.MOUSE_RELEASED,new EventHandler<MouseEvent>(){
                    @Override
                    public void handle(MouseEvent event) {
                        if ((event.getX()-initialClick.getKey() >= 0.0) && (event.getY()-initialClick.getValue()>=0.0)){
                        double sideLength = event.getX() - initialClick.getKey();
                        double[] xPoints = new double[3];
                        double[] yPoints = new double[3];

                        xPoints[0] = initialClick.getKey();
                        yPoints[0] = initialClick.getValue();

                        xPoints[1] = event.getX();
                        yPoints[1] = event.getY();

                        xPoints[2] = (event.getX() - (2 * (sideLength)));
                        yPoints[2] = event.getY();
                        if (currentShape == "tri"){
                            if(currentFillColor != null){
                                gc.fillPolygon(xPoints, yPoints, 3);
                            }
                            else{
                                gc.strokePolygon(xPoints, yPoints, 3);
                            }
                        }
                    }
                    else{
                        if((event.getX()-initialClick.getKey() < 0) && (event.getY()-initialClick.getValue()>=0)){
                            
                            double sideLength = initialClick.getKey() - event.getX();
                            double[] xPoints = new double[3];
                            double[] yPoints = new double[3];

                            xPoints[0] = initialClick.getKey();
                            yPoints[0] = initialClick.getValue();

                            xPoints[1] = event.getX();
                            yPoints[1] = event.getY();

                            xPoints[2] = (event.getX() + (2 * (sideLength)));
                            yPoints[2] = event.getY();
                            if (currentShape == "tri"){
                                if(currentFillColor != null){
                                    gc.fillPolygon(xPoints, yPoints, 3);
                                }
                                else{
                                    gc.strokePolygon(xPoints, yPoints, 3);
                                }
                            }
                        }
                        else{
                            if((event.getX()-initialClick.getKey() >= 0) && (event.getY()-initialClick.getValue()<0)){
                                double sideLength = event.getX() - initialClick.getKey();
                                double[] xPoints = new double[3];
                                double[] yPoints = new double[3];

                                xPoints[0] = initialClick.getKey();
                                yPoints[0] = initialClick.getValue();

                                xPoints[1] = event.getX();
                                yPoints[1] = event.getY();

                                xPoints[2] = (initialClick.getKey() + (2 * (sideLength)));
                                yPoints[2] = initialClick.getValue();
                                if (currentShape == "tri"){
                                    if(currentFillColor != null){
                                        gc.fillPolygon(xPoints, yPoints, 3);
                                    }
                                    else{
                                        gc.strokePolygon(xPoints, yPoints, 3);
                                    }
                                }
                            }
                            else{
                                if((event.getX()-initialClick.getKey() < 0) && (event.getY()-initialClick.getValue()<0)){
                                    double sideLength = initialClick.getKey() - event.getX();
                                    double[] xPoints = new double[3];
                                    double[] yPoints = new double[3];

                                    xPoints[0] = initialClick.getKey();
                                    yPoints[0] = initialClick.getValue();

                                    xPoints[1] = event.getX();
                                    yPoints[1] = event.getY();

                                    xPoints[2] = (initialClick.getKey() - (2 * (sideLength)));
                                    yPoints[2] = initialClick.getValue();
                                    if (currentShape == "tri"){
                                        if(currentFillColor != null){
                                            gc.fillPolygon(xPoints, yPoints, 3);
                                        }
                                        else{
                                            gc.strokePolygon(xPoints, yPoints, 3);
                                        }
                                    }
                                }
                            }
                        }
                    }

                    pane.snapshot(null, wim);
                }
            });

            }
        });
        
        triangle.setToggleGroup(toggleGroup);
        return triangle;
    }
    /**
     * ToggleButton for "Draw Polygon" is created as well as the corresponding EventHandler.
     * The EventHandler handles the drawing of a n-sided polygon on the canvas. The 
     * polygon is drawn by clicking and dragging. The polygon will be drawn
     * within an imaginary circle using a  radius staring where the mouse is 
     * clicked to the point where the mouse is released. The points of the polygon 
     * are then calculated, and the polygon is drawn. 
     * @return "Draw Polygon" ToggleButton to be added to the toolBar
     */
    private ToggleButton addPolygon(){
        ToggleButton polygon = new ToggleButton("Draw Polygon");
        
        polygon.setOnAction(new EventHandler<ActionEvent>() {
            
            @Override
            public void handle(ActionEvent event){
                Stage tempstage = new Stage();
                tempstage.setTitle("Draw Polygon");

                GridPane grid = new GridPane();
                grid.setAlignment(Pos.CENTER);
                grid.setHgap(10);
                grid.setVgap(10);
                grid.setPadding(new Insets(25,25,25,25));

                Label nlbl = new Label("Number of Sides: ");
                grid.add(nlbl,0,1);
                TextField n_field = new TextField();
                grid.add(n_field, 1, 1);

                Scene stageScene = new Scene(grid);
                tempstage.setScene(stageScene);

                tempstage.show();

                tempstage.setOnCloseRequest(new EventHandler<WindowEvent>() {

                    @Override
                    public void handle(WindowEvent event){
                        nSides = new Integer(n_field.getText());
                        System.out.println(nSides);
                    }
                 });

                canDraw= true;
                straightLineSelected=false;
                shapeSelected=true;
                currentShape = "poly";
                
                canvas.setOnMousePressed(new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent event){
                        canvas.setWidth(imageView.getFitWidth());
                        canvas.setHeight(imageView.getFitHeight());
                        gc.setLineWidth(currentWidth);
                        gc.setStroke(currentColor);
                        gc.setFill(currentFillColor);
                        
                        initialClick = new Pair(event.getX(),event.getY());
                        System.out.println(initialClick);

                    }
                });

                canvas.setOnMouseReleased(new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent event) {
                        double centerX = initialClick.getKey();
                        double centerY = initialClick.getValue();

                        double radius = event.getX() - centerX;

                        double[] xPoints = new double[nSides];
                        double[] yPoints = new double[nSides];

                        for(int i = 0; i < nSides; i++){
                            xPoints[i] = (centerX + (radius * Math.cos(2*Math.PI * i / nSides)));
                            yPoints[i] = (centerY + (radius * Math.sin(2*Math.PI * i / nSides)));
                            System.out.println("Point" + i + ": " + xPoints[i] + " " + yPoints[i]);
                        }
                        if (canDraw && shapeSelected && currentShape == "poly"){
                            gc.strokePolygon(xPoints, yPoints, nSides);
                        }
                    }
                });
           }
        });
        polygon.setToggleGroup(toggleGroup);
        return polygon;
    }
    /**
     * ToggleButton for "Draw Square" is created as well as the corresponding EventHandler.
     * The EventHandler handles the drawing of a square on the canvas. The 
     * square is drawn by clicking and dragging. The square will be drawn 
     * staring where the mouse is clicked, then height of the square is 
     * determined using the point where the mouse is released. The square is
     * drawn with each side being the length of the y-coordinate difference. The 
     * square can only be drawn if the mouse is dragged up-and-to-the-left or
     * down-and-to-the-right of the original click.
     * @return "Draw Square" ToggleButton to be added to the toolBar
     */
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
                            //undoStack.push(wim);
                        }
                });
            }
        });
        
        square.setToggleGroup(toggleGroup);
        return square;
    }
    /**
     * ToggleButton for "Draw Ellipse" is created as well as the corresponding EventHandler.
     * The EventHandler handles the drawing of an ellipse on the canvas. The 
     * ellipse is drawn by clicking and dragging. The ellipse will be drawn within
     * an imaginary rectangle that starts where the mouse is clicked, then goes 
     * to the point where the mouse is released. 
     * @return "Draw Ellipse" ToggleButton to be added to the toolBar
     * @see addRectangle()
     */
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
                            //undoStack.push(wim);
                        }
                    });
           }
       });
       
       
       ellipse.setToggleGroup(toggleGroup);
       return ellipse;
   }
    /**
     * ToggleButton for "Draw Circle" is created as well as the corresponding EventHandler.
     * The EventHandler handles the drawing of a circle on the canvas. The 
     * circle is drawn by clicking and dragging. The circle will be drawn in an 
     * imaginary square that starts where the mouse is clicked, and whose side 
     * length is determined by the y-coordinate difference between the original 
     * click and the point where the mouse is released. Like the square, the 
     * circle can only be drawn if the mouse is dragged up-and-to-the-left or
     * down-and-to-the-right of the original click. 
     * @return "Draw Rectangle" ToggleButton to be added to the toolBar
     * @see addSquare()
     */
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
                                        if (currentShape == "circle"){
                                            if(currentFillColor != null){
                                                gc.fillOval(initialClick.getKey(), initialClick.getValue(), lesser, lesser);
                                            }
                                            else{
                                                gc.strokeOval(initialClick.getKey(), initialClick.getValue(), lesser, lesser);
                                            }
                                        }
                                    }
                                    else{
                                        double lesser = event.getX()-initialClick.getValue();
                                        if (currentShape == "circle"){
                                            if(currentFillColor != null){
                                                gc.fillOval(initialClick.getKey(), initialClick.getValue(), lesser, lesser);
                                            }
                                            else{
                                                gc.strokeOval(initialClick.getKey(), initialClick.getValue(), lesser, lesser);
                                            }
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
                                            if (currentShape == "circle"){
                                                if(currentFillColor != null){
                                                    gc.fillOval(event.getX(), event.getY(), width, width);
                                                }
                                                else{
                                                    gc.strokeOval(event.getX(), event.getY(), width, width);
                                                }
                                            }
                                        }
                                        else{
                                            if (currentShape == "circle"){
                                                if(currentFillColor != null){
                                                    gc.fillOval(event.getX(), event.getY(), height, height);
                                                }
                                                else{
                                                    gc.strokeOval(event.getX(), event.getY(), height, height);
                                                }
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
                                            if (currentShape == "circle"){
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
                                        else{
                                            if((event.getX()-initialClick.getKey() < 0) && (event.getY()-initialClick.getValue()<0)){
                                                double originalX = initialClick.getKey();
                                                double originalY = initialClick.getValue();
                                                initialClick = new Pair(event.getX(),event.getY());
                                                double width = Math.abs(originalX - initialClick.getKey());
                                                double height = Math.abs(originalY - initialClick.getValue());
                                                if (currentShape == "circle"){
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
                                }
                                
                                initialClick = new Pair(0,0);
                            }
                            pane.snapshot(null, wim);
                            //undoStack.push(wim);
                        }
                });
            }
        });
        
        circle.setToggleGroup(toggleGroup);
        return circle;
    }
    /**
     * ToggleButton for "Grab Color" is created as well as the corresponding EventHandler.
     * The EventHandler handles the grabbing of the color where the mouse is when 
     * it is clicked. The global color is set to this value. 
     * @return "Grab Color" ToggleButton to be added to the toolBar
     * @see addSquare()
     */
    private ToggleButton addColorGrab(){
       ToggleButton colorGrab = new ToggleButton("Grab Color");
       
       colorGrab.setOnAction(new EventHandler<ActionEvent>() {
           @Override
           public void handle(ActionEvent event){
               canDraw= false;
               canvas.setOnMouseClicked(new EventHandler<MouseEvent>(){
                   @Override 
                   public void handle(MouseEvent event){
                       canvas.snapshot(null, wim);
                       currentColor = wim.getPixelReader().getColor((int)event.getX(), (int)event.getY());
                       System.out.println(currentColor);
                       colorGrabbed = true;
                       
                   }
               });
           }
       });
       colorGrab.setToggleGroup(toggleGroup);
       return colorGrab;
   }
   /**
     * Sets the current WritableImage to a new WritableImage. The WritableImage
     * is used in multiple methods, and should only be changed or set when necessary. 
     * @param wim New WritableImage to be used
     */
    public void setWim(WritableImage wim){
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
    /**
     * Gets the current Canvas being used by the toolBar.
     * @return Canvas being used
     */
    public Canvas getCanvas(){
        return canvas;
    }
    /**
     * Gets the current WritableImage being used by the toolBar.
     * @return Current version of the WritableImage being used
     */
    public WritableImage getWim(){
        return wim;
    }
    /**
     * Gets the boolean value that indicates whether or not the canvas has been
     * drawn on. 
     * @return true if the canvas has been drawn on.
     */
    public Boolean beenDrawnOn(){
        return drawnOn;
    }
}
