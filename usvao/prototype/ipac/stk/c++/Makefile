WAF=python waf

all: build

build:
	@$(WAF) configure
	@$(WAF) build

clean:
	@$(WAF) clean

distclean:
	@$(WAF) distclean

list:
	@$(WAF) list

install:
	@$(WAF) install

uninstall:
	@$(WAF) uninstall


.PHONY: all build clean dist distclean list install uninstall

