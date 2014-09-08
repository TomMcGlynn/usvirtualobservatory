rem
rem copy contents of e:\web\VAO from masttestiisvm1 to masttestisvm2
rem The log file is appended to with each invocation of this script
rem /mir mirrors the contents of the source directory, i.e. it will remove files from the destination if they do not
rem exist at the source
rem /NP no progress bar to the log file
rem See robocopyHelp.txt in this directory for further information about switches
rem
robocopy \\masttestiisvm1\e$\web\VAO\ \\masttestiisvm2\e$\web\VAO\ /mir /LOG+:robocopyVAO_MASTTESTIISVM2.log /NP
