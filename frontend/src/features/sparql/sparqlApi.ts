import { createApi, fetchBaseQuery } from '@reduxjs/toolkit/query/react';
import type {
  BaseQueryFn,
  FetchArgs,
  FetchBaseQueryError,
} from '@reduxjs/toolkit/query/react';
import { RootState } from '../../app/store';

// Default Sparql URL
const rawBaseQuery = fetchBaseQuery({
  baseUrl: 'https://api.druid.datalegend.net/datasets/AdamNet/all/services/endpoint/sparql',
});

// Get dynamic Sparql URL from selected dataset
const dynamicBaseQuery: BaseQueryFn<
  string | FetchArgs,
  unknown,
  FetchBaseQueryError
> = async (args, api, extraOptions) => {
  const dataSet = 1; //selectProjectId(api.getState() as RootState)
  // gracefully handle scenarios where data to generate the URL is missing
  if (!dataSet) {
    return {
      error: {
        status: 400,
        statusText: 'Bad Request',
        data: 'No dataset selected',
      },
    }
  }

  const urlEnd = typeof args === 'string' ? args : args.url
  // construct a dynamically generated portion of the url
  const adjustedUrl = ``;
  const adjustedArgs =
    typeof args === 'string' ? adjustedUrl : { ...args, url: adjustedUrl }
  // provide the amended url and other params to the raw base query
  return rawBaseQuery(adjustedArgs, api, extraOptions)
}

// Methods for the Sqarql endpoint
export const sparqlApi = createApi({
  reducerPath: 'sparql',
  baseQuery: dynamicBaseQuery,
  endpoints: (build) => ({
    // Send Sparql query to server and save results to state
    sendSparql: build.query({
      query: (arg) => {
        return ({
          url: '',
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