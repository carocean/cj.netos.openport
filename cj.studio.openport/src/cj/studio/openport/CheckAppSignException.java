package cj.studio.openport;

import cj.studio.ecm.net.CircuitException;

public class CheckAppSignException extends CircuitException {
    public CheckAppSignException(String status, Throwable e) {
        super(status, e);
    }

    public CheckAppSignException(String status, boolean isSystemException, Throwable e) {
        super(status, isSystemException, e);
    }

    public CheckAppSignException(String status, String e) {
        super(status, e);
    }

    public CheckAppSignException(String status, boolean isSystemException, String e) {
        super(status, isSystemException, e);
    }
}
