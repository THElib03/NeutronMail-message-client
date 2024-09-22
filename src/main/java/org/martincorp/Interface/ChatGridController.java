package org.martincorp.Interface;

import java.io.File;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Collections;

import org.martincorp.Codec.Encrypt;
import org.martincorp.Database.MessagerActions;
import org.martincorp.Model.BarMessage;
import org.martincorp.Model.Chat;
import org.martincorp.Model.Employee;
import org.martincorp.Model.Group;
import org.martincorp.Model.Message;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
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
                        if(mode){
                            chat = db.getChatById(newId);
                            group = null;
                        }
                        else{
                            chat = null;
                            group = db.getGroupById(newId);
                        }

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

        //Decide what we have to load:
        /*
         * Ok, so how we force if the program will load more?
         * * Do a check here about it there's more messages to load of how many?
         * * Or do the check when processing the load?
         * (All this also implies that we should disable the load when no more messages can be loaded so we aren't constantly querying and executing code)
         */
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
        Collections.sort(mess, Collections.reverseOrder());
        Iterator<Message> ite = mess.iterator();

        while(ite.hasNext()){
            Message message = ite.next();
            BorderPane messBox = new BorderPane();
            GridPane messGrid = new GridPane();
            AnchorPane emptyBox = new AnchorPane();

            LocalDateTime aproxNow = LocalDateTime.ofEpochSecond(System.currentTimeMillis() / 1000, 0, OffsetDateTime.now().getOffset());

            if(message.getFilename() != null){
                Label fileText = new Label(message.getFilename());
                ImageView fileView = new ImageView();
                messBox.setTop(new Label(message.getFilename()));
                messBox.setCenter(new ImageView());
            }
            if(message.getMessage() != null){
                Label messText = new Label(message.getMessage());


                messBox.setBottom(messText);
            }

            
            LocalDateTime messDate = message.getSendTime();
            Label timeLabel = new Label();

            if(Math.abs(Duration.between(messDate, aproxNow).toDays()) > 365){
                timeLabel.setText(longerFormatter.format(messDate));
            }
            else if(Math.abs(Duration.between(messDate, aproxNow).toDays()) > 1){
                timeLabel.setText(longFormatter.format(messDate)); 
            }
            else{
                timeLabel.setText(shortFormatter.format(messDate));
            }

            if(message.getSender() == db.user){
                messBox.setRight(timeLabel);
            }
            else{
                messBox.setLeft(timeLabel);
            }

            chatGrid.addRow(1, message.getSender() == db.user ? emptyBox : messBox, message.getSender() == db.user ? messBox : emptyBox);
        }
    }

    @FXML private void selectFile(){
        file = fileDialog.showOpenDialog(window);
        
        if(file.length() <= 2147481599){
            fileBut.setText(file.getName());
        }
        else{
            file = null;
            GUI.launchMessage(5, "Tamaño excesivo", "El archivo seleccionado supera el límite de 2 gigabytes.");
        }
    }

    @FXML private void sendMessage(){
        if(chat.getId() != 1){
            db.sendMessage(chatMode ? chat.getId() : group.getId(), chatMode, !messageText.getText().equals("") ? messageText.getText() : null, file != null ? file.getName() : null, file != null ? file : null, verifCheck.isSelected() ? true : false);
        }
        else{
            GUI.launchMessage(5, "Sin destinatario", "No se ha seleccionado ningún chat o\ngrupo al que enviar este mensaje.");
        }
    }

    public void setStage(Stage w){
        this.window = w;
    }
}
