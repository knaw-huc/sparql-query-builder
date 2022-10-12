import React, { useState, useMemo } from 'react';
import { motion, AnimatePresence } from "framer-motion"
import DataTable from 'react-data-table-component';
import Container from 'react-bootstrap/Container';
import Row from 'react-bootstrap/Row';
import Col from 'react-bootstrap/Col';
import Form from 'react-bootstrap/Form';
import Button from 'react-bootstrap/Button';
import InputGroup from 'react-bootstrap/InputGroup';
import Spinner from 'react-bootstrap/Spinner';
import styles from './Results.module.scss';
import { useSendSparqlQuery } from '../sparql/sparqlApi';
import { useAppSelector } from '../../app/hooks';
import { selectSentQuery } from '../querybuilder/queryBuilderSlice';
import './DataTableTheme';

export function Results() {
  const [filterText, setFilterText] = useState('');
  const [resetPaginationToggle, setResetPaginationToggle] = React.useState(false);

  const currentQuery = useAppSelector(selectSentQuery);

  // Make this lazy, only when query present. Or only mount component when query present
  const { data, isFetching, isLoading  } = useSendSparqlQuery(currentQuery, {
    skip: !currentQuery,
  });

  // Get table headers from returned JSON. Some basic cell formatting, to change
  const columns = data?.head.vars.map( (h: string) => { 
    return {
      name: h, 
      selector: (row: Object) => row[h as keyof Object],
      sortable: true,
      grow: h === 'year' ? 0 : 1,
      cell: (row: any) => row[h].startsWith('http') ? 
        <a href={row[h]} target="_blank">{row[h]}</a> :
        row[h],
    }
  });

  // Convert JSON results to something useable for the table: array of objects
  // with key as header value, and value as result value
  const dataItems = data?.results.bindings.map(
    ( item: any ) => {
      const arr = data.head.vars.map( (key: string) => {
        return {
          [key]: item[key].value,
        }
      });
    return Object.assign({}, ...arr);
  });

  // Add some free text filtering
  //todo const filteredItems = dataItems?.filter();

  // Make a text filter component
  const subHeaderComponentMemo = React.useMemo(() => {
    const handleClear = () => {
      if (filterText) {
        setResetPaginationToggle(!resetPaginationToggle);
        setFilterText('');
      }
    };

    return (
      <Row>
        <Col lg={8}>
          <h5>Results</h5>
        </Col>
        <Col lg={4}>
          <FilterComponent
            onFilter={ (e: React.ChangeEvent<HTMLInputElement>) => setFilterText(e.target.value)} 
            onClear={handleClear} 
            filterText={filterText} />
          </Col>
        </Row>
    );
  }, [filterText, resetPaginationToggle]);

  return (
    <AnimatePresence>
      { ( data || isFetching ) &&
      <motion.div
        initial={{ y: "100%" }}
        animate={{ y: 0 }}
        exit={{ y: "100%" }}
        key="results-container">
        <Container fluid className={styles.container}>
          <Row>
            <Col lg={12}>
              { isFetching ?
              <motion.div
                initial={{ opacity: 0 }}
                animate={{ opacity: 1 }}
                exit={{ opacity: 0 }}
                key="results-spinner"
                className={styles.spinner}>
                <Spinner animation="border" variant="primary" /> 
              </motion.div>
              :
              <motion.div
                initial={{ opacity: 0 }}
                animate={{ opacity: 1 }}
                exit={{ opacity: 0 }}
                key="results-table">
                <DataTable
                  columns={columns}
                  data={dataItems}
                  pagination 
                  paginationResetDefaultPage={resetPaginationToggle} // optionally, a hook to reset pagination to page 1
                  title={subHeaderComponentMemo}
                  theme="huc"
                  striped
                  highlightOnHover />
              </motion.div>
              }
            </Col>
          </Row>
        </Container>
      </motion.div>
      }
    </AnimatePresence>
  );
}

interface FilterProps {
  filterText: string;
  onFilter: React.ChangeEventHandler;
  onClear: React.MouseEventHandler;
}

const FilterComponent = ({ filterText, onFilter, onClear }:FilterProps) => (
  <InputGroup className={styles.filter} size="sm">
    <Form.Control
      placeholder="Filter results..."
      value={filterText}
      onChange={onFilter}
    />
    <Button 
      variant="outline-primary"
      onClick={onClear}>
      Clear
    </Button>
  </InputGroup>
);