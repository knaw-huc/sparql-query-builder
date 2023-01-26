import React, {useState, useMemo, useRef, useEffect} from 'react';
import {AnimatePresence} from 'framer-motion';
import DataTable from 'react-data-table-component';
import Container from 'react-bootstrap/Container';
import Row from 'react-bootstrap/Row';
import Col from 'react-bootstrap/Col';
import Form from 'react-bootstrap/Form';
import Button from 'react-bootstrap/Button';
import InputGroup from 'react-bootstrap/InputGroup';
import Spinner from 'react-bootstrap/Spinner';
import styles from './Results.module.scss';
import {useSendSparqlQuery} from '../sparql/sparqlApi';
import {useAppSelector} from '../../app/hooks';
import {selectSentQuery} from '../querybuilder/queryBuilderSlice';
import {selectedDatasets} from '../datasets/datasetsSlice';
import {Download} from '../download/Download';
import {FadeDiv, SlideDiv} from '../animations/Animations';
import './DataTableTheme';

type ResultsObject = {
  [key: string]: any
}

export function Results() {
  const [filterText, setFilterText] = useState('');
  const [resetPaginationToggle, setResetPaginationToggle] = React.useState(false);

  const currentQuery = useAppSelector(selectSentQuery);
  const currentDatasets = useAppSelector(selectedDatasets);

  const {data, isFetching, isError} = useSendSparqlQuery({
    query: currentQuery, 
    datasets: currentDatasets
  }, {
    skip: !currentQuery,
  });

  // makes sure results scroll into view when new results are available/query has been run
  const resultsRef = useRef<null | HTMLDivElement>(null);
  useEffect(() => {
    resultsRef.current && resultsRef.current.scrollIntoView()
  }, [data, isError])

  // Get table headers from returned JSON. 
  // Some basic cell formatting.
  const columns = data?.head.vars.map( (h: string) => {
    const emptyRow = {type:'', value:'',};
    return {
      name: <span className={styles.header}>{h}</span>, 
      selector: (row: ResultsObject) => {
        const selector = row[h] || emptyRow;
        return selector
      },
      cell: (row: ResultsObject) => {
        const selector = row[h] || emptyRow;
        return <CustomCell type={selector.type} value={selector.value} />
      },
      sortable: true,
      sortFunction: (rowA: ResultsObject, rowB: ResultsObject) => {
        const selectorA = rowA[h] || emptyRow;
        const selectorB = rowB[h] || emptyRow;
        const a = selectorA.value.toLowerCase();
        const b = selectorB.value.toLowerCase();
        return a > b ? 1 : ( b > a ? -1 : 0 );
      },
    }
  });

  const dataItems = data?.results.bindings;

  // Add some free text filtering
  const filteredItems = dataItems?.filter( (row: ResultsObject) => {
    const isPresent = Object.values(row).filter( (item: ResultsObject) => 
      item.value.toLowerCase().includes(filterText.toLowerCase())
    );
    return isPresent.length > 0
  });

  // Make a text filter component
  const headerComponentMemo = useMemo(() => {
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
          onFilter={(e: React.ChangeEvent<HTMLInputElement>) => setFilterText(e.target.value)} 
          onClear={handleClear} 
          filterText={filterText} />
      </div>
    );
  }, [filterText, resetPaginationToggle]);

  return (
    <AnimatePresence>
      {( data || isFetching || isError ) &&
      <SlideDiv key="results-container">
        <Container fluid className={styles.container}>
          <Row>
            <Col lg={12}>
              {isFetching ?
              <FadeDiv key="results-spinner" className={styles.spinner}>
                <Spinner animation="border" variant="primary" /> 
              </FadeDiv>
              :
              <FadeDiv key="results-table" refProps={resultsRef}>
                {isError ?
                  <p className={styles.error}>Oh no, something has gone wrong.</p>
                  :
                  <DataTable
                    columns={columns}
                    data={filteredItems}
                    pagination 
                    paginationResetDefaultPage={resetPaginationToggle} // optionally, a hook to reset pagination to page 1
                    title={<h5>Results ({filteredItems.length} records)</h5>}
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
              </FadeDiv>
              }
            </Col>
          </Row>
        </Container>
      </SlideDiv>
      }
    </AnimatePresence>
  );
}

type FilterProps = {
  filterText: string;
  onFilter: React.ChangeEventHandler;
  onClear: React.MouseEventHandler;
}

const FilterComponent = ({filterText, onFilter, onClear}: FilterProps) => (
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

type CellProps = {
  type: string;
  value: string;
};

const CustomCell = ({type, value}: CellProps) => (
  <div className={styles.cell}>
    {type === 'uri' ? 
      <a href={value} target="_blank" rel="noreferrer">{value}</a> 
      :
      <span>{value}</span>}
  </div>
);