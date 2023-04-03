import hashlib
import json
import os
import re
import requests
import sqlite3

from logging.config import dictConfig
from flask import Flask, request, Response
from flask_cors import CORS
from pathlib import Path

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

API_URL = os.environ.get('API_URL', default='http://localhost:7200/repositories/ga-wp3')
DB_PATH = Path(os.environ.get('DB_PATH', default='./ga_cache.db'))


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
    query_init = 'c31ec35bdb3eff3a111c7db445a0b30a'
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


@app.route('/ga', methods=['POST'])
def sparql():
    """This route is for direct sparql queries"""
    # get accept / content type headers
    accept_header = request.headers.get('Accept', type=str)
    content_type = request.headers.get('Content-Type', type=str)

    query = False
    if content_type == 'application/x-www-form-urlencoded':
        query = request.form.get('query', '').strip()

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
                API_URL,
                data=query,
                headers={
                    'Content-Type': 'application/sparql-query',
                    'Accept': accept_header
                }
            )
            response.set_data(backend_response._content)

        else:
            response.set_data(json.dumps({ 'message': 'no clue'}))

    # return response
    return response


if __name__ == '__main__':
    app.run()
