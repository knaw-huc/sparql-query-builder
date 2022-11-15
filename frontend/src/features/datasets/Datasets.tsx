import React, {useEffect} from 'react';
import Form from 'react-bootstrap/Form';
import Card from 'react-bootstrap/Card';
import styles from './Datasets.module.scss';
import {useGetDatasetsQuery} from './datasetsApi';
import {useAppSelector, useAppDispatch} from '../../app/hooks';
import {AnimatePresence} from 'framer-motion';
import Spinner from 'react-bootstrap/Spinner';
import {setSelectedDatasets, selectedDatasets} from './datasetsSlice';
import type {Dataset} from './datasetsSlice';
import {FadeDiv} from '../animations/Animations';

export function Datasets() {
  const {data, isFetching, isError} = useGetDatasetsQuery(undefined);
  const currentDatasets = useAppSelector(selectedDatasets);
  const dispatch = useAppDispatch();

  // enable all datasets by default/on load
  useEffect(() => {
    data?.length > 0 && dispatch(setSelectedDatasets(data));
  }, [data, dispatch]);

  function toggleDataset(set: Dataset){
    const filteredSets = currentDatasets.filter( (activeSet: Dataset) => activeSet.id !== set.id);
    dispatch(setSelectedDatasets(filteredSets.length < currentDatasets.length ? filteredSets : currentDatasets.concat(set)))
  }

  return (
    <Card bg="light" className={styles.card}>
      <Card.Header as="h5">Data sets</Card.Header>
      <Card.Body>
        <AnimatePresence mode="wait">
          {isFetching ?
            <FadeDiv key="datasets-loader">
              <Spinner animation="border" variant="primary" />
            </FadeDiv>
            :
            <FadeDiv key="datasets">
              {isError ?
                <Card.Title as="h6">Error fetching the data sets</Card.Title>
                :
                <>
                  <Card.Title as="h6">Select the data sets you wish to use</Card.Title>
                  <Form>
                    {data.map((set: Dataset) => (
                      <Form.Check 
                        checked={currentDatasets.some( (s: Dataset) => s.id === set.id)}
                        key={set.id} 
                        type="switch"
                        id={set.id}
                        value={set.id}
                        name={set.name}
                        label={set.name}
                        onChange={() => toggleDataset(set)}
                      />
                    ))}
                  </Form>
                </>
              }
            </FadeDiv>
          }
        </AnimatePresence>
      </Card.Body>
    </Card>
  )
}
