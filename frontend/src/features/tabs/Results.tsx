import React, { useState, useMemo } from 'react';
import DataTable from 'react-data-table-component';
import styles from './TabsPanel.module.scss';
import { useSendSparqlQuery } from '../sparql/sparqlApi';
import { useAppSelector } from '../../app/hooks';
import { selectSentQuery } from '../querybuilder/queryBuilderSlice';

export function Results() {
  const [filterText, setFilterText] = useState('');
  const [resetPaginationToggle, setResetPaginationToggle] = React.useState(false);

  const currentQuery = useAppSelector(selectSentQuery);

  // Make this lazy, only when query present. Or only mount component when query present
  const { data } = useSendSparqlQuery(currentQuery) || [];
  console.log(data)

  // No headers in JSON?? This is dirty. Also, Typescript
  const headers = data && Object.keys(data[0]);
  const columns = headers ? headers.map( (h: string) => { 
    return {
      name: h, 
      selector: (row: Object) => row[h as keyof Object],
      sortable: true,
    }
  }) : [];

  const filteredItems = data && data.filter(
    ( item: any ) => item.year && item.year.toLowerCase().includes(filterText.toLowerCase()),
  );

  // Make a text filter component
  const subHeaderComponentMemo = React.useMemo(() => {
    const handleClear = () => {
      if (filterText) {
        setResetPaginationToggle(!resetPaginationToggle);
        setFilterText('');
      }
    };

    return (
      <FilterComponent 
        onFilter={ (e: React.ChangeEvent<HTMLInputElement>) => setFilterText(e.target.value)} 
        onClear={handleClear} 
        filterText={filterText} />
    );
  }, [filterText, resetPaginationToggle]);

  return (
    <DataTable
      columns={columns}
      data={filteredItems}
      pagination 
      paginationResetDefaultPage={resetPaginationToggle} // optionally, a hook to reset pagination to page 1
      subHeader
      subHeaderComponent={subHeaderComponentMemo}
      fixedHeader={true}
    />
  );
}

interface FilterProps {
  filterText: string;
  onFilter: React.ChangeEventHandler;
  onClear: React.MouseEventHandler;
}

const FilterComponent = ({ filterText, onFilter, onClear }:FilterProps) => (
  <>
    <input
      id="search"
      type="text"
      placeholder="Filter by year"
      value={filterText}
      onChange={onFilter}
    />
    <button type="button" onClick={onClear}>
      X
    </button>
  </>
);