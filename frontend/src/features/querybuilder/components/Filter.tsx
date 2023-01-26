import React, {useState, useEffect} from 'react';
import styles from './Filter.module.scss';

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

export type FilterState = {
  value: string;
  select: string;
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
  const [currentFilter, setCurrentFilter] = useState<FilterState>({value: '', select: '='});

  useEffect(() => {
    onChange(currentFilter, level, propertyArrayIndex, dataType)
  }, [onChange, currentFilter, level, propertyArrayIndex, dataType]);

  return (
    <div style={{paddingLeft: `${level ? level * 2 - 2: 0}rem`}}>
      <label className={styles.label}><strong>{label}</strong> {typeMap[dataType].label}</label>
      <div className={styles.inputWrapper}>
        {['gYear', 'gYearMonth', 'date', 'integer'].includes(dataType) &&
          <select 
            className={styles.selectFilter} 
            onChange={e => setCurrentFilter({value: currentFilter.value, select: e.target.value})}
            value={currentFilter.select}>
            <option value="<">{dataType === 'integer' ? 'Less than' : 'Before'}</option>
            <option value="=">Exactly</option>
            <option value=">">{dataType === 'integer' ? 'More than' : 'After'}</option>
          </select>
        }
        <input 
          type={typeMap[dataType].input} 
          className={styles.textInput} 
          placeholder={typeMap[dataType].placeholder}
          value={value || ''}
          onChange={e => setCurrentFilter({value: e.target.value, select: currentFilter.select})}/>
      </div>
    </div>
  )
}