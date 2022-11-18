import aiohttp
import asyncio
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


async def main():

    async with aiohttp.ClientSession() as session:

        for _ in range(20):
            
            async with session.post(
                f'{API_URL}/sparql',
                data=QUERY,
                params={ 'format': 'json' }
            ) as resp:
                response = await resp.text()
                print(response)

if __name__ == '__main__':
    asyncio.run(main())
    print("--- %s seconds ---" % (time.time() - start_time))