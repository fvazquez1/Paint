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
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Pair;

/**
 * Custom ToolBar class for Paint.java
 * <P>
 * Anything housed in the ToolBar is found in this class. All ToggleButtons,
 * necessary EventHandlers, and necessary variables are in this class. The
 * toolBar handles all drawing capabilities.
 *
 * @author Francisco Vazquez
 */
public class toolBar extends ToolBar {

    static Stage stage;
    static boolean canDraw, drawnOn;
    static ImageView imageView;
    static Canvas canvas;
    static GraphicsContext gc;
    static WritableImage wim;
    static Color currentColor, currentFillColor;
    static double currentWidth;
    static boolean straightLineSelected, shapeSelected, colorGrabbed, eraserSelected;
    static Pair<Double, Double> initialClick;
    static Pane pane;
    static ToggleGroup toggleGroup;
    static String currentShape = "";
    static String currentText = "";
    static Stack<WritableImage> undoStack;
    static Integer nSides;

    /**
     * Constructor for toolBar object.
     *
     * @param stage Stage to display the toolBar on
     * @param imageView ImageView being used by the stage
     */
    public toolBar(Stage stage, ImageView imageView) {
        this.stage = stage;
        currentColor = Color.BLACK;
        canDraw = false;
        this.imageView = imageView;
        toggleGroup = new ToggleGroup();
        //adds all the buttons to the tool bar
        this.getItems().addAll(addEraser(), addDrawLine(), addFreeDrawLine(), addRectangle(),
                addTriangle(), addSquare(), addEllipse(), addCircle(), addPolygon(),
                addLineColor(), addDashedLine(), addFillColor(), addColorGrab(), addLineWidth(),
                addTextBox());
        canvas = new Canvas(imageView.getFitWidth(), imageView.getFitHeight());
        gc = canvas.getGraphicsContext2D();
        wim = new WritableImage((int) imageView.getFitWidth() + 20, (int) imageView.getFitHeight() + 20);
    }

    /**
     * ToggleButton for "Draw Line" is created as well as the corresponding
     * EventHandler. The EventHandler handles the drawing of a straight line on
     * the canvas.
     *
     * @return "Draw Line" ToggleButton to be added to the toolBar
     */
    private ToggleButton addDrawLine() {
        ToggleButton drawLine = new ToggleButton("Draw Line");
        drawLine.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                //Sets booleans that may cause potential errors
                drawnOn = true;
                shapeSelected = false;
                eraserSelected = false;
                if ((ToggleButton) toggleGroup.getSelectedToggle() != null) {
                    canDraw = true;
                    straightLineSelected = true;
                } else {
                    canDraw = false;
                    straightLineSelected = false;
                }

                //Event Handler for the canvas when this button is pressed
                //and the mouse is pressed on the canvas. 
                canvas.setOnMousePressed(
                        new EventHandler<MouseEvent>() {

                    @Override
                    public void handle(MouseEvent event) {
                        //Set drawing parameters to what has been set by the user
                        gc.setStroke(currentColor);
                        canvas.setWidth(paint.menuBar.imageView.getFitWidth());
                        canvas.setHeight(paint.menuBar.imageView.getFitHeight());
                        if (canDraw && straightLineSelected) {
                            //Keeping track of where the mouse was first clicked
                            initialClick = new Pair(event.getX(), event.getY());
                        }

                    }
                });
                //Event Handler for the canvas when this button is pressed
                //and the mouse is released on the canvas. 
                canvas.addEventHandler(MouseEvent.MOUSE_RELEASED,
                        new EventHandler<MouseEvent>() {

                    @Override
                    public void handle(MouseEvent event) {
                        if (canDraw && straightLineSelected) {
                            gc = paint.toolBar.gc;

                            //Draws the line from the point where the mouse was first clicked
                            //to the current point where the mouse was released
                            gc.strokeLine(initialClick.getKey(), initialClick.getValue(), event.getX(), event.getY());

                            //Snapshots everything on the pane (includes canvas and image view)
                            //Snapshot is saved to wim
                            pane.snapshot(null, wim);

                            //Pushes the newly saved wim to the undo stack
                            paint.menuBar.undoStack.push(wim);
                        }
                    }
                });

            }
        });
        //Tooltip for this button
        drawLine.setTooltip(new Tooltip("Draw a straight line"));
        //Adding this button to the toggle group so that no other drawing tools
        //can be selected at the same time. 
        drawLine.setToggleGroup(toggleGroup);

        return drawLine;
    }

    /**
     * ToggleButton for "Free Draw Line" is created as well as the corresponding
     * EventHandler. The EventHandler handles the free drawing of a line on the
     * canvas. The line drawing starts from the point where the mouse is
     * clicked, and is drawn to the position of the mouse as long as it is being
     * held down.
     *
     * @return "Free Draw Line" ToggleButton to be added to the toolBar
     */
    private ToggleButton addFreeDrawLine() {
        ToggleButton freeLine = new ToggleButton("Free Draw Line");
        freeLine.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                //Sets booleans that may cause potential errors
                drawnOn = true;
                straightLineSelected = false;
                shapeSelected = false;
                eraserSelected = false;
                if ((ToggleButton) toggleGroup.getSelectedToggle() != null) {
                    canDraw = true;
                } else {
                    canDraw = false;
                }

                //Event Handler for the canvas when this button is pressed
                //and the mouse is pressed on the canvas. 
                canvas.setOnMousePressed(
                        new EventHandler<MouseEvent>() {

                    @Override
                    public void handle(MouseEvent event) {
                        canvas.setWidth(imageView.getFitWidth());
                        canvas.setHeight(imageView.getFitHeight());
                        if (canDraw && !straightLineSelected && !shapeSelected) {
                            //Starts the path
                            gc.beginPath();
                            gc.setLineWidth(currentWidth);
                            gc.setStroke(currentColor);
                            gc.moveTo(event.getX(), event.getY());
                            gc.stroke();
                        }
                    }
                });
                //Event Handler for the canvas when this button is pressed
                //and the mouse is dragged on the canvas. 
                canvas.addEventHandler(MouseEvent.MOUSE_DRAGGED,
                        new EventHandler<MouseEvent>() {

                    @Override
                    public void handle(MouseEvent event) {
                        if (canDraw && !straightLineSelected && !shapeSelected) {
                            //Continues the line to the current position of the mouse
                            gc.setFill(currentColor);
                            gc.lineTo(event.getX(), event.getY());
                            gc.stroke();
                        }
                    }
                });
                //Event Handler for the canvas when this button is pressed
                //and the mouse is released on the canvas. 
                canvas.addEventHandler(MouseEvent.MOUSE_RELEASED, new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent event) {
                        //Snapshots everything on the pane (includes canvas and image view)
                        //Snapshot is saved to wim 
                        pane.snapshot(null, wim);

                        //Pushes the newly saved wim to the undo stack
                        paint.menuBar.undoStack.push(wim);;

                    }
                });
            }
        });
        //Sets the tooltip for this button
        freeLine.setTooltip(new Tooltip("Free-hand draw a line"));

        //Adding this button to the toggle group so that no other drawing tools
        //can be selected at the same time.
        freeLine.setToggleGroup(toggleGroup);

        return freeLine;
    }

    /**
     * Button for "Select Line Color" is created as well as the corresponding
     * EventHandler. The EventHandler handles the changing of the line color by
     * opening a separate window with a ColorChooser. The color is only saved
     * when the window that was opened is closed.
     *
     * @return "Select Line Color" ToggleButton to be added to the toolBar
     */
    private Button addLineColor() {
        Button lineColor = new Button("Select Line Color");
        ColorPicker colorpicker = new ColorPicker(Color.BLACK);
        //Checks if the color grabber tool has been used
        if (colorGrabbed) {
            colorpicker.setValue(currentColor);
        }
        lineColor.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                //Opens up a small window for a color chooser
                StackPane stack = new StackPane();
                stack.setPadding(new Insets(25, 25, 25, 25));
                Stage tempstage = new Stage();
                stack.getChildren().add(colorpicker);
                Scene stageScene = new Scene(stack);
                tempstage.setScene(stageScene);

                tempstage.show();
                //The following only triggers when the window is closed
                tempstage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                    @Override
                    public void handle(WindowEvent event) {
                        //Sets the current color to what has been picked by the user
                        currentColor = colorpicker.getValue();
                    }
                });
            }
        });
        //Sets the tool tip for this button
        lineColor.setTooltip(new Tooltip("Change the line color"));

        return lineColor;
    }

    /**
     * Button for "Select Fill Color" is created as well as the corresponding
     * EventHandler. The EventHandler handles the changing of the fill color by
     * opening a separate window with a ColorChooser. The color is only saved
     * when the window that was opened is closed.
     *
     * @return "Select Fill Color" ToggleButton to be added to the toolBar
     */
    private Button addFillColor() {
        Button fillColor = new Button("Select Fill Color");
        ColorPicker colorpicker = new ColorPicker(Color.TRANSPARENT);
        fillColor.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                //Opens up a small window for a color chooser
                StackPane stack = new StackPane();
                stack.setPadding(new Insets(25, 25, 25, 25));
                Stage tempstage = new Stage();
                stack.getChildren().add(colorpicker);
                Scene stageScene = new Scene(stack);
                tempstage.setScene(stageScene);

                tempstage.show();
                //The following only triggers when the window is closed
                tempstage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                    @Override
                    public void handle(WindowEvent event) {
                        //Sets the current fill color to what has been picked by the user
                        currentFillColor = colorpicker.getValue();
                    }
                });
            }
        });
        //Sets the tool tip for this button
        fillColor.setTooltip(new Tooltip("Change the fill color of shapes to be drawn"));

        return fillColor;
    }

    /**
     * Button for "Set Line Width" is created as well as the corresponding
     * EventHandler. The EventHandler handles the changing of the line width by
     * opening a separate window with a Slider. The color is only saved when the
     * window that was opened is closed.
     *
     * @return "Select Line Color" ToggleButton to be added to the toolBar
     */
    private Button addLineWidth() {
        Button lineWidth = new Button("Set Line Width");
        //Creates a slider that will be used to adjust the line width
        Slider slider = new Slider(1, 20, 1);
        slider.setMajorTickUnit(1.0);
        slider.setSnapToTicks(true);
        slider.setShowTickMarks(true);
        slider.setShowTickLabels(true);

        lineWidth.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                //Opens up a small window for the slider to appear on 
                StackPane stack = new StackPane();
                stack.setPadding(new Insets(15, 15, 15, 15));
                Stage tempstage = new Stage();
                stack.getChildren().add(slider);
                Scene stageScene = new Scene(stack);
                tempstage.setWidth(425);
                tempstage.setScene(stageScene);

                tempstage.show();
                ////The following only triggers when the window is closed
                tempstage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                    @Override
                    public void handle(WindowEvent event) {
                        //The current width of the line is set to the value the slider was left at
                        currentWidth = slider.getValue();
                    }
                });
            }
        });
        //Set the tool tip for this button
        lineWidth.setTooltip(new Tooltip("Change the line width, and also the text size of inserted text"));

        return lineWidth;
    }

    /**
     * ToggleButton for "Insert Text" is created as well as the corresponding
     * EventHandler. The EventHandler handles the setting of the desired text to
     * be printed by opening a separate window with a TextArea for the user to
     * input their desired text. The text is only saved when the window that was
     * opened is closed.
     *
     * @return "Insert Text" ToggleButton to be added to the toolBar
     */
    private ToggleButton addTextBox() {
        ToggleButton txtBox = new ToggleButton("Insert Text");

        txtBox.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                //Sets booleans that can cause errors
                canDraw = false;
                shapeSelected = false;
                straightLineSelected = false;
                //Sets up a window so text can be written by the user
                Stage tempstage = new Stage();
                tempstage.setTitle("Text To Be Written");
                TextArea textArea = new TextArea();
                VBox vbox = new VBox(textArea);
                Scene stageScene = new Scene(vbox);
                tempstage.setScene(stageScene);
                tempstage.show();
                // text is set when the pop-up window is closed
                tempstage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                    @Override
                    public void handle(WindowEvent event) {
                        currentText = textArea.getText();
                    }
                });
                // text is printed on the canvas where ever the mouse is clicked on the canvas
                canvas.setOnMousePressed(new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent event) {
                        //canvas is resized if needed
                        canvas.setWidth(imageView.getFitWidth());
                        canvas.setHeight(imageView.getFitHeight());
                        // the coordinates of the click are saved
                        initialClick = new Pair(event.getX(), event.getY());
                        // color and size are set. Then the text is printed 
                        gc.setStroke(currentColor);
                        gc.setFont(Font.font(currentWidth + 12));
                        gc.strokeText(currentText, initialClick.getKey(), initialClick.getValue());
                    }
                });

            }
        });
        //sets the tool tip for this button
        txtBox.setTooltip(new Tooltip("Set text to be inserted. Click anywhere on canvas to insert text."));
        //Adding this button to the toggle group so that no other drawing tools
        //can be selected at the same time.
        txtBox.setToggleGroup(toggleGroup);

        return txtBox;
    }

    /**
     * ToggleButton for "Draw Rectangle" is created as well as the corresponding
     * EventHandler. The EventHandler handles the drawing of a rectangle on the
     * canvas. The rectangle is drawn by clicking and dragging. The rectangle
     * will be drawn staring where the mouse is clicked, then drawn to the point
     * where the mouse is released.
     *
     * @return "Draw Rectangle" ToggleButton to be added to the toolBar
     */
    private ToggleButton addRectangle() {
        ToggleButton rectangle = new ToggleButton("Draw Rectangle");

        rectangle.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                //Sets booleans that may cause errors
                canDraw = true;
                shapeSelected = true;
                straightLineSelected = false;
                currentShape = "rect";
                eraserSelected = false;
                //the following triggers on the first mouse press
                canvas.setOnMousePressed(
                        new EventHandler<MouseEvent>() {

                    @Override
                    public void handle(MouseEvent event) {
                        //canvas is resized if needed
                        canvas.setWidth(imageView.getFitWidth());
                        canvas.setHeight(imageView.getFitHeight());
                        //saves the coordinates of where the mouse was pressed on the canvas
                        if (canDraw && !straightLineSelected && shapeSelected) {
                            initialClick = new Pair(event.getX(), event.getY());
                        }
                    }
                });
                //the following triggers when the mouse is released after the initial press
                canvas.addEventHandler(MouseEvent.MOUSE_RELEASED,
                        new EventHandler<MouseEvent>() {

                    @Override
                    public void handle(MouseEvent event) {
                        if (canDraw && !straightLineSelected && shapeSelected && currentShape == "rect") {
                            //sets cosmetic values such as line width and color
                            gc.setLineWidth(currentWidth);
                            gc.setStroke(currentColor);
                            gc.setFill(currentFillColor);
                            //handler if the mouse is released anywhere up and/or to the right of the initial mouse press
                            if ((event.getX() - initialClick.getKey() >= 0.0) && (event.getY() - initialClick.getValue() >= 0.0)) {
                                if (currentFillColor != null) {
                                    gc.fillRect(initialClick.getKey(), initialClick.getValue(), (event.getX() - initialClick.getKey()), (event.getY() - initialClick.getValue()));
                                } else {
                                    gc.strokeRect(initialClick.getKey(), initialClick.getValue(), (event.getX() - initialClick.getKey()), (event.getY() - initialClick.getValue()));
                                }
                            } else {//handler if the mouse is released anywhere up and/or to the left of the initial mouse press
                                if ((event.getX() - initialClick.getKey() < 0) && (event.getY() - initialClick.getValue() >= 0)) {
                                    double originalX = initialClick.getKey();
                                    double originalY = initialClick.getValue();
                                    initialClick = new Pair(event.getX(), originalY);
                                    if (currentFillColor != null) {
                                        gc.fillRect(initialClick.getKey(), initialClick.getValue(), (originalX - initialClick.getKey()), (event.getY() - initialClick.getValue()));
                                    } else {
                                        gc.strokeRect(initialClick.getKey(), initialClick.getValue(), (originalX - initialClick.getKey()), (event.getY() - initialClick.getValue()));
                                    }
                                } else { //handler if the mouse is released anywhere down and/or to the right of the initial mouse press
                                    if ((event.getX() - initialClick.getKey() >= 0) && (event.getY() - initialClick.getValue() < 0)) {
                                        double originalX = initialClick.getKey();
                                        double originalY = initialClick.getValue();
                                        initialClick = new Pair(originalX, event.getY());
                                        if (currentFillColor != null) {
                                            gc.fillRect(initialClick.getKey(), initialClick.getValue(), (event.getX() - initialClick.getKey()), (originalY - initialClick.getValue()));
                                        } else {
                                            gc.strokeRect(initialClick.getKey(), initialClick.getValue(), (event.getX() - initialClick.getKey()), (originalY - initialClick.getValue()));
                                        }
                                    } else {//handler if the mouse is released anywhere down and/or to the left of the initial mouse press
                                        if ((event.getX() - initialClick.getKey() < 0) && (event.getY() - initialClick.getValue() < 0)) {
                                            double originalX = initialClick.getKey();
                                            double originalY = initialClick.getValue();
                                            initialClick = new Pair(event.getX(), event.getY());
                                            if (currentFillColor != null) {
                                                gc.fillRect(initialClick.getKey(), initialClick.getValue(), (originalX - initialClick.getKey()), (originalY - initialClick.getValue()));
                                            } else {
                                                gc.strokeRect(initialClick.getKey(), initialClick.getValue(), (originalX - initialClick.getKey()), (originalY - initialClick.getValue()));
                                            }
                                        }
                                    }
                                }
                            }

                        }
                        //saves the new additions to the current wim
                        pane.snapshot(null, wim);
                        //Pushes the newly saved wim to the undo stack
                        paint.menuBar.undoStack.push(wim);;
                    }
                });

            }
        });
        //sets the tool tip for this button
        rectangle.setTooltip(new Tooltip("Draw a rectangle on the canvas"));
        //Adding this button to the toggle group so that no other drawing tools
        //can be selected at the same time.
        rectangle.setToggleGroup(toggleGroup);
        return rectangle;
    }

    /**
     * ToggleButton for "Eraser" is created as well as the corresponding
     * EventHandler. The EventHandler handles the erasing of anything that was
     * drawn. The eraser works similarly to the Free Draw Line.
     *
     * @return "Eraser" ToggleButton to be added to the toolBar
     * @see addFreeDrawLine()
     */
    private ToggleButton addEraser() {
        ToggleButton eraser = new ToggleButton("Eraser");

        eraser.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                //Sets booleans that may cause errors
                drawnOn = true;
                straightLineSelected = false;
                shapeSelected = false;
                eraserSelected = true;
                canDraw = false;
                //the following triggers when the mouse is pressed
                canvas.setOnMousePressed(
                        new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent event) {
                        //resizes the canvas if necessary
                        canvas.setWidth(imageView.getFitWidth());
                        canvas.setHeight(imageView.getFitHeight());
                        if (eraserSelected) {
                            //erases anything that has been drawn at the point where the mouse was clicked
                            gc.clearRect(event.getX(), event.getY(), currentWidth, currentWidth);
                        }
                    }
                });
                //the following is triggered when the mouse is pressed and is in the process of being dragged
                canvas.addEventHandler(MouseEvent.MOUSE_DRAGGED,
                        new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent event) {
                        if (eraserSelected) {
                            //erases at the point where the mouse is currently at
                            gc.clearRect(event.getX(), event.getY(), currentWidth, currentWidth);
                        }
                    }
                });
                //the following is triggered when the mouse is released after being pressed
                canvas.addEventHandler(MouseEvent.MOUSE_RELEASED, new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent event) {
                        //erases at the point where the mouse is currently at
                        gc.clearRect(event.getX(), event.getY(), currentWidth, currentWidth);
                        //saves the edited canvas to the wim
                        pane.snapshot(null, wim);
                    }
                });
            }
        });
        //sets the tool tip for this button
        eraser.setTooltip(new Tooltip("Erase anything that has been drawn"));
        //Adding this button to the toggle group so that no other drawing tools
        //can be selected at the same time.
        eraser.setToggleGroup(toggleGroup);

        return eraser;
    }

    /**
     * ToggleButton for "Draw Triangle" is created as well as the corresponding
     * EventHandler. The EventHandler handles the drawing of an isosceles
     * triangle on the canvas. The triangle is drawn by clicking and dragging.
     * The triangle will be drawn staring where the mouse is clicked, then one
     * of the two equal sides is drawn to the point where the mouse is released.
     * The triangle is then symmetrically drawn from the peak or trough of the
     * triangle.
     *
     * @return "Draw Triangle" ToggleButton to be added to the toolBar
     */
    private ToggleButton addTriangle() {
        ToggleButton triangle = new ToggleButton("Draw Triangle");

        triangle.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                //Sets booleans that may cause errors
                canDraw = true;
                shapeSelected = true;
                straightLineSelected = false;
                currentShape = "tri";
                eraserSelected = false;
                //the following triggers when the mouse is pressed
                canvas.setOnMousePressed(
                        new EventHandler<MouseEvent>() {

                    @Override
                    public void handle(MouseEvent event) {
                        //resizes the canvas if necessary
                        canvas.setWidth(imageView.getFitWidth());
                        canvas.setHeight(imageView.getFitHeight());
                        //sets cosmetic variables
                        gc.setLineWidth(currentWidth);
                        gc.setStroke(currentColor);
                        gc.setFill(currentFillColor);

                        if (canDraw && !straightLineSelected && shapeSelected) {
                            //saves the coordinates of where the mouse was pressed on the canvas
                            initialClick = new Pair(event.getX(), event.getY());
                        }

                    }
                });
                //the following triggers when the mouse is released after the initial press
                canvas.addEventHandler(MouseEvent.MOUSE_RELEASED, new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent event) {
                        //handler if the mouse is released anywhere up and/or to the right of the initial mouse press
                        if ((event.getX() - initialClick.getKey() >= 0.0) && (event.getY() - initialClick.getValue() >= 0.0)) {
                            //calculates the side length using the current x value and the initial x value
                            double sideLength = event.getX() - initialClick.getKey();
                            //creates an array to store the x and y coordinates
                            double[] xPoints = new double[3];
                            double[] yPoints = new double[3];
                            //first point is the initial click
                            xPoints[0] = initialClick.getKey();
                            yPoints[0] = initialClick.getValue();
                            //second point is the mouse's current position
                            xPoints[1] = event.getX();
                            yPoints[1] = event.getY();
                            //calculate the final point using the sideLength
                            xPoints[2] = (event.getX() - (2 * (sideLength)));
                            yPoints[2] = event.getY();
                            if (currentShape == "tri") {
                                if (currentFillColor != null) {
                                    gc.fillPolygon(xPoints, yPoints, 3);
                                } else {
                                    gc.strokePolygon(xPoints, yPoints, 3);
                                }
                            }
                        } else {//handler if the mouse is released anywhere up and/or to the left of the initial mouse press
                            if ((event.getX() - initialClick.getKey() < 0) && (event.getY() - initialClick.getValue() >= 0)) {
                                //calculates the side length using the current x value and the initial x value
                                double sideLength = initialClick.getKey() - event.getX();
                                //creates an array to store the x and y coordinates
                                double[] xPoints = new double[3];
                                double[] yPoints = new double[3];
                                //first point is the initial click
                                xPoints[0] = initialClick.getKey();
                                yPoints[0] = initialClick.getValue();
                                //second point is the mouse's current position
                                xPoints[1] = event.getX();
                                yPoints[1] = event.getY();
                                //calculate the final point using the sideLength
                                xPoints[2] = (event.getX() + (2 * (sideLength)));
                                yPoints[2] = event.getY();
                                if (currentShape == "tri") {
                                    if (currentFillColor != null) {
                                        gc.fillPolygon(xPoints, yPoints, 3);
                                    } else {
                                        gc.strokePolygon(xPoints, yPoints, 3);
                                    }
                                }
                            } else {//handler if the mouse is released anywhere down and/or to the right of the initial mouse press
                                if ((event.getX() - initialClick.getKey() >= 0) && (event.getY() - initialClick.getValue() < 0)) {
                                    //calculates the side length using the current x value and the initial x value
                                    double sideLength = event.getX() - initialClick.getKey();
                                    //creates an array to store the x and y coordinates
                                    double[] xPoints = new double[3];
                                    double[] yPoints = new double[3];
                                    //first point is the initial click
                                    xPoints[0] = initialClick.getKey();
                                    yPoints[0] = initialClick.getValue();
                                    //second point is the mouse's current position
                                    xPoints[1] = event.getX();
                                    yPoints[1] = event.getY();
                                    //calculate the final point using the sideLength
                                    xPoints[2] = (initialClick.getKey() + (2 * (sideLength)));
                                    yPoints[2] = initialClick.getValue();
                                    if (currentShape == "tri") {
                                        if (currentFillColor != null) {
                                            gc.fillPolygon(xPoints, yPoints, 3);
                                        } else {
                                            gc.strokePolygon(xPoints, yPoints, 3);
                                        }
                                    }
                                } else {//handler if the mouse is released anywhere down and/or to the left of the initial mouse press
                                    if ((event.getX() - initialClick.getKey() < 0) && (event.getY() - initialClick.getValue() < 0)) {
                                        //calculates the side length using the current x value and the initial x value
                                        double sideLength = initialClick.getKey() - event.getX();
                                        //creates an array to store the x and y coordinates
                                        double[] xPoints = new double[3];
                                        double[] yPoints = new double[3];
                                        //first point is the initial click
                                        xPoints[0] = initialClick.getKey();
                                        yPoints[0] = initialClick.getValue();
                                        //second point is the mouse's current position
                                        xPoints[1] = event.getX();
                                        yPoints[1] = event.getY();
                                        //calculate the final point using the sideLength
                                        xPoints[2] = (initialClick.getKey() - (2 * (sideLength)));
                                        yPoints[2] = initialClick.getValue();
                                        if (currentShape == "tri") {
                                            if (currentFillColor != null) {
                                                gc.fillPolygon(xPoints, yPoints, 3);
                                            } else {
                                                gc.strokePolygon(xPoints, yPoints, 3);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        //saves the edited canvas to the wim
                        pane.snapshot(null, wim);
                    }
                });

            }
        });
        //sets the tool tip for this button
        triangle.setTooltip(new Tooltip("Draw an isosceles triangle"));
        //Adding this button to the toggle group so that no other drawing tools
        //can be selected at the same time.
        triangle.setToggleGroup(toggleGroup);

        return triangle;
    }

    /**
     * ToggleButton for "Toggle Dashed Line" is created as well as the
     * corresponding EventHandler. The EventHandler handles the setting of the
     * dash for the line based on the current line width.
     *
     * @return "Toggle Dashed Line" ToggleButton to be added to the toolBar
     */
    public ToggleButton addDashedLine() {
        ToggleButton dashedLine = new ToggleButton("Toggle Dashed Line");
        //triggers when the button is pressed
        dashedLine.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                if (dashedLine.isSelected()) {
                    gc.setLineDashes(5 + currentWidth);
                } else {
                    gc.setLineDashes(0);
                }
            }
        });
        return dashedLine;
    }

    /**
     * ToggleButton for "Draw Polygon" is created as well as the corresponding
     * EventHandler. The EventHandler handles the drawing of a n-sided polygon
     * on the canvas. The polygon is drawn by clicking and dragging. The polygon
     * will be drawn within an imaginary circle using a radius staring where the
     * mouse is clicked to the point where the mouse is released. The points of
     * the polygon are then calculated, and the polygon is drawn.
     *
     * @return "Draw Polygon" ToggleButton to be added to the toolBar
     */
    private ToggleButton addPolygon() {
        ToggleButton polygon = new ToggleButton("Draw Polygon");

        polygon.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                //Opens a window to allow for the user to input the number of sides
                Stage tempstage = new Stage();
                tempstage.setTitle("Draw Polygon");

                GridPane grid = new GridPane();
                grid.setAlignment(Pos.CENTER);
                grid.setHgap(10);
                grid.setVgap(10);
                grid.setPadding(new Insets(25, 25, 25, 25));

                Label nlbl = new Label("Number of Sides: ");
                grid.add(nlbl, 0, 1);
                TextField n_field = new TextField();
                grid.add(n_field, 1, 1);

                Scene stageScene = new Scene(grid);
                tempstage.setScene(stageScene);

                tempstage.show();
                //the number of sides is saved when the pop-up window is closed
                tempstage.setOnCloseRequest(new EventHandler<WindowEvent>() {

                    @Override
                    public void handle(WindowEvent event) {
                        nSides = new Integer(n_field.getText());
                    }
                });
                //Sets booleans that may cause errors
                canDraw = true;
                straightLineSelected = false;
                shapeSelected = true;
                currentShape = "poly";
                //the following triggers when the mouse is pressed
                canvas.setOnMousePressed(new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent event) {
                        //resizes the canvas if necessary
                        canvas.setWidth(imageView.getFitWidth());
                        canvas.setHeight(imageView.getFitHeight());
                        //sets cosmetic variables
                        gc.setLineWidth(currentWidth);
                        gc.setStroke(currentColor);
                        gc.setFill(currentFillColor);
                        //saves the coordinates of where the mouse was pressed on the canvas
                        initialClick = new Pair(event.getX(), event.getY());
                    }
                });
                //the following triggers when the mouse is released after the initial press
                canvas.setOnMouseReleased(new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent event) {
                        //sets the center of the polygon to the initial click
                        double centerX = initialClick.getKey();
                        double centerY = initialClick.getValue();
                        //radius is calculated using the center x value and the current x value
                        double radius = event.getX() - centerX;
                        //creates an array to store the x and y coordinates
                        double[] xPoints = new double[nSides];
                        double[] yPoints = new double[nSides];
                        //coordinates for each point are calculated
                        for (int i = 0; i < nSides; i++) {
                            xPoints[i] = (centerX + (radius * Math.cos(2 * Math.PI * i / nSides)));
                            yPoints[i] = (centerY + (radius * Math.sin(2 * Math.PI * i / nSides)));
                            System.out.println("Point" + i + ": " + xPoints[i] + " " + yPoints[i]);
                        }
                        if (canDraw && shapeSelected && currentShape == "poly") {
                            //polygon is drawn
                            gc.strokePolygon(xPoints, yPoints, nSides);
                        }
                    }
                });
            }
        });
        //sets the tool tip for this button
        polygon.setTooltip(new Tooltip("Draw an n-sided polygon"));
        //Adding this button to the toggle group so that no other drawing tools
        //can be selected at the same time.
        polygon.setToggleGroup(toggleGroup);

        return polygon;
    }

    /**
     * ToggleButton for "Draw Square" is created as well as the corresponding
     * EventHandler. The EventHandler handles the drawing of a square on the
     * canvas. The square is drawn by clicking and dragging. The square will be
     * drawn staring where the mouse is clicked, then height of the square is
     * determined using the point where the mouse is released. The square is
     * drawn with each side being the length of the y-coordinate difference. The
     * square can only be drawn if the mouse is dragged up-and-to-the-left or
     * down-and-to-the-right of the original click.
     *
     * @return "Draw Square" ToggleButton to be added to the toolBar
     */
    private ToggleButton addSquare() {
        ToggleButton square = new ToggleButton("Draw Square");

        square.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                //Sets booleans that may cause errors
                canDraw = true;
                shapeSelected = true;
                straightLineSelected = false;
                currentShape = "square";
                //the following triggers when the mouse is pressed
                canvas.setOnMousePressed(new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent event) {
                        //resizes the canvas if necessary
                        canvas.setWidth(imageView.getFitWidth());
                        canvas.setHeight(imageView.getFitHeight());
                        if (canDraw && !straightLineSelected && shapeSelected) {
                            //saves the coordinates of where the mouse was pressed on the canvas
                            initialClick = new Pair(event.getX(), event.getY());
                        }
                    }
                });
                //the following triggers when the mouse is released after the initial press
                canvas.addEventHandler(MouseEvent.MOUSE_RELEASED, new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent event) {
                        if (canDraw && !straightLineSelected && shapeSelected && currentShape == "square") {
                            //sets cosmetic variables
                            gc.setLineWidth(currentWidth);
                            gc.setStroke(currentColor);
                            gc.setFill(currentFillColor);
                            //handler if the mouse is released anywhere up and/or to the right of the initial mouse press
                            if ((event.getX() - initialClick.getKey() >= 0) && (event.getY() - initialClick.getValue() >= 0)) {
                                //Checks whether to determine side length with x length or y length
                                if ((event.getX() - initialClick.getKey()) > (event.getY() - initialClick.getValue())) {
                                    double lesser = event.getY() - initialClick.getValue();
                                    if (currentFillColor != null) {
                                        gc.fillRect(initialClick.getKey(), initialClick.getValue(), lesser, lesser);
                                    } else {
                                        gc.strokeRect(initialClick.getKey(), initialClick.getValue(), lesser, lesser);
                                    }
                                } else {
                                    double lesser = event.getX() - initialClick.getValue();
                                    if (currentFillColor != null) {
                                        gc.fillRect(initialClick.getKey(), initialClick.getValue(), lesser, lesser);
                                    } else {
                                        gc.strokeRect(initialClick.getKey(), initialClick.getValue(), lesser, lesser);
                                    }
                                }
                            } else {//handler if the mouse is released anywhere up and/or to the left of the initial mouse press
                                if ((event.getX() - initialClick.getKey() < 0) && (event.getY() - initialClick.getValue() >= 0)) {
                                    double originalX = initialClick.getKey();
                                    double originalY = initialClick.getValue();

                                    double width = Math.abs(originalX - initialClick.getKey());
                                    double height = Math.abs(originalY - initialClick.getValue());

                                    if (width < height) {
                                        if (currentFillColor != null) {
                                            gc.fillRect(event.getX(), event.getY(), width, width);
                                        } else {
                                            gc.strokeRect(event.getX(), event.getY(), width, width);
                                        }
                                    } else {
                                        if (currentFillColor != null) {
                                            gc.fillRect(event.getX(), event.getY(), height, height);
                                        } else {
                                            gc.strokeRect(event.getX(), event.getY(), height, height);
                                        }
                                    }
                                } else {//handler if the mouse is released anywhere down and/or to the right of the initial mouse press
                                    if ((event.getX() - initialClick.getKey() >= 0) && (event.getY() - initialClick.getValue() < 0)) {
                                        double originalX = initialClick.getKey();
                                        double originalY = initialClick.getValue();
                                        initialClick = new Pair(originalX, event.getY());
                                        double width = Math.abs(originalX - initialClick.getKey());
                                        double height = Math.abs(originalY - initialClick.getValue());

                                        if (width < height) {
                                            if (currentFillColor != null) {
                                                gc.fillRect(initialClick.getKey(), initialClick.getValue(), width, width);
                                            } else {
                                                gc.strokeRect(initialClick.getKey(), initialClick.getValue(), width, width);
                                            }
                                        } else {
                                            if (currentFillColor != null) {
                                                gc.fillRect(initialClick.getKey(), initialClick.getValue(), height, height);
                                            } else {
                                                gc.strokeRect(initialClick.getKey(), initialClick.getValue(), height, height);
                                            }
                                        }
                                    } else {//handler if the mouse is released anywhere down and/or to the left of the initial mouse press
                                        if ((event.getX() - initialClick.getKey() < 0) && (event.getY() - initialClick.getValue() < 0)) {
                                            double originalX = initialClick.getKey();
                                            double originalY = initialClick.getValue();
                                            initialClick = new Pair(event.getX(), event.getY());
                                            double width = Math.abs(originalX - initialClick.getKey());
                                            double height = Math.abs(originalY - initialClick.getValue());

                                            if (width < height) {
                                                gc.strokeRect(initialClick.getKey(), initialClick.getValue(), width, width);
                                            } else {
                                                gc.strokeRect(initialClick.getKey(), initialClick.getValue(), height, height);
                                            }
                                        }
                                    }
                                }
                            }

                            initialClick = new Pair(0, 0);
                        }
                        //saves the edited canvas to the wim
                        pane.snapshot(null, wim);
                    }
                });
            }
        });
        //sets the tool tip for this button
        square.setTooltip(new Tooltip("Draw a square"));
        //Adding this button to the toggle group so that no other drawing tools
        //can be selected at the same time.
        square.setToggleGroup(toggleGroup);
        
        return square;
    }

    /**
     * ToggleButton for "Draw Ellipse" is created as well as the corresponding
     * EventHandler. The EventHandler handles the drawing of an ellipse on the
     * canvas. The ellipse is drawn by clicking and dragging. The ellipse will
     * be drawn within an imaginary rectangle that starts where the mouse is
     * clicked, then goes to the point where the mouse is released.
     *
     * @return "Draw Ellipse" ToggleButton to be added to the toolBar
     * @see addRectangle()
     */
    private ToggleButton addEllipse() {
        ToggleButton ellipse = new ToggleButton("Draw Ellipse");

        ellipse.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                //Sets booleans that may cause errors
                canDraw = true;
                shapeSelected = true;
                straightLineSelected = false;
                currentShape = "ellipse";
                //the following triggers when the mouse is pressed
                canvas.setOnMousePressed(new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent event) {
                        //resizes the canvas if necessary
                        canvas.setWidth(imageView.getFitWidth());
                        canvas.setHeight(imageView.getFitHeight());
                        if (canDraw && !straightLineSelected && shapeSelected) {
                            //saves the coordinates of where the mouse was pressed on the canvas
                            initialClick = new Pair(event.getX(), event.getY());
                        }
                    }
                });
                //the following triggers when the mouse is released after the initial press
                canvas.addEventHandler(MouseEvent.MOUSE_RELEASED, new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent event) {
                        if (canDraw && !straightLineSelected && shapeSelected && currentShape == "ellipse") {
                            //sets cosmetic variables
                            gc.setLineWidth(currentWidth);
                            gc.setStroke(currentColor);
                            gc.setFill(currentFillColor);
                            //handler if the mouse is released anywhere up and/or to the right of the initial mouse press
                            if ((event.getX() - initialClick.getKey() >= 0.0) && (event.getY() - initialClick.getValue() >= 0.0)) {
                                if (currentFillColor != null) {
                                    gc.fillOval(initialClick.getKey(), initialClick.getValue(), (event.getX() - initialClick.getKey()), (event.getY() - initialClick.getValue()));
                                } else {
                                    gc.strokeOval(initialClick.getKey(), initialClick.getValue(), (event.getX() - initialClick.getKey()), (event.getY() - initialClick.getValue()));
                                }
                            } else {//handler if the mouse is released anywhere up and/or to the left of the initial mouse press
                                if ((event.getX() - initialClick.getKey() < 0) && (event.getY() - initialClick.getValue() >= 0)) {
                                    double originalX = initialClick.getKey();
                                    double originalY = initialClick.getValue();
                                    initialClick = new Pair(event.getX(), originalY);
                                    if (currentFillColor != null) {
                                        gc.fillOval(initialClick.getKey(), initialClick.getValue(), (originalX - initialClick.getKey()), (event.getY() - initialClick.getValue()));
                                    } else {
                                        gc.strokeOval(initialClick.getKey(), initialClick.getValue(), (originalX - initialClick.getKey()), (event.getY() - initialClick.getValue()));
                                    }
                                } else {//handler if the mouse is released anywhere down and/or to the right of the initial mouse press
                                    if ((event.getX() - initialClick.getKey() >= 0) && (event.getY() - initialClick.getValue() < 0)) {
                                        double originalX = initialClick.getKey();
                                        double originalY = initialClick.getValue();
                                        initialClick = new Pair(originalX, event.getY());
                                        if (currentFillColor != null) {
                                            gc.fillOval(initialClick.getKey(), initialClick.getValue(), (event.getX() - initialClick.getKey()), (originalY - initialClick.getValue()));
                                        } else {
                                            gc.strokeOval(initialClick.getKey(), initialClick.getValue(), (event.getX() - initialClick.getKey()), (originalY - initialClick.getValue()));
                                        }
                                    } else {//handler if the mouse is released anywhere down and/or to the left of the initial mouse press
                                        if ((event.getX() - initialClick.getKey() < 0) && (event.getY() - initialClick.getValue() < 0)) {
                                            double originalX = initialClick.getKey();
                                            double originalY = initialClick.getValue();
                                            initialClick = new Pair(event.getX(), event.getY());
                                            if (currentFillColor != null) {
                                                gc.fillOval(initialClick.getKey(), initialClick.getValue(), (originalX - initialClick.getKey()), (originalY - initialClick.getValue()));
                                            } else {
                                                gc.strokeOval(initialClick.getKey(), initialClick.getValue(), (originalX - initialClick.getKey()), (originalY - initialClick.getValue()));
                                            }
                                        }
                                    }
                                }
                            }

                        }
                        //saves the edited canvas to the wim
                        pane.snapshot(null, wim);
                    }
                });
            }
        });
        //sets the tool tip for this button
        ellipse.setTooltip(new Tooltip("Draw an ellipse"));
        //Adding this button to the toggle group so that no other drawing tools
        //can be selected at the same time.
        ellipse.setToggleGroup(toggleGroup);
        
        return ellipse;
    }

    /**
     * ToggleButton for "Draw Circle" is created as well as the corresponding
     * EventHandler. The EventHandler handles the drawing of a circle on the
     * canvas. The circle is drawn by clicking and dragging. The circle will be
     * drawn in an imaginary square that starts where the mouse is clicked, and
     * whose side length is determined by the y-coordinate difference between
     * the original click and the point where the mouse is released. Like the
     * square, the circle can only be drawn if the mouse is dragged
     * up-and-to-the-left or down-and-to-the-right of the original click.
     *
     * @return "Draw Rectangle" ToggleButton to be added to the toolBar
     * @see addSquare()
     */
    private ToggleButton addCircle() {
        ToggleButton circle = new ToggleButton("Draw Circle");

        circle.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                //Sets booleans that may cause errors
                canDraw = true;
                shapeSelected = true;
                straightLineSelected = false;
                currentShape = "circle";
                canvas.setOnMousePressed( new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent event) {
                        //resizes the canvas if necessary
                        canvas.setWidth(imageView.getFitWidth());
                        canvas.setHeight(imageView.getFitHeight());
                        if (canDraw && !straightLineSelected && shapeSelected) {
                            //saves the coordinates of where the mouse was pressed on the canvas
                            initialClick = new Pair(event.getX(), event.getY());
                        }
                    }
                });
                canvas.addEventHandler(MouseEvent.MOUSE_RELEASED, new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent event) {
                        if (canDraw && !straightLineSelected && shapeSelected && currentShape == "circle") {
                            //sets cosmetic variables
                            gc.setLineWidth(currentWidth);
                            gc.setStroke(currentColor);
                            gc.setFill(currentFillColor);
                            //handler if the mouse is released anywhere up and/or to the right of the initial mouse press
                            if ((event.getX() - initialClick.getKey() >= 0) && (event.getY() - initialClick.getValue() >= 0)) {
                                if ((event.getX() - initialClick.getKey()) > (event.getY() - initialClick.getValue())) {
                                    double lesser = event.getY() - initialClick.getValue();
                                    if (currentShape == "circle") {
                                        if (currentFillColor != null) {
                                            gc.fillOval(initialClick.getKey(), initialClick.getValue(), lesser, lesser);
                                        } else {
                                            gc.strokeOval(initialClick.getKey(), initialClick.getValue(), lesser, lesser);
                                        }
                                    }
                                } else {
                                    double lesser = event.getX() - initialClick.getValue();
                                    if (currentShape == "circle") {
                                        if (currentFillColor != null) {
                                            gc.fillOval(initialClick.getKey(), initialClick.getValue(), lesser, lesser);
                                        } else {
                                            gc.strokeOval(initialClick.getKey(), initialClick.getValue(), lesser, lesser);
                                        }
                                    }
                                }
                            } else {//handler if the mouse is released anywhere up and/or to the left of the initial mouse press
                                if ((event.getX() - initialClick.getKey() < 0) && (event.getY() - initialClick.getValue() >= 0)) {
                                    double originalX = initialClick.getKey();
                                    double originalY = initialClick.getValue();

                                    double width = Math.abs(originalX - initialClick.getKey());
                                    double height = Math.abs(originalY - initialClick.getValue());

                                    if (width < height) {
                                        if (currentShape == "circle") {
                                            if (currentFillColor != null) {
                                                gc.fillOval(event.getX(), event.getY(), width, width);
                                            } else {
                                                gc.strokeOval(event.getX(), event.getY(), width, width);
                                            }
                                        }
                                    } else {
                                        if (currentShape == "circle") {
                                            if (currentFillColor != null) {
                                                gc.fillOval(event.getX(), event.getY(), height, height);
                                            } else {
                                                gc.strokeOval(event.getX(), event.getY(), height, height);
                                            }
                                        }
                                    }
                                } else {//handler if the mouse is released anywhere down and/or to the right of the initial mouse press
                                    if ((event.getX() - initialClick.getKey() >= 0) && (event.getY() - initialClick.getValue() < 0)) {
                                        double originalX = initialClick.getKey();
                                        double originalY = initialClick.getValue();
                                        initialClick = new Pair(originalX, event.getY());
                                        double width = Math.abs(originalX - initialClick.getKey());
                                        double height = Math.abs(originalY - initialClick.getValue());
                                        if (currentShape == "circle") {
                                            if (width < height) {
                                                if (currentFillColor != null) {
                                                    gc.fillOval(initialClick.getKey(), initialClick.getValue(), width, width);
                                                } else {
                                                    gc.strokeOval(initialClick.getKey(), initialClick.getValue(), width, width);
                                                }
                                            } else {
                                                if (currentFillColor != null) {
                                                    gc.fillOval(initialClick.getKey(), initialClick.getValue(), height, height);
                                                } else {
                                                    gc.strokeOval(initialClick.getKey(), initialClick.getValue(), height, height);
                                                }
                                            }
                                        }
                                    } else {//handler if the mouse is released anywhere down and/or to the left of the initial mouse press
                                        if ((event.getX() - initialClick.getKey() < 0) && (event.getY() - initialClick.getValue() < 0)) {
                                            double originalX = initialClick.getKey();
                                            double originalY = initialClick.getValue();
                                            initialClick = new Pair(event.getX(), event.getY());
                                            double width = Math.abs(originalX - initialClick.getKey());
                                            double height = Math.abs(originalY - initialClick.getValue());
                                            if (currentShape == "circle") {
                                                if (width < height) {
                                                    if (currentFillColor != null) {
                                                        gc.fillOval(initialClick.getKey(), initialClick.getValue(), width, width);
                                                    } else {
                                                        gc.strokeOval(initialClick.getKey(), initialClick.getValue(), width, width);
                                                    }
                                                } else {
                                                    if (currentFillColor != null) {
                                                        gc.fillOval(initialClick.getKey(), initialClick.getValue(), height, height);
                                                    } else {
                                                        gc.strokeOval(initialClick.getKey(), initialClick.getValue(), height, height);
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            initialClick = new Pair(0, 0);
                        }
                        //saves the edited canvas to the wim
                        pane.snapshot(null, wim);
                    }
                });
            }
        });
        //sets the tool tip for this button
        circle.setTooltip(new Tooltip("Draw a circle"));
        //Adding this button to the toggle group so that no other drawing tools
        //can be selected at the same time.
        circle.setToggleGroup(toggleGroup);
        
        return circle;
    }

    /**
     * ToggleButton for "Grab Color" is created as well as the corresponding
     * EventHandler. The EventHandler handles the grabbing of the color where
     * the mouse is when it is clicked. The global color is set to this value.
     *
     * @return "Grab Color" ToggleButton to be added to the toolBar
     * @see addSquare()
     */
    private ToggleButton addColorGrab() {
        ToggleButton colorGrab = new ToggleButton("Grab Color");

        colorGrab.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                //Sets a boolean that may cause errors
                canDraw = false;
                canvas.setOnMouseClicked(new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent event) {
                        //saves the canvas to the wim
                        canvas.snapshot(null, wim);
                        //sets the color to whatever the color is at the position of the mouse
                        currentColor = wim.getPixelReader().getColor((int) event.getX(), (int) event.getY());
                        colorGrabbed = true;
                    }
                });
            }
        });
        //sets the tool tip for this button
        colorGrab.setTooltip(new Tooltip("Grab a color from the canvas"));
        //Adding this button to the toggle group so that no other drawing tools
        //can be selected at the same time.
        colorGrab.setToggleGroup(toggleGroup);
        
        return colorGrab;
    }

    /**
     * Sets the current WritableImage to a new WritableImage. The WritableImage
     * is used in multiple methods, and should only be changed or set when
     * necessary.
     *
     * @param wim New WritableImage to be used
     */
    public void setWim(WritableImage wim) {
        this.wim = wim;
    }

    /**
     * Sets the current Pane to a new Pane. This Pane is used primarily to save
     * the current image with any potential changes made to it.
     *
     * @param pane New Pane to be used
     */
    public void setPane(Pane pane) {
        this.pane = pane;
    }

    /**
     * Gets the current Canvas being used by the toolBar.
     *
     * @return Canvas being used
     */
    public Canvas getCanvas() {
        return canvas;
    }

    /**
     * Gets the current WritableImage being used by the toolBar.
     *
     * @return Current version of the WritableImage being used
     */
    public WritableImage getWim() {
        return wim;
    }

    /**
     * Gets the boolean value that indicates whether or not the canvas has been
     * drawn on.
     *
     * @return true if the canvas has been drawn on.
     */
    public Boolean beenDrawnOn() {
        return drawnOn;
    }

    /**
     * Sets the canvas of the current toolBar to the inputed Canvas c
     *
     * @param c New canvas to be worked on by the toolBar
     */
    public void setCanvas(Canvas c) {
        canvas = c;
    }

    /**
     * Sets the graphics context of the current toolBar to the inputed Graphics
     * Context gc. This method should be used when the Canvas is changed.
     * Setting the new graphics context to be that of the new Canvas.
     *
     * @param gc New graphics context to be used by the toolBar.
     */
    public void setGraphicsContext(GraphicsContext gc) {
        this.gc = gc;
    }

    /**
     * Gets the message that will be logged that tells the user what the current
     * tool being used is as well as telling the user what the current file
     * being worked on is. This is only meant to be used for logging.
     *
     * @return logMessage - The message to be logged
     */
    public String getLogMessage() {
        String logMessage = "";
        //Log message contains the current opened file (if there is one)
        //as well as the current tool selected.
        if (paint.menuBar.file != null) {
            logMessage = "Current Tool Selected: " + toggleGroup.getSelectedToggle().toString()
                    + "\n "
                    + "Current File: " + paint.menuBar.file.getAbsolutePath();
        } else {
            if (toggleGroup.getSelectedToggle() == null) {
                logMessage = "Current Tool Selected: NONE"
                        + "\n Current File: Unsaved";
            } else {
                logMessage = "Current Tool Selected: " + toggleGroup.getSelectedToggle().toString()
                        + "\n Current File: Unsaved";
            }
        }

        return logMessage;
    }

}
