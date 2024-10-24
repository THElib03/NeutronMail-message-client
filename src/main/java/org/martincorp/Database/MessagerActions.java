package org.martincorp.Database;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.martincorp.Codec.Encrypt;
import org.martincorp.Interface.GUI;
import org.martincorp.Model.Chat;
import org.martincorp.Model.Employee;
import org.martincorp.Model.Group;
import org.martincorp.Model.Message;

/**
 * @author <a href="https://github.com/THElib03">Martín Marín</a>
 * @version 1.0, 12/16/23
 */
public class MessagerActions {
    //Variables:
    public static int user;
    private int chatOffset = 0;
    private MessagerBridge bridge;
    private Encrypt enc = new Encrypt();

    //SQL variables:
    private final String EMP_BY_ID = "SELECT * FROM employee JOIN `active` ON (employee.emp_id=`active`.act_emp) WHERE emp_id = ?";
    private final String EMP_BY_ALIAS = "SELECT * FROM employee JOIN `active` ON (employee.emp_id=`active`.act_emp) WHERE emp_alias = ?";

    private final String RECEIVER_CERT = "SELECT * FROM certificate WHERE cert_emp = ?";
    private final String USER_CERT = "SELECT * FROM certificate WHERE cert_emp = ?";
    private final String EDIT_EMP_CERT = "UPDATE certificate SET cert_public_key = ? WHERE cert_emp = ?";
    private final String EDIT_GROUP_CERT = "UPDATE certificate SET cert_public_key = ? WHERE cert_group = ?";

    private final String USER_GROUPS = "SELECT * FROM publicgroup WHERE grp_id IN (SELECT gru_group FROM groupuser WHERE gru_user = ?)";
    private final String GROUP_USERS = "SELECT * FROM employee JOIN `active` ON (employee.emp_id=`active`.act_emp) WHERE emp_id IN (SELECT gru_user FROM groupuser WHERE gru_group = ?)";
    private final String GROUP_BY_ID = "SELECT grp_id, grp_name, grp_creationDate FROM publicgroup WHERE grp_id = ?";
    private final String GROUP_BY_NAME = "SELECT";
    private final String NEW_GROUP = "INSERT INTO certificate VALUES (?, 1, ?, ?)";    

    private final String CHAT = "SELECT * FROM chat WHERE chat_id = ?";
    private final String CHAT_USERS = "SELECT emp_id, emp_fname, emp_lname, emp_sDate, emp_eDate, emp_alias, emp_email FROM employee e LEFT JOIN groupuser g ON (e.emp_id = g.gru_user) LEFT JOIN chat c1 ON (e.emp_id = c1.chat_user1) LEFT JOIN chat c2 ON (e.emp_id = c2.chat_user2) WHERE ";

    private final String SEND_MESS = "INSERT INTO message VALUES (NULL, ?, ?, ?, ?, ?, ?, ?, ?, NULL, 0)";
    private final String CHAT_MESS = "SELECT mes_chat, mes_sender, mes_message, mes_filename, IF(mes_file = 'empty', FALSE, TRUE) AS hasFile, mes_sendTime, mes_status FROM message WHERE mes_chat = ? ORDER BY mes_id ASC LIMIT 25 OFFSET ? ";
    private final String GROUP_MESS = "SELECT mes_group, mes_sender, mes_message, mes_filename, IF(mes_file = 'empty', FALSE, TRUE) AS hasFile, mes_sendTime, mes_status FROM message WHERE mes_group = ? ORDER BY mes_id ASC LIMIT 25 OFFSET ?";
    private final String CHAT_AVAILABLE = "SELECT COUNT(*) FROM chat WHERE mes_chat = ? ORDER BY mes_id LIMIT 25 OFFSET ?";
    private final String GROUP_AVAILABLE = "SELECT COUNT(*) FROM chat WHERE mes_group = ? ORDER BY mes_id LIMIT 25 OFFSET ?";
    private final String LAST_MESS = "WITH chat_messages AS(SELECT CASE WHEN mes_chat IS NULL THEN FALSE ELSE TRUE END AS `mode`, mes_chat, mes_group, mes_sender, mes_message, mes_filename, mes_sendTime, COUNT(CASE WHEN mes_status = 0 THEN 1 ELSE NULL END) AS unread_mes, ROW_NUMBER() OVER (PARTITION BY mes_group ORDER BY mes_sendTime DESC) AS rn FROM message GROUP BY mes_chat, mes_group, mes_sender, mes_message, mes_filename, mes_sendTime) SELECT `mode`, mes_chat, mes_group, mes_sender, mes_message, mes_filename, mes_sendTime, unread_mes, IF(`mode` = 0, grp_name, NULL) AS grp_name, emp_alias, CONCAT_WS(' ', emp_fname, emp_lname) FROM chat_messages LEFT JOIN publicgroup ON (mes_group = publicgroup.grp_id) LEFT JOIN employee ON (mes_sender = employee.emp_id) LEFT JOIN chat ON (mes_chat = chat.chat_id) WHERE (mes_group IN (SELECT gru_group FROM groupuser WHERE gru_user = ?) OR (chat_user1 = ? OR chat_user2 = ?)) AND rn = 1 GROUP BY mes_chat, mes_group, mes_sender, mes_message, mes_filename, mes_sendTime";
    private final String NEW_MESS = "INSERT INTO message(mes_chat, mes_sender, mes_receiver, mes_filename, mes_message) VALUES(?, ?, ?, ?, ?)";

    //Builder:
    public MessagerActions(){
        bridge = new MessagerBridge();
    }

    //Methods:
    public Employee getEmployeeById(int id){
        PreparedStatement empSta;

        try{
            empSta = bridge.conn.prepareStatement(EMP_BY_ID);
            empSta.setInt(1, id);

            ResultSet res = empSta.executeQuery();
            if(res.next()){
                return new Employee(res.getInt(1), res.getString(2) + " " + res.getString(3), res.getDate(4), res.getDate(5), res.getString(6), res.getString(7), res.getBoolean(9));
            }
            else{
                return new Employee(0, "Información del empleado no encontrada", new Date(System.currentTimeMillis()), new Date(System.currentTimeMillis()), "", "", false);
            }
        }
        catch(SQLException sqle){
            sqle.printStackTrace();
            GUI.launchMessage(2, "Error de base de datos", "Ha ocurrido un error inesperado durante la comunicación\ncon la base de datos. Inténtelo de nuevo en unos instantes:\n\n" + sqle.getMessage());
            bridge.checkConnection(true);
            return new Employee(0, "Información del empleado no encontrada", new Date(System.currentTimeMillis()), new Date(System.currentTimeMillis()), "", "", false);
        }
    }

    public Employee getEmployeeByAlias(String alias){
        PreparedStatement empSta;

        try{
            empSta = bridge.conn.prepareStatement(EMP_BY_ALIAS);
            empSta.setString(1, alias);

            ResultSet res = empSta.executeQuery();
            if(res.next()){
                return new Employee(res.getInt(1), res.getString(2) + " " + res.getString(3), res.getDate(4), res.getDate(5), res.getString(6), res.getString(7), res.getBoolean(9));
            }
            else{
                return new Employee(0, "Información del empleado no encontrada", new Date(System.currentTimeMillis()), new Date(System.currentTimeMillis()), "", "", false);    
            }
        }
        catch(SQLException sqle){
            sqle.printStackTrace();
            GUI.launchMessage(2, "Error de base de datos", "Ha ocurrido un error inesperado durante la comunicación\ncon la base de datos. Inténtelo de nuevo en unos instantes:\n\n" + sqle.getMessage());
            bridge.checkConnection(true);
            return new Employee(0, "Información del empleado no encontrada", new Date(System.currentTimeMillis()), new Date(System.currentTimeMillis()), "", "", false);
        }
    }

    public void getCert(int userId){}

    public void checkCert(){
        PreparedStatement certSta;

        try{
            certSta = bridge.conn.prepareStatement(USER_CERT);
            certSta.setInt(1, user);

            ResultSet res = certSta.executeQuery();
            res.next();
            if(new String(res.getBytes(4), StandardCharsets.UTF_8).equals("empty")){
                byte[] key = enc.generateNewPair();
                if(key != null){
                    editCert(true, user, key);
                    key = null;
                }
            }
        }
        catch(SQLException sqle){
            sqle.printStackTrace();
            GUI.launchMessage(2, "Error de base de datos", "Ha ocurrido un error inesperado durante la comunicación\ncon la base de datos. Inténtelo de nuevo en unos instantes:\n\n" + sqle.getMessage());
            bridge.checkConnection(true);
        }
    }

    public void editCert(boolean mode, int user, byte[] pubKey){
        PreparedStatement certSta;

        try{
            if(mode){
                certSta = bridge.conn.prepareStatement(EDIT_EMP_CERT);
            }
            else{
                certSta = bridge.conn.prepareStatement(EDIT_GROUP_CERT);
            }
            
            certSta.setBytes(1, pubKey);
            certSta.setInt(2, user);
            certSta.executeUpdate();
        }
        catch(SQLException sqle){
            sqle.printStackTrace();
            GUI.launchMessage(2, "Error de base de datos", "Ha ocurrido un error inesperado\ndurante la modificaciónd de datos\ncon la base de datos. Inténtelo de nuevo en unos instantes:\n\n" + sqle.getMessage());
            bridge.checkConnection(true);
        }
    }

    /**
     * Searches for the group that matches the given ID.
     * @since 0.240924
     * @param id ID of the Group which will be retrieved
     * @return A Group object whose ID matches the one given
     */
    public Group getGroupById(int id){
        Group grp = new Group();
        
        try{
            PreparedStatement grpSta = bridge.conn.prepareStatement(GROUP_BY_ID);
            grpSta.setInt(1, id);
            ResultSet res = grpSta.executeQuery();

            while(res.next()){
                grp.setId(res.getInt("1"));
                grp.setName(res.getString(2));
                grp.setOwner(res.getInt(3));
            }
        }
        catch(SQLException sqle){
            sqle.printStackTrace();
            GUI.launchMessage(id, "Error de base", "");
        }

        return grp;
    }
    
    /**
     * Searches for the Chat that matches a given ID.
     * @since 0.130525
     * @param id id of the chat which will be accesed
     * @return a Chat object whose id matches the one given as a parameter
     */
    public Chat getChatById(int id){
        PreparedStatement chtSta;

        try{
            chtSta = bridge.conn.prepareStatement(CHAT);
            chtSta.setInt(1, id);
            ResultSet chatRes = chtSta.executeQuery();
            chatRes.next();

            return new Chat(chatRes.getInt(1), chatRes.getInt(2), chatRes.getInt(3));
        }
        catch(SQLException sqle){
            sqle.printStackTrace();
            GUI.launchMessage(2, "Error de base de datos", "Ha ocurrido un error inesperado durante la comunicación\ncon la base de datos. Inténtelo de nuevo en unos instantes:\n\n" + sqle.getMessage());
            bridge.checkConnection(true);
            return new Chat(1, 1, 1);
        }
    }

    /**
     * Querys the database for the user or users related to a chat or group.
     * @since 1.0
     * @param id id of the chat which we need the other employee
     * @return returns the employee of the given chat id
     */
    public ArrayList<Employee> getChatUsers(int id, boolean mode){
        ArrayList<Employee> emps = new ArrayList<Employee>();
        PreparedStatement userSta;

        try{
            if(mode){
                userSta = bridge.conn.prepareStatement(CHAT_USERS + "c1.chat_id = ? or c2.chat_id = ? GROUP BY emp_id");
                userSta.setInt(1, id);
                userSta.setInt(2, id);
            }
            else{
                userSta = bridge.conn.prepareStatement(CHAT_USERS + "gru_group = ? GROUP BY emp_id");
                userSta.setInt(1, id);
            }
            ResultSet res = userSta.executeQuery();

            while(res.next()){
                Employee e = new Employee(res.getInt(1), res.getString(2) + " " + res.getString(3), res.getDate(4), res.getDate(5), res.getString(6), res.getString(7), false);
                emps.add(e);
            }
        }
        catch(SQLException sqle){
            sqle.printStackTrace();
            GUI.launchMessage(2, "Error de base de datos", "Ha ocurrido un error durante la\ncomunicación con la base de datos.");
            bridge.checkConnection(true);
        }
        
        return emps;
    }

    public boolean sendMessage(int chat, boolean mode, String message, String filename, File file, boolean hash){
        PreparedStatement messSta; 
        // byte[] newHash = new byte[];

        try{
            messSta = bridge.conn.prepareStatement(SEND_MESS);
            //mes_chat & mes_group
            if(mode){
                messSta.setInt(1, chat);
                messSta.setNull(2, Types.INTEGER);
            }
            else{
                messSta.setNull(1, Types.INTEGER);
                messSta.setInt(2, chat);
            }
            //mes_sender
            messSta.setInt(3, user);
            //mes_receiver
            if(mode){
                // messSta.setInt(4, getChatUser(chat).getId());
            }
            else{
                messSta.setNull(4, Types.INTEGER);
            }
            //mes_message
            if(!message.equals("")){
                messSta.setString(5, message);
            }
            else{
                messSta.setNull(5, Types.VARCHAR);
            }
            //mes_filename & mes_file
            if(filename != null && file != null){
                if(hash){
                    byte[] newHash = enc.hash(Files.readAllBytes(file.toPath()));
                    System.out.println(newHash);
                }
                messSta.setString(6, filename);
                // messSta.setBinaryStream(7, new ByteArrayInputStream(file), file.length);
            }
            else{
                messSta.setNull(6, Types.VARCHAR);
                messSta.setNull(7, Types.BLOB);
            }
            //mes_hash
            messSta.setBoolean(8, hash);

            messSta.executeUpdate();

            return true;
        }
        catch(SQLException sqle){
            sqle.printStackTrace();
            GUI.launchMessage(2, "", "");
            bridge.checkConnection(true);
            return false;
        }
        catch(IOException ioe){
            ioe.printStackTrace();
            GUI.launchMessage(2, "", "");
            return false;
        }
    }

    public List<Message> getMessages(int id, boolean mode, int offset) {
        List<Message> messages = new ArrayList<Message>();

        try{
            PreparedStatement messSta = bridge.conn.prepareStatement(mode ? CHAT_MESS : GROUP_MESS);
            messSta.setInt(1, id);
            messSta.setInt(2, offset);
            ResultSet res = messSta.executeQuery();

            while(res.next()){
                messages.add(new Message(res.getInt(1), mode, res.getInt(2), res.getString(3), res.getString(4), res.getBoolean(5), res.getTimestamp(6).toLocalDateTime(), res.getBoolean(7)));
            }
        }
        catch(SQLException sqle){
            sqle.printStackTrace();
            GUI.launchMessage(2, "Error de base de datos", "Ha ocurrido un error inesperado durante la comunicación\ncon la base de datos. Inténtelo de nuevo en unos instantes:\n\n" + sqle.getMessage());
            bridge.checkConnection(true);
        }

        if(messages.size() == 0){
            messages.add(new Message(1, mode, 1, "No se ha mandado ningún mensaje aún", null, false, LocalDateTime.now(), false));
        }

        return messages;
    }

    public boolean isMessageAvailable(int id, boolean mode, int offset){
        try{
            PreparedStatement avaSta = bridge.conn.prepareStatement(mode ? CHAT_AVAILABLE : GROUP_AVAILABLE);
            avaSta.setInt(1, id);
            avaSta.setInt(2, offset);
            ResultSet res = avaSta.executeQuery();

            if(res.next()){
                if(res.getInt(1) > 0){
                    return true;
                }
                else{
                    return false;
                }
            }
            else{
                return false;
            }
        }
        catch(SQLException sqle){
            sqle.printStackTrace();
            GUI.launchMessage(2, "Error de base de datos", "Ha ocurrido un error inesperado durante la comunicación\ncon la base de datos. Inténtelo de nuevo en unos instantes:\n\n" + sqle.getMessage());
            bridge.checkConnection(true);
            return false;
        }
    }

    /**
     * Fetches and returns the last message from every chat or group the user has together with the info of the user or group related to it.
     * @return An ArrayList containing multiple instances of the classes Message and Employee and in some cases the class Group too. 
     */
    public ArrayList<Object> getLastMess(){
        ArrayList<Message> fakeList = new ArrayList<Message>();
        ArrayList<Object> lastList = new ArrayList<Object>();

        try{
            PreparedStatement lastSta = bridge.conn.prepareStatement(LAST_MESS, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            lastSta.setInt(1, user);
            lastSta.setInt(2, user);
            lastSta.setInt(3, user);
            ResultSet res = lastSta.executeQuery();

            while(res.next()){
                Message fakeMess = new Message();
                fakeMess.setChat(res.getRow());
                fakeMess.setSendTime(res.getTimestamp(7).toLocalDateTime());

                fakeList.add(fakeMess);
            }

            Collections.sort(fakeList, Collections.reverseOrder());

            Iterator<Message> ite = fakeList.iterator();
                
            while(ite.hasNext()){
                Message lastMessage = new Message();
                Employee lastEmp = new Employee();
                Group lastGroup = new Group();

                res.absolute(ite.next().getChat());

                if(res.getBoolean(1)){
                    lastMessage.setMode(true);
                    lastMessage.setChat(res.getInt(2));
                }
                else{
                    lastMessage.setMode(false);
                    lastMessage.setChat(res.getInt(3));
                }

                lastMessage.setSender(res.getInt(4));
                lastMessage.setMessage(res.getString(5));
                lastMessage.setFilename(res.getString(6));
                lastMessage.setSendTime(res.getTimestamp(7).toLocalDateTime());
                lastList.add(lastMessage);

                lastEmp.setAlias(res.getString(10));
                lastEmp.setName(res.getString(11));
                lastList.add(lastEmp);

                if(!res.getBoolean(1)){
                    lastGroup.setId(res.getInt(3));
                    lastGroup.setName(res.getString(9));
                    lastList.add(lastGroup);
                }

                lastList.add(res.getInt(8));
            }
        }
        catch(SQLException sqle){
            sqle.printStackTrace();
            GUI.launchMessage(2, "Error de base de datos", "Ha ocurrido un error inesperado durante la comunicación\ncon la base de datos. Inténtelo de nuevo en unos instantes:\n\n" + sqle.getMessage());
            bridge.checkConnection(true);
        }

        return lastList;
    }
}
