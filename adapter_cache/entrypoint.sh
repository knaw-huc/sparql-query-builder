#!/bin/sh
python3 /app/ent_prop_discovery.py
exec gunicorn -b :5000 -t 60 -w4 app:app