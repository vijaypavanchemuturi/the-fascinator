@ECHO off
CD /d $INSTALL_PATH
"$JDKPath\bin\java" -jar $INSTALL_PATH\TFInstaller.jar "$INSTALL_PATH" "${fedoraAdminPassword}" "${solrAdminPassword}" "$JDKPath"
del $INSTALL_PATH\stderr
del $INSTALL_PATH\stdout
del $INSTALL_PATH\TFInstaller.jar
rmdir /S /Q "$INSTALL_PATH\lib"