import logging

from flask import Flask, jsonify
from flask_cors import CORS


app = Flask(__name__)
cors = CORS(app, resources={r'*': {
    'origins': '*'
}})

API_URL = 'https://localhost:8080'


@app.route('/actuator/platform', methods=['GET', 'OPTIONS'])
def platform():
    response = jsonify({'data': 'actuator'})
    return response

@app.route('/api/agent', methods=['GET', 'OPTIONS'])
def agent():
    response = jsonify({'data': 'agent'})
    return response

@app.route('/api/agent/list', methods=['GET', 'OPTIONS'])
def agent_list():
    response = jsonify({'data': 'agent_list'})
    return response

# http://127.0.0.1:5000/api/agent
# http://127.0.0.1:5000/api/agent/list
