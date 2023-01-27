import {createSlice, PayloadAction} from '@reduxjs/toolkit';
import {RootState} from '../../app/store';

export type Dataset = {
  id: string;
  name: string;
}

export type DatasetsState = {
  sets: Dataset[];
}

const initialState: DatasetsState = {
  sets: [],
};

export const datasetsSlice = createSlice({
  name: 'selectedDatasets',
  initialState,
  // The `reducers` field lets us define reducers and generate associated actions
  reducers: {
    setSelectedDatasets: (state, action: PayloadAction<Dataset[]>) => {
      state.sets = action.payload;
    },
  },
});

export const {setSelectedDatasets} = datasetsSlice.actions;

export const selectedDatasets = (state: RootState) => state.selectedDatasets.sets;

export default datasetsSlice.reducer;
