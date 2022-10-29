import json
import logging
import requests
import sys

from logging.config import dictConfig
from quart_cors import cors
from quart import Quart, jsonify, request, websocket

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



app = Quart(__name__)
cors = cors(app, allow_origin='*')

#sio = socketio.Client()
#sio.connect('http://localhost:44444')



API_URL = 'http://127.0.0.1:8080'

# this dictionary stores all user agents
user_agents = []


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
    #try:
    # request all agents
    response = requests.get(f'{API_URL}/api/agent/list')
    all_agents = json.loads(response.text)

    # store all DB agents in the result list and store
    # user agents in the user_agents dictionary if necessary
    resources = []
    for agent in all_agents:
        if agent['agentType'] == 'DB':
            resources.append({
                'id': agent['uuid'],
                'name': agent['nickname']
            })
        elif agent['agentType'] == 'User':
            agent_uuid = agent['uuid']
            if agent_uuid not in user_agents:
                user_agents.append(agent_uuid)

    # maybe there are no user agents, create one
    if len(user_agents) < 1:
        create_user_agent()

    return jsonify(resources)
    # except:
    #     return []

    # # maybe there are no user agents, create one
    # if len(user_agents) < 5:
    #     create_user_agent()
    #     print(user_agents)

    # # register user client
    # response = requests.get(f'{API_URL}/api/sse/register/{user_agents[0]}')
    # print('----------')
    # print(response.text)
    # print('----------')

    # return []


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


# @app.route('/ga/sparql', methods=['POST'])
# def sparql():
#     """This route is for direct sparql queries"""
#     # breakdown request
#     req = json.loads(request.data.decode('UTF-8'))
#     # # create a payload suitable for the GA backend
#     # payload = {
#     #     'query': req['query'],
#     #     'queryType': 'USER_QUERY',
#     #     'selectedSources': [s['id'] for s in req['datasets']]
#     # }
#     # for now, just pick the first user agent
#     user_agent_id = list(user_agents.keys())[0]
#     # send post request to GA backend
#     response = requests.post(
#         f'{API_URL}/api/agent/user/{user_agent_id}/query',
#         data=req['query'],
#         params={ 'format': 'json' }
#     )

#     return []






# http://127.0.0.1:5000/api/agent
# http://127.0.0.1:5000/api/agent/list
# /api/agent/user/USERAGENTID/query

if __name__ == "__main__":
    app.run()
