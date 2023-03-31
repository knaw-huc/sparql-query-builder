import React from 'react';
import {v4 as uuidv4} from 'uuid';
import Select, {components} from 'react-select';
import {useAppDispatch, useAppSelector} from '../../../app/hooks';
import * as queries from '../helpers/queries';
import {useSendSparqlQuery} from '../../sparql/sparqlApi';
import {selectSelectedDatasets} from '../../datasets/datasetsSlice';
import update from 'immutability-helper';
import {setSelectedEntity, setSelectedProperties, selectSelectedEntity, selectSelectedProperties} from '../queryBuilderSlice';
import Spinner from 'react-bootstrap/Spinner';
import styles from './Selector.module.scss';
import {selectorTheme} from '../helpers/themes';
import {useTranslation, Trans} from 'react-i18next';
import type {
  SelectorProps, 
  PropertyData, 
  Property, 
  EntityData, 
  Entity, 
  ActionTypes, 
  NoOptions, 
  CustomOptionProps
} from '../../../types/queryBuilder';
import {FadeDiv} from '../../animations/Animations';

export const PropertySelector = ({multiSelect, selector, level, propertyArrayIndex}: SelectorProps) => {
  const dispatch = useAppDispatch();
  const selectedProperties = useAppSelector(selectSelectedProperties);
  const currentDatasets = useAppSelector(selectSelectedDatasets);
  const {data, isFetching, isError} = useSendSparqlQuery({
    query: queries.propertyQuery((selector.ot || selector.value) as string), 
    datasets: currentDatasets
  });
  const results = data?.results.bindings;
  const {t} = useTranslation(['querybuilder']);

  // Reformat results
  const resultsOptions = results && 
    results.map((item: PropertyData) => {
      const uuid = uuidv4();
      const otLabel = item.ot && queries.getLabel(item.ot!.value);
      const label = item.l ? item.l.value : queries.getLabel(item.pred!.value);
      const newLabelForQuery = selector.labelForQuery ? 
        `${selector.labelForQuery}_${label}${otLabel ? `_${otLabel}` : ''}` : 
        `${label}${otLabel ? `_${otLabel}` : ''}`;
      return {
        label: `${label}${otLabel ? `: ${otLabel}` : ''}`,
        value: item.pred!.value,
        ot: item.ot && item.ot?.value,
        propertyType: queries.getLabel(item.tpe!.value),
        dataType: item.dt && queries.getLabel(item.dt!.value), 
        labelForQuery: newLabelForQuery,
        uuid: uuid,
      }
    }).sort((a: Property, b: Property) => 
      a.label.toLowerCase() < b.label.toLowerCase() ? -1 : (a.label.toLowerCase() > b.label.toLowerCase() ? 1 : 0)
    );

  const setProperties = (data: Property, changedValue: ActionTypes, level?: number, propertyArrayIndex?: number) => {
    // Properties trees are arrays within the property array: [ [{propertyObject}, {propertyObject}], [{propertyObject}] ]
    // Keep track of these arrays using the propertyArrayIndex (index # of parent array) and level (index # of object being selected)
    switch(changedValue.action) {
      case 'select-option':
        // add new value to state, or change existing value
        if (propertyArrayIndex === undefined) {
          // first property, so new property tree
          dispatch(setSelectedProperties([...selectedProperties, [changedValue.option as Property]]));
        }
        else {
          // add or change existing property tree
          dispatch(setSelectedProperties( 
            update(selectedProperties, {[propertyArrayIndex]: {$set: [...selectedProperties[propertyArrayIndex].slice(0, level), data]}})
          ));
        }
        break;

      case 'clear':
        // clear: pressing the X in selectbox
        if (propertyArrayIndex === undefined) {
          // reset all if entity properties are cleared
          dispatch(setSelectedProperties([]));
        }
        else {
          // single selection for sub-properties, just remove object from array tree
          dispatch(setSelectedProperties(
            update(selectedProperties, {[propertyArrayIndex]: {$set: [...selectedProperties[propertyArrayIndex].slice(0, level)]}})
          ));
        }
        break;

      case 'remove-value':
        // Only for multiselect, remove individual values
        dispatch(setSelectedProperties(
          update(selectedProperties, {$splice: [[selectedProperties.findIndex(selectedPropArr => 
            selectedPropArr.some(selectedProp => changedValue.removedValue!.uuid === selectedProp.uuid)), 1]]}
          )
        ));
        break;
    }
  }

  return (
    <FadeDiv
      layout
      style={{paddingLeft: `${level !== undefined ? level * 2 - 2 : 0}rem`}} 
      className={level !== undefined ? styles.level : ''}>
      <label className={styles.label}>
        <Trans
          i18nKey="selector.propertyLabel"
          ns="querybuilder"
          values={{parentLabel: selector.label}}
          components={{bold: <strong />}}
        />
      </label>
      <Select 
        components={{ Option: CustomOption }}
        isOptionSelected={(option, selectValue) =>
          selectValue.some(v => 
            (v as Property).label === (option as Property).label && 
            (v as Property).value === (option as Property).value
          )
        }
        className={styles.select}
        options={resultsOptions} 
        placeholder={t('selector.placeholder')}
        isMulti={multiSelect}
        value={
          propertyArrayIndex === undefined ? 
          selectedProperties.map(property => property[0]) :
          selectedProperties[propertyArrayIndex] ?
          selectedProperties[propertyArrayIndex][level] :
          ''
        }
        isClearable={true}
        noOptionsMessage={() => <NoOptionsMessage isFetching={isFetching} isError={isError} />}
        onChange={(data, changedValue) => setProperties(data as Property, changedValue as ActionTypes, level, propertyArrayIndex)}
        theme={selectorTheme} />
    </FadeDiv>
  );
}

export const defaultSelectionObject = {label: '', value: '', uuid: ''};

export const EntitySelector = () => {
  const dispatch = useAppDispatch();
  const selectedEntity = useAppSelector(selectSelectedEntity);
  const currentDatasets = useAppSelector(selectSelectedDatasets);
  const {data, isFetching, isError} = useSendSparqlQuery({
    query: queries.entityQuery, 
    datasets: currentDatasets
  });
  const results = data?.results.bindings;
  const {t} = useTranslation(['querybuilder']);

  const setEntity = (data: Entity) => {
    dispatch(setSelectedEntity(data ? data : defaultSelectionObject));
    // reset properties
    dispatch(setSelectedProperties([]));
  }

  // Reformat results
  const resultsOptions = results && 
    results.map((item: EntityData) => {
      const uuid = uuidv4();
      return {
        label: item.l ? item.l.value : queries.getLabel(item.c!.value),
        value: item.c!.value,
        uuid: uuid,
      }
    }).sort((a: Entity, b: Entity) => 
      a.label.toLowerCase() < b.label.toLowerCase() ? -1 : (a.label.toLowerCase() > b.label.toLowerCase() ? 1 : 0)
    );

  return (
    <div>
      <label className={styles.label}>
        {t('selector.entityLabel')}
      </label>
      <Select 
        components={{ Option: CustomOption }}
        isOptionSelected={(option, selectValue) =>
          selectValue.some(v => 
            (v as Entity).label === (option as Entity).label && 
            (v as Entity).value === (option as Entity).value
          )
        }
        className={styles.select}
        options={resultsOptions} 
        placeholder={isFetching ? t('selector.placeholderLoading') : t('selector.placeholder')}
        value={selectedEntity.label ? selectedEntity : ''}
        isClearable={true}
        noOptionsMessage={() => <NoOptionsMessage isFetching={isFetching} isError={isError} />}
        onChange={data => setEntity(data as Entity)}
        theme={selectorTheme} />
    </div>
  );
}


const NoOptionsMessage = ({isFetching, isError}: NoOptions) => {
  const {t} = useTranslation(['querybuilder']);
  return (
    isFetching ? 
      <Spinner
        as="span"
        animation="border"
        size="sm"
        role="status"
        aria-hidden="true"
      /> : 
    isError ? 
    <span>{t('selector.error')}</span> : 
    <span>{t('selector.noResults')}</span>
  )
}

const CustomOption = (props: CustomOptionProps) => {
  const propertyData = props.data as Property;
  const propertyOrEntityData = props.data as Property | Entity;
  return (
    <components.Option {...props}>
      {propertyOrEntityData.label} 
      {propertyData.propertyType && 
        <span className={`${styles.propertyType} ${propertyData.dataType ? styles[propertyData.dataType] : ''}`}>
          {propertyData.propertyType} 
          {propertyData.dataType ? `: ${propertyData.dataType}` : ''}
        </span>
      }
      <span className={styles.schema}>
        {propertyOrEntityData.value} 
      </span>
    </components.Option>
  )
}
