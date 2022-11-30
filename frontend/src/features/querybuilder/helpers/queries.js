export function getLabel(schema: string | undefined) {
  // check for hash or slash, grab value after it
  var hash = schema.lastIndexOf('#');
  var slash = schema.lastIndexOf('/');
  return schema.substring((hash !== -1 ? hash : slash) + 1);
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

// Shows up in the Query code editor and gets sent to the endpoint.
// Ugly formatting here for nice formatting in the code editor
export const resultQuery = (entity: any, properties: any, subProperties: any) => {
  const propertyLabels = properties.map((item: any) => '?' + item.label).join(' ');
  const propertySelectors = properties.map((item: any) => '?' + entity.label + ' <' + item.value + '> ?' + item.label + '.').join('\n  ');
  const subPropertySelectors = subProperties.map((item: any) => '?' + item.label + ' <' + item.value + '> ?' + item.subLabel + '.').join('\n  ');
  const subPropertyLabels = subProperties.map((item: any) => '?' + item.subLabel).join(' ');
  return (
    !entity ? '' :
    
`SELECT ?${entity.label} ${propertyLabels} ${subPropertyLabels}
WHERE {
  ?${entity.label} a <${entity.value}>.
  ${propertySelectors}
  ${subPropertySelectors}
} LIMIT 200`

)};