package org.martincorp.Interface;

import java.io.IOException;
import java.net.URL;

import org.martincorp.Database.MessagerActions;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class TemplateController {
    //@FXML objects section:
    @FXML BorderPane root;
    @FXML Menu backMenu;
    @FXML Label backText;
    @FXML ImageView backView;
    @FXML MenuItem newChatItem;
    @FXML MenuItem searchChatItem;
    @FXML MenuItem newGroupItem;
    @FXML MenuItem searchGroupItem;
    @FXML MenuItem settingsItem;
    @FXML MenuItem closeItem;
    @FXML MenuItem aboutItem;

    //Other Variables:
    private static Stage window;
    private static MessagerActions db = new MessagerActions();
    // private static GrouperActions = new GrouperActions();
    private static BorderPane templateRoot;

    //Equivalent to main method when the controller is started:
    @FXML
    public void initialize(){
        db.checkCert();
        templateRoot = root;

        backMenu.setGraphic(backText);
        URL backImg = getClass().getResource("/Img/back_icon.png");
        backView.setImage(new Image(backImg.toExternalForm()));
    }

    //GUI actions:


    //Methods:
    public static void cleanTemplate(){
        templateRoot.setLeft(null);
        templateRoot.setCenter(null);
        templateRoot.setRight(null);
        templateRoot.setBottom(null);
    }
    
    public static void cleanLeft(){
        templateRoot.setLeft(null);
    }

    public static void cleanRight(){
        templateRoot.setRight(null);
    }

    public static void setLeft(Node node){
        templateRoot.setLeft(node);
    }

    public static void setRight(Node node){
        templateRoot.setRight(node);
    }

    public static void launchTemplate(Stage window){
        try{
            FXMLLoader loader = new FXMLLoader(GUI.class.getResource("/Fxml/template.fxml"));
            Parent root = loader.load();

            ChatScrollController mCont = loader.getController();
            Scene newScene = new Scene(root);
            mCont.setStage(window);

            Platform.runLater( () -> window.setScene(newScene));
        }
        catch(IOException ioe){
            ioe.printStackTrace();
            GUI.launchMessage(2, "Error de interfaz", "No se ha modido cargar la vista seleccionada del programa.\n\n" + ioe.getMessage());
        }
    }

    public static void launchStart(Stage window){
        try{
            FXMLLoader loader = new FXMLLoader(GUI.class.getResource("/Fxml/start.fxml"));
            Parent root = loader.load();

            Stage newWindow = new Stage();
            Scene startScene = new Scene(root);

            StartController sCont = loader.getController();
            sCont.setStage(newWindow);
            sCont.setLogo();

            URL iconURL = GUI.class.getResource("/Img/icon.png");
            newWindow.getIcons().add(new Image(iconURL.toExternalForm()));
            newWindow.setTitle("NeutronMail para administradores");
            newWindow.initModality(Modality.NONE);
            newWindow.setScene(startScene);
            newWindow.show();

            sCont.launchMain();
        }
        catch(IOException ioe){
            ioe.printStackTrace();
            GUI.launchMessage(2, "Error de interfaz", "No se ha modido cargar la vista seleccionada del programa.\n\n" + ioe.getMessage());
        }
    }

    public static void loadMain(Stage window){
        try{               
            FXMLLoader sclLoader = new FXMLLoader(GUI.class.getResource("/Fxml/chatScroll.fxml"));
            Parent sclRoot = sclLoader.load();
            FXMLLoader chtLoader = new FXMLLoader(GUI.class.getResource("/Fxml/chatGrid.fxml"));
            Parent chtRoot = chtLoader.load();

            ChatScrollController mCont = sclLoader.getController();
            mCont.setStage(window);
            ChatGridController cgCont = chtLoader.getController();
            
            Platform.runLater( () -> cleanTemplate());
            Platform.runLater( () -> templateRoot.setLeft(sclRoot));
            Platform.runLater( () -> templateRoot.setCenter(chtRoot));
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

    public static void loadChat(int chatId, boolean chatMode){
        try{
            FXMLLoader loader = new FXMLLoader(GUI.class.getResource("/Fxml/chatGrid.fxml"));
            Parent root = loader.load();

            ChatGridController cGrdCont = loader.getController();
            cGrdCont.setStage(window);

            Platform.runLater( () -> cleanRight());
            Platform.runLater( () -> templateRoot.setRight(root));

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
    }

    public static void launchAbout(){
        try{
            FXMLLoader loader = new FXMLLoader(GUI.class.getResource("/Fxml/about.fxml"));
            Parent root = loader.load();

            Stage newWindow = new Stage();
            Scene aboutScene = new Scene(root);

            AboutController aCont = loader.getController();
            aCont.setStage(newWindow);

            URL iconURL = GUI.class.getResource("/Img/icon.png");
            newWindow.getIcons().add(new Image(iconURL.toExternalForm()));
            newWindow.setTitle("Sobre el cliente de NeutronMail");
            newWindow.setWidth(640);
            newWindow.setHeight(480);
            newWindow.initModality(Modality.APPLICATION_MODAL);
            newWindow.setScene(aboutScene);
            newWindow.showAndWait();
        }
        catch(IOException ioe){
            ioe.printStackTrace();
            GUI.launchMessage(2, "Error de interfaz", "No se ha modido cargar la vista seleccionada del programa.\n\n" + ioe.getMessage());
        }
    }

    public static void setStage(Stage w){
        window = w;
    }
}
