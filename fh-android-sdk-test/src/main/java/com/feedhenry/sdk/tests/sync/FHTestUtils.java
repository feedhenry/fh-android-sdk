/**
 * Copyright (c) 2015 FeedHenry Ltd, All Rights Reserved.
 *
 * Please refer to your contract with FeedHenry for the software license agreement.
 * If you do not have a contract, you do not have a license to use this software.
 */
package com.feedhenry.sdk.tests.sync;

import android.util.Log;
import java.util.Date;
import java.util.Random;

import org.json.fh.JSONArray;
import org.json.fh.JSONObject;

import com.feedhenry.sdk.sync.FHSyncDataRecord;
import com.feedhenry.sdk.sync.FHSyncPendingRecord;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

public class FHTestUtils {
    private static final String TAG = FHTestUtils.class.getSimpleName();

    public static JSONObject generateJSON() throws Exception {
        JSONObject ret = new JSONObject();
        ret.put("testStringKey", genRandomString(10));
        ret.put("testNumberKey", new Random().nextInt());
        JSONArray arr = new JSONArray();
        arr.put(genRandomString(10));
        arr.put(genRandomString(10));
        ret.put("testArrayKey", arr);
        JSONObject dict = new JSONObject();
        dict.put(genRandomString(10), genRandomString(10));
        dict.put(genRandomString(10), genRandomString(10));
        ret.put("testDictKey", dict);
        return ret;
    }

    private static String genRandomString(int pLength) {
        Random r = new Random();
        String letter = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < pLength; i++) {
            sb.append(letter.charAt(r.nextInt(1000000) % (letter.length())));
        }
        return sb.toString();
    }

    public static FHSyncDataRecord generateRadomDataRecord() throws Exception {
        JSONObject json = generateJSON();
        return new FHSyncDataRecord(json);
    }

    public static FHSyncPendingRecord generateRandomPendingRecord() throws Exception {
        FHSyncPendingRecord pending = new FHSyncPendingRecord();
        pending.setInFlightDate(new Date());
        pending.setInFlight(true);
        pending.setCrashed(false);
        pending.setAction("create");
        pending.setTimestamp(new Date().getTime());
        pending.setUid(genRandomString(10));
        pending.setPreData(generateRadomDataRecord());
        pending.setPostData(generateRadomDataRecord());
        return pending;
    }
    
    public static <T> T instanciatePrivateInnerClass(String className, Object outerInstance, Object... params) {
        try {
            Class<T> innerClass = findClass(className, outerInstance.getClass().getDeclaredClasses());
            
            Constructor<T> innerClassConstructor = findConstructor(innerClass, outerInstance.getClass(), params);
            innerClassConstructor.setAccessible(true);
            params = prependTo(outerInstance, params);
            return innerClassConstructor.newInstance(params);
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            Log.e(TAG, "Error creating inner class " + className, ex);
            throw new IllegalArgumentException(ex);
        }
    }

    private static <T> Class<T>findClass(String className, Class<?>[] declaredClasses) {
        for (Class<?> klass : declaredClasses) {
            if (klass.getSimpleName().matches(className)) {
                return (Class<T>) klass;
            }
        }
        throw new IllegalArgumentException(String.format("Class name %s not found ", className));
    }

    private static <T> Constructor<T> findConstructor(Class<T> innerClass, Class<?> outerClassType, Object[] params) {
        try {
            Class<?>[] paramsKlasses = new Class[params.length + 1];
            paramsKlasses[0] = outerClassType;
            for (int i = 0; i < params.length; i++) {
                paramsKlasses[i + 1] = params[i].getClass();
            }
            Constructor<T>[] constructors = (Constructor<T>[]) innerClass.getDeclaredConstructors();
            for (Constructor<T> constructor : constructors) {
                Class<?>[] constructorParams = constructor.getParameterTypes();
                if (constructorParams.length == paramsKlasses.length) {
                    boolean found = true;
                    for (int i = 0; i < params.length; i++) {
                        if (!constructorParams[i].isAssignableFrom(paramsKlasses[i])) {
                            found = false;
                            break;
                        }
                        
                    }
                    if (found) {
                        return constructor;
                    }
                }
            }
            Log.e(TAG, "No Constructor.");
            throw new IllegalArgumentException("Could not find a constructor for " + params.toString());
        } catch (SecurityException ex) {
            Log.e(TAG, "Error finding constructor.", ex);
            throw new IllegalArgumentException(ex);
        }
    }

    private static <T> T[] prependTo(T outerInstance, T[] params) {
        ArrayList<T> paramsList = new ArrayList<>(params.length + 1);
        
        paramsList.add(outerInstance);
        for (T param : params) {
            paramsList.add(param);
        }
        return paramsList.toArray(params);
    }

    /**
     * 
     * Will scan source and its super classes for fields with a type of value.  
     * All instances will be replaced by value
     * 
     * @param source the target object to have value injected into
     * @param value the value to override
     */
    public static void injectInto(Object source, Object value) {
        injectInto(source, source.getClass(), value);
    }

    private static void injectInto(Object source, Class<? extends Object> sourceClass, Object value) {
        Field[] fields = sourceClass.getDeclaredFields();
        for (Field field : fields) {
            if (field.getType().isAssignableFrom(value.getClass())) {
                try {
                    field.setAccessible(true);
                    field.set(source, value);
                } catch (IllegalArgumentException | IllegalAccessException ex) {
                    Log.e(TAG, ex.getMessage(), ex);
                    throw new RuntimeException(ex);
                }
            }
        }
        if (sourceClass.getSuperclass() != Object.class) {
            injectInto(source,sourceClass.getSuperclass(), value);
        }
    }

    static Object getPrivateField(Object client, String fieldName) {
        try {
            Class<? extends Object> klass = client.getClass();
            Field field = klass.getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(client);
        } catch (IllegalAccessException | IllegalArgumentException | NoSuchFieldException | SecurityException ex) {
            Log.e(FHTestUtils.class.getName(), ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
        
    }
    
}
