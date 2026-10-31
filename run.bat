@echo off
call .\mvnw.cmd package -DskipTests
java -jar target/finance-tracker-0.0.1-SNAPSHOT.jar
pause
