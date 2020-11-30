NAME = faragom/sealoidc
VERSION = 0.0.44

.PHONY: all build

all: build

build:
	mvn clean install && docker build -t $(NAME):$(VERSION) --rm .
