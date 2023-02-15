import React, {useEffect} from 'react';
import {useAppDispatch, useAppSelector} from '../../../app/hooks';
import {
  setActiveQuery, 
  setSelectedLimit,
  selectSelectedEntity,
  selectSelectedProperties,
  selectSelectedLimit,
  selectActiveQuery,
} from '../queryBuilderSlice';
import styles from './Builder.module.scss';
import * as queries from '../helpers/queries';
import {EntitySelector, PropertySelector} from './Selector';
import {Filter, typeMap} from './Filter';
import {useTranslation} from 'react-i18next';

export type Entity = {
  label: string; // appears in the dropdown
  value: string; // this is the uri (value from c)
  uuid: string;
}

export type Property = {
  label: string; // appears in the dropdown
  value: string; // this is the uri or filter
  uuid: string; // unique id/key for use in array map
  ot?: string; // value of ot
  propertyType?: string;
  dataType?: string; // derived from optional dt
  additionalFilter?: string; // for date/number filtering 
  labelForQuery?: string; // value that gets passed as a label to the sparql query
}

export const Builder = () => {
  const dispatch = useAppDispatch();
  const currentQuery = useAppSelector(selectActiveQuery);
  const selectedEntity = useAppSelector(selectSelectedEntity);
  const selectedProperties = useAppSelector(selectSelectedProperties);
  const selectedLimit = useAppSelector(selectSelectedLimit);
  const {t} = useTranslation(['querybuilder']);
  const theQuery = queries.resultQuery(selectedEntity, selectedProperties, selectedLimit);

  // Set query in code editor when one of these values changes
  useEffect(() => {
    dispatch(setActiveQuery(selectedEntity.value ? theQuery : ''));
  }, [selectedEntity, dispatch, theQuery]);

  // Keep track of sync between QB and Editor. Warn user when editor is out of sync with builder.
  const isSynced = theQuery === currentQuery || !currentQuery;

  return (
    <div className={styles.builder}>
      <h5 className={styles.header}>{t('builder.header')}</h5>
      {!isSynced && <p className={styles.warning}>{t('builder.warning')}</p>}
      <EntitySelector />
      {selectedEntity.value.length > 0 &&
        <PropertySelector
          key={selectedEntity.value}
          selector={selectedEntity}
          multiSelect={true}
          level={0} />
      }
      {selectedProperties.map((propertyArray, i) =>
        ((propertyArray[0].dataType && typeMap[propertyArray[0].dataType]) || propertyArray[0].ot) &&
        // Only show this if there's an OT or filterable datatype
        <div 
          key={`group-${propertyArray[0].uuid}`}
          className={styles.propertyGroup}>
          {propertyArray.map((property, j) => [
            property.ot && 
              <PropertySelector
                key={property.uuid}
                selector={property}
                multiSelect={false}
                propertyArrayIndex={i}
                level={j+1} />,
            property.dataType && typeMap[property.dataType] &&
              <Filter 
                key={`filter-${property.uuid}`}
                level={j+1}
                selector={property}
                propertyArrayIndex={i} />
            ]
          )}
        </div>
      )}
      <div className={styles.limit}>
        <label className={styles.label}>{t('builder.limit')}</label>
        <input 
          className={styles.limitInput} 
          type="number" value={selectedLimit} 
          onChange={e => dispatch(setSelectedLimit(parseInt(e.target.value) || 1000))}
        />
      </div>
    </div>
  )
}