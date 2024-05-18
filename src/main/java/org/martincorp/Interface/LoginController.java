package org.martincorp.Interface;

import org.martincorp.Database.LoggerActions;
import org.martincorp.Database.LoggerBridge;
import org.martincorp.Database.MessagerBridge;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

//DONE: When password is incorrect, it doesn't even mention it or launch an exception.

public class LoginController {
    //@FXML objects section:
    private @FXML TextField userText;
    private @FXML PasswordField passText;
    private @FXML Button cancelBut;
    private @FXML Button approveBut;
    
    //Other variables section:
    private static Stage window;
    private LoggerActions db;

    //Equivalent to main method when the controller is started:
    @FXML
    public void initialize() {
        initListeners();
        db = new LoggerActions();
    }

    //Object's listeners initializer:
    private void initListeners(){
        userText.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent e){
                if(e.getCode() == KeyCode.ENTER){
                    sendPassword();
                }
            }
        });
        passText.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent e){
                if(e.getCode() == KeyCode.ENTER){
                    sendPassword();
                }
            }
        });

        cancelBut.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent e){
                window.close();
            }
        });
        cancelBut.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent e){
                if(e.getCode() == KeyCode.ENTER){
                    window.close();
                }
            }
        });
        approveBut.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent e){
                if(e.getCode() == KeyCode.ENTER){
                    sendPassword();
                }
            }
        });
        approveBut.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent e){
                sendPassword();
            }
        });
    }
    
    //Misc. methods:
    public static void setStage(Stage w){
        window = w;
    }

    private void sendPassword(){
        if(db.checkUser(userText.getText(), passText.getText())){
            window.close();
            TemplateController.launchStart(window);
        }
    }
}
