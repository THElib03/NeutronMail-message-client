package org.martincorp.Model;

import java.time.LocalDateTime;

/**
 * @author <a href="https://github.com/THElib03">Martín Marín</a>
 * @version 1.0, 18/02/24
 */
public class Message implements Comparable<Message>{
    //Variables:
    private int chat, sender;
    private boolean mode, hasFile, status;
    private String message, filename;
    private LocalDateTime sendTime;

    //Builders:
    /**
     * Basic builder for Message
     * 
     * @since 1.0
     * @param c chat ID the message belongs to
     * @param mode whether the mesage belongs to a personal chat or a group
     * @param sen employee ID of the message sender
     * @param m message string sent
     * @param h whether the message comes with a file or not
     * @param fn filename of the file sent
     * @param t date and time when the message was sent
     * @param sta whether the message has been read or not
     */
    public Message(int c, boolean mode, int sen, String m, String fn, boolean h, LocalDateTime t, boolean sta){
        //DONE: rewrite Message in the DB to have a collumn for a string written as the message body or text, the should be optional now and this would allow proper comunication
          // i can't just name the file like the text.
        this.chat = c;
        this.mode = mode;
        this.sender = sen;
        this.message = m;
        this.filename = fn;
        this.hasFile = h;
        this.sendTime = t;
        this.status = sta;
    }

    /**
     * Empty builder for Message.
     */
    public Message(){}

    //Methods:
    @Override
    public int compareTo(Message m){
        return this.sendTime.compareTo(m.getSendTime());
    }

      //Getters & Setters:
    public int getChat(){
        return chat;
    }

    public boolean getMode(){
        return mode;
    }

    public int getSender(){
        return sender;
    }

    public String getMessage(){
        return message;
    }

    public String getFilename(){
        return filename;
    }

    public boolean getHasFile(){
        return hasFile;
    }

    public LocalDateTime getSendTime(){
        return sendTime;
    }

    public void setChat(int c){
        this.chat = c;
    }

    public void setMode(boolean m){
        this.mode = m;
    }

    public void setSender(int s){
        this.sender = s;
    }

    public void setMessage(String m){
        this.message = m;
    }

    public void setFilename(String f){
        this.filename = f;
    }

    public void setHasFile(boolean h){
        this.hasFile = h;
    }

    public void setSendTime(LocalDateTime t){
        this.sendTime = t;
    }
}
