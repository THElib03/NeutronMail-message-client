package org.martincorp.Model;

public class Chat {
    //Variables:
    private int id, user1, user2;

    //Builder:
    /**
     * @param i id of Chat
     * @param u1 first user of Chat
     * @param u2 second user of Chat
     */
    public Chat(int i, int u1, int u2) {
        this.id = i;
        this.user1 = u1;
        this.user2 = u2;
    }

    //Methods:
      //Getters & Setters:
    public int getId() {
        return this.id;
    }

    public int getUser1() {
        return this.user1;
    }

    public int getUser2() {
        return this.user2;
    }

    public void setId(int i) {
        this.id = i;
    }

    public void setUser1(int u1) {
        this.user1 = u1;
    }

    public void setUser2(int u2) {
        this.user2 = u2;
    }
}
