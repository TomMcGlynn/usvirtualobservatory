"c:\Program Files\IIS\Microsoft Web Deploy V3\msdeploy" -verb:sync -source:package=E:\web\VAO\deploy\VAO.zip -dest:auto,computername=mastopsiisvm1 >> VAOSyncIISConfigTomastopsiisvm1.log
rem
"c:\Program Files\IIS\Microsoft Web Deploy V3\msdeploy" -verb:sync -source:package=E:\web\VAO\deploy\VAO.zip -dest:auto,computername=mastopsiisvm2 >> VAOSyncIISConfigTomastopsiisvm2.log
rem
rem "c:\Program Files\IIS\Microsoft Web Deploy V3\msdeploy" -verb:sync -source:package=E:\web\VAO\deploy\VAO.zip -dest:auto,computername=masttestiisvm1 >> VAOSyncIISConfigTomasttestiisvm1.log
rem
rem "c:\Program Files\IIS\Microsoft Web Deploy V3\msdeploy" -verb:sync -source:package=E:\web\VAO\deploy\VAO.zip -dest:auto,computername=masttestiisvm2 >> VAOSyncIISConfigTomasttestiisvm2.log

