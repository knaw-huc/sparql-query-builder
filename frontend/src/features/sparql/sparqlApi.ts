import {createApi, fetchBaseQuery} from '@reduxjs/toolkit/query/react';
import {RootState} from '../../app/store';

const headerTypes: Record<string, string> = {
  json: 'application/sparql-results+json',
  xml: 'application/sparql-results+xml',
  csv: 'text/csv',
}

// Create Sparql URL and headers for requested data type.
const dynamicBaseQuery = fetchBaseQuery({
  baseUrl: process.env.REACT_APP_API,
  prepareHeaders: (headers, {getState}) => {
    const dataType = (getState() as RootState).download.dataType;
    headers.set('Accept', headerTypes[dataType]);
    // TODO: This is apparently needed by Virtuoso sparql services,
    // which don't seem to accept JSON. Weird stuff.
    headers.set('Content-Type','application/x-www-form-urlencoded')
    return headers;
  },
});

// Methods for the Sqarql endpoint
export const sparqlApi = createApi({
  reducerPath: 'sparql',
  baseQuery: dynamicBaseQuery,
  tagTypes: ['Query', 'FETCH_ERROR', 'PARSING_ERROR', 'OTHER_ERROR'],
  endpoints: (build) => ({
    // Send Sparql query to server and save results to state
    sendSparql: build.query({
      query: ({query, datasets}) => {
        // set params here to comply with x-www-form-urlencoded.
        // otherwise, just throw this value into the body key
        const params = new URLSearchParams({ 
          query: query,
          datasets: JSON.stringify(datasets),
        });
        return ({
          url: '', //'sparql', // to change, see datasets api and .env files
          method: 'POST',
          body: params,
        })
      },
      providesTags: (result, error, {query, datasets}) => 
        result ?
        [{type:'Query', query: query, datasets: datasets}] :
        error?.status === 'FETCH_ERROR' ?
        ['FETCH_ERROR'] :
        error?.status === 'PARSING_ERROR' ?
        ['PARSING_ERROR'] :
        ['OTHER_ERROR']
    }),
    refetchErroredQuery: build.mutation<null, void>({
      queryFn: () => ({ data: null }),
      invalidatesTags: ['FETCH_ERROR', 'OTHER_ERROR'],
    }),
  }),
});

export const {
  useSendSparqlQuery,
  useRefetchErroredQueryMutation,
} = sparqlApi;

export const selectCurrentResults = sparqlApi.endpoints.sendSparql;
