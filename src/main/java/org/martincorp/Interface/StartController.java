package org.martincorp.Interface;

import java.io.IOException;
import java.net.URL;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;

public class StartController{
    //@FXML objects section:
    private @FXML BorderPane bPane;
    private @FXML ImageView logoView;

    //Other variables section:
    private Stage window;

    //Equivalent to main method when the controller is started:
    @FXML
    public void initialize() {}

    //Misc. methods:
    public void setStage(Stage w) {
        window = w;
    }

    public void setLogo(){
        URL logoURL = getClass().getResource("/Img/logo.png");
        Image logo = new Image(logoURL.toExternalForm(), window.getWidth(), window.getHeight(), true, true);
        logoView.setImage(logo);

        try{
            Thread.sleep(750);
        }
        catch(InterruptedException ie){
            ie.printStackTrace();
        }
    }

    public void launchMain(){
        try{               
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fxml/template.fxml"));
            Parent root = loader.load();

            TemplateController tCont = loader.getController();
            tCont.setStage(window);
            tCont.loadMain(window);
            
            Scene newScene = new Scene(root, window.getWidth(), window.getHeight());
            Platform.runLater( () -> window.setScene(newScene));
        }
        catch(IOException ioe){
            ioe.printStackTrace();
            GUI.launchMessage(2, "Error de interfaz", "No se ha modido cargar la vista seleccionada del programa.\n\n" + ioe.getMessage());
        }
        catch(IllegalStateException ise){
            ise.printStackTrace();
            GUI.launchMessage(2, "Error de interfaz", "No se ha modido cargar la vista seleccionada del programa.\n\n" + ise.getMessage());
        }
    }
}
