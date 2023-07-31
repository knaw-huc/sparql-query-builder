SPARQL Query Builder
====================
User Interface to assist in building a basic SPARQL query by 1) determining the entities, 2) determining the properties of a selected entity and 3) filtering for a specific value in case of a DataProperty or continuing again with 2) case of an ObjectProperty.

Any SPARQL endpoint can be used by setting the [`REACT_APP_API` environment variable](https://github.com/knaw-huc/sparql-query-builder/blob/main/frontend/.env.production#L6). Optionally a proxy can be put inbetween, so the list of entities will be cached instead of being requested from the endpoint over and over again. In this case point the [`REACT_APP_API` environment variable](https://github.com/knaw-huc/sparql-query-builder/blob/main/frontend/.env.production#L6) to the proxy and [point the proxy to the SPARQL endpoint](https://github.com/knaw-huc/sparql-query-builder/blob/main/docker-compose.yml#L26) to be cached.
