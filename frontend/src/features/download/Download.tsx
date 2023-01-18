import ButtonGroup from 'react-bootstrap/ButtonGroup';
import Button from 'react-bootstrap/Button';
import Spinner from 'react-bootstrap/Spinner';
import styles from './Download.module.scss';
import {useDownloadFileMutation} from './downloadApi';
import {useAppSelector, useAppDispatch} from '../../app/hooks';
import {selectSentQuery} from '../querybuilder/queryBuilderSlice';
import {selectedDatasets} from '../datasets/datasetsSlice';
import {selectDataType, setDataType} from './downloadSlice';

interface DownloadFn {
  (subString: string): Promise<void>;
}

interface DownloadProps {
  download: 'json' | 'csv' | 'xml';
  onClick: DownloadFn;
  isLoading: boolean;
  activeType: string;
};

export function Download() {
  const currentQuery = useAppSelector(selectSentQuery);
  const currentDatasets = useAppSelector(selectedDatasets);
  const dispatch = useAppDispatch();
  const activeDataType = useAppSelector(selectDataType);

  const [ downloadFile, {isLoading} ] = useDownloadFileMutation();

  const handleDownload = async (type: string) => {
    dispatch(setDataType(type));
    try {
      await downloadFile({query: currentQuery, datasets: currentDatasets});
      // after requesting download, reset datatype to JSON
      dispatch(setDataType('json'));
    } catch {
      console.log('error downloading');
    }
  }

  return (
    <ButtonGroup className={styles.resultsButtons}>
      <DownloadButton 
        download="xml" 
        onClick={handleDownload} 
        isLoading={isLoading}
        activeType={activeDataType}/>
      <DownloadButton 
        download="csv" 
        onClick={handleDownload} 
        isLoading={isLoading}
        activeType={activeDataType}/>
    </ButtonGroup>
  )
}

const DownloadButton = ({download, onClick, isLoading, activeType}: DownloadProps) =>
  <Button 
    variant="secondary" 
    size="sm" 
    className={styles.download}
    disabled={isLoading && download === activeType}
    onClick={() => onClick(download)}>
    {isLoading && download === activeType ?
      <Spinner
        as="span"
        animation="border"
        size="sm"
        role="status"
        aria-hidden="true"
      /> 
      :
      download
    }
  </Button>
