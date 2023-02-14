import React, {memo} from 'react';
import {v4 as uuidv4} from 'uuid';
import Select, {components, OptionProps} from 'react-select';
import {useAppSelector} from '../../../app/hooks';
import * as queries from '../helpers/queries';
import {useSendSparqlQuery} from '../../sparql/sparqlApi';
import {selectSelectedDatasets} from '../../datasets/datasetsSlice';
import Spinner from 'react-bootstrap/Spinner';
import styles from './Selector.module.scss';
import type {Property, Entity, ActionTypes} from './Builder';
import {selectorTheme} from '../helpers/themes';
import {useTranslation, Trans} from 'react-i18next';

interface OnChangeData {
  (
    data: Property | Entity,
    changedValue: ActionTypes, 
    level?: number, 
    propertyArrayIndex?: number
  ): void;
}

type SelectorProps = {
  onChange: OnChangeData;
  type: 'entity' | 'property';
  parentUri?: string;
  parentLabel?: string;
  labelForQuery?: string;
  multiSelect: boolean;
  level?: number;
  propertyArrayIndex?: number;
  value?: Property[] | Property | Entity | string;
}

// as returned by a sparql db
type SparqlObject = {
  type: string;
  value: string;
}

type EntityData = {
  c: SparqlObject; // uri to get properties of in next step
  l?: SparqlObject; // label
  p?: SparqlObject; // parent, we don't do anything with this yet
  pred?: never;
  tpe?: never;
  dt?: never;
  ot?: never;
}

type PropertyData = {
  l?: SparqlObject; // label
  pred: SparqlObject; // uri to use in the sparql query
  tpe: SparqlObject; // type of property
  dt?: SparqlObject; // type of data
  ot?: SparqlObject; // entity the property belongs to, we use this to delve deeper
  c?: never;
  p?: never;
}

const Selector = ({onChange, type, parentUri, parentLabel, labelForQuery, multiSelect, level, propertyArrayIndex, value}: SelectorProps) => {
  const currentDatasets = useAppSelector(selectSelectedDatasets);

  const {data, isFetching, isError} = useSendSparqlQuery({
    query: type === 'entity' ? queries.entityQuery : queries.propertyQuery(parentUri as string), 
    datasets: currentDatasets
  });

  const results = data?.results.bindings;

  const {t} = useTranslation(['querybuilder']);

  // console.log(results)

  // Reformat results
  const resultsOptions = results && 
    results.map((item: EntityData | PropertyData) => {
      const uuid = uuidv4();
      if (type === 'entity') {
        return {
          label: item.l ? item.l.value : queries.getLabel(item.c!.value),
          value: item.c!.value,
          uuid: uuid,
        }
      }
      else {
        const otLabel = item.ot && queries.getLabel(item.ot!.value);
        const label = item.l ? item.l.value : queries.getLabel(item.pred!.value);
        const newLabelForQuery = labelForQuery ? 
          `${labelForQuery}_${label}${otLabel ? `_${otLabel}` : ''}` : 
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
      }
    }).sort((a: Entity | Property, b: Entity | Property) => {
      const la = a.label.toLowerCase(),
            lb = b.label.toLowerCase();
      return la < lb ? -1 : (la > lb ? 1 : 0)
    });

  return (
    <div style={{paddingLeft: `${level !== undefined ? level * 2 - 2 : 0}rem`}} className={level !== undefined ? styles.level : ''}>
      {type === 'entity' ?
        <label className={styles.label}>
          {t('selector.entityLabel')}
        </label>
        :
        <label className={styles.label}>
          <Trans
            i18nKey="selector.propertyLabel"
            ns="querybuilder"
            values={{ parentLabel: parentLabel}}
            components={{bold: <strong />}}
          />
        </label>
      }
      <Select 
        components={{ Option: CustomOption }}
        isOptionSelected={(option, selectValue) =>
          selectValue.some(v => 
            (v as Property | Entity).label === (option as Property | Entity).label && 
            (v as Property | Entity).value === (option as Property | Entity).value
          )
        }
        className={styles.select}
        options={resultsOptions} 
        placeholder={t('selector.placeholder')}
        isMulti={multiSelect}
        value={value}
        isClearable={true}
        noOptionsMessage={() => isFetching ? 
          <Spinner
            as="span"
            animation="border"
            size="sm"
            role="status"
            aria-hidden="true"
          /> : 
          ( isError ? t('selector.error') : t('selector.noResults'))
        }
        onChange={(data, changedValue) => onChange(data as Property | Entity, changedValue as ActionTypes, level, propertyArrayIndex)}
        theme={selectorTheme} />
    </div>
  );
}

export default memo(Selector);

interface CustomOptionProps extends OptionProps {
  data: unknown;
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
