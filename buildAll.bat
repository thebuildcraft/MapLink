@echo off & setlocal enabledelayedexpansion

echo ==================== Note: All build jars will be in the folder called 'buildAllJars' ====================
del /F /Q buildAllJars\original\*
del /F /Q buildAllJars\*
mkdir buildAllJars
mkdir buildAllJars\original

@rem Loop trough everything in the version properties folder
for %%f in (versionProperties\*) do (
    @rem Get the name of the version that is going to be compiled
    set version=%%~nf

    @rem Clean out the folders, build it, and merge it
    echo ==================== Cleaning workspace to build !version! ====================
    call .\gradlew.bat clean -PmcVer="!version!" --no-daemon
    del fabric\build\libs\*.jar
    del /F /Q fabric\build
    del forge\build\libs\*.jar
    del /F /Q forge\build
    del neoforge\build\libs\*.jar
    del /F /Q neoforge\build
    del spigot\build\libs\*.jar
    del /F /Q spigot\build
    echo ==================== Building !version! ====================
    call .\gradlew.bat build -PmcVer="!version!" --no-daemon
    echo ==================== Copying jars ====================
    copy fabric\build\libs\*.jar buildAllJars\original\
    copy forge\build\libs\*.jar buildAllJars\original\
    copy neoforge\build\libs\*.jar buildAllJars\original\
    copy spigot\build\libs\*.jar buildAllJars\original\
    echo ==================== Deleting unnecessary *-all.jars ====================
    del /F /Q buildAllJars\original\*-all.jar
    del /F /Q buildAllJars\original\*-dev-shadow.jar
    @rem echo ==================== Merging !version! ====================
    @rem call .\gradlew.bat mergeJars -PmcVer="!version!" --no-daemon
    @rem echo ==================== Moving Merged jar ====================
    @rem move Merged\*.jar buildAllJars\
)

echo ================================================================
echo =========================== FINISHED ===========================
echo ================================================================

endlocal
