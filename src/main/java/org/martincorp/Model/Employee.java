package org.martincorp.Model;

import java.util.Date;

public class Employee {
    //Variables:
    private int id;
    private String name, alias, email;
    private boolean online;
    private Date sDate, eDate;

    //Builder:
    /**
     * Basic builder for Employee.
     * @param i id of Employee
     * @param n name of Employee
     * @param a alias of Employee
     * @param m email of Employee
     * @param s date of contract start of Employee
     * @param e date contract end of Employee
     */
    public Employee(int i, String n, Date s, Date e, String a, String m, boolean o){
        this.id = i;
        this.name = n;
        this.alias = a;
        this.email = m;
        this.online = o;
        this.sDate = s;
        
        if(eDate != null){
            this.eDate = eDate;
        }
        else{
            this.eDate = new Date();
        }
    }

    /**
     * Empty, could still be usefull.
     */
    public Employee(){}

    //Methods:
      //Getters & Setters:
    public int getId(){
        return this.id;
    }

    public String getName(){
        return this.name;
    }

    public String getAlias(){
        return this.alias;
    }

    public String getEmail(){
        return this.email;
    }

    public boolean getOnline(){
        return this.online;
    }

    public Date getSDate(){
        return this.sDate;
    }

    public Date getEDate(){
        return this.eDate;
    }

    public void setId(int i){
        this.id = i;
    }

    public void setName(String n){
        this.name = n;
    }

    public void setAlias(String a){
        this.alias = a;
    }

    public void setEmail(String m){
        this.email = m;
    }

    public void setOnline(boolean o){
    
    }

    public void setSDate(Date s){
        this.sDate = s;
    }

    public void setEDate(Date e){
        this.eDate = e;
    }
}
