package com.feedhenry.sdk.utils;

import android.content.Context;
import android.util.Base64;
import org.jboss.aerogear.AeroGearCrypto;
import org.jboss.aerogear.crypto.CryptoBox;
import org.jboss.aerogear.crypto.RandomUtils;
import org.jboss.aerogear.crypto.keys.PrivateKey;
import org.jboss.aerogear.crypto.password.Pbkdf2;

import java.security.spec.InvalidKeySpecException;

public class EncryptedDataManager {

    private static final String KEY_IV = "IV";
    private static final String KEY_SALT = "SALT";

    private static EncryptedDataManager encryptedDataManager;
    private static DataManager dataManager;

    private final CryptoBox cryptoBox;
    private byte[] iv;

    private EncryptedDataManager(String password) {
        cryptoBox = createCryptoBox(password);
    }

    public static synchronized EncryptedDataManager getInstance(Context context, String password) {
        if (encryptedDataManager == null) {
            encryptedDataManager = new EncryptedDataManager(password);
            dataManager = DataManager.init(context);
        }
        return encryptedDataManager;
    }

    private CryptoBox createCryptoBox(String passphrase) {

        byte[] salt;

        String stringIV = dataManager.read(KEY_IV);
        String stringSalt = dataManager.read(KEY_SALT);

        // First execution
        if (stringIV == null || stringSalt == null) {
            iv = RandomUtils.randomBytes();
            salt = RandomUtils.randomBytes();

            dataManager.save(KEY_IV, encode(iv));
            dataManager.save(KEY_SALT, encode(salt));
        } else {
            iv = decode(stringIV);
            salt = decode(stringSalt);
        }

        try {
            Pbkdf2 pbkdf2 = AeroGearCrypto.pbkdf2();
            byte[] rawPassword = pbkdf2.encrypt(passphrase, salt);
            PrivateKey privateKey = new PrivateKey(rawPassword);
            return new CryptoBox(privateKey);
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Decode a string using Base64
     *
     * @param value String encoded
     * @return Decoded String
     */
    private byte[] decode(String value) {
        return Base64.decode(value, Base64.DEFAULT);
    }

    /**
     * Encode a byte[] using Base64
     *
     * @param value byte[] decoded
     * @return String encoded
     */
    private String encode(byte[] value) {
        return Base64.encodeToString(value, Base64.DEFAULT);
    }

    public void save(String key, String value) {
        dataManager.save(key, encode(cryptoBox.encrypt(iv, decode(value))));
    }

    public String read(String key) {
        String data = dataManager.read(key);
        return (data != null) ? encode(cryptoBox.decrypt(iv, decode(data))) : data;
    }

    public void remove(String key) {
        dataManager.remove(key);
    }

}
