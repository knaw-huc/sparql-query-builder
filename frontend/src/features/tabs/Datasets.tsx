import Form from 'react-bootstrap/Form';
import styles from './TabsPanel.module.scss';

export function Datasets() {
  return (
    <div>
      <h5>Select the data sets you wish to use</h5>
      <Form>
        {[1,2,3].map((set) => (
          <Form.Check 
            key={`dataset-${set}`} 
            type="checkbox"
            id={`dataset-${set}`}
            label={`Dataset ${set}`}
          />
        ))}
      </Form>
    </div>
  );
}
