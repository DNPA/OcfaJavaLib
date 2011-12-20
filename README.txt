HOWTO add a java module to your ocfacase.

1)
add needed jar files as an entry in <case>.conf. It assumes that the jar fiels are in /usr/local/digiwash/lib
example:
javalibraries=javalucene.jar

2) add the class files that should be started to the javamodules entry
exmple:
javamodules=nl.klpd.tde.ocfamodule.languagerecognizer.LanguageRecognizer,nl.klpd.tde.ocfamodule.luceneindexer.LuceneIndexer,nl.klpd.tde.ocfamodule.languageweaver.LanguageWeaver

3) if you are using the indexer. Make sure that it is not normally started by casemon. You might want to remove the index link.

4) Start casemon.pl

5) start the OcfaModule.jar
java -jar /usr/local/digiwash/lib/OcfaJavaLib.jar

6) The system uses log4j for logging. You can specify the log file in the enrty
log4j. A sample logfile is included in the src directory
example:
log4j=/usr/local/digiwash/etc/log4j.properties

7) Check for existance of the log-dir defined in the log4j.properties file and create if not present.

