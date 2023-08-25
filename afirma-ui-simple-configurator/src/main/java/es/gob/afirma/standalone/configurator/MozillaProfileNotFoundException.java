/* Copyright (C) 2011 [Gobierno de Espana]
 * This file is part of "Cliente @Firma".
 * "Cliente @Firma" is free software; you can redistribute it and/or modify it under the terms of:
 *   - the GNU General Public License as published by the Free Software Foundation;
 *     either version 2 of the License, or (at your option) any later version.
 *   - or The European Software License; either version 1.1 or (at your option) any later version.
 * You may contact the copyright holder at: soporte.afirma@seap.minhap.es
 */

package es.gob.afirma.standalone.configurator;

/**
 * Excepci&oacute;n que indica que no ha sido posible encontrar directorios de perfil
 * de Firefox en el sistema.
 */
public final class MozillaProfileNotFoundException extends Exception {

	private static final long serialVersionUID = 1L;

	/**
	 * Construye la excepci&oacute;n.
	 */
	public MozillaProfileNotFoundException() {
		super();
	}

	/**
	 * Construye la excepci&oacute;n indicando un mensaje de error.
	 * @param msg Mensaje de error.
	 */
	public MozillaProfileNotFoundException(final String msg) {
		super(msg);
	}
}