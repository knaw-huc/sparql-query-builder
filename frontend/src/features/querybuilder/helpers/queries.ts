import type {Entity, Property} from '../BuilderRedux';

export function getLabel(item: any, type?: string) {
  if (!item.hasOwnProperty('c') && !item.hasOwnProperty('pred')) {
    return '';
  }
  if (item.hasOwnProperty('l') && type !== 'ot') {
    return item.l.value;
  }
  // either an entity or property
  const labelData = type === 'ot' ? item.ot : item.c || item.pred;
  // check for hash or slash, grab value after it
  var hash = labelData.value.lastIndexOf('#');
  var slash = labelData.value.lastIndexOf('/');
  return labelData.value.substring((hash !== -1 ? hash : slash) + 1);
}

export const entityQuery = `
  PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
  SELECT DISTINCT ?c ?l ?p
  WHERE {
    { ?s a ?c . }
    OPTIONAL { ?c rdfs:label ?l }
    OPTIONAL { ?c rdfs:subClassOf ?p }
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
    BIND ( DATATYPE(?obj) AS ?dt).
    OPTIONAL {?obj a ?ot}.
    OPTIONAL { ?pred rdfs:label ?l }
  }
`;

// Shows up in the Query code editor and gets sent to the endpoint
// Ugly formatting here for nice formatting in the code editor
export const resultQuery = (entity: Entity, properties: Property[][]) => {

  const propertyLabels = properties.map(
    (propertyPath: Property[]) => propertyPath.map(
      (property: Property) => !property.filterType ? `?${property.labelForQuery}` : ''
    ).join(' ')
  ).join(' ');

  const propertySelectors = properties.map(
    (propertyPath: Property[]) => propertyPath.map(
      (property: Property, i: number) => 
        property.filterType === 'stringFilter' ?
        `filter contains (?${propertyPath[i-1].labelForQuery}, "${property.value}")`
        :
        `?${(i > 0 ? propertyPath[i-1].labelForQuery : entity.label)} <${property.value}> ?${property.labelForQuery}.`
    ).join('\n  ')
  ).join('\n  ');

  return (
    !entity ? '' :  
`SELECT ?${entity.label} ${propertyLabels}
WHERE {
  ?${entity.label} a <${entity.value}>.
  ${propertySelectors}
} LIMIT 1000`
)};