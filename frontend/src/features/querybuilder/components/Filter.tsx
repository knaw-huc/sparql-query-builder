import React, {useState, useEffect} from 'react';
import Select from 'react-select';
import type {SingleValue} from 'react-select';
import styles from './Filter.module.scss';
import {selectorTheme} from '../helpers/themes';

export type FilterDataType = string; // possibly narrow this down later on, depending on the data types we might get

interface OnChangeFilter {
  (filter: FilterState, level: number, propertyArrayIndex: number, dataType: FilterDataType): void;
}

type DataTypeProps = {
  level: number;
  propertyArrayIndex: number;
  onChange: OnChangeFilter;
  dataType: FilterDataType;
  value: string;
  label: string;
}

type SelectOption = {
  value: string;
  label: string;
}

export type FilterState = {
  value: string;
  select: SingleValue<SelectOption>;
}

export const typeMap: {[key: string]: {input: string; label: string; placeholder: string}} = {
    string: {
      input: 'text',
      label: 'must contain this text',
      placeholder: 'Enter optional text to filter on...',
    },
    date: {
      input: 'date',
      label: 'must have this date',
      placeholder: 'yyyy-mm-dd',
    },
    integer: {
      input: 'number',
      label: 'must have this number',
      placeholder: 'Set number...',
    },
    gYear: {
      input: 'text',
      label: 'must be in this year',
      placeholder: 'yyyy',
    },
    gYearMonth: {
      input: 'text',
      label: 'must be in this year and month',
      placeholder: 'yyyy-mm',
    },
}

export const Filter = ({level, propertyArrayIndex, onChange, dataType, value, label}: DataTypeProps) => {
  const options = [
    {value: '<', label: ['gYear', 'gYearMonth', 'date'].includes(dataType) ? 'Earlier than' : 'Smaller than'},
    {value: '=', label: 'Exactly'},
    {value: '>', label: ['gYear', 'gYearMonth', 'date'].includes(dataType) ? 'Later than' : 'Larger than'},
  ];
  const [currentFilter, setCurrentFilter] = useState<FilterState>({value: '', select: options[1]});

  useEffect(() => {
    onChange(currentFilter, level, propertyArrayIndex, dataType)
  }, [onChange, currentFilter, level, propertyArrayIndex, dataType]);

  return (
    <div style={{paddingLeft: `${level ? level * 2 - 2: 0}rem`}}>
      <label className={styles.label}><strong>{label}</strong> {typeMap[dataType].label}</label>
      <div className={styles.inputWrapper}>
        {['gYear', 'gYearMonth', 'date', 'integer'].includes(dataType) &&
          <Select 
            className={styles.select}
            options={options}
            value={currentFilter.select}
            onChange={data => setCurrentFilter({...currentFilter, select: data})}
            theme={selectorTheme} />
        }
        <input 
          type={typeMap[dataType].input} 
          className={styles.textInput} 
          placeholder={typeMap[dataType].placeholder}
          value={currentFilter.value}
          onChange={e => setCurrentFilter({...currentFilter, value: e.target.value})}/>
      </div>
    </div>
  )
}