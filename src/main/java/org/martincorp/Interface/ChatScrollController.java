package org.martincorp.Interface;

import org.martincorp.Database.MessagerActions;
import org.martincorp.Model.Chat;
import org.martincorp.Model.Employee;
import org.martincorp.Model.Group;
import org.martincorp.Model.Message;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class ChatScrollController {
    //FXML objects section:
    @FXML private GridPane chatGrid;

    //Other variables:
    private MessagerActions db;
    private Stage window;

    private DateTimeFormatter shortFormatter = DateTimeFormatter.ofPattern("HH:mm");
    private DateTimeFormatter longFormatter = DateTimeFormatter.ofPattern("dd-MM HH:mm");
    private DateTimeFormatter longerFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    //Equivalent to main method when the controller is started:
    @FXML
    public void initialize(){
        db = new MessagerActions();

        loadMessBoard();
    }

    //Methods:
    public void setStage(Stage w){
        this.window = w;
    }

    //TODO: ok theres something wrong here, there are 9 Db requests in one method, 4 or 5 of them are in FUCKING LOOPS, i need to revise this.
    public void loadMessBoard(){
        //A list of every last message of each chat the user has.
        ArrayList<Object> messList = db.getLastMess();
        //Another one for every element we create to represent messages.
        ObservableList<Node> rows;
        
        //Current time, for reference against all messages.
        LocalDateTime aproxNow = LocalDateTime.ofEpochSecond(System.currentTimeMillis() / 1000, 0, OffsetDateTime.now().getOffset());

        //Sorting
          //If there are chats for this user, sort by time and then reverse it for newer to older, else add a placeholder message.
        if(!(messList.size() > 0)){
            messList.add(new Message(1, false, 1, "No hay chats recientes", null, false, aproxNow, false));
        }

        //Rendering
        Iterator messIte = messList.iterator();

        if(messList.size() > 0){
            //For every message get needed info and put it into JavaFX elements, add everything to the array at the top 
            while(messIte.hasNext()){
                Message m = (Message) messIte.next();
                Employee e = (Employee) messIte.next();
                
                Group g = new Group();
                if(!m.getMode()){
                    g = (Group) messIte.next();
                }

                int unread = (Integer) messIte.next();

                //DONE: Get chat name, last message filename and last message date.
                  //Find out how to properly get the other user's name. (Do it in DBActions?).
                Label nameLabel = new Label();

                //User name (alias) of the other user or group name.
                if(m.getMode()){
                    nameLabel.setText(e.getAlias());
                }
                else{
                    nameLabel.setText(g.getName());
                }

                nameLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 15.0));

                //Timestamp of the last message and comparison to the previously declared timestamp (now).
                LocalDateTime messDate = m.getSendTime();
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

                timeLabel.setFont(new Font("Segoe UI", 15));
                chatGrid.addRow(chatGrid.getRowCount(), nameLabel, timeLabel);
                
                //Add a corresponding new click listener to every Node created previously.
                rows = chatGrid.getChildren();
                for(Node row : rows){
                    if(chatGrid.getRowIndex(row) == chatGrid.getRowCount() - 1){
                        chatGrid.setMargin(row, new Insets(8, 8, 0, 8));

                        if(m.getChat() != 1){
                            row.setOnMouseClicked(new EventHandler<MouseEvent>() {
                                @Override
                                public void handle(MouseEvent e){
                                    TemplateController.loadChat(m.getChat(), m.getMode());
                                }
                            });
                        }
                    }
                }

                //Get message main text. ADD CHECK TO GET FILENAME IF NO MESSAGE. 
                Label messinfoLabel = new Label();

                if(!m.getMode()){
                    messinfoLabel.setText(e.getAlias() + ": ");
                }

                if(!m.getMessage().equals("")){
                    messinfoLabel.setText(messinfoLabel.getText() + m.getMessage());
                }
                else{
                    messinfoLabel.setText(messinfoLabel.getText() + m.getFilename());
                }

                messinfoLabel.setFont(new Font("Segoe UI", 15));


                //Same for read/unread state
                if(unread == 0){
                    chatGrid.addRow(chatGrid.getRowCount(), messinfoLabel);
                }
                else{
                    Label unreadLabel = new Label(Integer.toString(unread) + "  ");
                    unreadLabel.setFont(new Font("Segoe UI", 15));
                    chatGrid.addRow(chatGrid.getRowCount(), messinfoLabel, unreadLabel);
                }
                 
                //Again, add click listeners.
                rows = chatGrid.getChildren();
                for(Node row : rows){
                    if(chatGrid.getRowIndex(row) == chatGrid.getRowCount() - 1){
                        chatGrid.setMargin(row, new Insets(0, 8, 8, 8));

                        if(m.getChat() != 1){
                            row.setOnMouseClicked(new EventHandler<MouseEvent>() {
                                @Override
                                public void handle(MouseEvent e){
                                    TemplateController.loadChat(m.getChat(), m.getMode());
                                }
                            });
                        }   
                    }
                }
            }
        }
        else{
            GUI.launchMessage(2, "Error de interfaz", "No se ha podido cargar el listado de chats.");
        }
    }
}
