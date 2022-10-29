import json
import logging
import requests
import sys

from logging.config import dictConfig
from flask import Flask, jsonify, request
from flask_cors import CORS
from sseclient import SSEClient

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


QUERY = '''
PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX ga: <https://data.goldenagents.org/ontology/>

SELECT DISTINCT * WHERE {
  ?creativeAgent a ga:CreativeAgent .
  ?creativeAgent ga:hasName ?nameOfTheAgent
}
LIMIT 30
'''


app = Flask(__name__)
cors = CORS(app, resources={r'/ga/*': {
    'origins': '*'
}})


API_URL = 'http://127.0.0.1:8080'

# this list stores all user agents
user_agents = []
# this set stores all uuid's of DB agents
db_agents = set([])


# helper function to create a User agent
def create_user_agent():
    try:
        response = requests.get(f'{API_URL}/api/agent/user')
        agent_data = json.loads(response.text)
        user_agents.append(agent_data['uuid'])
        return True
    except:
        return False

    

@app.route('/ga/getresources', methods=['GET'])
def get_resources():
    """This route gets all resources from the backend and
    returns the DB agents and stores the user agents"""
    try:
        # request all agents
        response = requests.get(f'{API_URL}/api/agent/list')
        all_agents = json.loads(response.text)

        # store all DB agents in the result list and store
        # user agents in the user_agents dictionary if necessary
        resources = []
        for agent in all_agents:
            # get agent id
            agent_id = agent['uuid']
            if agent['agentType'] == 'DB':
                # add to list with resources
                resources.append({
                    'id': agent_id,
                    'name': agent['nickname']
                })
                # and also add to our global db_agents set
                db_agents.add(agent_id)

            elif agent['agentType'] == 'User':
                if agent_id not in user_agents:
                    user_agents.append(agent_id)

        print(db_agents)

        # maybe there are no user agents, create one
        if len(user_agents) < 1:
            create_user_agent()

        return jsonify(resources)
    except:
        return []


@app.route('/ga/sparql', methods=['POST'])
def sparql():
    """This route is for direct sparql queries"""
    # breakdown request
    req = json.loads(request.data.decode('UTF-8'))
    # send the post request to the GA backend
    response = requests.post(
        f'{API_URL}/sparql',
        data=req['query'],
        params={ 'format': 'json' }
    )
    # and send the result back to the frontend
    return response.text


@app.route('/ga/test', methods=['GET'])
def test():
    agent = user_agents[0]
    # register user
    messages = SSEClient(f'http://127.0.0.1:8080/api/sse/register/{agent}')
    # do a query and wait for messages
    payload = {
        'query': QUERY,
        'queryType': 'USER_QUERY',
        'selectedSources': list(db_agents)
    }
    print('\n\n', payload, '\n\n')
    response = requests.post(
        f'{API_URL}/api/agent/user/{agent}/query',
        json={'data': QUERY},
        headers={ 'Content-Type': 'application/json'},
        params={ 'format': 'json' }
    )
    return []