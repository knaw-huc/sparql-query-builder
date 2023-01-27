import React, {useState, useEffect, useCallback} from 'react';
import {useAppDispatch} from '../../../app/hooks';
import update from 'immutability-helper';
import {setActiveQuery} from '../queryBuilderSlice';
import styles from './Builder.module.scss';
import * as queries from '../helpers/queries';
import Selector from './Selector';
import {Filter, typeMap} from './Filter';
import type {FilterState, FilterDataType} from './Filter';

// TODO: some more filters? Not only text, also data/year etc
// split this file into chunks

export type Entity = {
  label: string; // appears in the dropdown
  value: string; // this is the uri (value from c)
  uuid: string;
}

export type Property = {
  label: string; // appears in the dropdown
  value: string; // this is the uri, or filter
  uuid: string; // unique id/key for use in array map
  additionalFilter?: string; // for date/number filtering 
  ot?: string; // value of ot
  propertyType?: string;
  dataType?: string; // derived from optional dt
  labelForQuery?: string; // value that gets passed as a label to the sparql query
}

// Type appliccable changing selectbox
export type ActionTypes = {
  action: 'clear' | 'create-option' | 'deselect-option' | 'pop-value' | 'remove-value' | 'select-option' | 'set-value';
  option?: Property | Entity;
  removedValue?: Property | Entity;
  removedValues: Property[] | Entity[];
}

const defaultSelectionObject = {label: '', value: '', uuid: ''};

export const Builder = () => {
  const dispatch = useAppDispatch();

  const [selectedEntity, setSelectedEntity] = useState<Entity>(defaultSelectionObject);
  const [selectedProperties, setSelectedProperties] = useState<Property[][]>([]);
  const [selectedLimit, setSelectedLimit] = useState<number>(1000);

  // Set query in code editor when one of these values changes
  useEffect(() => {
    const theQuery = queries.resultQuery(selectedEntity, selectedProperties, selectedLimit);
    dispatch(setActiveQuery(selectedEntity.value ? theQuery : ''));
  }, [selectedEntity, selectedProperties, dispatch, selectedLimit]);

  // Keep track of selections and set tree accordingly
  const setEntity = useCallback((data: Entity) => {
    setSelectedEntity(data ? data : defaultSelectionObject);
    // reset properties
    setSelectedProperties([]);
  }, []);

  const setProperties = useCallback((data: Property, changedValue: ActionTypes, level?: number, propertyArrayIndex?: number) => {
    // Properties trees are arrays within the property array: [ [{propertyObject}, {propertyObject}], [{propertyObject}] ]
    // Keep track of these arrays using the propertyArrayIndex (index # of parent array) and level (index # of object being selected)
    switch(changedValue.action) {
      case 'select-option':
        // add new value to state, or change existing value
        if (propertyArrayIndex === undefined) {
          // first property, so new property tree
          setSelectedProperties(oldProperties => [...oldProperties, [changedValue.option as Property]]);
        }
        else {
          // add or change existing property tree
          setSelectedProperties(oldProperties => 
            update(oldProperties, {[propertyArrayIndex]: {$set: [...oldProperties[propertyArrayIndex].slice(0, level), data]}})
          );
        }
        break;

      case 'clear':
        // clear: pressing the X in selectbox
        if (propertyArrayIndex === undefined) {
          // reset all if entity properties are cleared
          setSelectedProperties([]);
        }
        else {
          // single selection for sub-properties, just remove object from array tree
          setSelectedProperties(oldProperties => 
            update(oldProperties, {[propertyArrayIndex]: {$set: [...oldProperties[propertyArrayIndex].slice(0, level)]}})
          );
        }
        break;

      case 'remove-value':
        // Only for multiselect, remove individual values
        setSelectedProperties(oldProperties => 
          update(oldProperties, {$splice: [[oldProperties.findIndex(oldPropArr => 
            oldPropArr.some(oldProp => changedValue.removedValue!.uuid === oldProp.uuid)), 1]]}
          )
        );
        break;
    }
  }, []);

  const setFilter = useCallback((filter: FilterState, level: number, propertyArrayIndex: number, dataType: FilterDataType) => {    
    setSelectedProperties((oldProperties) => {
      const newProperty = filter.value !== '' ? 
        [...oldProperties[propertyArrayIndex].slice(0, level), ...[{
          label: '',
          value: filter.value,
          dataType: dataType + 'Filter',
          uuid: '',
          additionalFilter: filter.select!.value,
        }]] 
        :
        [...oldProperties[propertyArrayIndex].slice(0, level)];
      const newState = update(oldProperties, {[propertyArrayIndex]: {$set: newProperty}});
      return newState;
    });
  }, []);

  return (
    <div className={styles.builder}>
      <h5 className={styles.header}>Build your query</h5>

      <Selector 
        onChange={setEntity}
        type="entity"
        multiSelect={false} />

      {selectedEntity.value.length > 0 &&
        <Selector
          key={selectedEntity.value}
          type="property"
          parentUri={selectedEntity.value} 
          parentLabel={selectedEntity.label}
          onChange={setProperties} 
          multiSelect={true}
          level={0}
          value={selectedProperties.map( (property) => property[0] )} />
      }
      {selectedProperties.map((propertyArray, i) =>
        ((propertyArray[0].dataType !== undefined && typeMap[propertyArray[0].dataType]) || propertyArray[0].ot) &&
        // No need to show anything if there's no OT or filterable datatype
        <div 
          key={`group-${propertyArray[0].uuid}`}
          className={styles.propertyGroup}>
          {propertyArray.map((property, j) => [
            property.ot && 
              <Selector
                type="property" 
                key={property.uuid}
                parentUri={property.ot} 
                parentLabel={property.label}
                labelForQuery={property.labelForQuery}
                onChange={setProperties} 
                multiSelect={false}
                propertyArrayIndex={i}
                level={j+1}
                value={selectedProperties[i][j+1]} />,

            property.dataType && typeMap[property.dataType] &&
              <Filter 
                key={`filter-${property.uuid}`}
                level={j+1}
                label={property.label}
                propertyArrayIndex={i}
                onChange={setFilter}
                dataType={property.dataType}
                value={selectedProperties[i][j+1]?.value} />
            ]
          )}
        </div>
      )}
      <div className={styles.limit}>
        <label className={styles.label}>Limit results to</label>
        <input 
          className={styles.limitInput} 
          type="number" value={selectedLimit} 
          onChange={e => setSelectedLimit(parseInt(e.target.value) || 1000)}
        />
      </div>
    </div>
  )
}