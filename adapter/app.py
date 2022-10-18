import logging

from flask import Flask, jsonify
from flask_cors import CORS

import sys
from logging.config import dictConfig

dictConfig({
    'version': 1,
    'formatters': {'default': {
        'format': '[%(asctime)s] %(levelname)s in %(module)s: %(message)s',
    }},
    'handlers': {'wsgi': {
        'class': 'logging.StreamHandler',
        'stream': 'ext://sys.stdout',
        'formatter': 'default'
    }},
    'root': {
        'level': 'INFO',
        'handlers': ['wsgi']
    }
})


app = Flask(__name__)
cors = CORS(app, resources={r'/ga/*': {
    'origins': '*'
}})

API_URL = 'https://localhost:8080'

@app.route('/', methods=['GET', 'OPTIONS', 'POST'])
def agent():
    response = jsonify({'data': 'agent'})
    return response

@app.route('/actuator/platform', methods=['GET', 'OPTIONS'])
def platform():
    response = jsonify({'data': 'actuator'})
    return response

@app.route('/api/agent/list', methods=['GET', 'OPTIONS'])
def agent_list():
    response = jsonify({'data': 'agent_list'})
    return response

# http://127.0.0.1:5000/api/agent
# http://127.0.0.1:5000/api/agent/list
