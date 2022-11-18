import React, {useEffect} from 'react';
import {useAppSelector, useAppDispatch} from '../../app/hooks';
import {selectActiveQuery, selectSentQuery, setSentQuery} from './queryBuilderSlice';
import Select from 'react-select';
import styles from './QueryBuilder.module.scss';
import * as queries from './helpers/queries';
import {useSendSparqlQuery} from '../sparql/sparqlApi';
import {setSelectedDatasets, selectedDatasets} from '../datasets/datasetsSlice';

const theme = (theme: any) => ({
  ...theme,
  borderRadius: 0,
  colors: {
    ...theme.colors,
    primary25: '#efc501',
    primary: 'black',
  }
});

const options = [
  {value: 'pt', label: 'Painter'},
  {value: 'wr', label: 'Writer'},
  {value: 'ph', label: 'Philosopher'},
  {value: 'sc', label: 'Scientist'},
  {value: 'co', label: 'Colonist'},
];

export const Builder = () => {
  const dispatch = useAppDispatch();
  const currentDatasets = useAppSelector(selectedDatasets);

  const {data, isFetching, isError, error} = useSendSparqlQuery({
    query: queries.initialQuery, 
    datasets: currentDatasets
  });

  const dataItems = data?.results.bindings;

  const options = dataItems && 
    dataItems.map( (item: any) => { 
      return {value: item.c.value, label: item.c.value} 
    });

  return (
    <div>
      <h5 className={styles.header}>Build your query</h5>
      <Select 
        className={styles.select}
        options={options} 
        placeholder="Give me every..."
        theme={theme} />
    </div>
  )
}
