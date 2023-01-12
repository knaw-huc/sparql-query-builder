/*import React, {useState, useEffect} from 'react';
import {useAppSelector, useAppDispatch} from '../../app/hooks';
import {AnimatePresence} from 'framer-motion';
import {setActiveQuery} from './queryBuilderSlice';
import Select from 'react-select';
import type {PropsValue, Theme} from 'react-select';
import styles from './QueryBuilder.module.scss';
import * as queries from './helpers/queries';
import {useSendSparqlQuery} from '../sparql/sparqlApi';
import {selectedDatasets} from '../datasets/datasetsSlice';
import Spinner from 'react-bootstrap/Spinner';
import {FadeDiv} from '../animations/Animations';

// TODO: 
// Check Typescript types?
// Do some additional filtering/labeling for duplicate results?

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

interface SelectedData {
  value: SparqlResults;
  label: string;
}

export interface EntityState {
  value: string;
  label: string;
}

export interface PropertyState extends EntityState {
  ot?: string;
}

export interface SubPropertyState extends EntityState {
  subLabel: string;
  parent: string;
}

interface SelectorData extends EntityState {}

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

  const [selectedEntity, setSelectedEntity] = useState<EntityState>({value: '', label: ''});
  const [selectedProperties, setSelectedProperties] = useState<PropertyState[]>([]);
  const [selectedSubProperties, setSelectedSubProperties] = useState<SubPropertyState[]>([]);

  // Set query in code editor when one of these values changes
  useEffect(() => {
    const theQuery = queries.resultQuery(selectedEntity, selectedProperties, selectedSubProperties);
    selectedEntity.value && dispatch(setActiveQuery(theQuery));
  }, [selectedEntity, selectedProperties, selectedSubProperties, dispatch]);
  
  // Keep track of selected entities and set query accordingly
  const setSelectedEntityObject = (selector: SelectorData, data: SelectedData) => {
    setSelectedEntity({
      value: data.value.c!.value, 
      label: data.value.l?.value || queries.getLabel(data.value.c!.value)
    });
    setSelectedProperties([]);
    setSelectedSubProperties([]);
  }

  // Keep track of selected properties for each entity, passed down to PropertySelect
  const setSelectedPropertiesObject = (selector: SelectorData, data: SelectedData[]) => {
    const dataForQuery = data.map((d: SelectedData) => { 
      return {
        value: d.value.pred!.value,
        label: d.value.l?.value || queries.getLabel(d.value.pred!.value),
        ot: d.value.ot?.value,
      }
    });
    setSelectedProperties(dataForQuery);

    // Check if subproperties are still applicable
    const newArray = selectedSubProperties.filter(
      (sp: SubPropertyState) => data.some((nd: SelectedData) => nd.value.ot?.value === sp.parent)
    );
    newArray && setSelectedSubProperties(newArray);
  }

  // Keep track of selected subproperties for each property that has an ot
  const setSelectedSubPropertiesObject = (selector: SelectorData, data: SelectedData) => {
    // Check if data already exists in properties array.
    const newArray = selectedSubProperties.filter((o: SubPropertyState) => o.parent !== selector.value);
    const dataForQuery = {
        value: data.value.pred!.value,
        label: selector.label,
        subLabel: `${selector.label}_${data.value.l?.value || queries.getLabel(data.value.pred!.value)}`,
        parent: selector.value,
    };
    setSelectedSubProperties([...newArray, dataForQuery]);
  }

  return (
    <>
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
        {selectedProperties.map( (property: PropertyState) => {
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
    </>
  )
}

interface SparqlSelectTypes {
  selector: SelectorData;
  onChange: (selector: SelectorData, data: any) => void;
  multiSelect: boolean;
  label: string;
  placeholder: string;
  level: number;
}

const SparqlSelect = ({selector, onChange, multiSelect, label, placeholder, level}: SparqlSelectTypes) => {
  const currentDatasets = useAppSelector(selectedDatasets);
  const theQuery = level === 0 ? queries.entityQuery : queries.propertyQuery(selector.value);

  const {data, isFetching, isError} = useSendSparqlQuery({
    query: theQuery, 
    datasets: currentDatasets
  });

  const results = data?.results.bindings;

  // Reformat and rearrange results
  const resultsOptions = results && 
    results.map((item: SparqlResults) => {
      return {
        value: item,
        label: (item.l?.value || queries.getLabel(item.c?.value || item.pred?.value)) +
          (item.hasOwnProperty('ot') && level < 2 ? `: ${queries.getLabel(item.ot?.value)}` : ''),
      }
    })
    .sort((a: SelectedData, b: SelectedData) => {
      const la = a.label.toLowerCase(),
            lb = b.label.toLowerCase();
      return la < lb ? -1 : (la > lb ? 1 : 0)
    })
    // and no duplicate preds if final level, since we're not looking at the ot there
    .filter((v: SelectedData, i: number, a: any[]) => 
      level === 2 ? 
        (a.findIndex((v2: SelectedData) => (v2.value.pred!.value === v.value.pred!.value)) === i) :
        true
    );

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
          ( isError ? 'Nothing found' : 'Something\'s gone wrong with fetching the data')
        }
        onChange={(e: PropsValue<SelectedData>) => onChange(selector, e)}
        theme={theme} />
    </div>
  );
}*/

export {}