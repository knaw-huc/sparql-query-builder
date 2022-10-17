import Form from 'react-bootstrap/Form';
import Card from 'react-bootstrap/Card';
import styles from './Datasets.module.scss';
import {useGetDatasetsQuery} from './datasetsApi';
import {useAppSelector} from '../../app/hooks';
import {motion, AnimatePresence} from "framer-motion";
import Spinner from 'react-bootstrap/Spinner';

export function Datasets() {
  const {data, isFetching, isError} = useGetDatasetsQuery(undefined);

  return (
    <Card bg="light" className={styles.card}>
      <Card.Header as="h5">Data sets</Card.Header>
      <Card.Body>
        <AnimatePresence mode="wait">
          {isFetching ?
            <motion.div
              initial={{opacity: 0}}
              animate={{opacity: 1}}
              exit={{opacity: 0}}
              key="datasets-loader">
              <Spinner animation="border" variant="primary" />
            </motion.div>
            :
            <motion.div
              initial={{opacity: 0}}
              animate={{opacity: 1}}
              exit={{opacity: 0}}
              key="datasets">
              {/*isError ? // removed this check for now, to be put back when api actually works
                <Card.Title as="h6">Error fetching the data sets</Card.Title>
                : */
                <>
                  <Card.Title as="h6">Select the data sets you wish to use</Card.Title>
                  Datasets JSON object should be an array, something like this:
                  <pre><code>
                  {
                    JSON.stringify(
                      [
                        {
                          name: 'Friendly name #1',
                          id: 'some_id_1',
                        },
                        {
                          name: 'Friendly name #2',
                          id: 'some_id_2',
                        },
                      ],
                      undefined, 2
                    )
                  }
                  </code></pre>
                  We can then pass along the selected id's to the sparql query to Python
                </>
              }

              {/*<Form>
                {[1,2,3].map((set) => (
                  <Form.Check 
                    key={`dataset-${set}`} 
                    type="switch"
                    id={`dataset-${set}`}
                    label={`Dataset ${set}`}
                  />
                ))}
              </Form>*/}

            </motion.div>
          }
        </AnimatePresence>
      </Card.Body>
    </Card>
  )
}
