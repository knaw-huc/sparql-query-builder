import {createApi, fetchBaseQuery} from '@reduxjs/toolkit/query/react';

export const datasetsApi = createApi({
  reducerPath: 'datasets',
  baseQuery: fetchBaseQuery({ baseUrl: process.env.REACT_APP_DATASETS_API }), // change this later to same uri als sparql api
  endpoints: (build) => ({
    getDatasets: build.query({
      query: () => 'getresources', // change this later on?, see .env and sparqlapi files
    }),
  }),
});

export const {
  useGetDatasetsQuery,
} = datasetsApi;