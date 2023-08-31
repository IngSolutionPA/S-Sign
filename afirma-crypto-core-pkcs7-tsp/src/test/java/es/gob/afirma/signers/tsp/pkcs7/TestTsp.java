package es.gob.afirma.signers.tsp.pkcs7;

import es.gob.afirma.core.AOException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.URI;
import java.security.MessageDigest;

import org.junit.Ignore;
import org.junit.Test;

/** Pruebas de sellos de tiempo.*/
public class TestTsp {

	private static final String CATCERT_TSP_SSL = "http://tsp.pki.gob.pa/"; //$NON-NLS-1$
	private static final boolean CATCERT_REQUIRECERT = true;

	/** Prueba de obtenci&oacute;n directa de <i>token</i> TSP RFC3161 por HTTP.
	 * @throws Exception En cualquier error */
	@SuppressWarnings("static-method")
	@Test
	//@Ignore
	public void TestRfc3161TokenHttp() throws Exception {

	    final CMSTimestamper cmsTsp = new CMSTimestamper(
        CATCERT_REQUIRECERT,
        new URI(CATCERT_TSP_SSL),
        null,
        null,
        null,
        null,
        null
    );
                
		try {
			System.out.println("Realizando solicitud de sellado de tiempo...");
			final byte[] tspToken = cmsTsp.getTimeStampToken(
				MessageDigest.getInstance("SHA-256").digest("Hola".getBytes()), //$NON-NLS-1$ //$NON-NLS-2$
				"SHA-256", //$NON-NLS-1$
				null
			);
			System.out.println("Respuesta de la TSA recibida.");
			try (
				final OutputStream fos = new FileOutputStream(File.createTempFile("TSP_", ".asn1")); //$NON-NLS-1$ //$NON-NLS-2$
			) {
				fos.write(tspToken);
				fos.flush();
			}
			System.out.println("Sello de tiempo obtenido: " + new String(tspToken));
		} catch (final AOException e) {
			System.out.println("Excepción AO: " + e.getMessage());
			e.printStackTrace();
			throw new AOException("Error obteniendo la respuesta de la TSA: " + e, e);
		}
	}

}
