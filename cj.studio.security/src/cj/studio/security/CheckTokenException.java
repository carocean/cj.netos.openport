package cj.studio.security;

import cj.studio.ecm.net.CircuitException;

public class CheckTokenException extends CircuitException {

	public CheckTokenException(String status, boolean isSystemException, String e) {
		super(status, isSystemException, e);
		// TODO Auto-generated constructor stub
	}

	public CheckTokenException(String status, boolean isSystemException, Throwable e) {
		super(status, isSystemException, e);
		// TODO Auto-generated constructor stub
	}

	public CheckTokenException(String status, String e) {
		super(status, e);
		// TODO Auto-generated constructor stub
	}

	public CheckTokenException(String status, Throwable e) {
		super(status, e);
		// TODO Auto-generated constructor stub
	}

}
