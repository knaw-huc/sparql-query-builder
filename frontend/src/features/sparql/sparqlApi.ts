import { createApi, fetchBaseQuery } from '@reduxjs/toolkit/query/react';
import type {
  BaseQueryFn,
  FetchArgs,
  FetchBaseQueryError,
} from '@reduxjs/toolkit/query/react';
import { RootState } from '../../app/store';
import { selectDataType } from '../download/downloadSlice';

const headerTypes: Record<string, string> = {
  json: 'application/sparql-results+json',
  xml: 'application/sparql-results+xml',
  csv: 'text/csv',
}

// Sparql URL
const rawBaseQuery = fetchBaseQuery({
  baseUrl: process.env.REACT_APP_API,
  prepareHeaders: (headers, {getState}) => {
    const dataType = (getState() as RootState).download.dataType;
    console.log(dataType)
    headers.set('Accept', headerTypes[dataType]);
    return headers;
  },
});

// Get dynamic Sparql URL to set headers
// const dynamicBaseQuery: BaseQueryFn<
//   string | FetchArgs,
//   unknown,
//   FetchBaseQueryError
// > = async (args, api, extraOptions) => {
//   const dataType = selectDataType(api.getState() as RootState)
//   // gracefully handle scenarios where data to generate the URL is missing
//   if (!dataType) {
//     return {
//       error: {
//         status: 400,
//         statusText: 'Bad Request',
//         data: 'Invalid data type',
//       },
//     }
//   }

//   const urlEnd = typeof args === 'string' ? args : args.url
//   // construct a dynamically generated portion of the url
//   const adjustedUrl = ``;
//   const adjustedArgs =
//     typeof args === 'string' ? adjustedUrl : { ...args, url: adjustedUrl }
//   // provide the amended url and other params to the raw base query
//   return rawBaseQuery(adjustedArgs, api, extraOptions)
// }

// Methods for the Sqarql endpoint
export const sparqlApi = createApi({
  reducerPath: 'sparql',
  baseQuery: rawBaseQuery,
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