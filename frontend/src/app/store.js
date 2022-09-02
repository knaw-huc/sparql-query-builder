import { configureStore } from '@reduxjs/toolkit';
import { setupListeners } from '@reduxjs/toolkit/query';
import counterReducer from '../features/counter/counterSlice';
import { serverStatusApi } from '../features/serverstatus/serverStatusApi';

export const store = configureStore({
  reducer: {
    counter: counterReducer,
    [serverStatusApi.reducerPath]: serverStatusApi.reducer,
  },
  // adding the api middleware enables caching, invalidation, polling and other features of `rtk-query`
  middleware: (getDefaultMiddleware) =>
    getDefaultMiddleware().concat(serverStatusApi.middleware),
});

setupListeners(store.dispatch);