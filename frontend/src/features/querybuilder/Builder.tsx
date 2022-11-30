import React, {useState, useEffect} from 'react';
import {useAppSelector, useAppDispatch} from '../../app/hooks';
import {AnimatePresence} from 'framer-motion';
import {setActiveQuery} from './queryBuilderSlice';
import Select from 'react-select';
import type {SingleValue, MultiValue, Theme} from 'react-select';
import styles from './QueryBuilder.module.scss';
import * as queries from './helpers/queries';
import {useSendSparqlQuery} from '../sparql/sparqlApi';
import {setSelectedDatasets, selectedDatasets} from '../datasets/datasetsSlice';
import Spinner from 'react-bootstrap/Spinner';
import {FadeDiv} from '../animations/Animations';

// TODO: 
// Better Typescript types
// Do some filtering for duplicate results??

interface SparqlObject {
  type: string;
  value: string;
}

interface EntityResults {
  c: SparqlObject;
  l?: SparqlObject;
  p?: SparqlObject;
  pred?: never;
  tpe?: never;
  dt?: never;
  ot?: never;
}

interface PropertyResults {
  c?: never;
  p?: never;
  pred: SparqlObject;
  tpe: SparqlObject;
  dt?: SparqlObject;
  ot?: SparqlObject;
  l?: SparqlObject;
}

type SparqlResults = EntityResults | PropertyResults;

interface PropertySelectData {
  label: string;
  value: PropertyResults;
}

interface SelectedOptions {
  value: string;
  label: string;
}

interface SelectedPropertyOptions extends SelectedOptions {
  ot?: string;
}

interface SelectedSubPropertyOptions extends SelectedOptions {
  subLabel: string;
  parent: string;
}

const theme = (theme: Theme) => ({
  ...theme,
  borderRadius: 0,
  colors: {
    ...theme.colors,
    primary25: '#efc501',
    primary: 'black',
  }
});

export const Builder = () => {
  const dispatch = useAppDispatch();

  const [selectedEntity, setSelectedEntity] = useState<SelectedOptions>({value:'', label:''});
  const [selectedProperties, setSelectedProperties] = useState<SelectedPropertyOptions[]>([]);
  const [selectedSubProperties, setSelectedSubProperties] = useState<SelectedSubPropertyOptions[]>([]);

  // Set query in code editor when one of these values changes
  useEffect(() => {
    const theQuery = queries.resultQuery(selectedEntity, selectedProperties, selectedSubProperties);
    selectedEntity.value && dispatch(setActiveQuery(theQuery));
  }, [selectedEntity, selectedProperties, selectedSubProperties]);
  
  // Keep track of selected entities and set query accordingly
  const setSelectedEntityObject = (selector: SelectedOptions, newData: SelectedOptions) => {
    setSelectedEntity(newData);
    setSelectedProperties([]);
    setSelectedSubProperties([]);
  }

  // Keep track of selected properties for each entity, passed down to PropertySelect
  const setSelectedPropertiesObject = (selector: SelectedOptions, newData: PropertySelectData[]) => {
    const dataForQuery = newData.map((d: PropertySelectData) => { 
      return {
        label: d.value.l?.value || queries.getLabel(d.value.pred.value),
        value: d.value.pred.value,
        ot: d.value.ot?.value,
      }
    });
    setSelectedProperties(dataForQuery);

    // Check if subproperties are still applicable
    const newArray = selectedSubProperties.filter(
      (sp: SelectedSubPropertyOptions) => newData.some((nd: PropertySelectData) => nd.value.ot?.value === sp.parent)
    );
    newArray && setSelectedSubProperties(newArray);
  }

  // Keep track of selected subproperties for each property that has an ot
  const setSelectedSubPropertiesObject = (selector: SelectedOptions, newData: PropertySelectData) => {
    // Check if data already exists in properties array.
    const newArray = selectedSubProperties.filter((o: SelectedSubPropertyOptions) => o.parent !== selector.value);
    const dataForQuery = {
        label: selector.label,
        value: newData.value.pred.value,
        subLabel: `${selector.label}_${newData.value.l?.value || queries.getLabel(newData.value.pred.value)}`,
        parent: selector.value,
    };
    setSelectedSubProperties([...newArray, dataForQuery]);
  }

  return (
    <div>
      <h5 className={styles.header}>Build your query</h5>

      <SparqlSelect 
        selector={selectedEntity} 
        onChange={setSelectedEntityObject} 
        multiSelect={false}
        label="Select entity"
        placeholder="Give me every..."
        level={0} />

      <AnimatePresence mode="wait">
        {selectedEntity.value &&
          <FadeDiv key={selectedEntity.value}>
            <SparqlSelect 
              selector={selectedEntity} 
              onChange={setSelectedPropertiesObject} 
              multiSelect={true}
              label="Select properties"
              placeholder="must have a..."
              level={1} />
          </FadeDiv>
        }
      </AnimatePresence>

      <AnimatePresence>
        {selectedProperties.map( (property: SelectedPropertyOptions) => {
          return (
            property.ot &&
            <FadeDiv key={property.ot}>
              <SparqlSelect
                selector={{
                  label: property.label, 
                  value: property.ot
                }} 
                onChange={setSelectedSubPropertiesObject}
                multiSelect={false}
                label="Select sub-properties"
                placeholder="must have a..."
                level={2} />
            </FadeDiv>
          )
        })}
      </AnimatePresence>
    </div>
  )
}

interface SparqlSelectTypes {
  selector: SelectedOptions;
  // Hier even naar kijken
  onChange: (selector: SelectedOptions, newData: any) => void;
  multiSelect: boolean;
  label: string;
  placeholder: string;
  level: number;
}

type OnChange = SingleValue<React.ChangeEvent<HTMLSelectElement>> | MultiValue<React.ChangeEvent<HTMLSelectElement>>;

const SparqlSelect = ({selector, onChange, multiSelect, label, placeholder, level}: SparqlSelectTypes) => {
  const currentDatasets = useAppSelector(selectedDatasets);
  const theQuery = level === 0 ? queries.entityQuery : queries.propertyQuery(selector.value);

  const {data, isFetching, isError, error} = useSendSparqlQuery({
    query: theQuery, 
    datasets: currentDatasets
  });

  const results = data?.results.bindings;

  // Reformat and rearrange results
  const resultsOptions = results && 
    results.map((item: SparqlResults) => {
      return {
        value: item.c?.value || item,
        label: (item.l?.value || queries.getLabel(item.c?.value || item.pred?.value)) +
          (item.hasOwnProperty('ot') ? `: ${queries.getLabel(item.ot?.value)}` : ''),
      }
    })
    .sort((a: SelectedOptions, b: SelectedOptions) => {
      const la = a.label.toLowerCase(),
            lb = b.label.toLowerCase();
      return la < lb ? -1 : (la > lb ? 1 : 0)
    });

  return (
    <div>
      <label id={`level-${level}-label`} htmlFor={`level-${level}-input`} className={styles.label}>
        {label} 
        {level > 0 && 
          <span> for <span className={styles.labelEmphasis}>
            {selector.label}
            {level === 2 && `: ${queries.getLabel(selector.value)}`}
          </span></span>
        }
      </label>
      <Select 
        aria-labelledby="property-label"
        inputId="property-input"
        className={styles.select}
        options={resultsOptions} 
        placeholder={`${selector.label} ${placeholder}`}
        isMulti={multiSelect}
        noOptionsMessage={() => isFetching ? 
          <Spinner
            as="span"
            animation="border"
            size="sm"
            role="status"
            aria-hidden="true"
          /> : 
          'Nothing found'
        }
        onChange={(e: OnChange) => onChange(selector, e)}
        theme={theme} />
    </div>
  );
}