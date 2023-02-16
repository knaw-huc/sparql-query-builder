export interface DownloadFn {
  (subString: string): Promise<void>;
}

export type DownloadProps = {
  download: 'json' | 'csv' | 'xml';
  onClick: DownloadFn;
  isLoading: boolean;
  activeType: string;
};

export type RequestedData = {
  dataType: string;
}