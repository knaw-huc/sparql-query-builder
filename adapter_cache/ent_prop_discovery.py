import json
import os
import sqlite3
from sqlite3 import Error
from pathlib import Path

from SPARQLWrapper import SPARQLWrapper, JSON

CREATE_ENTITIES_TABLE = """
    CREATE TABLE IF NOT EXISTS entities (
        id integer PRIMARY KEY,
        name text NOT NULL,
        value text NOT NULL,
        data text NOT NULL,
        UNIQUE(value)
    );
"""


CREATE_PROPERTIES_TABLE = """
    CREATE TABLE IF NOT EXISTS properties (
        id integer PRIMARY KEY,
        entity_id integer NOT NULL,
        entity_value text NOT NULL,
        name text NOT NULL,
        value text NOT NULL,
        data text NOT NULL,
        FOREIGN KEY (entity_id) REFERENCES entities (id),
        UNIQUE(entity_id, value)
    );
"""


ENT_QUERY = """
    PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
    SELECT DISTINCT ?c ?l
    WHERE {
      {?s a ?c .}
      OPTIONAL {?c rdfs:label ?l}
      FILTER (!isBlank(?c))
    }
    """

PROP_QUERY = """
    PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
    PREFIX owl: <http://www.w3.org/2002/07/owl#>
    SELECT DISTINCT ?pred ?tpe ?dt ?ot ?l WHERE {
        ?sub ?pred ?obj .
        ?sub a <ENTITY_URI> .
        BIND (
            IF(isURI(?obj),
                owl:ObjectProperty,
                owl:DatatypeProperty) AS ?tpe
            ) .
        BIND ( DATATYPE(?obj) AS ?dt).
        OPTIONAL {?obj a ?ot}.
        OPTIONAL { ?pred rdfs:label ?l }
    }
    """

API_URL = os.environ.get('API_URL', default='http://localhost:7200/repositories/ga-wp3')
DB_PATH = Path(os.environ.get('DB_PATH', default='./ga_cache.db'))


def sparql_query(qry, uri):
    sparql = SPARQLWrapper(uri)
    sparql.setReturnFormat(JSON)
    sparql.setQuery(qry)
    try:
        result = sparql.queryAndConvert()
        return result['results']['bindings']
    except Exception as e:
        return []


def create_database(path):
    """create a database connection to a SQLite database"""
    conn = None
    try:
        conn = sqlite3.connect(path)
    except Error as e:
        print(e)
    finally:
        if conn:
            conn.close()


def create_connection(path):
    """create a database connection to the SQLite database
    specified by db_file"""
    conn = None
    try:
        conn = sqlite3.connect(path)
        return conn
    except Error as e:
        print(e)

    return conn


def create_table(conn, table_qry):
    """create a table from the create_table_sql statement"""
    try:
        c = conn.cursor()
        c.execute(table_qry)
    except Error as e:
        print(e)


def __get_name(uri):
    name = uri.split('/')[-1]
    if '#' in name:
        name = name.split('#')[-1]
    return name


def insert_entity(conn, entity):
    """inserts an entity"""
    # get some info
    value = entity['c']['value']
    name = __get_name(value)
    entity['l'] = { 'type': 'literal', 'value': name }
    data = json.dumps(entity)
    entity = (name, value, data)

    # create cursor
    cursor = conn.cursor()
    # see if this value exists
    qry = f'SELECT id, value FROM entities WHERE value="{value}"'
    cursor.execute(qry)
    result = cursor.fetchone()
    if result:
        return result
    else:
        qry = 'INSERT OR IGNORE INTO entities(name,value,data) VALUES(?,?,?)'
        cursor.execute(qry, entity)
        conn.commit()
        return (cursor.lastrowid, value)


def insert_property(conn, property, entity_summary):
    """inserts a property"""
    # get some info
    value = property['pred']['value']
    name = __get_name(value)
    property['l'] = { 'type': 'literal', 'value': name }
    data = json.dumps(property)
    # entity summary
    (ent_id, ent_value) = entity_summary
    property = (ent_id, ent_value, name, value, data)

     # create cursor
    cursor = conn.cursor()
    # see if this value exists
    qry = f'SELECT id FROM properties WHERE entity_id={ent_id}' \
        + f' AND value="{value}"'
    cursor.execute(qry)
    result = cursor.fetchone()
    if not result:
        qry = 'INSERT OR IGNORE INTO ' \
            + 'properties(entity_id,entity_value,name,value,data) ' \
            + 'VALUES(?,?,?,?,?)'
        cursor.execute(qry, property)
        conn.commit()
        return True
    else:
        return False


if __name__ == '__main__':
    if DB_PATH.exists():
        print('DB exists')
    else:
        # create database
        create_database(DB_PATH)
        # create connection
        conn = create_connection(DB_PATH)
        # create tables
        create_table(conn, CREATE_ENTITIES_TABLE)
        create_table(conn, CREATE_PROPERTIES_TABLE)

        ent_res = sparql_query(ENT_QUERY, API_URL)

        # loop over entities
        for e in ent_res:
            # and store entity
            value = e['c']['value']

            print(value)
            ent_summary = insert_entity(conn, e)

            # sparql query to find properties
            # of this entity
            prop_query = PROP_QUERY.replace(
                '<ENTITY_URI>',
                f'<{value}>'
            )
            prop_res = sparql_query(prop_query, API_URL)
            print(len(prop_res))

            # loop over properties
            for p in prop_res:
                p_value = p['pred']['value']

                # store property
                insert_property(conn, p, ent_summary)

        print('Done')
