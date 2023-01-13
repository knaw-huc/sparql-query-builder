import React, {useState, useEffect} from 'react';
import {useAppSelector, useAppDispatch} from '../../app/hooks';
import {AnimatePresence} from 'framer-motion';
import {v4 as uuidv4} from 'uuid';
import update from 'immutability-helper';
import {setActiveQuery} from './queryBuilderSlice';
import Select, {components, OptionProps} from 'react-select';
import type {PropsValue, Theme} from 'react-select';
import styles from './QueryBuilder.module.scss';
import * as queries from './helpers/queries';
import {useSendSparqlQuery} from '../sparql/sparqlApi';
import {selectedDatasets} from '../datasets/datasetsSlice';
import Spinner from 'react-bootstrap/Spinner';
import {FadeDiv} from '../animations/Animations';

// Ga naar entity selector en een property selector. Property selector heeft eigen children met nieuwe property selectors.

interface SparqlObject {
  type: string;
  value: string;
}

// l = label
// c = uri to get properties of in next step
// p = parent, we don't do anything with this yet
interface EntityData {
  c: SparqlObject;
  l?: SparqlObject;
  p?: SparqlObject;
}

// l = label
// pred = uri to use in the sparql query
// tpe = data type
// dt = 
// ot = entity the property belongs to, we use this to delve deeper
interface PropertyData {
  l?: SparqlObject;
  pred: SparqlObject;
  tpe: SparqlObject;
  dt?: SparqlObject;
  ot?: SparqlObject;
}

export type Entity = {
  label: string;
  value: string; // this is the uri
}

export type Property = {
  label: string;
  value: string; // this is the uri
  ot: string;
  otLabel: string;
  uuid: string;
  labelForQuery: string;
}

const theme = (theme: Theme) => ({
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

  const [selectedEntity, setSelectedEntity] = useState<Entity>({label: '', value: ''});
  const [selectedProperties, setSelectedProperties] = useState<any>([]);

  // Set query in code editor when one of these values changes
  useEffect(() => {
    const theQuery = queries.resultQuery(selectedEntity, selectedProperties);
    selectedEntity.value && dispatch(setActiveQuery(theQuery));
  }, [selectedEntity, selectedProperties, dispatch]);
  
  // Keep track of selections and set tree accordingly
  const setEntity = (data: Entity) => {
    setSelectedEntity(data);
    // reset properties
    setSelectedProperties([]);
  }

  const setProperties = (data: Property, level: number, parentLevel: number, changedValue: any) => {
    // Properties trees are arrays within the property array: [ [{propertyObject}, {propertyObject}], [{propertyObject}] ]
    // Keep track of these arrays using the parentLevel (index # of parent array) and level (index # of object being selected)
    if (level === 0) {
      if (changedValue.action === 'select-option') {
        // add value to state
        const uuid = uuidv4();
        setSelectedProperties([...selectedProperties, [{...changedValue.option, uuid: uuid}]])
      }
      if (changedValue.action === 'remove-value') {
        // remove value from state
        const removeIndex = selectedProperties.findIndex( 
          (oldProp: Property[]) => changedValue.removedValue.label === oldProp[0].label
        );
        const newState = update(selectedProperties, {$splice: [[removeIndex, 1]]});
        setSelectedProperties(newState);
      }
      if (changedValue.action === 'clear') {
        setSelectedProperties([]);
      }
    }
    if (level > 0) {
      // set children
      const uuid = uuidv4();
      const newProperty = [...selectedProperties[parentLevel].slice(0, level), ...[{...data, uuid: uuid}]];
      const newState = update(selectedProperties, {[parentLevel]: {$set: newProperty}});    
      setSelectedProperties(newState);
    }
  }

  return (
    <>
      <h5 className={styles.header}>Build your query</h5>

      <EntitySelector onChange={setEntity}  />

        {selectedEntity.value.length > 0 &&
          <PropertySelect 
            parentUri={selectedEntity.value} 
            parentLabel={selectedEntity.label}
            onChange={setProperties} 
            multiSelect={true}
            level={0}
            value={selectedProperties.map( (property: Property[]) => property[0] )} />
        }
        {selectedProperties.map((propertyArray: Property[], i: number) =>
          propertyArray.map((property: Property, j: number) =>
            property.ot.length > 0 &&
              <PropertySelect 
                key={property.uuid}
                parentUri={property.ot} 
                parentLabel={property.label}
                labelForQuery={property.labelForQuery}
                onChange={setProperties} 
                multiSelect={false}
                parentLevel={i}
                level={j+1}
                value={selectedProperties[i][j+1]} />
          )
        )}

    </>
  )
}

const EntitySelector = ({onChange}: any) => {
  const currentDatasets = useAppSelector(selectedDatasets);

  const {data, isFetching, isError} = useSendSparqlQuery({
    query: queries.entityQuery, 
    datasets: currentDatasets
  });

  const results = data?.results.bindings;

  console.log('Entity results')
  console.log(results)

  // Reformat results
  const resultsOptions = results && 
    results.map((item: EntityData) => {
      return {
        label: queries.getLabel(item),
        value: item.c.value,
      }
    }).sort((a: Entity, b: Entity) => {
      const la = a.label.toLowerCase(),
            lb = b.label.toLowerCase();
      return la < lb ? -1 : (la > lb ? 1 : 0)
    });

  return (
    <SelectDropdown 
      label="Pick an entity you wish to explore"
      placeholder="Select entity..."
      isFetching={isFetching}
      isError={isError}
      multiSelect={false}
      resultsOptions={resultsOptions}
      onChange={onChange}
    />
  );
}

interface SparqlSelectTypes {
  dataSelector?: Property;
  onChange: (newData: any, parent: any) => void;
  multiSelect: boolean;
}

const PropertySelect = ({parentUri, parentLabel, labelForQuery, onChange, multiSelect, level, parentLevel, value}: any) => {
  const currentDatasets = useAppSelector(selectedDatasets);

  const {data, isFetching, isError} = useSendSparqlQuery({
    query: queries.propertyQuery(parentUri), 
    datasets: currentDatasets
  });

  const results = data?.results.bindings;

  console.log('Property results')
  console.log(results)

  // Reformat results
  const resultsOptions = results && 
    results.map((item: PropertyData) => {
      const otLabel = item.hasOwnProperty('ot') && queries.getLabel(item, 'ot');
      const label = queries.getLabel(item);
      const newLabelForQuery = labelForQuery ? 
        `${labelForQuery}_${label}${otLabel ? `_${otLabel}` : ''}` : 
        `${label}${otLabel ? `_${otLabel}` : ''}`;
      return {
        label: `${label}${otLabel ? `: ${otLabel}` : ''}`,
        value: item.pred.value,
        ot: item.hasOwnProperty('ot') && item.ot?.value,
        otLabel: item.hasOwnProperty('ot') && queries.getLabel(item, 'ot'),
        labelForQuery: newLabelForQuery,
      }
    }).sort((a: Property, b: Property) => {
      const la = a.label.toLowerCase(),
            lb = b.label.toLowerCase();
      return la < lb ? -1 : (la > lb ? 1 : 0)
    });;


  return (
    <SelectDropdown 
      label={`Select properties for ${parentLabel}`}
      placeholder="Select..."
      isFetching={isFetching}
      isError={isError}
      multiSelect={multiSelect}
      resultsOptions={resultsOptions}
      onChange={onChange}
      level={level}
      parentLevel={parentLevel}
      value={value}
    />
  );
}

const CustomOption = (props: any) => {
  return (
    <components.Option {...props}>
      {props.data.label} 
      <span className={styles.schema}>
        {props.data.value} 
      </span>
    </components.Option>
  )
}

const SelectDropdown = ({label, placeholder, isFetching, isError, multiSelect, resultsOptions, onChange, level, parentLevel, value}: any) => {
  return (
    <div style={{paddingLeft: `${level * 1.5}rem`}}>
      <label className={styles.label}>
        {label}
      </label>
      <Select 
        components={{ Option: CustomOption }}
        className={styles.select}
        options={resultsOptions} 
        placeholder={placeholder}
        isMulti={multiSelect}
        value={value}
        noOptionsMessage={() => isFetching ? 
          <Spinner
            as="span"
            animation="border"
            size="sm"
            role="status"
            aria-hidden="true"
          /> : 
          ( isError ? 'Something\'s gone wrong with fetching the data' : 'Nothing found')
        }
        onChange={(e: any, changedValue: any) => onChange(e, level, parentLevel, changedValue)}
        theme={theme} />
    </div>
  )
}