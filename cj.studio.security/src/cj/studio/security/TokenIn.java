package cj.studio.security;

public enum TokenIn {
	headersOfRequest, parametersOfRequest,/**即访问该方法不需要token，即完全开放的方法*/nope
}
