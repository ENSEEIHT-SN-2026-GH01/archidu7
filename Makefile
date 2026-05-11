PHONY : build clean test

default: build

ROOT = $(shell git rev-parse --show-toplevel)
SOURCE_FOLDER = $(ROOT)/src
BINARY_FOLDER = $(ROOT)/bin
TEST_FOLDER = $(ROOT)/tests
LIB_FOLDER = $(ROOT)/lib
TEST_BINARY_FOLDER = $(TEST_FOLDER)/bin

ASSET_FOLDER = $(SOURCE_FOLDER)/assets
BIN_ASSET_FOLDER = $(BINARY_FOLDER)/assets
JUNIT = $(TEST_FOLDER)/lib/junit4.jar

EXEC = archidu7.jar

EMPTY :=
SPACE := $(EMPTY) $(EMPTY)

BUILD_CLASSPATH = $(subst $(SPACE),,$(foreach PATH,$(shell find lib -name "*"),$(ROOT)/$(PATH):))$(SOURCE_FOLDER)
TEST_CLASSPATH = $(ROOT)/$(EXEC):$(JUNIT)

MANIFEST_FILE = $(SOURCE_FOLDER)/META-INF/MANIFEST.MF

SRC = $(shell find $(SOURCE_FOLDER) -name "*.java")
TEST_SRC = $(shell find $(TEST_FOLDER) -name "*.java")

build: $(EXEC)
	
$(EXEC) : $(SRC)
	@mkdir -p $(BINARY_FOLDER)
	@cp -R $(ASSET_FOLDER) $(BIN_ASSET_FOLDER)

	@cd $(SOURCE_FOLDER) && javac -d $(BINARY_FOLDER) -cp $(BUILD_CLASSPATH) App.java

	@cp -R $(LIB_FOLDER) $(BINARY_FOLDER)
	@jar --create --file=$(EXEC) --manifest $(MANIFEST_FILE) -C $(BINARY_FOLDER) .

test: build $(TEST_SRC)
	@mkdir -p $(TEST_BINARY_FOLDER)

	@cd $(TEST_FOLDER) && javac -d $(TEST_BINARY_FOLDER) -cp $(TEST_CLASSPATH) $(TEST_SRC)

clean:
	@rm -fr $(BINARY_FOLDER)
	@rm -fr $(TEST_BINARY_FOLDER)
	@rm archidu7.jar
