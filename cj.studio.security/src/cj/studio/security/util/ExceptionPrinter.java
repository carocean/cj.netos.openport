package cj.studio.security.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;

import cj.studio.ecm.net.Circuit;
import cj.studio.ecm.net.CircuitException;
import cj.studio.security.ResponseClient;
import cj.ultimate.gson2.com.google.gson.Gson;

public class ExceptionPrinter {
	public void printException(Throwable e, Circuit circuit) {
		e = searchNonInvocationTargetException(e);
		CircuitException ce = CircuitException.search(e);
		if (ce != null) {
			ce.printStackTrace();
			printError(Integer.valueOf(ce.getStatus()), ce.getMessage(), ce, circuit);
		} else {
			e.printStackTrace();
			printError(500, e.getMessage(), e, circuit);
		}
	}

	private Throwable searchNonInvocationTargetException(Throwable e) {
		if (e instanceof InvocationTargetException) {
			InvocationTargetException ie=(InvocationTargetException)e;
			return ie.getTargetException();
		}
		return e;
	}
	private void printError(int status, String message, Throwable e, Circuit circuit) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		e.printStackTrace(pw);
		pw.close();
		ResponseClient<?> rc = new ResponseClient<>(status, e.getMessage(), String.class.getName(),null,sw.toString());
		String json = new Gson().toJson(rc);
		circuit.content().writeBytes(json.getBytes());
	}
}
