package org.martincorp.Model;

import java.util.Date;

public class User {
    //Variables:
    private int id;
    private String firstName, lastName, alias, email;
    private Date startDate, endDate;

    //Builder:
    public User(int id, String fn, String ln, String a, String e, Date sDate, Date eDate){
        this.id = id;
        this.firstName = fn;
        this.lastName = ln;
        this.alias = a;
        this.email = e;
        this.startDate = sDate;

        if(eDate != null){
            this.endDate = eDate;
        }
        else{
            this.endDate = new Date();
        }
    }

    public User(){}

    //Methods:

      //Getters & Setters:
    public int getId(){
        return id;
    }
    
    public String getFirstName(){
        return firstName;
    }

    public String getLastName(){
        return lastName;
    }

    public String getAlias(){
        return alias;
    }

    public String getEmail(){
        return email;
    }

    public Date getStartDate(){
        return startDate;
    }

    public Date getEndDate(){
        return endDate;
    }

    public void setId(int i){
        this.id = i;
    }

    public void setFirstName(String fn){
        this.firstName = fn;
    }

    public void setLastName(String ln){
        this.lastName = ln;
    }

    public void setAlias(String a){
        this.alias = a;
    }

    public void setEmail(String email){
        this.email = email;
    }

    public void setStartDate(Date sDate){
        this.startDate = sDate;
    }

    public void setEndDate(Date eDate){
        this.endDate = eDate;
    }
}
