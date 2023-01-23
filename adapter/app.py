import hashlib
import json
import logging
import re
import requests
import sqlite3
import sys
import time
import uuid

from logging.config import dictConfig
from flask import Flask, jsonify, request, Response
from flask_cors import CORS
from pathlib import Path
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

API_URL = 'http://127.0.0.1:8080'

DB_PATH = Path('./testing/ga_cache.db')

FORMATS = {
    'application/sparql-results+xml': 'xml',
    'application/sparql-results+json': 'json',
    'application/x-www-form-urlencoded': 'json',
    'text/csv': 'csv',
}


def query_cleaner(query):
    """Function that removes elememts that the expert
    system can't handle"""
    lines = query.split('\n')
    # remove optional
    lines = [l for l in lines if 'OPTIONAL {' not in l]
    # join again and return
    return '\n'.join(lines).replace('.', '')


def create_user_agent():
    """helper function to create a User agent"""
    try:
        response = requests.get(f'{API_URL}/api/agent/user')
        agent_data = json.loads(response.text)
        user_agents.append(agent_data['uuid'])
        return True
    except:
        return False


def create_response_from_cache(bindings):
    response = {
        'head': {
            'link': [],
            'vars': []
        },
        'results': {
            'distinct': False,
            'ordered': True,
            'bindings': bindings
        }
    }
    response = json.dumps(response)
    return response


def is_initial_query(query):
    """Checks if sparql query from front end is intial entity query"""
    # md5 of initial query
    query_init = '3e706daa06e2714757fcf7106a71e000'
    # compare incoming query with init query
    query_md5 = hashlib.md5(query.encode()).hexdigest()
    return query_init == query_md5


def is_property_query(query):
    """Checks if sparql query from front end is secondary property query.
    Returns the entity we are processing, or False"""
    # md5 of secondary property query
    query_prop = '450a939f59cbdff99c23a3da7995f212'
    # try to remove entity (we don't know which entity)
    query_cleaned = re.sub('\?sub a <.*?> \.', '?sub a <> .', query)
    query_md5 = hashlib.md5(query_cleaned.encode()).hexdigest()
    # now we check if this is the property query
    if query_md5 == query_prop:
        try:
            # return the entity
            return re.search('\?sub a <(.*?)> \.', query).group(1)
        except:
            return False
    else:
        # no, not the property query, return False
        return False


def collect_bindings(cursor):
    """Helper function to translate a query into a list
    containing all bindings"""
    bindings = ', '.join([r[0] for r in cursor.fetchall()])
    return json.loads(f'[{bindings}]')



# setting up the app and CORS
app = Flask(__name__)
cors = CORS(app, resources={r'/ga/*': {
    'origins': '*'
}})

# setup a connection/cursor with database
conn = sqlite3.connect(DB_PATH, check_same_thread=False)
cursor = conn.cursor()

# this list stores all user agents
user_agents = []

# this set stores all uuid's of DB agents
db_agents = set([])

    
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

    # form a response
    response = Response()
    response.content_encoding = 'UTF-8'
    response.content_type = accept_header

    # check if initial query
    if is_initial_query(query):
        # initial query, select all entities
        qry = f'SELECT data FROM entities ORDER BY name'
        cursor.execute(qry)
        bindings = collect_bindings(cursor)
        response.set_data(create_response_from_cache(bindings))

    else:
        # we are not dealing the initial entities query,
        # is this the second, properties, query?
        entity = is_property_query(query)

        if bool(entity):
            # get all properties of this entity schema
            qry = f'SELECT data FROM properties WHERE ' \
                + f'entity_value="{entity}" ORDER BY name'
            cursor.execute(qry)
            bindings = collect_bindings(cursor)
            response.set_data(create_response_from_cache(bindings))

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
