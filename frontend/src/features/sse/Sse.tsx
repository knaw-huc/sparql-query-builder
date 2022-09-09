import React from 'react';
import { useSseRegisterQuery } from './sseApi';
import { RootState } from '../../app/store';
import { useAppSelector } from '../../app/hooks';
import { getUuid } from '../uuid/uuidSlice';

export const Sse = () => {
  const {
    data,
    error,
  } = useSseRegisterQuery( useAppSelector(getUuid) );

  console.log('sse')
  console.log(data)

  const stats = useAppSelector( (state: RootState) => state.sse );

  console.log(stats)

  return (
    <p>
      
    </p>
  );
}
