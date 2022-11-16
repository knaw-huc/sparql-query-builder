import json
import requests
import time

QUERY = '''
PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX ga: <https://data.goldenagents.org/ontology/>

SELECT DISTINCT * WHERE {
  ?creativeAgent a ga:CreativeAgent .
  ?creativeAgent ga:hasName ?nameOfTheAgent
}
LIMIT 30
'''

API_URL = 'http://127.0.0.1:8080'

start_time = time.time()


def main():

    response = requests.get(f'{API_URL}/api/agent/list')
    all_agents = json.loads(response.text)
    # print agents
    for agent in all_agents:
        print(agent, '\n')

    print('starting queries...')

    for _ in range(5):
        print('querying...')
        
        response = requests.post(
            f'{API_URL}/sparql',
            data=QUERY,
            params={ 'format': 'json' }
        )

        print('got response...')

    print('done with querying, sleeping 10 secs...')
    time.sleep(10)

    print('killing all agents')

    response = requests.get(f'{API_URL}/api/agent/list')
    all_agents = json.loads(response.text)

    for agent in all_agents:
        print('killing agent: ', agent['uuid'], ' with type: ', agent['agentType'])
        # kill agent
        kill_response = requests.post(
            f'{API_URL}/api/agent/kill',
            json = agent
        )

    print('all agents killed...')
    response = requests.get(f'{API_URL}/api/agent/list')
    all_agents = json.loads(response.text)
    print(all_agents)



if __name__ == '__main__':
    main()