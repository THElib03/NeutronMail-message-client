package org.martincorp.Codec;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.nio.file.Files;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.Base64;
import java.util.Random;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import org.martincorp.Interface.GUI;

public class Encrypt {
    //Variables:
    private MessageDigest hasher;
    private SecretKeyFactory skf;
    private int key_length;

    private static final Random RANDOM = new SecureRandom();
    private static final int ITERATIONS = 10000;
    private static final int KEYSIZE = 4096;


    //Builder:
      //So just in case anyone wants to secure change how this works/encrypts, I've left this ready to use any of the basic options provided by the standard Java libraries:
      // -1 - SHA-1, really, don't use this unless it doesn't matter if someones reverses this encryption.
      // 0 - MD5, the classic and unmatched MD5.
      // 1 - SHA-224, all of this are on the SHA2 standard.
      // 2 - SHA-256, most used and perfectly fine.
      // 3 - SHA-384
      // 4 - SHA-512
    public Encrypt(int mode){
        try{
            switch(mode){
                case -1:
                    //Why are you doing this to yourself?
                    hasher = MessageDigest.getInstance("SHA-1");
                    skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
                    key_length = 160;
                    break;
                case 0:
                    hasher = MessageDigest.getInstance("MD5");
                    skf = SecretKeyFactory.getInstance("PBEWithMD5AndTripleDES");
                    key_length = 128;
                    break;
                case 1:
                    hasher = MessageDigest.getInstance("SHA-224");
                    skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA224");
                    key_length = 224;
                    break;
                case 2:
                    hasher = MessageDigest.getInstance("SHA-256");
                    skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
                    key_length = 256;
                    break;
                case 3:
                    hasher = MessageDigest.getInstance("SHA-384");
                    skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA384");
                    key_length = 384;
                    break;
                case 4:
                    hasher = MessageDigest.getInstance("SHA-512");
                    skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512");
                    key_length = 512;
                    break;
            }
        }
        catch(NoSuchAlgorithmException nsae){
            nsae.printStackTrace();
            GUI.launchMessage(2, "Error de encriptación", "Ha ocurrido un problema al iniciar el módulo de protección de contraseñas.");
        }
    }

    //Methods:
    /* Simpler hash creator excerpt taken from Reflectoring.io, check project documentation for links. */
    public byte[] hash(byte[] input){
        return hasher.digest(input);
    }

    public String hashString(byte[] input){
        String decoded = null;
        byte[] buffer = hasher.digest(input);

        BigInteger decoder = new BigInteger(1, buffer);
        decoded = decoder.toString(16);
        while(decoded.length() < 32){
            decoded = "0".concat(decoded);
        }

        return decoded;
    }

    /* Hashing & Salting*/
    /**
     * Returns a random 0salt to be used to hash a password (provided by 'assylias' from stackoverflow.com, check project documentation for links and info).
     * @return a 16 bytes random salt
     */
    public byte[] getSalt(){
        byte[] salt = new byte[32];
        RANDOM.nextBytes(salt);
        return salt;
    }

    /**
     * Returns a salted and hashed password using the provided hash. (provided by 'assylias' from stackoverflow.com, check project documentation for links and info)<br>
     * Note - side effect: the password is destroyed (the char[] is filled with zeros)
     *
     * @param password the password to be hashed.
     * @param salt     a 16 bytes salt, ideally obtained with the getNextSalt method. (Changed to 32).
     *
     * @return the hashed password with a pinch of salt.
     */
    public byte[] saltedHash(char[] password, byte[] salt){
        PBEKeySpec spec = new PBEKeySpec(password, salt, ITERATIONS, key_length);
        Arrays.fill(password, Character.MIN_VALUE);

        try{
            return skf.generateSecret(spec).getEncoded();
        }
        catch(InvalidKeySpecException ikse){
            GUI.launchMessage(2, "Error de encriptación", "");
            throw new AssertionError("Error while hashing a password: " + ikse.getMessage(), ikse);
            
        }
        finally{
            spec.clearPassword();
        }
    }

    /**
     * Returns true if the given password and salt match the hashed value, false otherwise.<br>
     * Note - side effect: the password is destroyed (the char[] is filled with zeros)
     *
     * @param password     the password to check
     * @param salt         the salt used to hash the password
     * @param expectedHash the expected hashed value of the password
     *
     * @return true if the given password and salt match the hashed value, false otherwise
     */
    public boolean checkPassword(char[] password, byte[] salt, byte[] expectedHash){
        byte[] passHash = saltedHash(password, salt);
        Arrays.fill(password, Character.MIN_VALUE);

        if(passHash.length != expectedHash.length) return false;

        for(int i = 0; i < passHash.length; i++){
            if(passHash[i] != expectedHash[i]) return false;
        }
        return true;
    }

    /**
     * Generates a random password of a given length, using letters and digits.
     *
     * @param length the length of the password
     *
     * @return a random password
     */
    public static String generateRandomPassword(int length) {
        StringBuilder sb = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            int newChar = RANDOM.nextInt(62);
            if (newChar <= 9) {
                sb.append(String.valueOf(newChar));
            } else if (newChar < 36) {
                sb.append((char) ('a' + newChar - 10));
            } else {
                sb.append((char) ('A' + newChar - 36));
            }
        }
        
        return sb.toString();
    }

    //RSA related methods:
    public byte[] generateNewPair(){
        try{
            KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
            gen.initialize(KEYSIZE);
            KeyPair pair = gen.generateKeyPair();

            //TODO: vale, ahí una cosa en la clase Cipher llamada wrap y unwrap de keys, puede que sea una manera de ofuscar esta clave privada.
            OutputStream os = new FileOutputStream(new File("conf.conf"));
            os.write(Base64.getEncoder().encode(pair.getPrivate().getEncoded()));

            os.close();
            return pair.getPublic().getEncoded();
        }
        catch(NoSuchAlgorithmException nsae){
            nsae.printStackTrace();
            return null;
        }
        catch(IOException ioe) {
            ioe.printStackTrace();
            GUI.launchMessage(2, "Error de escritura", "Ha ocurrido un error al intentar crear un\nnuevo perfil de configuración.\n\n" + ioe.getMessage());
            return null;
        }
    }

    public Key retrievePrivKey(){
        try{
            InputStream is = new FileInputStream(new File("conf.conf"));

            return new SecretKeySpec(Base64.getDecoder().decode(is.readAllBytes()), skf.getAlgorithm());
        }
        catch(IOException ioe) {
            ioe.printStackTrace();
            GUI.launchMessage(2, "Error de escritura", "Ha ocurrido un error al intentar crear un\nnuevo perfil de configuración.\n\n" + ioe.getMessage());
            return null;
        }
    }

    public boolean decrypt(byte[] file, String filename){
        try{
            Cipher ci = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
            ci.init(Cipher.ENCRYPT_MODE, retrievePrivKey());
            
            FileOutputStream fos = new FileOutputStream(filename);
            fos.write(ci.doFinal(file));

            fos.close();
            return true;
        }
        catch(NoSuchAlgorithmException nsae){
            nsae.printStackTrace();
            GUI.launchMessage(2, "Error de seguridad", "No se ha podido iniciar el módulo de seguridad.\n\n" + nsae.getMessage());
            return false;
        }
        catch(NoSuchPaddingException nspe){
            nspe.printStackTrace();
            GUI.launchMessage(2, "Error de seguridad", "No se ha podido iniciar el módulo de seguridad.\n\n" + nspe.getMessage());
            return false;
        }
        catch(InvalidKeyException ike){
            ike.printStackTrace();
            GUI.launchMessage(2, "Error de seguridad", "Las claves de seguridad no son correctas o están\ncorruptas, contecte con su administrador.");
            return false;
        }
        catch(IllegalBlockSizeException ibse){
            ibse.printStackTrace();
            GUI.launchMessage(2, "Error de escritura", "No se ha podido guardar el archivo descargado.\n\n" + ibse.getMessage());
            return false;
        }
        catch(BadPaddingException bpe){
            bpe.printStackTrace();
            GUI.launchMessage(2, "Error de seguridad", "No se ha podido desencriptar correctamente el\narchivo descargado.\n\n" + bpe.getMessage());
            return false;
        }
        catch(IOException ioe){
            ioe.printStackTrace();
            GUI.launchMessage(2, "Error de escritura", "No se ha podido guardar el archivo descargado.\n\n" + ioe.getMessage());
            return false;
        }
    }

    public byte[] encrypt(File file, Key pubKey){
        try{
            Cipher ci = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
            ci.init(Cipher.ENCRYPT_MODE, retrievePrivKey());
            return ci.doFinal(Files.readAllBytes(file.toPath()));
        }
        catch(NoSuchAlgorithmException nsae){
            nsae.printStackTrace();
            GUI.launchMessage(2, "Error de seguridad", "No se ha podido iniciar el módulo de seguridad.\n\n" + nsae.getMessage());
            return null;
        }catch(NoSuchPaddingException nspe){
            nspe.printStackTrace();
            GUI.launchMessage(2, "Error de seguridad", "No se ha podido iniciar el módulo de seguridad.\n\n" + nspe.getMessage());
            return null;
        }
        catch(IllegalBlockSizeException ibse){
            ibse.printStackTrace();
            GUI.launchMessage(2, "Error de escritura", "No se ha podido guardar el archivo descargado.\n\n" + ibse.getMessage());
            return null;
        }
        catch(BadPaddingException bpe){
            bpe.printStackTrace();
            GUI.launchMessage(2, "Error de seguridad", "No se ha podido desencriptar correctamente el\narchivo seleccionado.\n\n" + bpe.getMessage());
            return null;
        }
        catch(InvalidKeyException ike){
            ike.printStackTrace();
            GUI.launchMessage(2, "Error de seguridad", "Las claves de seguridad no son correctas o están\ncorruptas, contecte con su administrador.");
            return null;
        }
        catch(IOException ioe){
            ioe.printStackTrace();
            GUI.launchMessage(2, "Error de lectura", "No se ha podido leer el archivo seleccionado.\n\n" + ioe.getMessage());
            return null;
        }
    }
}