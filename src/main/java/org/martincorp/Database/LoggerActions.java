package org.martincorp.Database;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;

import org.martincorp.Codec.Encrypt;
import org.martincorp.Interface.GUI;

public class LoggerActions {
    //Variables:
    private LoggerBridge bridge;
    private Encrypt enc;

      //SQL variables:
    private final String LOGIN = "SELECT * FROM `user` WHERE user_alias LIKE ?";
    private final String COM_NAME = "SELECT com_name FROM company WHERE com_id = ?";
    private final String ACTIVE = "UPDATE `active` SET act_logged = ? WHERE act_emp = ?";

    //Builder:
    public LoggerActions(){
        bridge = new LoggerBridge();
        enc = new Encrypt(4);
    }

    //Methods:
    public boolean checkUser(String name, String pass){
        boolean done = false;
        PreparedStatement loginSta;
        byte[] salt = null;
        byte[] hash = null;

        if(bridge.connected){ 
            if(!name.equals("") && !pass.equals("")){
                try{
                    //TODO: move login query to user table.
                    loginSta = bridge.conn.prepareStatement(LOGIN);
                    loginSta.setString(1, name);
                    ResultSet res = loginSta.executeQuery();
                    
                    if(res.next()){
                        byte[] wall = res.getBytes(4);
                        salt = Arrays.copyOfRange(wall, 0, 32);
                        hash = Arrays.copyOfRange(wall, 32, 96);
                        if(enc.checkPassword(pass.toCharArray(), salt, hash)){
                            String corp = getCorp(res.getInt(3));
                            MessagerBridge messager = new MessagerBridge(corp);
                            MessagerActions.user = new MessagerActions().getEmployeeByAlias(res.getString(2)).getId();

                            setActive(corp, true);

                            bridge.closeConnection();
                            res.close();

                            if(!bridge.connected){
                                done = true;
                            }
                            else{
                                done = true;
                                GUI.launchMessage(2, "Error de base de datos.", "La conexión de inicio de sesión no se ha cerrado correctamente.\nSe recomienda cerrar la aplicación lo antes posible para evitar una brecha de seguridad.");
                            }
                        }
                        else{
                            done = false;
                            GUI.launchMessage(5, "", "La contraseña introducida no es correcta,\npor favor inténtelo de nuevo.");
                        }
                    }
                    else{
                        done = false;
                        GUI.launchMessage(5, "", "No se ha encontrado ninguna compañía con las credenciales indicadas.");
                    }
                }
                catch(SQLException sqle){
                    sqle.printStackTrace();
                    done = false;
                    GUI.launchMessage(2, "Error de base de datos", "Ha ocurrido un error inesperado durante la comunicación\ncon la base de datos. Inténtelo de nuevo en unos instantes:\n\n" + sqle.getMessage());
                    bridge.checkConnection(true);
                }
            }
            else{
                done = false;
                GUI.launchMessage(5, "Datos incorrectos", "Uno de los campos obligatorios está vacío, por favor inténtelo de nuevo.");
            }
        }
        else{
            bridge.startConnection();
            GUI.retryDatabase("", 1);
        }

        return done;
    }

    public String getCorp(int id){
        PreparedStatement comSta;

        try{
            comSta = bridge.conn.prepareStatement(COM_NAME);
            comSta.setInt(1, id);
            ResultSet res = comSta.executeQuery();

            if(res.next()){
                return res.getString(1);
            }
            else{
                return "MartinCORP";
            }
        }
        catch(SQLException sqle){
            sqle.printStackTrace();
            GUI.launchMessage(2, "Error de base de datos", "Ha ocurrido un error al intentar introducir datos.\n\n" + sqle.getMessage());
            bridge.checkConnection(false);
            return "MartinCORP";
        }
    }

    public void setActive(String corp, boolean state){
        PreparedStatement actSta;

        try{
            bridge.querier.execute("USE " + corp);
            actSta = bridge.conn.prepareStatement(ACTIVE);
            actSta.setBoolean(1, state);
            actSta.setInt(2, MessagerActions.user);
            actSta.executeUpdate();
        }
        catch(SQLException sqle){
            sqle.printStackTrace();
            GUI.launchMessage(2, "Error de base de datos", "Ha ocurrido un error al intentar introducir datos.\n\n" + sqle.getMessage());
            bridge.checkConnection(false);
        }
    }
}
