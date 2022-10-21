import json
import logging
import requests
import sys

from logging.config import dictConfig
from flask import Flask, jsonify
from flask_cors import CORS

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

API_URL = 'http://127.0.0.1:8080'


@app.route('/ga/getresources', methods=['GET'])
def get_resources():
    """This route gets all resources from the backend"""
    try:
        response = requests.get(f'{API_URL}/api/agent/list')
        all_agents = json.loads(response.text)
        resources = [
            { 'id': agent['uuid'], 'name': agent['nickname'] } 
            for agent in all_agents 
            if agent['agentType'] == 'DB'
        ]
        return resources
    except:
        return []

@app.route('/ga/', methods=['GET', 'OPTIONS', 'POST'])
def agent():
    response = jsonify({'data': ['url_1', 'url_2']})
    return response



# http://127.0.0.1:5000/api/agent
# http://127.0.0.1:5000/api/agent/list
