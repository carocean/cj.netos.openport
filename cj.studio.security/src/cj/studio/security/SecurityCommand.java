package cj.studio.security;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import cj.studio.ecm.CJSystem;
import cj.studio.ecm.EcmException;
import cj.studio.ecm.net.Circuit;
import cj.studio.ecm.net.CircuitException;
import cj.studio.ecm.net.Frame;
import cj.studio.ecm.net.io.MemoryContentReciever;
import cj.studio.security.annotation.CjPermission;
import cj.studio.security.annotation.CjPermissionParameter;
import cj.studio.security.util.ExceptionPrinter;
import cj.ultimate.gson2.com.google.gson.Gson;
import cj.ultimate.gson2.com.google.gson.reflect.TypeToken;
import cj.ultimate.util.StringUtil;

class SecurityCommand {
	String servicepath;
	Class<?> face;
	ISecurityService service;
	Method method;
	List<MethodParameter> parameters;
	IAccessControlStrategy acsStrategy;
	ICheckTokenStrategy ctstrategy;

	public SecurityCommand(String servicepath, Class<?> face, ISecurityService service, Method method,
			IAccessControlStrategy acsStrategy, ICheckTokenStrategy ctstrategy) {
		this.servicepath = servicepath;
		this.face = face;
		this.service = service;
		this.method = method;
		this.acsStrategy = acsStrategy;
		this.ctstrategy = ctstrategy;
		parseMethodParameters();
	}

	private void parseMethodParameters() {
		this.parameters = new ArrayList<>();
		Parameter[] params = method.getParameters();
		for (Parameter p : params) {
			CjPermissionParameter pp = p.getAnnotation(CjPermissionParameter.class);
			if (pp == null) {
				continue;
			}
			Class<?> runType = p.getType();
			String position = String.format("%s.%s->%s", face.getName(), method.getName(), pp.name());
			MethodParameter mp = new MethodParameter(position, p, pp, runType);
			this.parameters.add(mp);
		}
	}

	public void doCommand(Frame frame, Circuit circuit) throws CircuitException {
		CjPermission mperm = this.method.getAnnotation(CjPermission.class);
		if (mperm == null) {
			throw new CircuitException("801", "拒绝访问");
		}
		String token = "";
		Map<String, Object> tokenInfo = null;
		switch (mperm.tokenIn()) {
		case headersOfRequest:
			token = frame.head(mperm.checkTokenName());
			if (StringUtil.isEmpty(token)) {
				throw new CircuitException("801", String.format("请求头中无名为%s的令牌，拒绝访问", mperm.checkTokenName()));
			}
			try {
				tokenInfo = this.ctstrategy.checkToken(token);
				this.acsStrategy.checkRight(tokenInfo,mperm.acl());
			} catch (Throwable e) {
				ExceptionPrinter printer = new ExceptionPrinter();
				printer.printException(e, circuit);
				return;
			}
			frame.content().accept(new MyMemoryContentReciever(this, frame, circuit));
			break;
		case parametersOfRequest:
			token = frame.parameter(mperm.checkTokenName());
			if (StringUtil.isEmpty(token)) {
				throw new CircuitException("801", String.format("请求参数中无名为%s的令牌，拒绝访问", mperm.checkTokenName()));
			}
			try {
				tokenInfo = this.ctstrategy.checkToken(token);
				this.acsStrategy.checkRight(tokenInfo,mperm.acl());
			} catch (Throwable e) {
				ExceptionPrinter printer = new ExceptionPrinter();
				printer.printException(e, circuit);
				return;
			}
			frame.content().accept(new MyMemoryContentReciever(this, frame, circuit));
			break;
		case nope:
			frame.content().accept(new MyMemoryContentReciever(this, frame, circuit));
			break;
		}

	}

}

class MethodParameter {
	Class<?> useType;
	CjPermissionParameter pp;
	Parameter p;
	String position;

	public MethodParameter(String position, Parameter p, CjPermissionParameter pp, Class<?> runType) {
		this.pp = pp;
		this.p = p;
		if (pp.type() != null) {
			if (runType.isAssignableFrom(pp.type())) {
				useType = pp.type();
			} else {
				useType = runType;
			}
		} else {
			useType = runType;
		}
		CJSystem.logging().info(String.format("\t\t\t\t参数：%s %s %s", pp.name(), useType.getName(), pp.in().name()));
	}

}

class MyMemoryContentReciever extends MemoryContentReciever {
	Frame frame;
	Circuit circuit;
	SecurityCommand cmd;

	public MyMemoryContentReciever(SecurityCommand cmd, Frame frame, Circuit circuit) {
		this.frame = frame;
		this.circuit = circuit;
		this.cmd = cmd;
	}

	@Override
	public void done(byte[] b, int pos, int length) throws CircuitException {
		super.done(b, pos, length);
		try {
			Map<String, Object> contentMap = null;
			if (frame.content().revcievedBytes() > 0) {
				String json = new String(readFully());
				contentMap = new Gson().fromJson(json, new TypeToken<Map<String, Object>>() {
				}.getType());
			}
			Object[] args = new Object[cmd.parameters.size()];
			for (int i = 0; i < cmd.parameters.size(); i++) {
				MethodParameter p = cmd.parameters.get(i);
				switch (p.pp.in()) {
				case header:
					Object v = frame.head(p.pp.name());
					args[i] = reflactValue(v, p);
					args[i] = reflactValue(v, p);
					break;
				case parameter:
					v = frame.parameter(p.pp.name());
					if (!StringUtil.isEmpty((String) v)) {
						try {
							v = URLDecoder.decode((String) v, "utf-8");
						} catch (UnsupportedEncodingException e) {
						}
					}
					args[i] = reflactValue(v, p);
					break;
				case content:
					if (contentMap != null) {
						v = contentMap.get(p.pp.name());
						args[i] = reflactValue(v, p);
					}
					break;
				}
			}

			Object result = cmd.method.invoke(cmd.service, args);
			doResponse(result);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			ExceptionPrinter printer = new ExceptionPrinter();
			printer.printException(e, circuit);
		} catch (Throwable e) {
			ExceptionPrinter printer = new ExceptionPrinter();
			printer.printException(e, circuit);
		}
		this.circuit = null;
		this.cmd = null;
		this.frame = null;
	}

	private void doResponse(Object result) {
		Class<?> dataType = null;
		String[] dataElements = null;
		String datastr = "";
		if (result == null) {
			dataType = Void.class;
		} else {
			dataType = result.getClass();
			datastr = new Gson().toJson(result);
			if (Collection.class.isAssignableFrom(dataType)) {
				Collection<?> col = (Collection<?>) result;
				if (!col.isEmpty()) {
					Object obj = null;
					for (Object o : col) {
						obj = o;
						break;
					}
					dataElements = new String[] { obj.getClass().getName() };
				}
			}
			if (Map.class.isAssignableFrom(dataType)) {
				@SuppressWarnings("unchecked")
				Map<Object, Object> map = (Map<Object, Object>) result;
				if (!map.isEmpty()) {
					Set<Entry<Object, Object>> set = map.entrySet();
					Entry<Object, Object> entry = null;
					for (Entry<Object, Object> _entry : set) {
						entry = _entry;
						break;
					}
					dataElements = new String[] { entry.getKey().getClass().getName(),
							entry.getValue().getClass().getName() };
				}

			}
		}
		ResponseClient<?> rc = new ResponseClient<>(200, "ok", dataType.getName(), dataElements, datastr);
		String json = new Gson().toJson(rc);
		circuit.content().writeBytes(json.getBytes());

	}

	private Object reflactValue(Object v, MethodParameter p) throws CircuitException {
		if (v == null || StringUtil.isEmpty(v + "")) {
			if (p.useType.isPrimitive()) {
				throw new CircuitException("500", "必须为基本型赋值");
			}
			return null;
		}
		if (p.useType.equals(String.class)) {
			return v;
		}
		Class<?>[] eleType = p.pp.elementType();
		if (eleType == null || eleType[0] == Void.class) {
			return new Gson().fromJson(v + "", p.useType);
		}
		if (Collection.class.isAssignableFrom(p.p.getType()) || Map.class.isAssignableFrom(p.p.getType())) {
			if (p.pp.type() != Void.class && !p.p.getType().isAssignableFrom(p.pp.type())) {
				throw new EcmException(String.format("参数注解CjPermissionParameter声明的类型不是参数类型或其派生类型。在：%s", p.position));
			}
		}
		MyParameterizedType pti = new MyParameterizedType(p.useType, eleType, p.position);
		return new Gson().fromJson(v + "", pti);
	}
}

class MyParameterizedType implements ParameterizedType {
	Type[] elementType;
	Class<?> rawType;

	public MyParameterizedType(Class<?> rawType, Type[] elementType, String onmessage) {
		this.elementType = elementType;
		this.rawType = rawType;
		if (Map.class.isAssignableFrom(rawType)) {
			if (elementType != null && elementType[0] != Void.class && elementType.length != 2) {
				throw new RuntimeException("缺少Map及其派生类的元素Key和value的类型声明,在：" + onmessage);
			}
		}
		if (Collection.class.isAssignableFrom(rawType)) {
			if (elementType != null && elementType[0] != Void.class && elementType.length != 1) {
				throw new RuntimeException("缺少Collection及其派生类的元素类型声明,在：" + onmessage);
			}
		}
	}

	@Override
	public Type[] getActualTypeArguments() {
		return elementType;
	}

	@Override
	public Type getRawType() {
		return rawType;
	}

	@Override
	public Type getOwnerType() {
		return rawType.getDeclaringClass();
	}
}