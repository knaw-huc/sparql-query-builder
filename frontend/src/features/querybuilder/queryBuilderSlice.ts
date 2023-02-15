import {createSlice, PayloadAction} from '@reduxjs/toolkit';
import {RootState} from '../../app/store';
import {Entity, Property} from './components/Builder';
import {defaultSelectionObject} from './components/Selector';

export type QueryState = {
  active: string;
  sent: string;
  selectedEntity: Entity;
  selectedProperties: Property[][];
  selectedLimit: number;
}

const initialState: QueryState = {
  active: '',
  sent: '',
  selectedEntity: defaultSelectionObject,
  selectedProperties: [],
  selectedLimit: 1000,
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
    setSelectedEntity: (state, action: PayloadAction<Entity>) => {
      state.selectedEntity = action.payload;
    },
    setSelectedProperties: (state, action: PayloadAction<Property[][]>) => {
      state.selectedProperties = action.payload;
    },
    setSelectedLimit: (state, action: PayloadAction<number>) => {
      state.selectedLimit = action.payload;
    },
  },
});

export const {setActiveQuery, setSentQuery, setSelectedEntity, setSelectedProperties, setSelectedLimit} = queryBuilderSlice.actions;

// The function below is called a selector and allows us to select a value from
// the state. Selectors can also be defined inline where they're used instead of
// in the slice file. For example: `useSelector((state: RootState) => state.counter.value)`
export const selectActiveQuery = (state: RootState) => state.queryBuilder.active;
export const selectSentQuery = (state: RootState) => state.queryBuilder.sent;
export const selectSelectedEntity = (state: RootState) => state.queryBuilder.selectedEntity;
export const selectSelectedProperties = (state: RootState) => state.queryBuilder.selectedProperties;
export const selectSelectedLimit = (state: RootState) => state.queryBuilder.selectedLimit;

export default queryBuilderSlice.reducer;
