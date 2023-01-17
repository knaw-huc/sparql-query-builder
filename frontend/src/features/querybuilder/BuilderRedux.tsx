import React, {useState, useEffect} from 'react';
import {useAppSelector, useAppDispatch} from '../../app/hooks';
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

// TODO
// nicer typescript
// case insensitive text filter
// css/usability

interface SparqlObject {
  // as returned by a sparql db
  type: string;
  value: string;
}

interface EntityData {
  c: SparqlObject; // uri to get properties of in next step
  l?: SparqlObject; // label
  p?: SparqlObject; // parent, we don't do anything with this yet
  pred?: never;
  tpe?: never;
  dt?: never;
  ot?: never;
}

interface PropertyData {
  l?: SparqlObject; // label
  pred: SparqlObject; // uri to use in the sparql query
  tpe: SparqlObject; // type of property
  dt?: SparqlObject; // type of data
  ot?: SparqlObject; // entity the property belongs to, we use this to delve deeper
  c?: never;
  p?: never;
}

export type Entity = {
  label: string; // appears in the dropdown
  value: string; // this is the uri (value from c)
}

type FilterType = 'stringFilter';

export type Property = {
  label: string; // appears in the dropdown
  value: string; // this is the uri, or filter
  ot?: string; // value of ot
  otLabel?: string; // label derived from value of ot
  dataType?: string; // derived from optional dt
  uuid?: string; // unique id/key for use in array map
  labelForQuery?: string; // value that gets passed as a label to the sparql query
  filterType?: FilterType; // type of filter applied (only one for now)
}

interface OnChangeData {
  (data: any, changedValue?: any, level?: number, parentLevel?: number): void;
}

interface OnChangeFilter {
  (e: any, level: number, parentLevel: number, filterType: FilterType): void;
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

  const setProperties = (data: Property, changedValue: any, level?: number, parentLevel?: number) => {
    // Properties trees are arrays within the property array: [ [{propertyObject}, {propertyObject}], [{propertyObject}] ]
    // Keep track of these arrays using the parentLevel (index # of parent array) and level (index # of object being selected)
    if (level !== undefined && level === 0) {
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
    if (level !== undefined && level > 0) {
      // set children
      const uuid = uuidv4();
      const newProperty = [...selectedProperties[parentLevel as number].slice(0, level as number), ...[{...data, uuid: uuid}]];
      const newState = update(selectedProperties, {[parentLevel as number]: {$set: newProperty}});    
      setSelectedProperties(newState);
    }
  }

  const setFilter = (e: any, level: number, parentLevel: number, filterType: FilterType) => {
    const newProperty = e.target.value ? 
      [...selectedProperties[parentLevel].slice(0, level), ...[{
        label: '',
        value: e.target.value,
        filterType: filterType,
      }]] 
      :
      [...selectedProperties[parentLevel].slice(0, level)];
    const newState = update(selectedProperties, {[parentLevel]: {$set: newProperty}});    
    setSelectedProperties(newState);
  }

  console.log(selectedProperties)

  return (
    <>
      <h5 className={styles.header}>Build your query</h5>

      <Selector 
        onChange={setEntity}
        type="entity"
        multiSelect={false} />

      {selectedEntity.value.length > 0 &&
        <Selector
          type="property"
          parentUri={selectedEntity.value} 
          parentLabel={selectedEntity.label}
          onChange={setProperties} 
          multiSelect={true}
          level={0}
          value={selectedProperties.map( (property: Property[]) => property[0] )} />
      }
      {selectedProperties.map((propertyArray: Property[], i: number) =>
        propertyArray.map((property: Property, j: number) => [
          property.ot && 
            <Selector
              type="property" 
              key={property.uuid}
              parentUri={property.ot} 
              parentLabel={property.label}
              labelForQuery={property.labelForQuery}
              onChange={setProperties} 
              multiSelect={false}
              parentLevel={i}
              level={j+1}
              value={selectedProperties[i][j+1]} />,

          property.dataType && property.dataType === 'string' && 
              <Input 
                key={`stringFilter-${property.uuid}`}
                level={j+1}
                parentLevel={i}
                onChange={setFilter}
                label={`${property.label} must contain`} 
                placeholder="Enter optional text to filter on..."
                filterType="stringFilter"
                value={selectedProperties[i][j+1]?.value} />
          ]
        )
      )}
    </>
  )
}

interface SelectorProps {
  onChange: OnChangeData;
  type: 'entity' | 'property';
  parentUri?: string;
  parentLabel?: string;
  labelForQuery?: string;
  multiSelect: boolean;
  level?: number;
  parentLevel?: number;
  value?: Property[] | Property;
}

const Selector = ({onChange, type, parentUri, parentLabel, labelForQuery, multiSelect, level, parentLevel, value}: SelectorProps) => {
  const currentDatasets = useAppSelector(selectedDatasets);

  const {data, isFetching, isError} = useSendSparqlQuery({
    query: type === 'entity' ? queries.entityQuery : queries.propertyQuery(parentUri as string), 
    datasets: currentDatasets
  });

  const results = data?.results.bindings;

  // Reformat results
  const resultsOptions = results && 
    results.map((item: EntityData | PropertyData) => {
      if (type === 'entity') {
        return {
          label: queries.getLabel(item),
          value: item.c!.value,
        }
      }
      else {
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
          value: item.pred!.value,
          ot: item.hasOwnProperty('ot') && item.ot?.value,
          otLabel: item.hasOwnProperty('ot') && queries.getLabel(item, 'ot'),
          dataType: dataType, 
          labelForQuery: newLabelForQuery,
        }
      }
    }).sort((a: Entity | Property, b: Entity | Property) => {
      const la = a.label.toLowerCase(),
            lb = b.label.toLowerCase();
      return la < lb ? -1 : (la > lb ? 1 : 0)
    });

  return (
    <SelectDropdown 
      label={type === 'entity' ? 'Pick an entity you wish to explore' : `Select properties for ${parentLabel}`}
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

interface CustomOptionProps extends OptionProps {
  data: any;
}

const CustomOption = (props: CustomOptionProps) => {
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
  resultsOptions: Entity[] | Property[];
  onChange: OnChangeData;
  level?: number;
  parentLevel?: number;
  value?: Property[] | Property;
}

const SelectDropdown = ({label, placeholder, isFetching, isError, multiSelect, resultsOptions, onChange, level, parentLevel, value}: SelectDropdownProps) => {
  console.log(level)
  return (
    <div style={{paddingLeft: `${level ? level * 1.5 : 0}rem`}} className={level !== undefined ? styles.level : ''}>
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

interface InputProps {
  label: string;
  level: number;
  parentLevel: number;
  onChange: OnChangeFilter;
  placeholder: string;
  filterType: FilterType;
  value: string;
}

const Input = ({label, level, parentLevel, onChange, placeholder, filterType, value}: InputProps) => {
  return (
    <div style={{paddingLeft: `${level ? level * 1.5 : 0}rem`}} className={level !== undefined ? styles.level : ''}>
      <label 
        htmlFor="" 
        className={styles.label}>
        {label}
      </label>
      <input 
        type="text" 
        className={styles.textInput} 
        placeholder={placeholder}
        value={value || ''}
        onChange={(e: any) => onChange(e, level, parentLevel, filterType)}/>
    </div>
  )
}