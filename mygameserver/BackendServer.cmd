@echo off
@title BackendServer
set CLASSPATH=.;target\*;.;target\lib\*;
java -Djavax.net.ssl.trustStore=keys\denny.keystore -Djavax.net.ssl.keyStore=keys\denny.keystore -Djavax.net.ssl.trustStorePassword=awfva21. -Djavax.net.ssl.keyStorePassword=awfva21.  org.server.startup.StartupServer org.server.backend.BackendServer
pause