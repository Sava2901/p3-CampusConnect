@echo off
setlocal
powershell -ExecutionPolicy Bypass -File "%~dp0\run.ps1" "data\other_data_base.json"
endlocal
