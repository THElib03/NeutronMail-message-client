package org.martincorp.Model;

public class BarMessage {
    //Variables:
    private int id;
    private String message, fileName, filePath;
    private boolean mode, verif;

    //Builder:
    /**
     * Basic constructor for BarMessage
     * @param i meant for the value 'chatId' from ChatGridController
     * @param me meant for the String from the TextField 'titleText' from ChatGridController
     * @param fn meant for the selected file from the Button 'fileBut' from ChatGridController, takes the the getName() value
     * @param fp meant for the selected file from the Button 'fileBut' from ChatGridController, takes the the getAbsolutePath() value
     * @param mo meant for the chat type loaded/to load on the chatGrid from ChatGridController
     * @param v meant for the value 'verifCheck' from ChatGridController
     */
    public BarMessage(int i, String me, String fn, String fp, boolean mo, boolean v){
        this.id = i;
        this.message = me;
        this.fileName = fn;
        this.filePath = fp;
        this.mode = mo;
        this.verif = v;
    }

    //Methods:
      //Getters & Setters
    public int getId() {
        return this.id;
    }

    public String getMessage(){
        return this.message;
    }

    public String getFileName(){
        return this.fileName;
    }

    public String getFilePath(){
        return this.filePath;
    }

    public boolean getMode(){
        return this.mode;
    }

    public boolean getVerif(){
        return this.verif;
    }

    public void setId(int i){
        this.id = i;
    }

    public void setMessage(String me){
        this.message = me;
    }

    public void setFileName(String fn){
        this.fileName = fn;
    }

    public void setFilePath(String fp){
        this.filePath = fp;
    }

    public void setMode(boolean mo){
        this.mode = mo;
    }

    public void setVerif(boolean v){
        this.verif = v;
    }
}
