import {createSlice, PayloadAction} from '@reduxjs/toolkit';
import {RootState} from '../../app/store';
import Cookies from 'universal-cookie';

export interface Dataset {
  id: string;
  name: string;
}

export interface DatasetsState {
  sets: Dataset[];
}

const cookies = new Cookies();

const initialState: DatasetsState = {
  sets: cookies.get('querylist') ? cookies.get('querylist')[0].datasets : [],
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
