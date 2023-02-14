import {createSlice, PayloadAction} from '@reduxjs/toolkit';
import {RootState} from '../../app/store';

export type Dataset = {
  id: string;
  name: string;
}

export type DatasetsState = {
  selectedSets: Dataset[];
  sentSets: Dataset[];
}

const initialState: DatasetsState = {
  selectedSets: [],
  sentSets: [],
};

export const datasetsSlice = createSlice({
  name: 'selectedDatasets',
  initialState,
  // The `reducers` field lets us define reducers and generate associated actions
  reducers: {
    setSelectedDatasets: (state, action: PayloadAction<Dataset[]>) => {
      state.selectedSets = action.payload;
    },
    setSentDatasets: (state, action: PayloadAction<Dataset[]>) => {
      state.sentSets = action.payload;
    },
  },
});

export const {setSelectedDatasets, setSentDatasets} = datasetsSlice.actions;

export const selectSelectedDatasets = (state: RootState) => state.selectedDatasets.selectedSets;
export const selectSentDatasets = (state: RootState) => state.selectedDatasets.sentSets;

export default datasetsSlice.reducer;
