import { 
  configureStore, 
  ThunkAction, 
  Action, 
  isRejectedWithValue 
} from '@reduxjs/toolkit';
import { setupListeners } from '@reduxjs/toolkit/query';
import type { MiddlewareAPI, Middleware } from '@reduxjs/toolkit';
import counterReducer from '../features/counter/counterSlice';
import queryBuilderReducer from '../features/querybuilder/queryBuilderSlice';
import downloadReducer from '../features/download/downloadSlice';
import notificationsReducer, { addNotification } from '../features/notifications/notificationsSlice';
import { sparqlApi } from '../features/sparql/sparqlApi';
import { downloadApi } from '../features/download/downloadApi';

/**
 * Log a warning and show a toast!
 */
export const rtkQueryErrorLogger: Middleware =
  (api: MiddlewareAPI) => (next) => (action) => {
    // RTK Query uses `createAsyncThunk` from redux-toolkit under the hood, 
    // so we're able to utilize these matchers!
    if (isRejectedWithValue(action)) {
      api.dispatch(addNotification({
        message: action.payload.error || action.payload.data.message,
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
    counter: counterReducer,
    download: downloadReducer,
  },
  middleware: (getDefaultMiddleware) =>
    getDefaultMiddleware()
      .concat(sparqlApi.middleware)
      .concat(rtkQueryErrorLogger)
      .concat(downloadApi.middleware)
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
