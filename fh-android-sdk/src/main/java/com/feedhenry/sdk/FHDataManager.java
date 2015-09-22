package com.feedhenry.sdk;

import android.content.Context;
import com.feedhenry.sdk.datamanager.FHStore;
import org.jboss.aerogear.android.store.DataManager;
import org.jboss.aerogear.android.store.Store;
import org.jboss.aerogear.android.store.memory.EncryptedMemoryStoreConfiguration;
import org.jboss.aerogear.android.store.memory.MemoryStoreConfiguration;
import org.jboss.aerogear.android.store.sql.EncryptedSQLStore;
import org.jboss.aerogear.android.store.sql.EncryptedSQLStoreConfiguration;
import org.jboss.aerogear.android.store.sql.SQLStoreConfiguration;

import java.security.spec.InvalidKeySpecException;

public final class FHDataManager {

    private Context context;

    private FHDataManager(Context context) {
        this.context = context;
    }

    public static FHDataManager with(Context context) {
        return new FHDataManager(context);
    }

    public FHStore getStore(String storeName) {
        Store store = DataManager.getStore(storeName);
        if (store == null) {
            throw new IllegalStateException("Store '" + storeName + "' not found");
        }
        return wrapperStore(store);
    }

    public FHMemoryStoreInitializer memoryStore(String name) {
        return new FHMemoryStoreInitializer(name);
    }

    public FHEncryptedMemoryStoreInitializer encryptedMemoryStore(String name) {
        return new FHEncryptedMemoryStoreInitializer(name);
    }

    public FHSQLStoreInitializer sqlStore(String name) {
        return new FHSQLStoreInitializer(name);
    }

    public FHEncryptedSQLStoreInitializer encryptedSqlStore(String name) {
        return new FHEncryptedSQLStoreInitializer(name);
    }

    private FHStore wrapperStore(Store store) {
        return new FHStore(store);
    }

    public abstract class FHStoreInitializer {

        private String name;

        public FHStoreInitializer(String name) {
            this.name = name;
        }

        String getName() {
            return name;
        }

    }

    public class FHMemoryStoreInitializer extends FHStoreInitializer {

        public FHMemoryStoreInitializer(String name) {
            super(name);
        }

        public FHStore init() {
            Store store = DataManager
                    .config(getName(), MemoryStoreConfiguration.class)
                    .store();
            return wrapperStore(store);
        }

    }

    public class FHEncryptedMemoryStoreInitializer extends FHStoreInitializer {

        private String password;

        public FHEncryptedMemoryStoreInitializer(String name) {
            super(name);
        }

        public FHEncryptedMemoryStoreInitializer usingPassword(String password) {
            this.password = password;
            return this;
        }

        public FHStore init() {
            Store store = DataManager
                    .config(getName(), EncryptedMemoryStoreConfiguration.class)
                    .usingPassword(password)
                    .store();
            return wrapperStore(store);
        }

    }

    public class FHSQLStoreInitializer extends FHStoreInitializer {

        private Class klass;

        public FHSQLStoreInitializer(String name) {
            super(name);
        }

        public FHSQLStoreInitializer forClass(Class klass) {
            this.klass = klass;
            return this;
        }

        public FHStore init() {
            Store store = DataManager
                    .config(getName(), SQLStoreConfiguration.class)
                    .withContext(context)
                    .forClass(klass)
                    .store();
            return wrapperStore(store);
        }

    }

    public class FHEncryptedSQLStoreInitializer extends FHStoreInitializer {

        private Class klass;
        private String passPhrase;

        public FHEncryptedSQLStoreInitializer(String name) {
            super(name);
        }

        public FHEncryptedSQLStoreInitializer forClass(Class klass) {
            this.klass = klass;
            return this;
        }

        public FHEncryptedSQLStoreInitializer usingPassphrase(String passPhrase) {
            this.passPhrase = passPhrase;
            return this;
        }

        public FHStore init() throws InvalidKeySpecException {
            try {
                EncryptedSQLStore store = (EncryptedSQLStore) DataManager
                        .config(getName(), EncryptedSQLStoreConfiguration.class)
                        .withContext(context)
                        .usingPassphrase(passPhrase)
                        .forClass(klass)
                        .store();
                store.openSync();
                return wrapperStore(store);
            } catch (Exception e) {
                throw new InvalidKeySpecException(e);
            }
        }

    }

}
