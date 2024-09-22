package org.martincorp.Database;

import java.beans.Statement;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
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
    private final String GROUP_BY_ID = "SELECT";
    private final String GROUP_BY_NAME = "SELECT";
    private final String NEW_GROUP = "INSERT INTO certificate VALUES (?, 1, ?, ?)";    

    private final String CHAT = "SELECT * FROM chat WHERE chat_id = ?";
    private final String USER_CHATS = "SELECT * FROM chat WHERE chat_user1 = ? OR chat_user2 = ?";

    private final String SEND_MESS = "INSERT INTO message VALUES (NULL, ?, ?, ?, ?, ?, ?, ?, ?, NULL, 0)";
    private final String CHAT_MESS = "SELECT mes_id, mes_chat, mes_sender, mes_message, mes_filename, IF(mes_file == 'empty', FALSE, TRUE) AS hasFile, mes_unread FROM message WHERE mes_chat = ? ORDER BY mes_id DESC LIMIT 25 OFFSET ? ";
    private final String GROUP_MESS = "SELECT mes_id, mes_group, mes_sender, mes_message, mes_filename, IF(mes_file == 'empty', FALSE, TRUE) AS hasFile, mes_unread FROM message WHERE mes_group = ? ORDER BY mes_id DESC LIMIT 25 OFFSET ?";
    private final String CHAT_AVAILABLE = "SELECT COUNT(*) FROM chat WHERE mes_chat = ? ORDER BY mes_id LIMIT 25 OFFSET ?";
    private final String GROUP_AVAILABLE = "SELECT COUNT(*) FROM chat WHERE mes_group = ? ORDER BY mes_id LIMIT 25 OFFSET ?";
    private final String LAST_MESS_GROUP = "WITH chat_messages AS(SELECT mes_group, mes_sender, mes_message, mes_filename, mes_sendTime, COUNT(CASE WHEN mes_status = 0 THEN 1 ELSE NULL END) AS unread_mes, ROW_NUMBER() OVER (PARTITION BY mes_group ORDER BY mes_sendTime DESC) AS rn FROM message WHERE mes_group IN (SELECT grp_id FROM publicgroup) GROUP BY mes_group, mes_sender, mes_message, mes_filename, mes_sendTime) SELECT cm.mes_group, cm.mes_sender, cm.mes_message, cm.mes_filename, cm.mes_sendTime, cm.unread_mes, grp_name, emp_alias, CONCAT_WS(' ', emp_fname, emp_lname) FROM chat_messages cm JOIN publicgroup ON (cm.mes_group = publicgroup.grp_id) JOIN employee ON (cm.mes_sender = employee.emp_id) WHERE grp_id IN (SELECT gru_group FROM groupuser WHERE gru_user = 2) AND cm.rn = 1";
    private final String LAST_MESS_CHAT =  "WITH chat_messages AS(SELECT mes_chat, mes_sender, mes_message, mes_filename, mes_sendTime, COUNT(CASE WHEN mes_status = 0 THEN 1 ELSE NULL END) AS unread_mes, ROW_NUMBER() OVER (PARTITION BY mes_chat ORDER BY mes_sendTime DESC) AS rn FROM message WHERE mes_chat IN (SELECT chat_id FROM chat) GROUP BY mes_chat, mes_sender, mes_message, mes_filename, mes_sendTime) SELECT cm.mes_chat, cm.mes_sender, cm.mes_message, cm.mes_filename, cm.mes_sendTime, cm.unread_mes, emp_alias, CONCAT_WS(' ', emp_fname, emp_lname) FROM chat_messages cm JOIN chat ON (cm.mes_chat = chat.chat_id) JOIN employee ON (cm.mes_sender = employee.emp_id) WHERE (chat_user1 = 2 OR chat_user2 = 2) AND cm.rn = 1";
    private final String LAST_MESS = "WITH chat_messages AS(SELECT CASE WHEN mes_chat IS NULL THEN FALSE ELSE TRUE END AS `mode`, mes_chat, mes_group, mes_sender, mes_message, mes_filename, mes_sendTime, COUNT(CASE WHEN mes_status = 0 THEN 1 ELSE NULL END) AS unread_mes, ROW_NUMBER() OVER (PARTITION BY mes_group ORDER BY mes_sendTime DESC) AS rn FROM message GROUP BY mes_chat, mes_group, mes_sender, mes_message, mes_filename, mes_sendTime) SELECT `mode`, mes_chat, mes_group, mes_sender, mes_message, mes_filename, mes_sendTime, unread_mes, IF(`mode` = 0, grp_name, NULL) AS grp_name, emp_alias, CONCAT_WS(' ', emp_fname, emp_lname) FROM chat_messages LEFT JOIN publicgroup ON (mes_group = publicgroup.grp_id) LEFT JOIN employee ON (mes_sender = employee.emp_id) LEFT JOIN chat ON (mes_chat = chat.chat_id) WHERE (mes_group IN (SELECT gru_group FROM groupuser WHERE gru_user = ?) OR (chat_user1 = ? OR chat_user2 = ?)) AND rn = 1 GROUP BY mes_chat, mes_group, mes_sender, mes_message, mes_filename, mes_sendTime";
    private final String UNREAD_CHAT = "SELECT COUNT(mes_status) FROM message WHERE mes_status = 0 AND mes_chat = ?";
    private final String UNREAD_GROUP = "SELECT COUNT(mes_status) FROM message WHERE mes_status = 0 AND mes_group = ?";
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
    
    public List<Group> getUserGroups(){
        PreparedStatement groupSta;
        List<Group> groups = new ArrayList<Group>();

        try{
            groupSta = bridge.conn.prepareStatement(USER_GROUPS);
            groupSta.setInt(1, user);
            ResultSet res = groupSta.executeQuery();

            while(res.next()){
                groups.add(new Group(res.getInt(1), res.getInt(4), res.getString(2)));
            }
        }
        catch(SQLException sqle){
            sqle.printStackTrace();
            GUI.launchMessage(2, "Error de base de datos", "Ha ocurrido un error inesperado durante la comunicación\ncon la base de datos. Inténtelo de nuevo en unos instantes:\n\n" + sqle.getMessage());
            bridge.checkConnection(true);
        }

        return groups;
    }

    public List<Employee> getGroupUsers(int id){
        List<Employee> emps = new ArrayList<Employee>();
        PreparedStatement grpSta;

        try{
            grpSta = bridge.conn.prepareStatement(GROUP_USERS);
            grpSta.setInt(1, id);
            ResultSet res = grpSta.executeQuery();

            while(res.next()){
                emps.add(new Employee(res.getInt(1), res.getString(2) + " " + res.getString(3), res.getDate(4), res.getDate(5), res.getString(6), res.getString(7), res.getBoolean(9)));
            }
        }
        catch(SQLException sqle){
            sqle.printStackTrace();
            GUI.launchMessage(2, "Error de base de datos", "Ha ocurrido un error inesperado durante la comunicación\ncon la base de datos. Inténtelo de nuevo en unos instantes:\n\n" + sqle.getMessage());
            bridge.checkConnection(true);
        }

        if(emps.size() == 0){
            emps.add(new Employee(0, "Información del empleado no encontrada", new Date(System.currentTimeMillis()), new Date(System.currentTimeMillis()), "", "", false));
        }
        return emps;
    }

    public Group getGroupById(int id){
        Group grp = new Group();
        PreparedStatement grpSta;

        try{
            grpSta = bridge.conn.prepareStatement(GROUP_BY_ID);
        }
        catch(SQLException sqle){
            sqle.printStackTrace();
            GUI.launchMessage(2, "Error de base de datos", "Ha ocurrido un error inesperado durante la comunicación\ncon la base de datos. Inténtelo de nuevo en unos instantes:\n\n" + sqle.getMessage());
            bridge.checkConnection(true);
        }

        return grp;
    }

    public Group getGroupByName(String name){
        Group grp = new Group();
        PreparedStatement grpSta;

        try{
            grpSta = bridge.conn.prepareStatement(GROUP_BY_NAME);
        }
        catch(SQLException sqle){
            sqle.printStackTrace();
            GUI.launchMessage(2, "Error de base de datos", "Ha ocurrido un error inesperado durante la comunicación\ncon la base de datos. Inténtelo de nuevo en unos instantes:\n\n" + sqle.getMessage());
            bridge.checkConnection(true);
        }

        return grp;
    }

    public List<Chat> getChats(){
        List<Chat> chats = new ArrayList<Chat>();
        PreparedStatement messSta;

        try{
            messSta = bridge.conn.prepareStatement(USER_CHATS);
            messSta.setInt(1, user);
            messSta.setInt(2, user);
            ResultSet res = messSta.executeQuery();

            while(res.next()){
                chats.add(new Chat(res.getInt(1), res.getInt(2), res.getInt(3)));
            }
        }
        catch(SQLException sqle){
            sqle.printStackTrace();
            GUI.launchMessage(2, "Error de base de datos", "Ha ocurrido un error inesperado durante la comunicación\ncon la base de datos. Inténtelo de nuevo en unos instantes:\n\n" + sqle.getMessage());
            bridge.checkConnection(true);
        }

        if(chats.size() == 0){
            chats.add(new Chat(1, 1, 1));
        }

        return chats;
    }

    /**
     * Searches for the chat of a given id.
     * @since 1.0
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
     * The name is confusing but is to look for a the other employee you're talking to in a chat.
     * @since 1.0
     * @param id id of the chat which we need the other employee
     * @return returns the employee of the given chat id
     */
    public Employee getChatUser(int id){
        Chat chat = getChatById(id);
            
        if(chat.getUser1() != user){
            return getEmployeeById(chat.getUser1());
        }
        else{
            return getEmployeeById(chat.getUser2());
        }
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
                messSta.setInt(4, getChatUser(chat).getId());
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
        PreparedStatement messSta;

        try{
            if(mode){
                messSta = MessagerBridge.conn.prepareStatement(CHAT_MESS);
            }
            else{
                messSta = MessagerBridge.conn.prepareStatement(GROUP_MESS);
            }
            messSta.setInt(1, id);
            messSta.setInt(2, offset);
            ResultSet res = messSta.executeQuery();

            while(res.next()) {
                messages.add(new Message(mode ? res.getInt(2) : res.getInt(3), mode, res.getInt(4), res.getInt(5), res.getString(6), res.getString(7), res.getBoolean(8), res.getTimestamp(9).toLocalDateTime()));
            }
        }
        catch(SQLException sqle){
            sqle.printStackTrace();
            GUI.launchMessage(2, "Error de base de datos", "Ha ocurrido un error inesperado durante la comunicación\ncon la base de datos. Inténtelo de nuevo en unos instantes:\n\n" + sqle.getMessage());
            bridge.checkConnection(true);
        }

        if(messages.size() == 0){
            messages.add(new Message(1, mode, 1, 1, "No se ha mandado ningún mensaje aún", null, false, LocalDateTime.now()));
        }

        return messages;
    }

    public boolean isMessageAvailable(int id, boolean mode, int offset){
        PreparedStatement avaSta;

        try{
            avaSta = bridge.conn.prepareStatement(mode ? CHAT_AVAILABLE : GROUP_AVAILABLE);
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
                    System.out.println(res.getString(9));
                    lastGroup.setName(res.getString(9));
                    lastList.add(lastGroup);
                }

                lastList.add(res.getInt(8));
            }

            System.out.println("adffafaf");
        }
        catch(SQLException sqle){
            sqle.printStackTrace();
            GUI.launchMessage(2, "Error de base de datos", "Ha ocurrido un error inesperado durante la comunicación\ncon la base de datos. Inténtelo de nuevo en unos instantes:\n\n" + sqle.getMessage());
            bridge.checkConnection(true);
        }

        return lastList;
    }
    
    /**
     * Fetches the messages a user has not yet read from a specific personal chat or group.
     * 
     * @since 1.0
     * @param chatId the id of the Chat which will be accessed
     * @param mode indicates whether the chat belongs to a personal chat or a group
     * @return returns an Integer with the amount of messages of chat left unread by the user
     */
    public int getUnreadMess(int chatId, boolean mode){
        PreparedStatement unrSta;
        int unread = 0;

        try{
            if(mode){
                unrSta = MessagerBridge.conn.prepareStatement(UNREAD_CHAT);
                unrSta.setInt(1, chatId);
            }
            else{
                unrSta = MessagerBridge.conn.prepareStatement(UNREAD_GROUP);
                unrSta.setInt(1, chatId);
            }

            ResultSet unrRes = unrSta.executeQuery();
            unrRes.next();
            unread = unrRes.getInt(1);
        }
        catch(SQLException sqle){
            sqle.printStackTrace();
            GUI.launchMessage(2, "Error de base de datos", "Ha ocurrido un error inesperado durante la comunicación\ncon la base de datos. Inténtelo de nuevo en unos instantes:\n\n" + sqle.getMessage());
            bridge.checkConnection(true);
        }
        
        return unread;
    }
}

