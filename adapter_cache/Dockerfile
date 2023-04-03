FROM python:3.7-slim

ENV PYTHONPATH /app
ENV PYTHONUNBUFFERED 1
ENV FLASK_APP=/app/app.py

WORKDIR /app
COPY . /app

RUN pip3 install --trusted-host pypi.python.org -r /app/requirements.txt

EXPOSE 5000

ENTRYPOINT ["/app/entrypoint.sh"]
