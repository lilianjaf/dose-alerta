package com.dosealerta.usuario.core.gateway;

public interface SenhaGateway {

	String hash(String senhaPlana);

	boolean confere(String senhaPlana, String hash);
}
