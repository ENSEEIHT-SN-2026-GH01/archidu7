.PHONY: build_windows

default: build_windows

build_windows: 
	Copy-Item -Path "src/assets" -Destination "bin/assets" -Recurse

	javac -d ../bin -cp "../lib/*;." Launcher.java
	jar --create --file=archidu7.jar --manifest=src/META-INF/MANIFEST.MF -C bin/ .