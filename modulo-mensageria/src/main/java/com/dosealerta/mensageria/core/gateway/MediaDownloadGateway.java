package com.dosealerta.mensageria.core.gateway;

import java.net.URI;

public interface MediaDownloadGateway {

	byte[] baixar(URI mediaUrl);
}
