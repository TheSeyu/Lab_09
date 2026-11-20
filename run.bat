@echo off
chcp 65001 >nul
if exist out rmdir /s /q out
dir /s /b src\*.java test\*.java > sources.txt
javac -encoding UTF-8 -d out @sources.txt || exit /b 1
del sources.txt

echo.
echo ######## CHUONG TRINH MINH HOA ########
java -Dstdout.encoding=UTF-8 -cp out payment.app.Main

echo.
echo ######## KIEM THU ########
java -Dstdout.encoding=UTF-8 -cp out payment.PaymentProcessorTest
pause
