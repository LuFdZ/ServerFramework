@echo off
@title MasterServer
set CLASSPATH=.;target\*;.;target\lib\*;
:: xcopy dist\*.*  target_run\/y/e 

java -Djavax.net.ssl.trustStore=keys\denny.keystore -Djavax.net.ssl.keyStore=keys\denny.keystore -Djavax.net.ssl.trustStorePassword=awfva21. -Djavax.net.ssl.keyStorePassword=awfva21. org.server.startup.StartupServer org.server.master.MasterServer
pause