import type {OptionProps, SingleValue} from 'react-select';
import type {Dataset} from './datasets';

/*
 * QueryBuilderSlice
*/

export type QueryState = {
  active: string;
  sent: string;
  selectedEntity: Entity;
  selectedProperties: Property[][];
  selectedLimit: number;
}

/*
 * Selector
*/

export type SelectorProps = {
  multiSelect: boolean;
  selector: Property;
  level: number;
  propertyArrayIndex?: number;
}

// as returned by a sparql db
export type SparqlObject = {
  type: string;
  value: string;
}

export type EntityData = {
  c: SparqlObject; // uri to get properties of in next step
  l?: SparqlObject; // label
  p?: SparqlObject; // parent, we don't do anything with this yet
  pred?: never;
  tpe?: never;
  dt?: never;
  ot?: never;
}

export type PropertyData = {
  l?: SparqlObject; // label
  pred: SparqlObject; // uri to use in the sparql query
  tpe: SparqlObject; // type of property
  dt?: SparqlObject; // type of data
  ot?: SparqlObject; // entity the property belongs to, we use this to delve deeper
  c?: never;
  p?: never;
}

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
  equalityOperator?: string; // for date/number filtering 
  labelForQuery?: string; // value that gets passed as a label to the sparql query
}

export type ActionTypes = {
  action: 'clear' | 'create-option' | 'deselect-option' | 'pop-value' | 'remove-value' | 'select-option' | 'set-value';
  option?: Property;
  removedValue?: Property;
  removedValues: Property[];
}

export type NoOptions = {
  isFetching: boolean;
  isError: boolean;
}

export interface CustomOptionProps extends OptionProps {
  data: unknown;
}

/*
 * Filter
*/

export type FilterDataType = string; // possibly narrow this down later on, depending on the data types we might get

export type DataTypeProps = {
  level: number;
  propertyArrayIndex: number;
  selector: Property;
}

export type SelectOption = {
  value: string;
  label: string;
}

export type FilterState = {
  value: string;
  select: SingleValue<SelectOption>;
}

/*
 * Cookies
*/

export interface QueryCookieObject {
  query: string;
  datetime: string;
  datasets: Dataset[];
  uuid: string;
  entity: Entity;
  // properties: Property[][];
  // limit: number;
}

export interface QueryCookiesFn {
  setKey: (arg: string) => void;
}