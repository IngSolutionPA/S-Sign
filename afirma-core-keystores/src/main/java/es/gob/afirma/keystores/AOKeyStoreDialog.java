/* Copyright (C) 2011 [Gobierno de Espana]
 * This file is part of "Cliente @Firma".
 * "Cliente @Firma" is free software; you can redistribute it and/or modify it under the terms of:
 *   - the GNU General Public License as published by the Free Software Foundation;
 *     either version 2 of the License, or (at your option) any later version.
 *   - or The European Software License; either version 1.1 or (at your option) any later version.
 * You may contact the copyright holder at: soporte.afirma@seap.minhap.es
 */

package es.gob.afirma.keystores;

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.security.KeyStore.PrivateKeyEntry;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import es.gob.afirma.core.AOCancelledOperationException;
import es.gob.afirma.core.AOException;
import es.gob.afirma.core.keystores.CertificateContext;
import es.gob.afirma.core.keystores.KeyStoreManager;
import es.gob.afirma.core.keystores.NameCertificateBean;
import es.gob.afirma.core.misc.Platform;
import es.gob.afirma.core.ui.AOUIFactory;
import es.gob.afirma.core.ui.KeyStoreDialogManager;
import es.gob.afirma.keystores.filters.CertificateFilter;

/** Di&aacute;logo para la selecci&oacute;n de certificados.
 * @author Carlos Gamuci. */
public final class AOKeyStoreDialog implements KeyStoreDialogManager {

	private static final Logger LOGGER = Logger.getLogger("es.gob.afirma"); //$NON-NLS-1$

	private static final String[] EXTS = new String[] { "pfx", "p12" }; //$NON-NLS-1$ //$NON-NLS-2$
	private static final String EXTS_DESC = " (*.p12, *.pfx)"; //$NON-NLS-1$

	private final AggregatedKeyStoreManager ksm;
	private final Object parentComponent;
	private final boolean checkPrivateKeys;
	private final boolean checkValidity;
	private final boolean showExpiredCertificates;
	private final List<? extends CertificateFilter> certFilters;
	private final boolean mandatoryCertificate;

	private String selectedAlias = null;

	private boolean allowExternalStores = true;

    /** Crea un di&aacute;logo para la selecci&oacute;n de un certificado.
     * @param ksm Gestor de los almac&eacute;nes de certificados a los que pertenecen los alias.
     *            Debe ser {@code null} si se quiere usar el m&eacute;todo para seleccionar
     *            otra cosa que no sean certificados X.509 (como claves de cifrado).
     * @param parentComponent Componente gr&aacute;fico sobre el que mostrar los di&aacute;logos.
     * @param checkPrivateKeys Indica si se debe comprobar que el certificado tiene clave
     *                         privada o no, para no mostrar aquellos que carezcan de ella.
     * @param checkValidity Indica si se debe comprobar la validez temporal de un
     *                      certificado al ser seleccionado.
     * @param showExpiredCertificates Indica si se deben o no mostrar los certificados caducados o
     *                                a&uacute;n no v&aacute;lidos. */
    public AOKeyStoreDialog(final AOKeyStoreManager ksm,
    		                final Object parentComponent,
    		                final boolean checkPrivateKeys,
    		                final boolean showExpiredCertificates,
    		                final boolean checkValidity) {
		this(
			ksm,
			parentComponent,
			checkPrivateKeys,
			showExpiredCertificates,
			checkValidity,
			null,
			false
		);
    }

    /** Crea un di&aacute;logo para la selecci&oacute;n de un certificado.
     * @param ksm Gestor de los almac&eacute;nes de certificados entre los que se selecciona.
     * @param parentComponent Componente gr&aacute;fico sobre el que mostrar los di&aacute;logos.
     * @param checkPrivateKeys Indica si se debe comprobar que el certificado tiene clave
     *                         privada o no, para no mostrar aquellos que carezcan de ella.
     * @param showExpiredCertificates Indica si se deben o no mostrar los certificados caducados o
     *                                aun no v&aacute;lidos.
     * @param checkValidity Indica si se debe comprobar la validez temporal de un
     *                      certificado al ser seleccionado.
     * @param certFilters Filtros sobre los certificados a mostrar.
     * @param mandatoryCertificate Indica si los certificados disponibles (tras aplicar el
     *                             filtro) debe ser solo uno. */
	public AOKeyStoreDialog(final AOKeyStoreManager ksm,
			                final Object parentComponent,
                            final boolean checkPrivateKeys,
                            final boolean showExpiredCertificates,
                            final boolean checkValidity,
                            final List<? extends CertificateFilter> certFilters,
                            final boolean mandatoryCertificate) {

		if (ksm == null) {
    		throw new IllegalArgumentException("El almacen de claves no puede ser nulo"); //$NON-NLS-1$
    	}

		this.ksm = new AggregatedKeyStoreManager(ksm);
		this.parentComponent = parentComponent;
		this.checkPrivateKeys = checkPrivateKeys;
		this.checkValidity = checkValidity;
		this.showExpiredCertificates = showExpiredCertificates;
		this.certFilters = certFilters != null ? new ArrayList<>(certFilters) : null;
		this.mandatoryCertificate = mandatoryCertificate;
	}

	@Override
	public NameCertificateBean[] getNameCertificates() {

    	final Map<String, String> aliassesByFriendlyName =
        		KeyStoreUtilities.getAliasesByFriendlyName(
    				this.ksm.getAliases(),
    				this.ksm,
    				this.checkPrivateKeys,
    				this.showExpiredCertificates,
    				this.certFilters
    			);

    	int i = 0;
    	final NameCertificateBean[] namedCerts =
    			new NameCertificateBean[aliassesByFriendlyName.size()];
    	for (final String certAlias : aliassesByFriendlyName.keySet().toArray(new String[aliassesByFriendlyName.size()])) {
    		final X509Certificate[] certChain = this.ksm.getCertificateChain(certAlias);
    		if (certChain != null) {
	    		namedCerts[i++] = new NameCertificateBean(
	    				certAlias,
	    				aliassesByFriendlyName.get(certAlias),
	    				certChain);
    		}
    		else {
    			LOGGER.warning("Se ha encontrado un certificado nulo en el almacen"); //$NON-NLS-1$
    		}
    	}

		return namedCerts;
	}

	@Override
	public void setKeyStoreManager(final KeyStoreManager ksm) {
		this.ksm.removeAll();
		this.ksm.addKeyStoreManager((AOKeyStoreManager) ksm);
	}

	@Override
	public boolean changeKeyStoreManager(final int keyStoreId, final Component parent) {

		AOKeyStoreManager newKsm = null;

		try {
			switch (keyStoreId) {
			// Almacen de Firefox
			case KEYSTORE_ID_MOZILLA:
				newKsm = openMozillaKeyStore(parent);
				break;

			// Almacen PKCS#12
			case KEYSTORE_ID_PKCS12:
				newKsm = openPkcs12KeyStore(parent);
				break;

			// DNIe
			case KEYSTORE_ID_DNIE:
				newKsm = openDnieKeyStore(parent);
				break;

			// Almacen del sistema
			case KEYSTORE_ID_SYSTEM:
			default:
				newKsm = openSystemKeyStore(parent);
				break;
			}
		}
		catch (final AOCancelledOperationException | AOKeystoreAlternativeException e) {
			LOGGER.info("Operacion cancelada por el usuario: " + e); //$NON-NLS-1$
			return false;
		}
		catch (final Exception e) {
			LOGGER.log(Level.SEVERE, "Error cambiando de almacen de claves: " + e, e); //$NON-NLS-1$
			AOUIFactory.showErrorMessage(
				KeyStoreMessages.getString("AOKeyStoreDialog.10"), //$NON-NLS-1$
				KeyStoreMessages.getString("AOKeyStoreDialog.9"), //$NON-NLS-1$
				AOUIFactory.ERROR_MESSAGE,
				e
			);
			return false;
		}

		// Establece el nuevo almacen cargado como el actual
		setKeyStoreManager(newKsm);

		return true;
	}

	@Override
	public int[] getAvailablesKeyStores() {

		// En linux no se puede cambiar entre el almacen central del sistema y el almacen de
		// Mozilla por un error en NSS que sigue cargando el almacen que ya tuviese aunque se le
		// indique otro. Por eso, solo damos la opcion de almacen central o almacen de Firefox,
		// segun el almacen que se cargue primero
		int[] keystoreTypes;
		if (Platform.getOS() == Platform.OS.LINUX) {
			if (this.ksm.getType() == AOKeyStore.SHARED_NSS ||
					this.ksm.getKeyStoreManagers().size() > 0 && this.ksm.getKeyStoreManagers().get(0).getType() == AOKeyStore.SHARED_NSS) {
				keystoreTypes = new int[] {
					KEYSTORE_ID_SYSTEM,
					KEYSTORE_ID_PKCS12,
					KEYSTORE_ID_DNIE
				};
			}
			else {
				keystoreTypes = new int[] {
					KEYSTORE_ID_MOZILLA,
					KEYSTORE_ID_PKCS12,
					KEYSTORE_ID_DNIE
				};
			}
		}
		else {
			keystoreTypes = new int[] {
				KEYSTORE_ID_SYSTEM,
				KEYSTORE_ID_MOZILLA,
				KEYSTORE_ID_PKCS12,
				KEYSTORE_ID_DNIE
			};
		}
		return keystoreTypes;
	}

	@Override
	public String getKeyStoreName() {
		final AOKeyStoreManager aoKsm;
		final List<AOKeyStoreManager> ksmList = this.ksm.getKeyStoreManagers();
		if (ksmList != null && ksmList.size() > 0) {
			aoKsm = ksmList.get(0);
		}
		else {
			aoKsm = this.ksm;
		}
		return aoKsm != null && aoKsm.getType() != null ? aoKsm.getType().getName() : null;
	}

	/**
	 * Carga el almac&eacute;n de claves del &uacute;ltimo perfil de Mozilla activo.
	 * @param parent Componente padre sobre el que mostrar los di&aacute;logos gr&aacute;ficos.
	 * @return Gestor del almac&eacute;n de claves o {@code null} si no se encuentra el almac&eacute;n,
	 * si no se pudo cargar o si se cancel&oacute; la carga.
	 * @throws AOCancelledOperationException Cuando el usuario cancela la operaci&oacute;n.
	 * @throws Exception Cuando no se puede cargar el almac&eacute;n de claves.
	 */
	private static AOKeyStoreManager openMozillaKeyStore(final Component parent) throws Exception {

		try {
			return AOKeyStoreManagerFactory.getAOKeyStoreManager(
				AOKeyStore.MOZ_UNI,
				null,
				null,
				AOKeyStore.MOZ_UNI.getStorePasswordCallback(parent),
				parent
			);
		}
		catch (final AOCancelledOperationException e) {
			throw e;
		}
		catch (final Exception e) {
			LOGGER.log(Level.WARNING,"No se ha podido cargar el almacen de claves de Mozilla: " + e, e); //$NON-NLS-1$
			throw e;
		}
	}


	/**
	 * Permite seleccionar un fichero PKCS#12, introducir su contrase&ntilde;a y cargarlo.
	 * @param parent Componente padre sobre el que mostrar los di&aacute;logos gr&aacute;ficos.
	 * @return Gestor del almac&eacute;n PKCS#12 o {@code null} si no se pudo cargar o si se
	 * cancel&oacute; la carga.
	 * @throws AOCancelledOperationException Cuando el usuario cancela la operaci&oacute;n.
	 * @throws Exception Cuando no se puede cargar el almac&eacute;n de claves.
	 */
	private static AOKeyStoreManager openPkcs12KeyStore(final Component parent) throws Exception {

		final File[] ksFile;
		ksFile = AOUIFactory.getLoadFiles(
			KeyStoreMessages.getString("AOKeyStoreDialog.6"), //$NON-NLS-1$
			null,
			null,
			EXTS,
			KeyStoreMessages.getString("AOKeyStoreDialog.7") + EXTS_DESC, //$NON-NLS-1$
			false,
			false,
			null,
			parent
		);

		// Cargamos el almacen
		try {
			return AOKeyStoreManagerFactory.getAOKeyStoreManager(
				AOKeyStore.PKCS12,
				ksFile[0].getAbsolutePath(),
				null,
				AOKeyStore.PKCS12.getStorePasswordCallback(parent),
				parent
			);
		}
		catch (final AOCancelledOperationException e) {
			throw e;
		}
		catch (final Exception e) {
			LOGGER.log(Level.WARNING,"No se ha podido cargar el almacen de claves PKCS#12 seleccionado: " + e, e); //$NON-NLS-1$
			throw e;
		}
	}

	/**
	 * Carga el almac&eacute;n de claves del DNIe.
	 * @param parent Componente padre sobre el que mostrar los di&aacute;logos gr&aacute;ficos.
	 * @return Gestor del almac&eacute;n de claves o {@code null} si no se encuentra un DNIe insertado,
	 * si no se pudo cargar o si se cancel&oacute; la carga.
	 * @throws AOCancelledOperationException Cuando el usuario cancela la operaci&oacute;n.
	 * @throws Exception Cuando no se puede cargar el almac&eacute;n de claves.
	 */
	private static AOKeyStoreManager openDnieKeyStore(final Component parent) throws Exception {

		final AOKeyStoreManager ksm = new AOKeyStoreManager();
		try {
			// Proporcionamos el componente padre como parametro
			ksm.init(
				AOKeyStore.DNIEJAVA,
				null,
				null,
				new Object[] { parent },
				true
			);
		}
		catch (final AOCancelledOperationException e) {
			throw e;
		}
		catch (final Exception e) {
			LOGGER.log(Level.WARNING,"No se ha podido cargar el DNIe: " + e, e); //$NON-NLS-1$
			throw e;
		}
		return ksm;
	}


	/**
	 * Carga el almac&eacute;n de claves del DNIe.
	 * @param parent Componente padre sobre el que mostrar los di&aacute;logos gr&aacute;ficos.
	 * @return Gestor del almac&eacute;n de claves o {@code null} si no se encuentra un DNIe insertado,
	 * si no se pudo cargar o si se cancel&oacute; la carga.
	 * @throws AOCancelledOperationException Cuando el usuario cancela la operaci&oacute;n.
	 * @throws AOKeystoreAlternativeException Cuando no se identifica el sistema operativo como uno de
	 * los soportados.
	 * @throws Exception Cuando no se puede cargar el almac&eacute;n de claves.
	 */
	private static AOKeyStoreManager openSystemKeyStore(final Component parent) throws AOKeystoreAlternativeException,
		                                                                               Exception {
		final AOKeyStore ks;
		final Platform.OS currentOs = Platform.getOS();
		if (currentOs == Platform.OS.WINDOWS) {
			ks = AOKeyStore.WINDOWS;
		}
		else if (currentOs == Platform.OS.LINUX || currentOs == Platform.OS.SOLARIS) {
			ks = AOKeyStore.SHARED_NSS;
		}
		else if (currentOs == Platform.OS.MACOSX) {
			ks = AOKeyStore.APPLE;
		}
		else {
			throw new AOKeystoreAlternativeException(null, "No se ha podido identificar un almacen del sistema compatible"); //$NON-NLS-1$
		}

		try {
			return AOKeyStoreManagerFactory.getAOKeyStoreManager(
				ks,
				null,
				null,
				ks.getStorePasswordCallback(parent),
				parent
			);
		}
		catch (final AOCancelledOperationException e) {
			throw e;
		}
		catch (final Exception e) {
			LOGGER.log(Level.WARNING,"No se ha podido cargar el almacen del sistema: " + e, e); //$NON-NLS-1$
			throw e;
		}
	}

	@Override
	public Object getKeyEntry(final String alias) throws AOException {

		PrivateKeyEntry pke = null;
		if (this.checkPrivateKeys) {
			try {
				pke = this.ksm.getKeyEntry(alias);
			}
			catch (final Exception e) {
				LOGGER.severe("No se ha podido extraer la clave del almacen: " + e); //$NON-NLS-1$
				throw new AOException("No se ha podido extraer la clave del almacen", e); //$NON-NLS-1$
			}
		}

		this.selectedAlias = alias;

		if (this.checkValidity && this.ksm != null) {

    		String errorMessage = null;

    		final X509Certificate cert = this.ksm.getCertificate(this.selectedAlias);

			try {
				cert.checkValidity();
			}
			catch (final CertificateExpiredException e) {
				errorMessage = KeyStoreMessages.getString("AOKeyStoreDialog.2"); //$NON-NLS-1$
			}
			catch (final CertificateNotYetValidException e) {
				errorMessage = KeyStoreMessages.getString("AOKeyStoreDialog.3"); //$NON-NLS-1$
			}
			catch (final Exception e) {
				errorMessage = KeyStoreMessages.getString("AOKeyStoreDialog.4"); //$NON-NLS-1$
			}

    		boolean rejected = false;

			if (errorMessage != null) {
				LOGGER.warning("Error durante la validacion: " + errorMessage); //$NON-NLS-1$
				if (AOUIFactory.showConfirmDialog(
						this.parentComponent,
						errorMessage,
						KeyStoreMessages.getString("AOKeyStoreDialog.5"), //$NON-NLS-1$
						AOUIFactory.YES_NO_OPTION,
						AOUIFactory.WARNING_MESSAGE
				) != AOUIFactory.YES_OPTION) {
					rejected = true;
				}
			}

			if (rejected) {
				throw new AOCancelledOperationException("Se ha reusado un certificado probablemente no valido"); //$NON-NLS-1$
			}
    	}

		return this.checkPrivateKeys ? pke : this.ksm.getCertificateChain(alias);
	}

	@Override
	public String show() throws AOCertificatesNotFoundException {

		final NameCertificateBean[] namedCertificates = getNameCertificates();

		// No mostramos el dialogo de seleccion si se ha indicado que se autoseleccione
		// un certificado en caso de ser el unico
		if (this.mandatoryCertificate && namedCertificates != null && namedCertificates.length == 1) {
			this.selectedAlias = namedCertificates[0].getAlias();
			return this.selectedAlias;
		}

		// Mostramos el dialogo de seleccion de certificados
		try {
			this.selectedAlias = AOUIFactory.showCertificateSelectionDialog(this.parentComponent, this);
		}
		catch (final IllegalStateException e) {
			throw new AOCertificatesNotFoundException("No se han encontrado certificados validos en el almacen", e); //$NON-NLS-1$
		}

		// Si devuelve null, es que el usuario cancelo el dialogo
		if (this.selectedAlias == null) {
			throw new AOCancelledOperationException("No se ha seleccionado certificado"); //$NON-NLS-1$
		}

		return this.selectedAlias;
	}

	@Override
	public String getSelectedAlias() {
		return this.selectedAlias;
	}

	@Override
	public CertificateContext getSelectedCertificateContext() {
		return new CertificateContext(this.ksm, this.selectedAlias);
	}

	@Override
	public void refresh() throws IOException {
		this.ksm.refresh();
	}

	@Override
	public void allowOpenExternalStores(final boolean showButton) {
		this.allowExternalStores = showButton;
	}

	@Override
	public boolean isExternalStoresOpeningAllowed() {
		return this.allowExternalStores;
	}
}
