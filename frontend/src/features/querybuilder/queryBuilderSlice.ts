import { createAsyncThunk, createSlice, PayloadAction } from '@reduxjs/toolkit';
import { RootState, AppThunk } from '../../app/store';

const placeholder = 
  "PREFIX ga: <http://www.goldenagents.org/ontology>\n"+
  "  SELECT * where\n"+
  "  ?a ga:CreativeAgent\n"+
  "  ?a ga:hasName";

export interface QueryState {
  value: string;
}

const initialState: QueryState = {
  value: placeholder,
};

export const queryBuilderSlice = createSlice({
  name: 'queryBuilder',
  initialState,
  // The `reducers` field lets us define reducers and generate associated actions
  reducers: {
    setActiveQuery: (state, action: PayloadAction<string>) => {
      state.value = action.payload;
    },
  },
});

export const { setActiveQuery } = queryBuilderSlice.actions;

// The function below is called a selector and allows us to select a value from
// the state. Selectors can also be defined inline where they're used instead of
// in the slice file. For example: `useSelector((state: RootState) => state.counter.value)`
export const selectQuery = (state: RootState) => state.queryBuilder.value;

export default queryBuilderSlice.reducer;
