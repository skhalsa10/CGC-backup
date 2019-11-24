package cgc.cgcstation;

import cgc.utils.Communicator;
import cgc.utils.MapInfo;
import cgc.utils.messages.Message;
import cgc.utils.messages.UpdatedHealth;
import cgc.utils.messages.UpdatedLocation;
import javafx.animation.AnimationTimer;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.paint.Color;


import java.util.HashMap;
import java.util.concurrent.PriorityBlockingQueue;

public class CGCGUI extends AnimationTimer implements Runnable, Communicator {

    private long lastUpdate = 0;
    private PriorityBlockingQueue<Message> messages;
    private Thread messageThread;
    private boolean isRunning;
    private Screen currentScreen;

    //GUI stuff
    private Stage stage;

    //main screen
    private Scene mainScene;
    private HBox mainRoot;

    //animated map
    private Canvas canvas;
    private GraphicsContext gc;
    private StackPane canvasContainer;


    //button stuff
    private VBox leftBPane;
    private VBox rightBPane;
    private Button exitEmergency;
    private Button enterEmergency;
    private Button viewHealth;
    private Button viewFinances;

    //State stuff
    private Point2D  TRexLoc;
    private boolean  TRexHealth = true;
    private HashMap<Integer,Point2D> tourLocations;
    private HashMap<Integer,Point2D> patrolLocations;
    private HashMap<Integer,Point2D> employeeLocations;
    private HashMap<Integer,Point2D> guestLocations;
    private HashMap<Integer,Point2D> kioskLocations;

    private HashMap<Integer,Boolean> tourHealth;
    private HashMap<Integer,Boolean> patrolHealth;
    private HashMap<Integer,Boolean> employeeHealth;
    private HashMap<Integer,Boolean> guestHealth;
    private HashMap<Integer,Boolean> kioskHealth;
    private boolean  elctricFenceHealth = true;




    public CGCGUI(Stage primaryStage, CGCStation cgcStation) {

        isRunning = true;
        messageThread = new Thread(this);
        messages = new PriorityBlockingQueue<>();
        currentScreen = Screen.MAIN;

        tourLocations = new HashMap<>();
        patrolLocations = new HashMap<>();
        employeeLocations = new HashMap<>();
        guestLocations = new HashMap<>();
        kioskLocations = new HashMap<>();

        tourHealth = new HashMap<>();
        patrolHealth = new HashMap<>();
        employeeHealth = new HashMap<>();
        guestHealth = new HashMap<>();
        kioskHealth = new HashMap<>();

        //GUI
        this.stage = primaryStage;
        stage.setTitle("Cretaceous Gardens Controller");

        //init main stuff
        mainRoot = new HBox();
        mainRoot.setAlignment(Pos.CENTER);
        canvasContainer = new StackPane();
        canvas = new Canvas(MapInfo.MAP_WIDTH,MapInfo.MAP_HEIGHT);
        gc = canvas.getGraphicsContext2D();

        //button stuff
        leftBPane = new VBox();
        leftBPane.setAlignment(Pos.CENTER);
        //leftBPane.setSpacing(5);
        //leftBPane.setPadding(new Insets(5, 5, 5, 5));
        rightBPane = new VBox();
        rightBPane.setAlignment(Pos.CENTER);
        //rightBPane.setSpacing(5);
        //rightBPane.setPadding(new Insets(5, 5, 5, 5));
        //buttons
        enterEmergency = new Button("Enter\nEmergency");
        enterEmergency.getStyleClass().add("enterEmergency-button");
        exitEmergency = new Button("Exit\nEmergency");
        exitEmergency.getStyleClass().add("exitEmergency-button");
        viewHealth = new Button("View\nHealth");
        viewHealth.getStyleClass().add("viewHealth-button");
        viewFinances = new Button("View\nFinances");
        viewFinances.getStyleClass().add("viewFinances-button");

        //populate stuff
        leftBPane.getChildren().addAll(enterEmergency,exitEmergency);
        rightBPane.getChildren().addAll(viewHealth,viewFinances);
        canvasContainer.getChildren().addAll(canvas);
        canvasContainer.getStyleClass().add("canvasContainer");
        mainRoot.getChildren().addAll(leftBPane,canvasContainer,rightBPane);

        //TESTING TODO REMOVE GOR GOLIVE
        TRexLoc = new Point2D(MapInfo.CENTER_TREX_PIT.getX(), MapInfo.CENTER_TREX_PIT.getY());


        //create scene and set style sheet
        mainScene = new Scene(mainRoot, 1000, 1000);
        mainScene.getStylesheets().add("cgc/cgcstation/GUI.css");

        //display the stage
        stage.setScene(mainScene);
        stage.show();

        messageThread.start();
        this.start();
    }




    @Override
    public void sendMessage(Message m) {
        messages.put(m);
    }

    @Override
    public void run() {
        while(isRunning){
            try {
                Message m = messages.take();
                processMessage(m);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private synchronized void processMessage(Message m){
        if(m instanceof UpdatedLocation){
            UpdatedLocation m2 = (UpdatedLocation) m;
            switch (m2.getEntityName()){
                case TREX:{
                    TRexLoc = m2.getLoc();
                }
                case GUEST_TOKEN:{
                    //this should replace location if it exists or add it if it doesnt
                    guestLocations.put(m2.getEntityID(), m2.getLoc());
                }
                case TOUR_VEHICLE:{
                    tourLocations.put(m2.getEntityID(), m2.getLoc());
                }
                case EMPLOYEE_TOKEN:{
                    employeeLocations.put(m2.getEntityID(), m2.getLoc());
                }
                case PATROL_VEHICLE:{
                    patrolLocations.put(m2.getEntityID(), m2.getLoc());

                }
            }
        }
        else if(m instanceof UpdatedHealth){
            UpdatedHealth m2 = (UpdatedHealth) m;
            switch (m2.getEntityName()){
                case TREX:{
                    TRexHealth = m2.isHealthStatus();
                }
                case GUEST_TOKEN:{
                    //this should replace location if it exists or add it if it doesnt
                    guestHealth.put(m2.getEntityID(), m2.isHealthStatus());
                }
                case TOUR_VEHICLE:{
                    tourHealth.put(m2.getEntityID(), m2.isHealthStatus());
                }
                case EMPLOYEE_TOKEN:{
                    employeeHealth.put(m2.getEntityID(), m2.isHealthStatus());
                }
                case PATROL_VEHICLE:{
                    patrolHealth.put(m2.getEntityID(), m2.isHealthStatus());

                }
            }
        }
        else{
            System.out.println("Cant process this message sorry");
        }
    }


    /**
     * this is used to paint the gui on the screen. it is needed to draw the animation.
     * @param now
     */
    @Override
    public void handle(long now) {
        //there are 1000 miliseconds in a second. if we divide this by 60 there
        // are 16.666667 ms between frame draws
        if (now - lastUpdate >= 16_667_000) {

            if(currentScreen == Screen.FINANCES){

            }else if(currentScreen == Screen.HEALTH) {

            }
            else {
                renderMainScreen();
            }

            // helped to stabalize the rendor time
            lastUpdate = now;
        }
    }

    private synchronized void renderMainScreen() {
        //first thing we need to do is paint the background of the map
        gc.setFill(MapInfo.CANVASBACKGROUND);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        //Draw road
        gc.setStroke(MapInfo.ROADCOLOR);
        gc.setLineWidth(16);
        gc.strokeLine(MapInfo.ROAD_SOUTH.getX(), MapInfo.ROAD_SOUTH.getY(), MapInfo.ROAD_NORTH.getX(), MapInfo.ROAD_NORTH.getY());
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(2);
        gc.strokeLine(MapInfo.ROAD_SOUTH.getX(), MapInfo.ROAD_SOUTH.getY(), MapInfo.ROAD_NORTH.getX(), MapInfo.ROAD_NORTH.getY());


        //draw the trex pit
        gc.setStroke(MapInfo.TREXPITSTROKE);
        gc.setLineWidth(4);
        gc.setFill(MapInfo.TREXPITFILL);
        gc.fillRect(MapInfo.UPPER_LEFT_TREX_PIT.getX(), MapInfo.UPPER_LEFT_TREX_PIT.getY(),MapInfo.TREX_PIT_WIDTH, MapInfo.TREX_PIT_HEIGHT);
        gc.strokeRect(MapInfo.UPPER_LEFT_TREX_PIT.getX(), MapInfo.UPPER_LEFT_TREX_PIT.getY(),MapInfo.TREX_PIT_WIDTH, MapInfo.TREX_PIT_HEIGHT);

        //draw the South building
        gc.setStroke(MapInfo.SOUTHSTROKE);
        gc.setLineWidth(4);
        gc.setFill(MapInfo.SOUTHFILL);
        gc.fillRect(MapInfo.UPPER_LEFT_SOUTH_BULDING.getX(), MapInfo.UPPER_LEFT_SOUTH_BULDING.getY(),MapInfo.SOUTHBUILDING_WIDTH, MapInfo.SOUTHBUILDING_HEIGHT);
        gc.strokeRect(MapInfo.UPPER_LEFT_SOUTH_BULDING.getX(), MapInfo.UPPER_LEFT_SOUTH_BULDING.getY(),MapInfo.SOUTHBUILDING_WIDTH, MapInfo.SOUTHBUILDING_HEIGHT);


        //DRAW TREX
        gc.setFill(MapInfo.TREX);
        gc.fillOval(TRexLoc.getX(),TRexLoc.getY(),8,8);

        //DRAW KIOSKS
        gc.setFill(MapInfo.KIOSK);
        for(Point2D p: kioskLocations.values()){
            gc.fillRect(p.getX(),p.getY(),8,8);
        }


//        tourLocations = new HashMap<>();
        //DRAW TOUR VEHICLES
        gc.setFill(MapInfo.TOURVEHICLE);
        for(Point2D p: tourLocations.values()){
            gc.fillOval(p.getX(),p.getY(),6,6);
        }
//        patrolLocations = new HashMap<>();
        //DRAW patrol VEHICLES
        gc.setFill(MapInfo.PATROLVEHICLE);
        for(Point2D p: patrolLocations.values()){
            gc.fillOval(p.getX(),p.getY(),6,6);
        }
//        employeeLocations = new HashMap<>();
        //DRAW employee tokens
        gc.setFill(MapInfo.EMPLOYEE);
        for(Point2D p: employeeLocations.values()){
            gc.fillOval(p.getX(),p.getY(),6,6);
        }
//        guestLocations = new HashMap<>();
        gc.setFill(MapInfo.GUEST);
        for(Point2D p: guestLocations.values()){
            gc.fillOval(p.getX(),p.getY(),6,6);
        }


    }
}
