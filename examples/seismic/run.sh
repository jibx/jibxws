CLASSPATH=../../lib/jibx-ws.jar:../../lib/commons-logging.jar:../../lib/log4j.jar:$JIBX_HOME/lib/jibx-run.jar:$JIBX_HOME/lib/xpp3.jar:$JIBX_HOME/lib/jibx-extras.jar
java -cp build/classes:$CLASSPATH com.sosnoski.seismic.client.BindClient $@
 
