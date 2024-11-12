package org.martincorp.Interface;

import java.io.File;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.martincorp.Codec.Encrypt;
import org.martincorp.Database.MessagerActions;
import org.martincorp.Model.BarMessage;
import org.martincorp.Model.Chat;
import org.martincorp.Model.Employee;
import org.martincorp.Model.Group;
import org.martincorp.Model.Message;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.FileChooser.ExtensionFilter;

public class ChatGridController {
    //FXML objects section:
    @FXML private ScrollPane scrollPane;
    @FXML private GridPane chatGrid;
    @FXML private TextField messageText;
    @FXML private Button fileBut;
    @FXML private CheckBox verifCheck;
    @FXML private Button sendBut;

    //Other variables:
    private static MessagerActions db = new MessagerActions();
    private Encrypt enc = new Encrypt();
    private Stage window;

    private DateTimeFormatter shortFormatter = DateTimeFormatter.ofPattern("HH:mm");
    private DateTimeFormatter longFormatter = DateTimeFormatter.ofPattern("dd-MM HH:mm");
    private DateTimeFormatter longerFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    private File file;
    private FileChooser fileDialog = new FileChooser();

    private static Chat chat;
    private static Group group;
    private static ArrayList<Employee> chatUsers;
    private static Boolean chatMode = null;
    private static int offset = 0;
    private static List<BarMessage> messageBar = new ArrayList<BarMessage>();


    //Equivalent to main method when the controller is started:
    @FXML
    public void initialize(){
        chatGrid.setPrefWidth(900);
        fileDialog.setTitle("Seleccione un archivo");
        fileDialog.getExtensionFilters().add(new ExtensionFilter("All Files", "*.*"));
    }

    //GUI actions:
      //scrollPane
    @FXML private void scrollCheck(ScrollEvent e){
        if(scrollPane.getVvalue() == 0.0){
            if(db.isMessageAvailable(chatMode ? chat.getId() : group.getId(), chatMode, offset)){
                loadMessages(db.getMessages(chatMode ? chat.getId() : group.getId(), chatMode, offset));
            }
        }
    }
      //fileBut
    @FXML private void fileButEnter(KeyEvent e){
        if(e.getCode() == KeyCode.ENTER){
            selectFile();
        }
    }
      //sendBut
    @FXML private void sendButEnter(KeyEvent e){
        if(e.getCode() == KeyCode.ENTER){
            sendMessage();
        }
    }

    //Methods:
    public void extSetup(int newId, boolean mode){
        //DONE: create static list that saves the contents of the file bottom bar.
        Iterator<BarMessage> ite = messageBar.iterator();

        //Taking note of this proccess cause im getting constantly lost
        while(ite.hasNext()){//Iterating through all saved messages.
            BarMessage message = ite.next();

            if(message.getId() == newId){ //Paso 4: hay algún mensaje ya guardado para este chat?
                //Paso 1: es el 'new' chat el 'old' chat ya cargado?
                if(chatMode != null){
                    if((chatMode ? chat.getId() : group.getId()) != newId){
                        Iterator<BarMessage> ite2 = messageBar.iterator();
                        while(ite.hasNext()){
                            if(ite2.next().getId() == (chatMode ? chat.getId() : group.getId())){//Paso 5: hay algún mensaje ya guardado para 'old' chat?
                                ite2.remove();
                            }
                        }

                        //Paso 6: guardar la barra actual
                        messageBar.add(new BarMessage(chatMode ? chat.getId() : group.getId(), messageText.getText(), file != null ? file.getName() : null, file != null ? file.getAbsolutePath() : null, mode, verifCheck.isSelected()));
                    
                        //Paso 7: cargar la nueva barra
                        messageText.setText(message.getMessage());
                        fileBut.setText(message.getFileName());
                        file = new File(message.getFilePath());
                        mode = message.getMode();
                        verifCheck.setSelected(message.getVerif());
                    }
                }
                //Si? Pues no se carga nada
            }
        }

        chatMode = mode;
        if(mode){
            chat = db.getChatById(newId);
            group = null;
        }
        else{
            chat = null;
            group = db.getGroupById(newId);
        } 

        //Decide what we have to load:
        /*
         * Ok, so how we force if the program will load more?
         * * Do a check here about if there's more messages to load of how many?
         * * Or do the check when processing the load?
         * (All this also implies that we should disable the load when no more messages can be loaded so we aren't constantly querying and executing code)
         */
        chatUsers = db.getChatUsers(newId, mode); 

        if(mode){
            loadMessages(db.getMessages(newId, mode, 0));
            offset = 25;
        }
        else{
            loadMessages(db.getMessages(newId, mode, 0));
            offset = 25;
        }
    }

    //TODO: Just get the messages and print them? Maybe the dynamic grid should be a unified method to avoid duplicate code
      //Alse should you start from last to first (newest to oldest) so it's easier to know where to put new rows in the grid? or would the reverse be the same?
    private void loadMessages(List<Message> mess){
        //addColumn/addRow doesn't work right with a given number (0) so we need a counter to add to the correct row.
        Iterator<Message> ite = mess.iterator();

        while(ite.hasNext()){
            Message message = ite.next();
            boolean read = false;

            GridPane messGrid = new GridPane();
            messGrid.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            GridPane.setMargin(messGrid, new Insets(0, 0, 0, 6));

            Label startLabel = new Label("");
            startLabel.setWrapText(true);
            messGrid.add(startLabel, 0, 0);
            GridPane.setMargin(startLabel, new Insets(2.5, 5, 2.5, 5));
            
            BorderPane emptyBox = new BorderPane();

            LocalDateTime aproxNow = LocalDateTime.ofEpochSecond(System.currentTimeMillis() / 1000, 0, OffsetDateTime.now().getOffset());

            if(!message.getFilename().equals("empty")){
                Label fileLabel = new Label(message.getFilename());
                fileLabel.setFont(GUI.segoe);
                fileLabel.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
                fileLabel.setWrapText(true);
                GridPane.setMargin(fileLabel, new Insets(2.5, 5, 2.5, 5));

                messGrid.addRow(messGrid.getRowCount(), fileLabel);
            }

            if(message.getMessage() != null){
                Label messLabel = new Label(message.getMessage());
                messLabel.setFont(Font.loadFont(getClass().getResourceAsStream("/Font/Segoe UI.ttf"), 15));
                messLabel.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
                messLabel.setWrapText(true);
                GridPane.setMargin(messLabel, new Insets(2.5, 5, 2.5, 5));

                messGrid.addRow(messGrid.getRowCount(), messLabel);
            }
            
            LocalDateTime messDate = message.getSendTime();
            Label timeLabel = new Label();
            timeLabel.setFont(Font.loadFont(getClass().getResourceAsStream("/Font/Segoe UI Italic.ttf"), 10));
            timeLabel.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            timeLabel.setWrapText(true);
            GridPane.setMargin(timeLabel, new Insets(2.5, 5, 5, 5));

            if(Math.abs(Duration.between(messDate, aproxNow).toDays()) > 365){
                timeLabel.setText(longerFormatter.format(messDate)); 
            }
            else if(Math.abs(Duration.between(messDate, aproxNow).toDays()) > 1){
                timeLabel.setText(longFormatter.format(messDate)); 
            }
            else{
                timeLabel.setText(shortFormatter.format(messDate));
            }

            messGrid.addRow(messGrid.getRowCount(), timeLabel);
            
            //Find sender employee & align texts. 
              //Loop through all employee of active chat/group
            for(Employee emp : chatUsers){
                if(message.getSender() == emp.getId()){
                    //Determine if the sender is the user or another employee
                    boolean isUser = message.getSender() == db.user ? true : false;

                    if(isUser){
                        messGrid.setBackground(new Background(new BackgroundFill(Paint.valueOf("A48EFF"), new CornerRadii(7), new Insets(0))));
                        messGrid.setAlignment(Pos.CENTER_RIGHT);
                    }
                    else{
                        messGrid.setBackground(new Background(new BackgroundFill(Paint.valueOf("C8BBFF"), new CornerRadii(7), new Insets(0))));
                        // messGrid.setAlignment(Pos.CENTER_LEFT);
                    }
                    //WHY DOES ALIGNING NOTHING???? FUCK OFF
                      //gridPane.setAlignment works for general, maybe add textAlign for the labels (doesn't sound good)
                    //Also why doesn't chatGrid nor scrollPane expand??
                      //Is scrollGrid maybe responsible??

                    //Iterate through the message nodes
                    ObservableList<Node> childs = messGrid.getChildren();
                    for(Node label : childs){
                        //If the node is the first AKA the nameLabel...
                        if(GridPane.getRowIndex(label) == 0){
                            //...and the sender is not the user, set the label text, font & alignment.
                            if(!isUser){
                                ((Label) label).setText(emp.getAlias());
                                ((Label) label).setFont(Font.loadFont(getClass().getResourceAsStream("/Font/Segoe UI.ttf"), 15));
                                ((Label) label).setWrapText(true);
                                ((Label) label).setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
                                ((Label) label).setAlignment(Pos.CENTER_LEFT);
                            }
                            else{
                                ((Label) label).setFont(new Font("Segoe UI", 0));
                            }
                        }
                        else{
                            if(isUser){
                                ((Label) label).setAlignment(Pos.CENTER_RIGHT);
                            }
                            else{
                                ((Label) label).setAlignment(Pos.CENTER_LEFT);
                            }
                        }
                    }

                    //Lastly depending on who's the sender set messGrid left or right.
                    if(isUser){
                        chatGrid.addRow(chatGrid.getRowCount(), emptyBox, messGrid);
                    }
                    else{                     
                        chatGrid.addRow(chatGrid.getRowCount(), messGrid, emptyBox);
                    }

                    //Stop the loop, no more iterations needed.
                    break;
                }
            }
        }
    }

    private void printMessage(){
        Platform.runLater(() -> {
            GridPane messGrid = new GridPane();
            messGrid.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            GridPane.setMargin(messGrid, new Insets(0, 0, 0, 6));

            Label startLabel = new Label("");
            startLabel.setWrapText(true);
            messGrid.add(startLabel, 0, 0);
            GridPane.setMargin(startLabel, new Insets(2.5, 5, 2.5, 5));
            
            BorderPane emptyBox = new BorderPane();

            LocalDateTime aproxNow = LocalDateTime.ofEpochSecond(System.currentTimeMillis() / 1000, 0, OffsetDateTime.now().getOffset());

            if(file != null){
                Label fileLabel = new Label(file.getName());
                fileLabel.setFont(GUI.segoe);
                fileLabel.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
                fileLabel.setWrapText(true);
                GridPane.setMargin(fileLabel, new Insets(2.5, 5, 2.5, 5));

                messGrid.addRow(messGrid.getRowCount(), fileLabel);
            }

            if(!messageText.getText().equals("")){
                Label messLabel = new Label(messageText.getText());
                messLabel.setFont(Font.loadFont(getClass().getResourceAsStream("/Font/Segoe UI.ttf"), 15));
                messLabel.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
                messLabel.setWrapText(true);
                GridPane.setMargin(messLabel, new Insets(2.5, 5, 2.5, 5));

                messGrid.addRow(messGrid.getRowCount(), messLabel);
            }

            LocalDateTime time = LocalDateTime.ofEpochSecond(System.currentTimeMillis() / 1000, 0, OffsetDateTime.now().getOffset());
            Label timeLabel = new Label(shortFormatter.format(time));
            timeLabel.setFont(Font.loadFont(getClass().getResourceAsStream("/Font/Segoe UI Italic.ttf"), 10));
            timeLabel.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            GridPane.setMargin(timeLabel, new Insets(2.5, 5, 2.5, 5));

            messGrid.addRow(messGrid.getRowCount(), timeLabel);

            Label loadingLabel = new Label("Enviando... ○");
            loadingLabel.setFont(Font.loadFont(getClass().getResourceAsStream("/Font/Segoe UI.ttf"), 12));
            loadingLabel.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            loadingLabel.setWrapText(true);
            GridPane.setMargin(loadingLabel, new Insets(2.5, 5, 5, 5));

            messGrid.addRow(messGrid.getRowCount(), loadingLabel);

            messGrid.setBackground(new Background(new BackgroundFill(Paint.valueOf("A48EFF"), new CornerRadii(7), new Insets(0))));
            messGrid.setAlignment(Pos.CENTER_RIGHT);

            ObservableList<Node> childs = messGrid.getChildren();
            for(Node label : childs){
                if(GridPane.getRowIndex(label) == 0){
                    ((Label) label).setFont(new Font("Segoe UI", 0));
                }
                else{
                    ((Label) label).setAlignment(Pos.CENTER_RIGHT);
                }
            }

            chatGrid.addRow(chatGrid.getRowCount(), emptyBox, messGrid); 
        });
    }

    private void editSentMessage(String newText){

    }

    @FXML private void selectFile(){
        file = fileDialog.showOpenDialog(window);
        
        if(file.length() <= 268435456){
            fileBut.setText(file.getName());
        }
        else{
            file = null;
            GUI.launchMessage(5, "Tamaño excesivo", "El archivo seleccionado supera el límite de 256 megabytes.");
        }
    }

    @FXML private void sendMessage(){
        Task<Boolean> uploadTask = new Task<Boolean>(){
            @Override
            protected Boolean call() throws Exception {
                printMessage();

                if((chatMode ? chat.getId() : group.getId()) != 1){
                    System.out.println(messageText.getText());
                    /* if(db.sendMessage(chatMode ? chat.getId() : group.getId(), chatMode, !messageText.getText().equals("") ? messageText.getText() : "", file != null ? file.getName() : null, file != null ? file : null, verifCheck.isSelected() ? true : false)){
                        return true;
                    }
                    else{
                        return false;
                    } */
                   return true;
                }
                else{
                    GUI.launchMessage(5, "Sin destinatario", "No se ha seleccionado ningún chat o\ngrupo al que enviar este mensaje.");
                    return false;
                }
            }

            @Override
            protected void succeeded(){

            }

            @Override 
            protected void failed(){

            }

            @Override
            protected void cancelled(){

            }
        };

        new Thread(uploadTask).start();
    }

    public void setStage(Stage w){
        this.window = w;
    }
}
