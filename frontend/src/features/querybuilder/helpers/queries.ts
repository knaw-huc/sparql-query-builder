import type {Entity, Property} from '../../../types/queryBuilder';

export function getLabel(schema: string) {
  // check for hash or slash, grab value after it
  var hash = schema.lastIndexOf('#');
  var slash = schema.lastIndexOf('/');
  return schema.substring((hash !== -1 ? hash : slash) + 1);
}

export const entityQuery = `
  PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
  SELECT DISTINCT ?c ?l ?p
  WHERE {
    {?s a ?c .}
    OPTIONAL {?c rdfs:label ?l}
    OPTIONAL {?c rdfs:subClassOf ?p}
  }
`;

export const propertyQuery = (schema: string) => `
  PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
  PREFIX owl: <http://www.w3.org/2002/07/owl#>
  SELECT DISTINCT ?pred ?tpe ?dt ?ot ?l WHERE {
    ?sub ?pred ?obj .
    ?sub a <${schema}> .
    BIND (
      IF(isURI(?obj),
        owl:ObjectProperty,
        owl:DatatypeProperty) AS ?tpe
      ) .
    BIND(DATATYPE(?obj) AS ?dt).
    OPTIONAL {?obj a ?ot}.
    OPTIONAL {?pred rdfs:label ?l}
  }
`;

// Get filter value
const getFilterValue = (filterType: string, labelForQuery: string, value: string, equalityOperator: string) => {
  switch(filterType) {
    case 'stringFilter':
      return `FILTER(CONTAINS(LCASE(?${labelForQuery}), "${value.toLowerCase()}"))`;
    case 'dateFilter':
      return `FILTER(?${labelForQuery} ${equalityOperator} "${value}"^^xsd:date)`;
    case 'integerFilter':
      return `FILTER(?${labelForQuery} ${equalityOperator} ${value})`;
    case 'gYearFilter':
      return `FILTER(?${labelForQuery} ${equalityOperator} "${value}"^^xsd:gYear)`;
    case 'gYearMonthFilter':
      return `FILTER(?${labelForQuery} ${equalityOperator} "${value}"^^xsd:gYearMonth)`;
    case 'datetimeFilter':
      return `FILTER(?${labelForQuery} ${equalityOperator} "${value}"^^xsd:dateTime)`;
    default: 
      return '';
  }
}

// Shows up in the Query code editor and gets sent to the endpoint
export const resultQuery = (entity: Entity, properties: Property[][], selectedLimit: number) => {

  const propertyLabels = properties.map(
    (propertyPath) => propertyPath.map(
      (property) => property.label !== '' && property.label !== undefined ? `?${property.labelForQuery}` : ''
    ).join(' ')
  ).join(' ');

  const propertySelectors = properties.map(
    (propertyPath) => propertyPath.map(
      (property, i) => property.value !== '' && property.value !== undefined ?
        (property.dataType && property.dataType.indexOf('Filter') !== -1 ?
          getFilterValue(
            property.dataType, 
            propertyPath[i-1].labelForQuery as string,
            property.value as string, 
            property.equalityOperator as string
          )
          :
          `?${(i > 0 ? propertyPath[i-1].labelForQuery : entity.label)} <${property.value}> ?${property.labelForQuery}.`
        )
        : ''
    ).join('\n  ')
  ).join('\n  ');

  // Ugly formatting here for nice formatting in the code editor
  return (
    !entity ? '' :  
`SELECT ?${entity.label} ${propertyLabels}
WHERE {
  ?${entity.label} a <${entity.value}>.
  ${propertySelectors}
} LIMIT ${selectedLimit}`
)};