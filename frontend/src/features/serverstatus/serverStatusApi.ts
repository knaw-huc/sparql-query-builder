import { createApi, fetchBaseQuery } from '@reduxjs/toolkit/query/react';

// Function to make async request for server status
export const serverStatusApi = createApi({
  reducerPath: 'serverStatusApi',
  baseQuery: fetchBaseQuery({ baseUrl: process.env.REACT_APP_API }),
  endpoints: (builder) => ({
    serverStatus: builder.query<{online: boolean}, void>({
      query: () => 'actuator/platform',
    }),
  }),
});

export const { useServerStatusQuery } = serverStatusApi;