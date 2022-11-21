import React, {useState} from 'react';
import {useAppSelector, useAppDispatch} from '../../app/hooks';
import {AnimatePresence} from 'framer-motion';
import {selectActiveQuery, selectSentQuery, setSentQuery} from './queryBuilderSlice';
import Select from 'react-select';
import styles from './QueryBuilder.module.scss';
import * as queries from './helpers/queries';
import {useSendSparqlQuery} from '../sparql/sparqlApi';
import {setSelectedDatasets, selectedDatasets} from '../datasets/datasetsSlice';
import Spinner from 'react-bootstrap/Spinner';
import {FadeDiv} from '../animations/Animations';

// TODO: Better typescript types

interface SelectOptions {
  label: string;
  value: string;
}

const theme = (theme: any) => ({
  ...theme,
  borderRadius: 0,
  colors: {
    ...theme.colors,
    primary25: '#efc501',
    primary: 'black',
  }
});

export const Builder = () => {
  const dispatch = useAppDispatch();
  const currentDatasets = useAppSelector(selectedDatasets);

  const [selectedEntities, setSelectedEntities] = useState([]);
  const [selectedProperties, setSelectedProperties] = useState({});

  // Do initial entity query
  const {data, isFetching, isError, error} = useSendSparqlQuery({
    query: queries.entityQuery, 
    datasets: currentDatasets
  });

  // Do some filtering on results
  const entities = data?.results.bindings;
  // Must have a label
  const entityOptions = entities && 
    entities.filter( (item: any) => item.hasOwnProperty('l')).map( (item: any) => {
      return {value: item.c.value, label: item.l.value}
    })
    // and no duplicates
    .filter((v: SelectOptions, i: number, a: any[]) => a.findIndex((v2: SelectOptions) => (v2.label === v.label)) === i);

  // Keep track of selected properties for each entity, passed down to PropertySelect
  const setSelectedPropertiesObject = ( entity: string, newData: SelectOptions) => {
    setSelectedProperties({...selectedProperties, [entity]: newData});
  }

  return (
    <div>
      <h5 className={styles.header}>Build your query</h5>
      <label id="entities-label" htmlFor="entities-input" className={styles.label}>
        Select entities
      </label>
      <Select 
        aria-labelledby="entities-label"
        inputId="entities-input"
        className={styles.select}
        placeholder="Give me every..."
        theme={theme}
        isMulti
        noOptionsMessage={() => isFetching ? 
          <Spinner
            as="span"
            animation="border"
            size="sm"
            role="status"
            aria-hidden="true"/>  : 
          'No properties found'
        }
        options={entityOptions}
        onChange={(e: any) => setSelectedEntities(e)} />

      <AnimatePresence>
        {selectedEntities.map( (entity: SelectOptions, i: number) => 
          <FadeDiv key={entity.value}>
            <PropertySelect entity={entity} onChange={setSelectedPropertiesObject}/>
          </FadeDiv>
        )}
      </AnimatePresence>

    </div>
  )
}

const PropertySelect = ({entity, onChange}: any) => {
  const currentDatasets = useAppSelector(selectedDatasets);
  const propertyQuery = queries.propertyQuery(entity.value);

  const {data, isFetching, isError, error} = useSendSparqlQuery({
    query: propertyQuery, 
    datasets: currentDatasets
  });

  const properties = data?.results.bindings;

  const propertyOptions = properties && 
    properties.filter( (item: any) => item.hasOwnProperty('l')).map( (item: any) => {
      return {value: item.pred.value, label: item.l.value}
    })
    .filter((v: SelectOptions, i: number, a: any[]) => a.findIndex((v2: SelectOptions) => (v2.label === v.label)) === i);

  return (
    <div>
      <label id="property-label" htmlFor="property-input" className={styles.label}>
        Properties for <span className={styles.labelEmphasis}>{entity.label}</span>
      </label>
      <Select 
        aria-labelledby="property-label"
        inputId="property-input"
        className={styles.select}
        options={propertyOptions} 
        placeholder={`${entity.label} must have a...`}
        isMulti
        noOptionsMessage={() => isFetching ? 
          <Spinner
            as="span"
            animation="border"
            size="sm"
            role="status"
            aria-hidden="true"
          /> : 
          'No properties found'
        }
        onChange={(e: any) => onChange(entity.label, e)}
        theme={theme} />
    </div>
  );
}