echo off
SET REV=%1%
echo Getting %rev% from cvs 

del *.aspx
cd help
del *.aspx

cvs update -r %REV%

cd ..
cvs update -r %REV%

