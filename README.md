liste des commandes a faire (à partir du dossier racine):

# build
rm lib/archidu7.jar
cd src
javac -d ../bin -cp "../lib/*;." Launcher.java
cd ..
jar --create --file=archidu7.jar --manifest=src/META-INF/MANIFEST.MF -C bin/ .

# lancer le projet
cp archidu7.jar lib/
cd lib
java -jar archidu7.jar
