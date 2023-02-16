export type ResultsObject = {
  [key: string]: any
}

export type FilterProps = {
  filterText: string;
  onFilter: React.ChangeEventHandler;
  onClear: React.MouseEventHandler;
}

export type CellProps = {
  type: string;
  value: string;
};