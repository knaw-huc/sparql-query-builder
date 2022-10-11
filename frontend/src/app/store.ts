import { configureStore, ThunkAction, Action } from '@reduxjs/toolkit';
import { setupListeners } from '@reduxjs/toolkit/query';
import counterReducer from '../features/counter/counterSlice';
import queryBuilderReducer from '../features/querybuilder/queryBuilderSlice';
import notificationsReducer from '../features/notifications/notificationsSlice';
import { sparqlApi } from '../features/sparql/sparqlApi';

export const store = configureStore({
  reducer: {
    queryBuilder: queryBuilderReducer,
    notifications: notificationsReducer,
    [sparqlApi.reducerPath]: sparqlApi.reducer,
    counter: counterReducer,
  },
  middleware: (getDefaultMiddleware) =>
    getDefaultMiddleware()
      .concat(sparqlApi.middleware)
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
