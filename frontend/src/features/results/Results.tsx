import React, { useState, useMemo } from 'react';
import { motion, AnimatePresence } from "framer-motion"
import DataTable from 'react-data-table-component';
import type SortFunction from 'react-data-table-component';
import Container from 'react-bootstrap/Container';
import Row from 'react-bootstrap/Row';
import Col from 'react-bootstrap/Col';
import Form from 'react-bootstrap/Form';
import Button from 'react-bootstrap/Button';
import InputGroup from 'react-bootstrap/InputGroup';
import Spinner from 'react-bootstrap/Spinner';
import styles from './Results.module.scss';
import { useSendSparqlQuery } from '../sparql/sparqlApi';
import { useAppSelector, useAppDispatch } from '../../app/hooks';
import { selectSentQuery } from '../querybuilder/queryBuilderSlice';
import { addNotification } from '../notifications/notificationsSlice';
import { Download } from '../download/Download';
import './DataTableTheme';

const customSort = (rows:any, field:any, sortDirection:any) => {
  console.log(rows)
  console.log(field)
  console.log(sortDirection)
};

export function Results() {
  const [filterText, setFilterText] = useState('');
  const [resetPaginationToggle, setResetPaginationToggle] = React.useState(false);
  const dispatch = useAppDispatch();

  const currentQuery = useAppSelector(selectSentQuery);

  const { data, isFetching, isLoading, isError, error  } = useSendSparqlQuery(currentQuery, {
    skip: !currentQuery,
  });

  console.log(error)
  console.log(data)

  // Get table headers from returned JSON. Some basic cell formatting, to change
  const columns = data?.head.vars.map( (h: string) => { 
    return {
      name: <span className={styles.header}>{h}</span>, 
      selector: (row: Object) => row[h as keyof Object],
      sortable: true,
      grow: h === 'year' ? 0 : 1,
      cell: (row: any) => <CustomCell type={row[h].type} value={row[h].value} />
    }
  });

  const dataItems = data?.results.bindings;

  // Add some free text filtering
  //todo const filteredItems = dataItems?.filter();

  // Make a text filter component
  const headerComponentMemo = React.useMemo(() => {
    const handleClear = () => {
      if (filterText) {
        setResetPaginationToggle(!resetPaginationToggle);
        setFilterText('');
      }
    };

    return (
      <div className={styles.resultsActions}>
        <Download />
        <FilterComponent
          onFilter={ (e: React.ChangeEvent<HTMLInputElement>) => setFilterText(e.target.value)} 
          onClear={handleClear} 
          filterText={filterText} />
      </div>
    );
  }, [filterText, resetPaginationToggle]);

  return (
    <AnimatePresence>
      { ( data || isFetching || isError ) &&
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
                {isError ?
                  <p className={styles.error}>Oh no, something has gone wrong.</p> 
                  :
                  <DataTable
                    columns={columns}
                    data={dataItems}
                    pagination 
                    paginationResetDefaultPage={resetPaginationToggle} // optionally, a hook to reset pagination to page 1
                    title={<h5>Results</h5>}
                    subHeader
                    subHeaderComponent={headerComponentMemo}
                    subHeaderWrap
                    theme="huc"
                    striped
                    highlightOnHover
                    paginationPerPage={20}
                    paginationRowsPerPageOptions={[20, 50, 100]} 
                  />
                }
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

interface CellProps {
  type: string;
  value: string;
};

const CustomCell = ({type, value}: CellProps) => (
  <div className={styles.cell}>
    { type === 'uri' ? <a href={value} target="_blank">{value}</a> : <span>{value}</span> }
  </div>
);