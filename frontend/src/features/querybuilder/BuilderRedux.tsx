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
import Form from 'react-bootstrap/Form';

interface SparqlObject {
  // as returned by a sparql db
  type: string;
  value: string;
}

interface EntityData {
  c: SparqlObject; // uri to get properties of in next step
  l?: SparqlObject; // label
  p?: SparqlObject; // parent, we don't do anything with this yet
}

interface PropertyData {
  l?: SparqlObject; // label
  pred: SparqlObject; // uri to use in the sparql query
  tpe: SparqlObject; // type of property
  dt?: SparqlObject; // type of data
  ot?: SparqlObject; // entity the property belongs to, we use this to delve deeper
}

export type Entity = {
  label: string; // appears in the dropdown
  value: string; // this is the uri (value from c)
}

export type Property = {
  label: string; // appears in the dropdown
  value: string; // this is the uri
  ot: string; // value of ot
  otLabel: string; // label derived from value of ot
  dataType: string; // derived from optional dt
  uuid: string; // unique id/key for use in array map
  labelForQuery: string; // value that gets passed as a label to the sparql query
}

export const Builder = () => {
  const dispatch = useAppDispatch();

  const [selectedEntity, setSelectedEntity] = useState<Entity>({label: '', value: ''});
  const [selectedProperties, setSelectedProperties] = useState<Property[][]>([]);

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

  const setProperties = (data: Property, changedValue: any, level: any, parentLevel?: any) => {
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
      console.log(`level: ${level}`)
      console.log(`parentlevel: ${parentLevel}`)
      // set children
      const uuid = uuidv4();
      const newProperty = [...selectedProperties[parentLevel].slice(0, level), ...[{...data, uuid: uuid}]];
      const newState = update(selectedProperties, {[parentLevel]: {$set: newProperty}});    
      setSelectedProperties(newState);
    }
  }

  const addFilter = (data: string, level: any, parentLevel: any) => {

  }

  console.log(selectedProperties)

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
          propertyArray.map((property: Property, j: number) => [
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
                value={selectedProperties[i][j+1]} />,

            property.dataType.length > 0 && 
              property.dataType === 'string' && 
                <Form key={`stringFilter-${property.uuid}`}>
                  <Form.Group controlId={`stringFilter-${property.uuid}`}>
                    <Form.Label>{property.label} must contain</Form.Label>
                    <Form.Control type="text" placeholder="TODO" />
                  </Form.Group>
                </Form>
            ]
          )
        )}

    </>
  )
}

interface EntitySelectorProps {
  onChange: (data: Entity) => void;
}

const EntitySelector = ({onChange}: EntitySelectorProps) => {
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

interface PropertySelectProps {
  parentUri: string;
  parentLabel: string;
  labelForQuery?: string;
  onChange: (data: any, changedValue: any, level: any, parentLevel?: any) => void;
  multiSelect: boolean;
  level: number;
  parentLevel?: number;
  value: Property[] | Property;
}

const PropertySelect = ({parentUri, parentLabel, labelForQuery, onChange, multiSelect, level, parentLevel, value}: PropertySelectProps) => {
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
      const dataType = item.hasOwnProperty('dt') && 
        // todo: define some more data types
        (item.dt?.value === 'http://www.w3.org/2001/XMLSchema#string' ? 'string' : false);
      return {
        label: `${label}${otLabel ? `: ${otLabel}` : ''}`,
        value: item.pred.value,
        ot: item.hasOwnProperty('ot') && item.ot?.value,
        otLabel: item.hasOwnProperty('ot') && queries.getLabel(item, 'ot'),
        dataType: dataType, 
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

// theme for the selection boxes
const theme = (theme: Theme) => ({
  ...theme,
  borderRadius: 0,
  colors: {
    ...theme.colors,
    primary25: '#efc501',
    primary: 'black',
  }
});

interface SelectDropdownProps {
  label: string;
  placeholder: string;
  isFetching: boolean;
  isError: boolean;
  multiSelect: boolean;
  resultsOptions: any;
  onChange: (data: any, changedValue?: any, level?: number, parentLevel?: number) => void;
  level?: number;
  parentLevel?: number;
  value?: Property[] | Property;
}

const SelectDropdown = ({label, placeholder, isFetching, isError, multiSelect, resultsOptions, onChange, level, parentLevel, value}: SelectDropdownProps) => {
  return (
    <div style={{paddingLeft: `${level ? level * 1.5 : 0}rem`}}>
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
        onChange={(e: any, changedValue: any) => onChange(e, changedValue, level, parentLevel)}
        theme={theme} />
    </div>
  )
}