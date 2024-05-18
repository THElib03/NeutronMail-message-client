package org.martincorp.Model;

public class Group {
    //Variables:
    private int id, owner;
    private String name;

    //Builder:
    /**
     * Basic builder for Group
     * @param i id of Group
     * @param o owner of Group
     * @param n name of Group
     */
    public Group(int i, int o, String n){
        this.id = i;
        this.owner = o;
        this.name = n;
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
