@echo off
cd /d "C:\Users\esracansu\OneDrive\Belgeler\GitHub\reveal_plant\ml-api"
call conda activate myenv
python -m uvicorn app.main:app --host 0.0.0.0 --port 8000 --reload
pause
