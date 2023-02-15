import React, {useState, useEffect} from 'react';
import Select from 'react-select';
import type {SingleValue} from 'react-select';
import update from 'immutability-helper';
import styles from './Filter.module.scss';
import {selectorTheme} from '../helpers/themes';
import {useTranslation} from 'react-i18next';
import {useAppDispatch, useAppSelector} from '../../../app/hooks';
import {setSelectedProperties, selectSelectedProperties} from '../queryBuilderSlice';
import type {Property} from './Builder';

export type FilterDataType = string; // possibly narrow this down later on, depending on the data types we might get

type DataTypeProps = {
  level: number;
  propertyArrayIndex: number;
  selector: Property;
}

type SelectOption = {
  value: string;
  label: string;
}

export type FilterState = {
  value: string;
  select: SingleValue<SelectOption>;
}

export const typeMap: {[key: string]: string;} = {
  string: 'text',
  date: 'date',
  integer: 'number',
  gYear: 'number',
  gYearMonth: 'month',
  datetime: 'datetime-local',
}

export const Filter = ({level, propertyArrayIndex, selector}: DataTypeProps) => {
  const dispatch = useAppDispatch();
  const selectedProperties = useAppSelector(selectSelectedProperties);
  const {t} = useTranslation(['querybuilder']);
  const dataType = selector.dataType as string;
  const options = [
    {value: '<', label: ['gYear', 'gYearMonth', 'date'].includes(dataType) ? t('filter.labelEarlier') : t('filter.labelSmaller')},
    {value: '=', label: t('filter.labelExactly')},
    {value: '>', label: ['gYear', 'gYearMonth', 'date'].includes(dataType) ? t('filter.labelLater') : t('filter.labelLarger')},
  ];
  const [currentFilter, setCurrentFilter] = useState<FilterState>({value: '', select: options[1]});

  const setFilter = () => { 
    const newProperty = currentFilter.value !== '' ? 
      [...selectedProperties[propertyArrayIndex].slice(0, level), ...[{
        label: '',
        value: currentFilter.value,
        dataType: dataType + 'Filter',
        uuid: '',
        equalityOperator: currentFilter.select!.value,
      }]] 
      :
      [...selectedProperties[propertyArrayIndex].slice(0, level)];
    const newState = update(selectedProperties, {[propertyArrayIndex]: {$set: newProperty}});   
    dispatch(setSelectedProperties(newState));
  };

  useEffect(() => {
    setFilter()
  }, [currentFilter]);

  return (
    <div style={{paddingLeft: `${level ? level * 2 - 2: 0}rem`}}>
      <label className={styles.label}><strong>{selector.label}</strong> {t(`filter.typeMap.${dataType}.label`)}</label>
      <div className={styles.inputWrapper}>
        {['gYear', 'gYearMonth', 'date', 'integer'].includes(dataType) &&
          <Select 
            className={styles.selectFilter}
            options={options}
            value={currentFilter.select}
            onChange={data => setCurrentFilter({...currentFilter, select: data})}
            theme={selectorTheme} />
        }
        <input 
          type={typeMap[dataType]} 
          className={styles.textInput} 
          placeholder={t(`filter.typeMap.${dataType}.placeholder`) as string}
          value={currentFilter.value}
          onChange={e => setCurrentFilter({...currentFilter, value: e.target.value})}/>
      </div>
    </div>
  )
}