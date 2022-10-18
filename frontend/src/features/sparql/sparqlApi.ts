import {createApi, fetchBaseQuery} from '@reduxjs/toolkit/query/react';
import type {
  BaseQueryFn,
  FetchArgs,
  FetchBaseQueryError,
} from '@reduxjs/toolkit/query/react';
import {RootState} from '../../app/store';

const headerTypes: Record<string, string> = {
  json: 'application/sparql-results+json',
  xml: 'application/sparql-results+xml',
  csv: 'text/csv',
}

// Sparql URL
const dynamicBaseQuery = fetchBaseQuery({
  baseUrl: process.env.REACT_APP_API,
  prepareHeaders: (headers, {getState}) => {
    const dataType = (getState() as RootState).download.dataType;
    headers.set('Accept', headerTypes[dataType]);
    return headers;
  },
});

// Methods for the Sqarql endpoint
export const sparqlApi = createApi({
  reducerPath: 'sparql',
  baseQuery: dynamicBaseQuery,
  endpoints: (build) => ({
    // Send Sparql query to server and save results to state
    sendSparql: build.query({
      query: (arg) => {
        return ({
          url: 'sparql', // to change, see datasets api and .env files
          method: 'POST',
          body: {query: arg},
        })
      },
    }),
  }),
});

export const {
  useSendSparqlQuery,
} = sparqlApi;