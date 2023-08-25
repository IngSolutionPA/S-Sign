/* Copyright (C) 2011 [Gobierno de Espana]
 * This file is part of "Cliente @Firma".
 * "Cliente @Firma" is free software; you can redistribute it and/or modify it under the terms of:
 *   - the GNU General Public License as published by the Free Software Foundation;
 *     either version 2 of the License, or (at your option) any later version.
 *   - or The European Software License; either version 1.1 or (at your option) any later version.
 * You may contact the copyright holder at: soporte.afirma@seap.minhap.es
 */

package es.gob.afirma.core.misc.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.Proxy;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.TrustManager;
import javax.security.auth.callback.PasswordCallback;

import es.gob.afirma.core.misc.AOUtil;
import es.gob.afirma.core.misc.Base64;
import es.gob.afirma.core.misc.Platform;

/** Clase para la lectura y env&iacute;o de datos a URL remotas.
 * @author Carlos Gamuci.
 * @author Tom&aacute;s Garc&iacute;a-Mer&aacute;s. */
public class UrlHttpManagerImpl implements UrlHttpManager {

	private static final Logger LOGGER = Logger.getLogger("es.gob.afirma"); //$NON-NLS-1$

	/** Nombre de la propiedad a establecer a <code>true</code> para deshabilitar las comprobaciones
	 * de confianza SSL en las peticiones. Si se establece a <code>false</code> o no se establece se
	 * usa la confianza por defecto de la JVM. */
	public static final String JAVA_PARAM_DISABLE_SSL_CHECKS = "disableSslChecks"; //$NON-NLS-1$

	/** Lista de dominios seguros para conexiones HTTPS. */
	public static final String JAVA_PARAM_SECURE_DOMAINS_LIST = "secureDomainsList"; //$NON-NLS-1$

	/** Tiempo de espera por defecto para descartar una conexi&oacute;n HTTP. */
	public static final int DEFAULT_TIMEOUT = -1;

	private static final String HTTPS = "https"; //$NON-NLS-1$

	private static final String URN_SEPARATOR = ":"; //$NON-NLS-1$
	private static final String PROT_SEPARATOR = URN_SEPARATOR + "//"; //$NON-NLS-1$

	private static final String ACCEPT = "Accept"; //$NON-NLS-1$

	private static KeyStore sslKeyStore = null;
	private static PasswordCallback sslKeyStorePasswordCallback = null;

	static {
		final CookieManager cookieManager = new CookieManager();
		cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
		CookieHandler.setDefault(cookieManager);
	}

	/** Constructor. */
	protected UrlHttpManagerImpl() {
		// Vacio y "protected"
	}

	/** Establece el almac&eacute;n de claves a usar en SSL.
	 * Esto permite usar un almac&eacute;n de claves que necesite una inicializaci&oacute;n a medida
	 * y que por lo tanto no baste con indicarlo en las variables de entorno.
	 * El almac&eacute;n debe proporcionarse inicializado y cargado.
	 * Si se especifica aqu&iacute; un almac&eacute;n de claves se ignoran las variables de entorno
	 * que pudiesen estar establecidas.
	 * @param ks Almac&eacute;n de claves a usar en SSL. */
	public static void setSslKeyStore(final KeyStore ks) {
		sslKeyStore = ks;
	}

	/** Establece el <code>PasswordCallback</code> a usar cuando se establece directamente un
	 * almac&eacute;n de claves a usar en SSL.
	 * @param pwc <code>PasswordCallback</code> a usar cuando se establece directamente un
	 * almac&eacute;n de claves a usar en SSL. */
	public static void setSslKeyStorePasswordCallback(final PasswordCallback pwc) {
		sslKeyStorePasswordCallback = pwc;
	}

	@Override
	public byte[] readUrl(final String url, final UrlHttpMethod method) throws IOException {
		return readUrl(url, DEFAULT_TIMEOUT, null, null, method);
	}

	private static boolean isLocal(final URL url) {
		if (url == null) {
			throw new IllegalArgumentException("La URL no puede ser nula"); //$NON-NLS-1$
		}
		try {
			return InetAddress.getByName(url.getHost()).isLoopbackAddress();
		}
		catch (final Exception e) {
			LOGGER.warning("Error comprobando si una URL es el bucle local: " + e); //$NON-NLS-1$
			return false;
		}
	}

	@Override
	public byte[] readUrl(final String urlToRead,
			              final int timeout,
			              final String contentType,
			              final String accept,
			              final UrlHttpMethod method) throws IOException {
		final Properties headers = new Properties();
		if (contentType != null) {
			headers.setProperty("Content-Type", contentType); //$NON-NLS-1$
		}
		if (accept != null) {
			headers.setProperty(ACCEPT, accept);
		}
		return readUrl(urlToRead, timeout, method, headers);
	}

	@Override
	public byte[] readUrl(final String urlToRead,
	                      final int timeout,
		                  final UrlHttpMethod method,
		                  final Properties requestProperties) throws IOException {
		if (urlToRead == null) {
			throw new IllegalArgumentException("La URL a leer no puede ser nula"); //$NON-NLS-1$
		}

		// Vemos si lleva usuario y contrasena
		final String authString;
		final String url;
		final URLName un = new URLName(urlToRead);

		if (un.getUsername() != null || un.getPassword() != null) {
			final String tmpStr;
			if (un.getUsername() != null && un.getPassword() != null) {
				tmpStr = un.getUsername() + URN_SEPARATOR + un.getPassword();
			}
			else if (un.getUsername() != null) {
				tmpStr = un.getUsername();
			}
			else {
				tmpStr = un.getPassword();
			}
			authString = Base64.encode(tmpStr.getBytes());
			url = un.getProtocol() +
				PROT_SEPARATOR +
					un.getHost() +
						(un.getPort() != -1 ? URN_SEPARATOR +
							Integer.toString(un.getPort()) : "") + //$NON-NLS-1$
								"/" + //$NON-NLS-1$
									(un.getFile() != null ? un.getFile() : ""); //$NON-NLS-1$
		}
		else {
			url = urlToRead;
			authString = null;
		}

		String urlParameters = null;
		String request = null;
		if (UrlHttpMethod.POST.equals(method) || UrlHttpMethod.PUT.equals(method)) {
			final StringTokenizer st = new StringTokenizer(url, "?"); //$NON-NLS-1$
			request = st.nextToken();
			if (url.contains("?")) { //$NON-NLS-1$
				urlParameters = st.nextToken();
			}
		}

		final URL uri = new URL(request != null ? request : url);

		final boolean needDisableSslChecks = Boolean.parseBoolean(
				System.getProperty(JAVA_PARAM_DISABLE_SSL_CHECKS, "false") //$NON-NLS-1$
		);
		final boolean isSecureDomain = checkIsSecureDomain(uri);

		if ((needDisableSslChecks || isSecureDomain) && uri.getProtocol().equals(HTTPS)) {
			try {
				disableSslChecks();
				LOGGER.info("Deshabilitada la comprobacion SSL para el acceso al dominio: " + uri.getHost()); //$NON-NLS-1$
			}
			catch(final Exception e) {
				LOGGER.warning(
					"No se ha podido ajustar la confianza SSL, es posible que no se pueda completar la conexion: " + e //$NON-NLS-1$
				);
			}
		}

		byte[] data;
		try {
			final HttpURLConnection conn;
			if (Platform.OS.ANDROID.equals(Platform.getOS()) || isLocal(uri)) {
				conn = (HttpURLConnection) uri.openConnection(Proxy.NO_PROXY);
			}
			else {
				conn = (HttpURLConnection) uri.openConnection();
			}

			conn.setUseCaches(false);
			conn.setDefaultUseCaches(false);

			conn.setRequestMethod(method.toString());

			// Trabajamos las cabeceras, las por defecto y las que nos indiquen

			final Properties headers = new Properties();
			if (requestProperties != null) {
				headers.putAll(requestProperties);
			}

			if (authString != null && !headers.containsKey("Authorization")) { //$NON-NLS-1$
				conn.addRequestProperty("Authorization", "Basic " + authString); //$NON-NLS-1$ //$NON-NLS-2$
			}
			if (!headers.containsKey(ACCEPT)) {
				conn.addRequestProperty(
						ACCEPT,
						"*/*" //$NON-NLS-1$
						);
			}
			if (!headers.containsKey("Connection")) { //$NON-NLS-1$
				conn.addRequestProperty("Connection", "keep-alive"); //$NON-NLS-1$ //$NON-NLS-2$
			}
			if (!headers.containsKey("Host")) { //$NON-NLS-1$
				conn.addRequestProperty("Host", uri.getHost()); //$NON-NLS-1$
			}
			if (!headers.containsKey("Origin")) { //$NON-NLS-1$
				conn.addRequestProperty("Origin", uri.getProtocol() +  "://" + uri.getHost()); //$NON-NLS-1$ //$NON-NLS-2$
			}

			// Ponemos el resto de las cabeceras
			for (final Map.Entry<?, ?> entry: headers.entrySet()) {
				conn.addRequestProperty(
						(String) entry.getKey(),
						(String) entry.getValue()
						);
			}

			if (urlParameters != null) {
				conn.setRequestProperty(
						"Content-Length", String.valueOf(urlParameters.getBytes(StandardCharsets.UTF_8).length) //$NON-NLS-1$
						);
				conn.setDoOutput(true);
				try (
						final OutputStream os = conn.getOutputStream()
						) {
					os.write(urlParameters.getBytes(StandardCharsets.UTF_8));
				}
			}

			// Si se ha establecido un tiempo de timeout, se configura como tiempo maximo hasta la conexion
			if (timeout != DEFAULT_TIMEOUT) {
				conn.setConnectTimeout(timeout);
			}

			conn.connect();
			final int resCode = conn.getResponseCode();
			final String statusCode = Integer.toString(resCode);
			if (statusCode.startsWith("4") || statusCode.startsWith("5")) { //$NON-NLS-1$ //$NON-NLS-2$
				if (uri.getProtocol().equals(HTTPS)) {
					enableSslChecks();
				}
				throw new HttpError(
						resCode,
						conn.getResponseMessage(),
						AOUtil.getDataFromInputStream(
								conn.getErrorStream()
								),
						url
						);
			}

			try (
					final InputStream is = conn.getInputStream()
					) {
				data = AOUtil.getDataFromInputStream(is);
			}
		}
		catch (final IOException e) {
			// Elevamos cualquier excepcion, pero nos aseguramos antes de reactivar
			// la validacion SSL si corresponde
			if (needDisableSslChecks && uri.getProtocol().equals(HTTPS)) {
				enableSslChecks();
			}
			throw e;
		}

		if (needDisableSslChecks && uri.getProtocol().equals(HTTPS)) {
			enableSslChecks();
		}

		return data;
	}

	/** Habilita las comprobaciones de certificados en conexiones SSL dej&aacute;ndolas con su
	 * comportamiento por defecto. */
	public static void enableSslChecks() {
		SslSecurityManager.enableSslChecks();
	}

	/** Establece los <code>TrustManager</code> de las conexiones SSL.
	 * @param tms <code>TrustManager</code> a establecer.
	 * @param hv Verificador de nombres de <i>host</i> a usar en las conexiones SSL.
	 * @throws KeyManagementException Si hay problemas en la gesti&oacute;n de claves SSL.
	 * @throws NoSuchAlgorithmException Si el JRE no soporta alg&uacute;n algoritmo necesario. */
	public static void setTrustManager(final TrustManager[] tms, final HostnameVerifier hv) throws KeyManagementException, NoSuchAlgorithmException {
		SslSecurityManager.setTrustManagerAndKeyManager(tms, hv, sslKeyStore, sslKeyStorePasswordCallback);
	}

	/** Deshabilita las comprobaciones de certificados en conexiones SSL, acept&aacute;dose entonces
	 * cualquier certificado.
	 * @throws KeyManagementException Si hay problemas en la gesti&oacute;n de claves SSL.
	 * @throws NoSuchAlgorithmException Si el JRE no soporta alg&uacute;n algoritmo necesario. */
	public static void disableSslChecks() throws KeyManagementException, NoSuchAlgorithmException {
		SslSecurityManager.disableSslChecks();
	}

	/**
	 * Comprueba si el dominio de una URL se considera seguro con respecto a una lista de dominios
	 * seguros establecidos a traves de la propiedad determinado por la constante
	 * JAVA_PARAM_SECURE_DOMAINS_LIST.
	 * @param url URL que se desea comprobar.
	 * @return {@code true} si el dominio de la URL es seguro, {@code false} en caso contrario.
	 */
	private static boolean checkIsSecureDomain(final URL url) {

		final String secureDomainsList = System.getProperty(JAVA_PARAM_SECURE_DOMAINS_LIST);
		if (secureDomainsList != null && !secureDomainsList.isEmpty()) {
			final String urlHost = url.getHost();
			final String [] secureDomainsArray = secureDomainsList.split(","); //$NON-NLS-1$
			for (final String secureDomain : secureDomainsArray) {
				// Caso 1 - Dominios con * al principio y al final. Ej: *.redsara.*
				final String replSecureDomain = secureDomain.replace("*","");  //$NON-NLS-1$//$NON-NLS-2$
				if (secureDomain.startsWith("*") && secureDomain.endsWith("*")) { //$NON-NLS-1$ //$NON-NLS-2$
					if (urlHost.contains(replSecureDomain)) {
						return true;
					}
				}
				// Caso 2 - Dominios con * solo al principio
				else if (secureDomain.startsWith("*")) {  //$NON-NLS-1$
					if (urlHost.endsWith(replSecureDomain)) {
						return true;
					}
				}
				// Caso 3 - Dominios con * solo al final
				else if (secureDomain.endsWith("*")) { //$NON-NLS-1$
					if (urlHost.startsWith(replSecureDomain)) {
						return true;
					}
				}
				// Caso 4 - Dominios sin *
				else if (urlHost.equals(replSecureDomain)) {
					return true;
				}
			}
		}
		return false;
	}

}
