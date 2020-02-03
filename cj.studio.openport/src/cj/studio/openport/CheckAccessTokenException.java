package cj.studio.openport;

import cj.studio.ecm.net.CircuitException;

public class CheckAccessTokenException extends CircuitException {

	public CheckAccessTokenException(String status, boolean isSystemException, String e) {
		super(status, isSystemException, e);
		// TODO Auto-generated constructor stub
	}

	public CheckAccessTokenException(String status, boolean isSystemException, Throwable e) {
		super(status, isSystemException, e);
		// TODO Auto-generated constructor stub
	}

	public CheckAccessTokenException(String status, String e) {
		super(status, e);
		// TODO Auto-generated constructor stub
	}

	public CheckAccessTokenException(String status, Throwable e) {
		super(status, e);
		// TODO Auto-generated constructor stub
	}

}
