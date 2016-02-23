package com.feedhenry.sdk.exceptions;

/**
 * This exception is thrown by the sync framework when a dataSetId does not have
 * an associated dataSet.
 */
public class DataSetNotFound extends Exception {
    
    public DataSetNotFound(String message) {
        super(message);
    }
    
}
