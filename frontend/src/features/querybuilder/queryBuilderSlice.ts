import {createSlice, PayloadAction} from '@reduxjs/toolkit';
import {RootState} from '../../app/store';
import Cookies from 'universal-cookie';

const cookies = new Cookies();

export interface QueryState {
  active: string;
  sent: string;
}

const initialState: QueryState = {
  active: cookies.get('querylist')[0] || '',
  sent: '',
};

export const queryBuilderSlice = createSlice({
  name: 'queryBuilder',
  initialState,
  // The `reducers` field lets us define reducers and generate associated actions
  reducers: {
    setActiveQuery: (state, action: PayloadAction<string>) => {
      state.active = action.payload;
    },
    setSentQuery: (state, action: PayloadAction<string>) => {
      state.sent = action.payload;
    },
  },
});

export const {setActiveQuery, setSentQuery} = queryBuilderSlice.actions;

// The function below is called a selector and allows us to select a value from
// the state. Selectors can also be defined inline where they're used instead of
// in the slice file. For example: `useSelector((state: RootState) => state.counter.value)`
export const selectActiveQuery = (state: RootState) => state.queryBuilder.active;
export const selectSentQuery = (state: RootState) => state.queryBuilder.sent;

export default queryBuilderSlice.reducer;
