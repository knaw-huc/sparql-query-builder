import { 
  configureStore, 
  ThunkAction, 
  Action, 
  isRejectedWithValue 
} from '@reduxjs/toolkit';
import {setupListeners} from '@reduxjs/toolkit/query';
import type {MiddlewareAPI, Middleware} from '@reduxjs/toolkit';
import queryBuilderReducer from '../features/querybuilder/queryBuilderSlice';
import downloadReducer from '../features/download/downloadSlice';
import notificationsReducer, {addNotification} from '../features/notifications/notificationsSlice';
import {sparqlApi} from '../features/sparql/sparqlApi';
import {datasetsApi} from '../features/datasets/datasetsApi';
import datasetsReducer from '../features/datasets/datasetsSlice';
import {downloadApi} from '../features/download/downloadApi';

/**
 * Log a warning and show a toast!
 */
export const rtkQueryErrorLogger: Middleware =
  (api: MiddlewareAPI) => (next) => (action) => {
    // RTK Query uses `createAsyncThunk` from redux-toolkit under the hood, 
    // so we're able to utilize these matchers!
    if (isRejectedWithValue(action)) {
      console.log(action)
      api.dispatch(addNotification({
        message: action.meta.arg.endpointName + ': ' + (action.payload.error || action.payload.data.message),
        type: 'error',
      }));
      console.warn('We got a rejected action!')
      console.warn(action)
    }

    return next(action)
  }

export const store = configureStore({
  reducer: {
    queryBuilder: queryBuilderReducer,
    notifications: notificationsReducer,
    [sparqlApi.reducerPath]: sparqlApi.reducer,
    [datasetsApi.reducerPath]: datasetsApi.reducer,
    selectedDatasets: datasetsReducer,
    download: downloadReducer,
  },
  middleware: (getDefaultMiddleware) =>
    getDefaultMiddleware()
      .concat(rtkQueryErrorLogger)
      .concat(sparqlApi.middleware)
      .concat(downloadApi.middleware)
      .concat(datasetsApi.middleware)
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
