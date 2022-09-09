import { configureStore, ThunkAction, Action } from '@reduxjs/toolkit';
import { setupListeners } from '@reduxjs/toolkit/query';
import counterReducer from '../features/counter/counterSlice';
import uuidReducer from '../features/uuid/uuidSlice';
import { serverStatusApi } from '../features/serverstatus/serverStatusApi';
import { sseApi } from '../features/sse/sseApi';
import { agentApi } from '../features/agent/agentApi';

export const store = configureStore({
  reducer: {
    uuid: uuidReducer,
    counter: counterReducer,
    [serverStatusApi.reducerPath]: serverStatusApi.reducer,
    [sseApi.reducerPath]: sseApi.reducer,
    [agentApi.reducerPath]: agentApi.reducer,
  },
  middleware: (getDefaultMiddleware) =>
    getDefaultMiddleware()
      .concat(serverStatusApi.middleware)
      .concat(agentApi.middleware)
      .concat(sseApi.middleware),
});

setupListeners(store.dispatch);

export type AppDispatch = typeof store.dispatch;
export type RootState = ReturnType<typeof store.getState>;
export type AppThunk<ReturnType = void> = ThunkAction<
  ReturnType,
  RootState,
  unknown,
  Action<string>
>;
