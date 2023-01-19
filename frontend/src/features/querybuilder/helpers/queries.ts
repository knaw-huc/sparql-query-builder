import type {Entity, Property} from '../Builder';

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

// Shows up in the Query code editor and gets sent to the endpoint
// Ugly formatting here for nice formatting in the code editor
export const resultQuery = (entity: Entity, properties: Property[][]) => {

  const propertyLabels = properties.map(
    (propertyPath: Property[]) => propertyPath.map(
      (property: Property) => property.label ? `?${property.labelForQuery}` : ''
    ).join(' ')
  ).join(' ');

  const propertySelectors = properties.map(
    (propertyPath: Property[]) => propertyPath.map(
      (property: Property, i: number) => 
        property.dataType && property.dataType === 'stringFilter' && property.value ?
        `FILTER(CONTAINS(LCASE(?${propertyPath[i-1].labelForQuery}), "${property.value.toLowerCase()}"))`
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