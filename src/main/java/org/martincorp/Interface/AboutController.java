package org.martincorp.Interface;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

public class AboutController {
    //@FXML objects section:
    @FXML private Label javaVerText;
    @FXML private Label fxVerText;
    @FXML private Button closeBut;

    //Other variables section:
    private Stage window;

    //Equivalent to main method when the controller is started:
    @FXML
    public void initialize() {
        javaVerText.setText(javaVerText.getText() + Runtime.version().toString());
        fxVerText.setText(fxVerText.getText() + FXMLLoader.JAVAFX_VERSION);
        initListeners();
    }

    //Object's listeners initializer:
    private void initListeners(){
        closeBut.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent e){
                window.close();
            }
        });
        closeBut.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent e){
                if(e.getCode() == KeyCode.ENTER){
                    window.close();
                }
            }
        });
    }

    //Misc. methods:
    public void setStage(Stage w){
        this.window = w;
    }
}
