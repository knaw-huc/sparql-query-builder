import { createAsyncThunk, createSlice, PayloadAction } from '@reduxjs/toolkit';
import { RootState, AppThunk } from '../../app/store';
import { v4 as uuidv4 } from 'uuid';

export interface UuidState {
  value: string
};

// set uuid for this session right away
const initialState: UuidState = {
  value: uuidv4()
};

export const uuidSlice = createSlice({
  name: 'uuid',
  initialState,
  reducers: {},
});

export const getUuid = (state: RootState) => state.uuid.value;

export default uuidSlice.reducer;
