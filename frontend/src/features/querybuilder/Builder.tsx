import React, {useState, useEffect} from 'react';
import {useAppSelector, useAppDispatch} from '../../app/hooks';
import {AnimatePresence} from 'framer-motion';
import {setActiveQuery} from './queryBuilderSlice';
import Select from 'react-select';
import styles from './QueryBuilder.module.scss';
import * as queries from './helpers/queries';
import {useSendSparqlQuery} from '../sparql/sparqlApi';
import {setSelectedDatasets, selectedDatasets} from '../datasets/datasetsSlice';
import Spinner from 'react-bootstrap/Spinner';
import {FadeDiv} from '../animations/Animations';

// TODO: 
// Implement Typescript types!
// Do some filtering for duplicate results??

type SparqlObject = {
  type: string;
  value: string;
}

type EntityResults = {
  c: SparqlObject;
  l?: SparqlObject;
  p?: SparqlObject;
}

type PropertyResults = {
  pred: SparqlObject;
  tpe: SparqlObject;
  dt: SparqlObject;
  ot?: SparqlObject;
  l?: SparqlObject;
}

type SparqlResults = EntityResults | PropertyResults | string;

interface SelectOptions {
  value: any;
  label: string;
}

interface SelectPropertyOptions extends SelectOptions {
  ot: boolean;
}

interface SelectSubPropertyOptions extends SelectOptions {
  subLabel: string;
  parent: string;
}

const theme = (theme: any) => ({
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

  const [selectedEntity, setSelectedEntity] = useState<SelectOptions>({label:'', value:''});
  const [selectedProperties, setSelectedProperties] = useState<SelectPropertyOptions[]>([]);
  const [selectedSubProperties, setSelectedSubProperties] = useState<SelectSubPropertyOptions[]>([]);

  // Set query in code editor when one of these values changes
  useEffect(() => {
    const theQuery = queries.resultQuery(selectedEntity, selectedProperties, selectedSubProperties);
    selectedEntity.value && dispatch(setActiveQuery(theQuery));
  }, [selectedEntity, selectedProperties, selectedSubProperties]);
  
  // Keep track of selected entities and set query accordingly
  const setSelectedEntityObject = (selector: any, newData: any) => {
    setSelectedEntity(newData);
    setSelectedProperties([]);
    setSelectedSubProperties([]);
  }

  // Keep track of selected properties for each entity, passed down to PropertySelect
  const setSelectedPropertiesObject = (selector: any, newData: any) => {
    const dataForQuery = newData.map((d: any) => { 
      return {
        label: d.value.l?.value || queries.getLabel(d.value.pred.value),
        value: d.value.pred.value,
        ot: d.value.ot?.value,
      }
    });
    setSelectedProperties(dataForQuery);

    // Check if subproperties are still applicable
    const newArray = selectedSubProperties.filter(
      (sp: any) => newData.some((nd: any) => nd.value.ot?.value === sp.parent)
    );
    newArray && setSelectedSubProperties(newArray);
  }

  // Keep track of selected subproperties for each property that has an ot
  const setSelectedSubPropertiesObject = (selector: any, newData: any) => {
    // Check if data already exists in properties array.
    const newArray = selectedSubProperties.filter((o: any) => o.parent !== selector.value);
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
        {selectedProperties.map( (property: any) => {
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
  selector: SelectOptions;
  onChange: any;
  multiSelect: boolean;
  label: string;
  placeholder: string;
  level: number;
}

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
    results.map((item: any) => {
      !item.hasOwnProperty('c') && console.log(item)
      return {
        value: item.c?.value || item,
        label: (item.l?.value || queries.getLabel(item.c?.value || item.pred.value)) +
          (item.hasOwnProperty('ot') ? `: ${queries.getLabel(item.ot.value)}` : ''),
      }
    })
    .sort((a: SelectOptions, b: SelectOptions) => {
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
        onChange={(e: any) => onChange(selector, e)}
        theme={theme} />
    </div>
  );
}