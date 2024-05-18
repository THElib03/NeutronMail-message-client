package org.martincorp.Interface;

import org.martincorp.Database.MessagerActions;
import org.martincorp.Model.Chat;
import org.martincorp.Model.Group;
import org.martincorp.Model.Message;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
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

    public void loadMessBoard(){
        List<Chat> chats = db.getChats();
        List<Group> groups = db.getUserGroups();
        ArrayList<Message> mess = new ArrayList<Message>();
        ObservableList<Node> rows;

        LocalDateTime aproxNow = LocalDateTime.ofEpochSecond(System.currentTimeMillis() / 1000, 0, OffsetDateTime.now().getOffset());

        //Fetching
        if(chats != null){
            for(Chat chat : chats){
                mess.add(db.getLastMess(chat.getId(), true)); //Chat last message
                if(mess.get(mess.size() - 1) == null){
                    mess.remove(mess.size() - 1);
                }
            }

            if(groups != null){
                for(Group group : groups){
                    mess.add(db.getLastMess(group.getId(), false)); //Group last message
                    if(mess.get(mess.size() - 1) == null){
                        mess.remove(mess.size() - 1);
                    }
                }
            }
        }

        //Sorting
        if(mess.size() > 0){
            Collections.sort(mess, Collections.reverseOrder());
        }
        else{
            mess.add(new Message(1, false, 1, 1, "No hay chats recientes", null, aproxNow));
        }

        //Rendering
        if(mess.size() > 0){
            for(Message m : mess){
                //DONE: Get chat name, last message filename and last message date.
                  //Find out how to properly get the other user's name. (Do it in DBActions?).
                Label nameLabel = new Label();

                if(m.getMode()){
                    nameLabel.setText(db.getChatUser(m.getChat()).getAlias());
                }
                else{
                    Group grp = db.getGroupById(m.getChat());
                    nameLabel.setText(grp.getName());
                }

                nameLabel.setFont(new Font("Segoe UI", 14));

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

                timeLabel.setFont(new Font("Segoe UI", 14));
                chatGrid.addRow(chatGrid.getRowCount(), nameLabel, timeLabel);

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

                Label filenameLabel = new Label("  " + m.getFilename());
                filenameLabel.setFont(new Font("Segoe UI", 14));

                if(db.getUnreadMess(m.getChat(), m.getMode()) == 0){
                    chatGrid.addRow(chatGrid.getRowCount(), filenameLabel);
                }
                else{
                    Label unreadLabel = new Label(Integer.toString(db.getUnreadMess(m.getChat(), m.getMode())) + "  ");
                    unreadLabel.setFont(new Font("Segoe UI", 14));
                    chatGrid.addRow(chatGrid.getRowCount(), filenameLabel, unreadLabel);
                }

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
    }

    /* public void showChat(int chatId, boolean chatMode){
        try{
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fxml/chatGrid.fxml"));
            Parent root = loader.load();

            ChatGridController cGrdCont = loader.getController();
            cGrdCont.setStage(window);

            Platform.runLater( () -> TemplateController.cleanRight());
            // Platform.runLater( () -> TemplateController.setRight(root));

            cGrdCont.extSetup(chatId, chatMode);
        }
        catch(IOException ioe){
            ioe.printStackTrace();
            GUI.launchMessage(2, "Error de interfaz", "No se ha modido cargar la vista seleccionada del programa.\n\n" + ioe.getMessage());
        }
        catch(IllegalStateException ise){
            ise.printStackTrace();
            GUI.launchMessage(2, "Error de interfaz", "No se ha modido cargar la vista seleccionada del programa.\n\n" + ise.getMessage());
        }
    } */
}
