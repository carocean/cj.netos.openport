package cj.studio.security;

import cj.studio.ecm.net.CircuitException;

public class CheckRightException extends CircuitException {

	public CheckRightException(String status, Throwable e) {
		super(status, e);
		// TODO Auto-generated constructor stub
	}

	public CheckRightException(String status, String e) {
		super(status, e);
		// TODO Auto-generated constructor stub
	}

	public CheckRightException(String status, boolean isSystemException, Throwable e) {
		super(status, isSystemException, e);
		// TODO Auto-generated constructor stub
	}

	public CheckRightException(String status, boolean isSystemException, String e) {
		super(status, isSystemException, e);
		// TODO Auto-generated constructor stub
	}

}
