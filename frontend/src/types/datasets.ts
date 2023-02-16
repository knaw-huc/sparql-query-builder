export type Dataset = {
  id: string;
  name: string;
}

export type DatasetsState = {
  selectedSets: Dataset[];
  sentSets: Dataset[];
}