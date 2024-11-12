package org.martincorp.Model;

import java.time.LocalDateTime;

public class Group {
    //Variables:
    private int id, owner;
    private String name;
    private LocalDateTime crationDate;
    private boolean isDeleted;

    //Builder:
    /**
     * Basic builder for Group
     * @param i id of Group
     * @param o owner of Group
     * @param n name of Group
     */
    public Group(int i, int o, String n, LocalDateTime c, boolean d){
        this.id = i;
        this.owner = o;
        this.name = n;
        this.crationDate = c;
        this.isDeleted = d;
    }

    /**
     * Empty builder for Group
     */
    public Group(){}

    //Methods:
      //Getters & Setters
    public int getId(){
        return this.id;
    }

    public int getOwner(){
        return this.owner;
    }

    public String getName(){
        return this.name;
    }

    public void setId(int i){
        this.id = i;
    }

    public void setOwner(int o){
        this.owner = o;
    }

    public void setName(String n){
        this.name = n;
    }
}
