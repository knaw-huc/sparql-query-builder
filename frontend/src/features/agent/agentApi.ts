import { createApi, fetchBaseQuery } from '@reduxjs/toolkit/query/react';
import { RootState } from '../../app/store';

// Function to make async request for server status
export const agentApi = createApi({
  reducerPath: 'agent',
  baseQuery: fetchBaseQuery({ baseUrl: process.env.REACT_APP_API, prepareHeaders: (headers) => {headers.set('Content-Type', 'application/json'); return headers;} }),
  endpoints: (build) => ({
    getAgent: build.query({
      query: () => `api/agent`,
    }),
    getAgentList: build.query({
      query: () => `api/agent/list`,
    }),
    sendQuery: build.query({
      query: (arg) => {
        const { userAgentId, post } = arg;
        return ({
          url: `api/agent/user/${userAgentId}/query`,
          method: 'POST',
          body: post
        })
      },
    }),
    queryResults: build.query({
      query: (arg) => ({
        url: `api/agent/user/queryresult`,
        method: 'POST',
        body: arg
      }),
    }),
  }),
});

export const { 
  useGetAgentQuery,
  useGetAgentListQuery,
  useSendQueryQuery,
  useQueryResultsQuery,
} = agentApi;