Please build java one as TFInstaller.jar 
Then copy it to izpack folder including library (dist\lib to izpack\lib)
Copy resources from java\resources to izpack\resources
To build an installer:
"\path\to\IzPack\bin\compile" install.xml -b . -o The-Fascinator-Installer.jar -k standard
