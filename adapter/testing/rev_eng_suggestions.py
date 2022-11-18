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

    # create a user
    response = requests.get(f'{API_URL}/api/agent/user')
    agent_data = json.loads(response.text)
    uuid = agent_data['uuid']

    # start a query
    response = requests.get(f'{API_URL}/api/agent/user/{uuid}/aqlqueryjson')
    response = json.loads(response.text)
    print(response)

    # users asks suggestions
    response = requests.get(f'{API_URL}/api/agent/user/{uuid}/aqlsuggestions')
    print(response.content)


if __name__ == '__main__':
    main()