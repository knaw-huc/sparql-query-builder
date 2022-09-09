import { createApi, fetchBaseQuery } from '@reduxjs/toolkit/query/react';

const headers = new Headers();

// Function to make async request for server status
export const sseApi = createApi({
  reducerPath: 'sse',
  baseQuery: fetchBaseQuery({ baseUrl: process.env.REACT_APP_API }),
  endpoints: (build) => ({
    sseRegister: build.query({
      query: (arg) => {
        const { uuid } = arg;
        return `api/sse/register/${uuid}`
      },
      async onCacheEntryAdded(
        arg,
        { updateCachedData, cacheDataLoaded, cacheEntryRemoved }
      ) {
        console.log(updateCachedData)
        console.log(cacheDataLoaded)
        const { uuid, userAgentId, queryId } = arg;
        console.log(`sse registered with uuid ${uuid}`);
        console.log(`query? ${queryId}`);

        // create a connection when the cache subscription starts
        // twee keer nu, not good
        // werk aan de winkel
        // check https://redux-toolkit.js.org/rtk-query/usage/streaming-updates
        const sse = new EventSource(
          `${process.env.REACT_APP_API}api/sse/register/${uuid}`
        );

        // dit moet allemaal eigenlijk onder try, maar die cacheDataLoaded resolved dus niet
        const listener = (event: MessageEvent) => {
          console.log(`event listener ${event.type} called for sseRegister`);
          const data = JSON.parse(event.data);
          console.log(data);
          updateCachedData((draft) => {
            console.log(draft);
            // draft.push(data);
          });
        };
        
        sse.addEventListener(`messages_${userAgentId}`, listener);
        sse.addEventListener(`agent_state_ready`, listener);

        // deze listener kan eigenlijk pas worden toegevoegd als de sse connectie er al is.
        // Hoe kunnen we dit hier gelijk doen zonder dat we de query id weten?
        // Of deze connectie anders eerst killen?
        // Is de query id in dit event type echt nodig en kan die niet uit de backend worden gesloopt?
        // Kunnen die IDs, ook agent id, er uberhaupt niet uit?
        sse.addEventListener(`query_progress_${queryId}`, listener);

        try {
          // wait for the initial query to resolve before proceeding
          await cacheDataLoaded;

          // Daan: deze promise resolved nooit. Ligt dat aan de API of aan ons?

        } catch {
          // no-op in case `cacheEntryRemoved` resolves before `cacheDataLoaded`,
          // in which case `cacheDataLoaded` will throw
        }

        // cacheEntryRemoved will resolve when the cache subscription is no longer active
        await cacheEntryRemoved;
        // perform cleanup steps once the `cacheEntryRemoved` promise resolves
        sse.close();
      },
    }),
    sseSubscribeAgentState: build.query({
      query: (arg) => {
        const { uuid } = arg;
        console.log(`registering agent_state_ready for uuid ${uuid}`)
        return `api/sse/subscribe/${uuid}/agent_state_ready`
      },
    }),
    sseSubscribeMessages: build.query({
      query: (arg) => {
        const { uuid, userAgentId } = arg;
        console.log(`registering messages_uuid for uuid ${uuid} and agent ${userAgentId}`)
        return `api/sse/subscribe/${uuid}/messages_${userAgentId}`
      },
    }),
    sseSubscribeQueryProgress: build.query({
      query: (arg) => {
        const { uuid, queryId } = arg;
        console.log(`registering query_progress for query id ${queryId}`)
        return `api/sse/subscribe/${uuid}/query_progress_${queryId}`
      },
    }),
  }),
});

export const { 
  useSseRegisterQuery,
  useSseSubscribeAgentStateQuery,
  useSseSubscribeMessagesQuery,
  useSseSubscribeQueryProgressQuery,
} = sseApi;