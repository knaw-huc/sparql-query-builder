import hashlib
import json
import logging
import requests
import sys
import time
import uuid

from logging.config import dictConfig
from flask import Flask, jsonify, request, Response
from flask_cors import CORS
from response_cache import FIRST_RESPONSE, SECOND_RESPONSE

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

FORMATS = {
    'application/sparql-results+xml': 'xml',
    'application/sparql-results+json': 'json',
    'application/x-www-form-urlencoded': 'json',
    'text/csv': 'csv',
}

# this list stores all user agents
user_agents = []
# this set stores all uuid's of DB agents
db_agents = set([])



def query_cleaner(query):
    """Function that removes elememts that the expert
    syetem can't handle"""
    lines = query.split('\n')
    # remove optional
    lines = [l for l in lines if 'OPTIONAL {' not in l]
    # join again and return
    return '\n'.join(lines).replace('.', '')


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

        # maybe there are no user agents, create one
        if len(user_agents) < 1:
            create_user_agent()

        return jsonify(resources)
    except:
        return []






@app.route('/ga/sparql', methods=['POST'])
def sparql():
    """This route is for direct sparql queries"""
    # get accept / content type headers
    accept_header = request.headers.get('Accept', type=str)
    content_type = request.headers.get('Content-Type', type=str)

    # no default value here, I expect the contents
    # coming from Daan's React app.
    format = FORMATS.get(accept_header)

    query = False
    dataset = False
    if content_type == 'application/x-www-form-urlencoded':
        query = request.form.get('query', '').strip()
        dataset = request.form.get('datasets', '').strip()

    query_init = '04f539d2cb903e6f5332544362b9c9e1'
    creative_act = 'ef8afa8b8dd63dc1353538438df4e1f7'
    document_creation = '403de5df5923b5c9061ac09f1d48fad9'

    query_md5 = hashlib.md5(query.encode()).hexdigest()
    print(f'--> md5: {query_md5}')
    print(query)

    response = Response()
    response.content_encoding = 'UTF-8'
    response.content_type = accept_header


    if query_md5 == query_init:
        response.set_data(json.dumps(FIRST_RESPONSE))
    elif query_md5 == creative_act:
        response.set_data(json.dumps(SECOND_RESPONSE))
    elif query_md5 == document_creation:
        response.set_data(json.dumps(SECOND_RESPONSE))
    elif bool(query):
        # send a real query to the GA backend
        backend_response = requests.post(
            f'{API_URL}/sparql',
            data=query,
            params={ 'format': format }
        )
        response.set_data(backend_response._content)
    else:
        response.set_data(json.dumps({ 'message': 'no clue'}))

    # return response
    return response
