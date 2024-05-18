package org.martincorp.Codec;

import java.io.BufferedReader; 
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.martincorp.Interface.GUI;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;

import javafx.scene.control.ButtonType;

public class SettingsParser {
    //Variables:
      //file variables:
    public static String path = "";
    private static File file = new File("");

      //settings values:
    private static String name = "";
    private static String language = "";
    private static boolean server = false;
    private static String link = "";
    private static int port;

    //Builder:
    public SettingsParser(String p){
        path = p;
        
        try{
            file = new File(path);
            if(file.exists()){
                boolean found = false;
                
                BufferedReader bf = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8));
                String json = bf.readLine();
                bf.close();

                if(json != null){
                    JsonArray array = JsonParser.parseString(json).getAsJsonArray();

                    if(array.size() > 0){
                        for(int i = 0; i < array.size(); i++){
                            JsonObject prof = array.get(i).getAsJsonObject();

                            if(prof.get("active").getAsBoolean()){
                                name = prof.get("name").getAsString();
                                language = prof.get("language").getAsString();
                                server = prof.get("server").getAsBoolean();
                                link = prof.get("link").getAsString();
                                port = prof.get("port").getAsInt();

                                found = true;
                                break;
                            }
                        }
                    }

                    if(!found){
                        writeDefault();
                    }
                }
                else{
                    writeDefault();
                }
            }
            else{
                writeDefault();
            }
        }
        catch(IllegalStateException ise){
            ise.printStackTrace();

        }
        catch(FileNotFoundException fnfe){
            fnfe.printStackTrace();
            GUI.launchMessage(2, "Error de lectura", "No se ha podido encontrar el archivo de configuración.\n\n" + fnfe.getMessage());
        }
        catch(IOException ioe){
            ioe.printStackTrace();
            GUI.launchMessage(2, "Error de escritura", "Ha ocurrido un error al intentar crear un\nnuevo perfil de configuración.\n\n" + ioe.getMessage());
        } 
    }

    public SettingsParser(){
        
    }

    //Methods:
      /*As a warning for any new dev in this section, JSON works in UTF-8 text enconding, so make sure to read/write that way, there's an easy example in many methods of the first version of the parse.
        Extracted from (23/08/2023):
        --WTF is this, does the same with 'ñ' & 'á'. JsonParser might have something but JsonStreamParser automatically creates JsonElements from the Reader. FUUUUUUCK.
        --Ok, so i just needed to add the proper charset the file readers, easiest way seems to be StandardCharsets.'CHARSET', BufferedReader/Writer are a viable option again.
      */
    public void newProfile(String n){
        Gson g = new Gson();
        boolean repeat = false;
        boolean done = false;

        //Check if the new name already exists:
        for(String name : getNames()){
            if(name.equals(n)){
                repeat = true;
                break;
            }
        }

        if(!repeat){
            try{
                JsonArray array = getProfiles();
                
                JsonObject newProf = new JsonObject();
                newProf.addProperty("name", n);
                newProf.addProperty("active", false);
                newProf.addProperty("language", language);
                newProf.addProperty("server", false);
                newProf.addProperty("link", "neutronmailservice.duckdns.org");
                newProf.addProperty("port", 3306);
                array.add(newProf);

                FileWriter fw = new FileWriter(file, StandardCharsets.UTF_8);
                fw.write(g.toJson(array));
                fw.close();
                done = true;
            }
            catch(FileNotFoundException fnfe){
                fnfe.printStackTrace();
                GUI.launchMessage(2, "Error de lectura", "No se ha podido encontrar el archivo de configuración.\n\n" + fnfe.getMessage());
                GUI.restartSettings("settings.set");
            }
            catch(IOException ioe){
                ioe.printStackTrace();
                GUI.launchMessage(2, "Error de escritura", "Ha ocurrido un error al intentar\nleer datos de configuración.\n\n" + ioe.getMessage());
            }

            if(done){
                Optional<ButtonType> load = GUI.launchMessage(1, "Advertencia", "Se ha creado un nuevo perfil con éxito,\ndesea cargar este perfil para poder editarlo?");

                if(load.isPresent() && load.get() == ButtonType.OK){
                    loadProfile(n);
                }
            }
        }
        else{
            GUI.launchMessage(5, "Datos incorrectos", "Ya existe un perfil de configuración con el\nmismo nombre, cambie el nombre o borre el perfil\nya existente.");
        }
    }

    public void writeDefault(){
        Gson g = new Gson();

        JsonArray array = new JsonArray();
        JsonObject defProf = new JsonObject();

        defProf.addProperty("name", "Default");
        defProf.addProperty("active", false);
        defProf.addProperty("language", "English");
        defProf.addProperty("server", false);
        defProf.addProperty("link", "neutronmailservice.duckdns.org");
        defProf.addProperty("port", 3306);
        array.add(defProf);

        writeProfiles(array);

        name = "Default";
        language = "English";
        server = false;
        link = "neutronmailservice.duckdns.org";
        port = 3306;
    }

    public void writeProfiles(JsonArray profiles){
        Gson g = new Gson();
        try{
            FileWriter fw = new FileWriter(file, StandardCharsets.UTF_8);
            fw.write(g.toJson(profiles));
            fw.close();
        }
        catch(FileNotFoundException fnfe){
            fnfe.printStackTrace();
            GUI.launchMessage(2, "Error de lectura", "No se ha podido encontrar el archivo de configuración.\n\n" + fnfe.getMessage());
            GUI.restartSettings("settings.set");
        }
        catch(IOException ioe){
            ioe.printStackTrace();
            GUI.launchMessage(2, "Error de escritura", "Ha ocurrido un error al intentar\nleer datos de configuración.\n\n" + ioe.getMessage());
        }
    }
    
    public JsonArray getProfiles(){
        JsonArray profiles = new JsonArray();

        try{
            profiles = JsonParser.parseReader(new JsonReader(new FileReader(file, StandardCharsets.UTF_8))).getAsJsonArray();
        }
        catch(FileNotFoundException fnfe){
            fnfe.printStackTrace();
            GUI.launchMessage(2, "Error de lectura", "No se ha podido encontrar el archivo de configuración.\n\n" + fnfe.getMessage());
            GUI.restartSettings("settings.set");
        }
        catch(IOException ioe){
            ioe.printStackTrace();
            GUI.launchMessage(2, "Error de escritura", "Ha ocurrido un error al intentar\nleer datos de configuración.\n\n" + ioe.getMessage());
        }
        catch(IllegalStateException ise){
            ise.printStackTrace();
        }
        finally{
            return profiles;
        }
    }
    
    public List<String> getNames(){
        ArrayList<String> names = new ArrayList<String>();

        for(JsonElement obj : getProfiles()){
            names.add(obj.getAsJsonObject().get("name").getAsString());
        }

        return names;
    }

    public void loadProfile(String na){
        Gson g = new Gson();
        int oPos = -1;
        int nPos = -1;
        boolean ol = false;
        boolean ne = false;

        List<String> names = getNames();
        for(String nom : names){
            if(nom.equals(name)){
                oPos = names.indexOf(name);
                ol = true;
            }
            if(nom.equals(na)){
                nPos = names.indexOf(name);
                ne = true;
            }

            if(ol && ne){
                break;
            }
        }

        if(oPos > -1 && nPos > -1){
            JsonArray array = getProfiles();
                
            //Deactivate old profile:
            array.get(oPos).getAsJsonObject().addProperty("active", false);
            //Activate new profile:
            array.get(nPos).getAsJsonObject().addProperty("active", true);

            writeProfiles(array);
                
            name = array.get(nPos).getAsJsonObject().get("name").getAsString();
            language = array.get(nPos).getAsJsonObject().get("language").getAsString();
            server = array.get(nPos).getAsJsonObject().get("server").getAsBoolean();
            link = array.get(nPos).getAsJsonObject().get("link").getAsString();
            port = array.get(nPos).getAsJsonObject().get("port").getAsInt();
        }
        else{
            GUI.launchMessage(2, "Error de lectura", "No se ha encontrado ningún perfil con el nombre " + na + ".\nInténtelo de nuevo más tarde o revise el\n archivo '" + path + "'.");
        }
    }

    public void saveProfile(String profile, String lang, boolean server, String link, int port){
        JsonArray profiles = getProfiles();

        for(JsonElement ele : profiles){
            if(profile.equals(ele.getAsJsonObject().get("name").getAsString())){
                ele.getAsJsonObject().addProperty("language", lang);
                ele.getAsJsonObject().addProperty("server", server);
                ele.getAsJsonObject().addProperty("link", link);
                ele.getAsJsonObject().addProperty("port", port);

                break;
            }
        }

        writeProfiles(profiles);
    }

      //Getters & setters
    public static void setName(String name) {
        SettingsParser.name = name;
    }
    public static String getName() {
        return name;
    }

    public static void setLanguage(String language) {
        SettingsParser.language = language;
    }
    public static String getLanguage() {
        return language;
    }

    public static void setServer(boolean server) {
        SettingsParser.server = server;
    }
    public static boolean getServer() {
        return server;
    }

    public static void setLink(String link) {
        SettingsParser.link = link;
    }
    public static String getLink() {
        return link;
    }

    public static void setPort(int port) {
        SettingsParser.port = port;
    }
    public static int getPort() {
        return port;
    }

    public String toString(){
        String result = "";
        Gson g = new Gson();

        result += "Parser for conf file: " + file.getAbsolutePath()
                + "\nThe file contains ";

        try{
            BufferedReader br = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8));
            JsonArray array = g.fromJson(br, JsonArray.class);
            result += array.size() + " profiles:";

            for(int i = 0; i < array.size(); i++){
                JsonObject prof = array.get(i).getAsJsonObject();
                result += "\n · " + prof.get("name").getAsString();
            }
        }
        catch(FileNotFoundException fnfe){
            fnfe.printStackTrace();
            GUI.launchMessage(2, "Error de lectura", "No se ha podido encontrar el archivo de configuración.\n\n" + fnfe.getMessage());
            GUI.restartSettings("settings.set");
        }
        catch(IOException ioe){
            ioe.printStackTrace();
            GUI.launchMessage(2, "Error de escritura", "Ha ocurrido un error al intentar\nleer datos de configuración.\n\n" + ioe.getMessage());
        }    

        return result;
    }
}
