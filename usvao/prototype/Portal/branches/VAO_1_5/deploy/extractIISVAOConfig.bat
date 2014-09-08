"c:\Program Files\IIS\Microsoft Web Deploy V3\msdeploy" -verb:sync -source:metakey=lm/w3svc/3,computername=masttestiisvm1 -disableLink:content -dest:package=VAO.zip >> VAO.log
rem "c:\Program Files\IIS\Microsoft Web Deploy V3\msdeploy" -verb:sync -source:metakey=lm/w3svc/3,computername=mastopsiisvm1 -disableLink:content -dest:package=VAO.zip >> VAO.log

