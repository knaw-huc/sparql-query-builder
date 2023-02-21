import React, {useEffect, useState} from 'react';
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
import {AnimatePresence, motion} from 'framer-motion';
import {FadeDiv} from '../../animations/Animations';

export const Builder = () => {
  const [sync, setSync] = useState(true);
  const dispatch = useAppDispatch();
  const currentQuery = useAppSelector(selectActiveQuery);
  const selectedEntity = useAppSelector(selectSelectedEntity);
  const selectedProperties = useAppSelector(selectSelectedProperties);
  const selectedLimit = useAppSelector(selectSelectedLimit);
  const {t} = useTranslation(['querybuilder']);

  // Set query in code editor when one of these values changes
  useEffect(() => {
    dispatch(setActiveQuery(selectedEntity.value ? queries.resultQuery(selectedEntity, selectedProperties, selectedLimit) : ''));
  }, [selectedEntity, selectedProperties, selectedLimit]);

  // Keep track of sync between QB and Editor. Warn user when editor (currentQuery) has been changed and is out of sync with QB.
  useEffect(() => {
    setSync(currentQuery === queries.resultQuery(selectedEntity, selectedProperties, selectedLimit) || !currentQuery)
  }, [currentQuery]);

  return (
    <motion.div layout="position" layoutRoot className={styles.builder}>
      <h5 className={styles.header}>{t('builder.header')}</h5>
      <AnimatePresence>
        {!sync && <FadeDiv key="sync" layout><p className={styles.warning}>{t('builder.warning')}</p></FadeDiv>}
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
          <FadeDiv layout key={`group-${propertyArray[0].uuid}`} className={styles.propertyGroup}>
            <AnimatePresence>
              {propertyArray.map((property, j) => [
                property.ot && 
                  <PropertySelector
                    key={`property-${property.uuid}`}
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
            </AnimatePresence>
          </FadeDiv>
        )}
        <FadeDiv layout key="limit" className={styles.limit}>
          <label className={styles.label}>{t('builder.limit')}</label>
          <input 
            className={styles.limitInput} 
            type="number" value={selectedLimit} 
            onChange={e => dispatch(setSelectedLimit(parseInt(e.target.value) || 1000))}
          />
        </FadeDiv>
      </AnimatePresence>
    </motion.div>
  )
}