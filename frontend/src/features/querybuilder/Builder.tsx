import Select from 'react-select';
import styles from './QueryBuilder.module.scss';

const theme = (theme: any) => ({
  ...theme,
  borderRadius: 0,
  colors: {
    ...theme.colors,
    primary25: '#efc501',
    primary: 'black',
  }
});

const options = [
  {value: 'pt', label: 'Painter'},
  {value: 'wr', label: 'Writer'},
  {value: 'ph', label: 'Philosopher'},
  {value: 'sc', label: 'Scientist'},
  {value: 'co', label: 'Colonist'},
];

const options2 = [
  {label: 'Sold to', value: 'sold'},
  {label: 'Bought from', value: 'bought'},
];

const options3 = [
  {label: 'Jan Six', value: 'six'},
  {label: 'Joost Vondel', value: 'vondel'},
];

export function Builder() {
  return (
    <div>
      <h5 className={styles.header}>Build your query</h5>
      <p>Just placeholder selectboxes for now</p>
      <Select 
        className={styles.select}
        options={options} 
        placeholder="Give me every..."
        theme={theme} />
      <Select 
        className={styles.select}
        options={options2} 
        placeholder="Who has..."
        theme={theme} />
      <Select 
        className={styles.select}
        options={options3} 
        placeholder="To..."
        theme={theme} />
    </div>
  );
}
