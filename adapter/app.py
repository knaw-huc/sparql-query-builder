import json
import logging
import requests
import sys

from logging.config import dictConfig
from flask import Flask, jsonify, request
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

# this dictionary stores all user agents
user_agents = {}


@app.route('/ga/getresources', methods=['GET'])
def get_resources():
    """This route gets all resources from the backend and
    returns the DB agents and stores the user agents"""
    try:
        response = requests.get(f'{API_URL}/api/agent/list')
        all_agents = json.loads(response.text)
        print(all_agents)
        # store all DB agents in this list
        resources = []
        for agent in all_agents:
            print('AGENT', agent)
            if agent['agentType'] == 'DB':
                resources.append({
                    'id': agent['uuid'],
                    'name': agent['nickname']
                })
            elif agent['agentType'] == 'User':
                agent_uuid = agent['uuid']
                if agent_uuid not in user_agents.keys():
                    user_agents[agent_uuid] = agent['nickname']
        return jsonify(resources)
    except:
        return []


@app.route('/ga/sparql', methods=['POST'])
def sparql():
    """This route is for direct sparql queries"""
    # breakdown request
    req = json.loads(request.data.decode('UTF-8'))
    # create a payload suitable for the GA backend
    payload = {
        'query': req['query'],
        'queryType': 'USER_QUERY',
        'selectedSources': [s['id'] for s in req['datasets']]
    }
    # for now, just pick the first user agent
    user_agent_id = list(user_agents.keys())[0]
    # send post request to GA backend
    response = requests.post(
        f'{API_URL}/api/agent/user/{user_agent_id}/query',
        json=payload
    )

    print(response.text)


    #print(request.data.['query'])
    #print(request.data.decode('UTF-8')['datasets'])
    return []



@app.route('/ga/', methods=['GET', 'OPTIONS', 'POST'])
def agent():
    response = jsonify({'data': ['url_1', 'url_2']})
    return response



# http://127.0.0.1:5000/api/agent
# http://127.0.0.1:5000/api/agent/list
# /api/agent/user/USERAGENTID/query
