Servidor falso de SMTP
======================

Como servidor falso de SMTP usaremos la herramienta [FakeSMTP](https://nilhcem.github.io/FakeSMTP/).

### Ejecución automática

Dentro del directorio ```additional-material``` se adjunta un pequeño script
```fake-smtp.sh``` que se encargará de descargar y ejecutar el servidor falso
de SMTP de forma automática. Para lanzarlo bastará ejecutar desde línea de
comandos, y encontrándose en el directorio del proyecto lo siguiente:

        $ pushd additional-material/fake-smtp && ./fakesmtp.sh && popd

### Ejecución manual

Para poder usarla en local, de forma manual, se seguirán los siguientes pasos
(asumimos entorno Unix en este tutorial):

1. Descargar y descomprimir FakeSMTP:

        $ wget http://nilhcem.github.com/FakeSMTP/downloads/fakeSMTP-latest.zip
        $ unzip fakeSMTP-latest.zip
        $ mv fakeSMTP-2.0.jar fakesmtp.jar
        $ rm fakeSMTP-latest.zip

2. Arrancar la herramienta:

        $ sudo java -jar fakesmtp.jar -s -o maildir

La herramienta por defecto intentará arrancarse en el puerto ```25```. Esto
debería requerir permisos de ```root```, por ello se usa ```sudo``` al
arrancarse. Si se desea ejecutar como usuario no privilegiado, será necesario
cambiar el puerto por defecto, lo cual se puede hacer con el flag ```-p```.
Pero esto requerirá cambiar también el ```remote-destination``` dentro de la
configuración del servidor Wildfly local. Ejemplo:

        $ java -jar fakesmtp.jar -s -o maildir -p 2525
        $ sed -i 's/remote-destination host="localhost" port="25"/remote-destination host="localhost" port="2525"/g' standalone.xml

El flag ```-o``` establecido en los comandos anteriores determina en qué
carpeta se guardarán los mails recibidos, en este caso se guardarían dentro de
la carpeta ```maildir``` del directorio actual.

