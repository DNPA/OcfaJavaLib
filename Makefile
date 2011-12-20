include configure.makeinfo
all:
	if [ $(CONF_OK) ]; then \
		echo Configuration seems ok; \
	else \
		echo Can not build with bad configuration;\
	exit 1;\
	fi;
	$(ANT_BINARY) -buildfile ant/build.xml

install: all
	$(ANT_BINARY) -buildfile ant/build.xml deploy

clean:
	$(ANT_BINARY) -buildfile ant/build.xml clean
