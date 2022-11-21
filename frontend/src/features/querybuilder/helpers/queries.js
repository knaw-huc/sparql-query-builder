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