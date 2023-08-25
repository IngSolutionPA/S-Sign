
# S-Sign 

### Configuración preliminar

* 	Descargar SDK en https://www.oracle.com/java/technologies/downloads/
* 	Descargar NETBEANS https://netbeans.apache.org/download/
* 	Agregar las siguientes variables de entorno:

Variable 1:  `{ 'Nombre': 'JAVA_HOME', ' Valor' : 'C:\Program Files\Java\jdk-20'}`

Variable 2:  `{'Nombre': 'M2_HOME', ' Valor' : 'C:\Program Files\NetBeans-18\netbeans\java\maven'}`

Variable 3:  `{'Nombre': 'M2', ' Valor' : ' %M2_HOME%\bin'}`

* 	Editar la variable `Path` y agregar: `%JAVA_HOME%\bin` y `%M2%`

* 	Descargar el proyecto https://github.com/IngSolutionPA/S-Sign.git
 
* 	Descomprimir y guardar en tu equipo

### Instalación

* 	Abrir cmd en la ruta donde guardaste el proyecto y escribe `mvn clean install`

Este comando generará todos los módulos básicos del proyecto, finalmente visualizaremos  `[INFO] BUILD SUCCESS`.

### Artefactos desplegables y aplicaciones

* Luego debemos construir los artefactos (JAR / WAR), para ello escribimos en la consola `mvn clean install -Denv=install`

Con esto, generamos lo siguiente:

* `afirma-server-triphase-signer`: WAR con el servicio para la generación de firmas trifásicas.
* `afirma-signature-retriever`: WAR con el servicio de recuperación de datos del servidor intermedio.
* `afirma-signature-storage`: WAR con el servicio de guardado de datos en el servidor intermedio.
* `afirma-simple`: JAR autoejecutable de AutoFirma (AutoFirma.jar).
* `afirma-ui-simple-configurator`: JAR autoejecutable del configurador necesario para la instalación de AutoFirma (AutoFirmaConfigurador.jar).


### Módulos vigentes

A continuación, se muestra un listado de los distintos módulos actualmente en uso en el proyecto:

* `afirma-core`: Módulo con los componentes principales del proyecto.
* `afirma-core-keystores`: Módulo con las clases de gestión de almacenes de claves de usuario.
* `afirma-core-massive`: Módulo con funcionalidades para la ejecución de operaciones masivas de firma.
* `afirma-crypto-batch-client`: Módulo con el componente cliente para la invocación de las operaciones de firma de lote en servidor.
* `afirma-crypto-cades`: Módulo con la lógica de generación de las firmas CAdES (excluidas cofirmas y contrafirmas) y ASiC-CAdES.
* `afirma-crypto-cades-multi`: Módulo con la lógica de generación de las cofirmas y contrafirmas CAdES.
* `afirma-crypto-cadestri-client`: Módulo con lógica de invocación para la generación de firmas trifásicas CAdES en servidor.
* `afirma-crypto-cms`: Módulo con la lógica de generación de las firmas CMS.
* `afirma-crypto-core-pkcs7`: Módulo con la lógica básica de estructuras PKCS#7, necesarias para la generación de firmas ASN.1 (CAdES, PAdES, etc.).
* `afirma-crypto-core-pkcs7-tsp`: Sin soporte. Módulo con la lógica para agregar sellos de siempre a firmas PKCS#7
* `afirma-crypto-core-xml`: Módulo con la lógica básica de estructuras XML, necesarias para la generación de firmas XML (XAdES, ODF, OOXML, etc.).
* `afirma-crypto-odf`: Módulo con la lógica de generación de las firmas ODF.
* `afirma-crypto-ooxml`: Módulo con la lógica de generación de las firmas OOXML.
* `afirma-crypto-padestri-client`: Módulo con lógica de invocación para la generación de firmas trifásicas PAdES en servidor.
* `afirma-crypto-pdf`: Módulo con la lógica de generación de las firmas PAdES.
* `afirma-crypto-pdf-common`: Módulo con recursos comunes utilizados en los módulos que operan sobre firmas PDF.
* `afirma-crypto-validation`: Módulo con la lógica de verificación de la integridad de las firmas CAdES, PAdES y XAdES (no incluye la comprobación de la validez de los certificados).
* `afirma-crypto-xades`: Módulo con la lógica de generación de las firmas XAdES, ASiC-XAdES y FacturaE.
* `afirma-crypto-xadestri-client`: Módulo con lógica de invocación para la generación de firmas trifásicas XAdES y de FacturaE en servidor.
* `afirma-crypto-xmlsignature`: Módulo con la lógica de generación de las firmas XMLdSig.
* `afirma-keystores-filters`: Módulo con los filtros de certificados utilizados por AutoFirma.
* `afirma-keystores-mozilla`: Módulo para la gestión del almacén de claves de Mozilla Firefox.
* `afirma-server-triphase-signer`: Módulo principal del servicio de firma trifásica y de lotes.
* `afirma-server-triphase-signer-cache`: Módulo con la interfaz que define las operaciones de guardado y recuperación de datos de caché del servidor trifásico.
* `afirma-server-triphase-signer-core`: Módulo con la funcionalidad básica de firma trifásica CAdES, PAdES, XAdES y de FacturaE.
* `afirma-server-triphase-signer-document`: Módulo con la interfaz que define las operaciones de guardado y recuperación de documentos para firmar del servidor trifásico.
* `afirma-signature-retriever`: Módulo principal del servicio de recuperación del servidor intermedio.
* `afirma-signature-storage`: Módulo principal del servicio de guardado del servidor intermedio.
* `afirma-simple`: Módulo principal de la aplicación AutoFirma.
* `afirma-simple-installer`: Módulo con los componentes para la generación de los instaladores de AutoFirma.
* `afirma-simple-plugin-hash`: Módulo con el plugin de AutoFirma para generación y validación de hashes.
* `afirma-simple-plugin-hash-exe`: Módulo de la aplicación EXE para el registro de las entradas de generación y validación de hashes en el menú contextual de Windows.
* `afirma-simple-plugin-validatecerts`: Módulo con el plugin de AutoFirma para validación de firmas.
* `afirma-simple-plugins`: Módulo con los recursos base para la implementación de plugins de AutoFirma.
* `afirma-ui-core-jse`: Módulo con las interfaces gráficas genéricas usadas por las distintas aplicaciones del Cliente @firma.
* `afirma-ui-core-jse-keystores`: Módulo con la interfaz gráfica del diálogo de selección de certificados.
* `afirma-ui-miniapplet-deploy`: Módulo principal para el desarrollo de AutoScript.
* `afirma-ui-simple-configurator`: Módulo principal de la aplicación de configuración ejecutada durante la instalación de AutoFirma.
