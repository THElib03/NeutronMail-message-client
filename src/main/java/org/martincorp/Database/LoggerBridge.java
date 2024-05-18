package org.martincorp.Database;

import java.sql.*;

import org.martincorp.Interface.GUI;

public class LoggerBridge {
    //Variables:
    private static String DBName;
    public static boolean connected;
    
    public static Connection conn;
    public static Statement querier;

    //Constructor:
    public LoggerBridge(String db){
        DBName = db;
        startConnection();
    }

    public LoggerBridge(){
        
    }
    
    //Métodos:
    public void startConnection(){
        try{
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection("jdbc:mysql://localhost/companies", "empl_pass", "hAytANtoiDIOtaahIFUEra");

            querier = conn.createStatement();
            querier.execute("USE " + DBName);
            ResultSet res = querier.executeQuery("SELECT * FROM company");
            res.next();

            connected = true;
            res.close();
        }
        catch(ClassNotFoundException cnfe){
            connected = false;
            cnfe.printStackTrace();
            GUI.retryDatabase("No se encuentra el módulo de conexión a la base de datos.\n\n" + cnfe.getMessage(), 1);
        }
        catch(SQLException sqle){
            sqle.printStackTrace();
            connected = false;
            GUI.retryDatabase(sqle.getMessage(), 1);
        }
    }

    public void closeConnection(){
        try{
            querier.close();
            conn.close();
            connected = false;
            System.out.println("Connection to database '" + getDB() + "' closed.");
        }
        catch(SQLException sqle){
            sqle.printStackTrace();
            GUI.launchMessage(2, "Error de base de datos", "Ha ocurrido un error al intentar cerrar la conexión.\nSe recomienda cerrar la aplicación para evitar brechas de seguridad.\n\n" + sqle.getMessage());
        }
    }

    public void checkConnection(boolean db){
        try{
            if(!conn.isValid(3)){
                GUI.retryDatabase("", 1);
            }
        }
        catch(SQLException sqle){
            //This catch is here for literally nothing, is only thrown if timeout is less than 0.
            sqle.printStackTrace();
        }
    }

    public String getDB(){
        return DBName;
    }
}
