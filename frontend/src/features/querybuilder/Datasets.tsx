import Form from 'react-bootstrap/Form';
import Card from 'react-bootstrap/Card';
import styles from './QueryBuilder.module.scss';

export const Datasets = () =>
  <Card bg="light" className={styles.card}>
    <Card.Header as="h5">Data sets</Card.Header>
    <Card.Body>
      <Card.Title as="h6">Select the data sets you wish to use</Card.Title>
      <Form>
        {[1,2,3].map((set) => (
          <Form.Check 
            key={`dataset-${set}`} 
            type="switch"
            id={`dataset-${set}`}
            label={`Dataset ${set}`}
          />
        ))}
      </Form>
    </Card.Body>
  </Card>
