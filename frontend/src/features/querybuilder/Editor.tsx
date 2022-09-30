import CodeMirror from '@uiw/react-codemirror';
import { langs } from '@uiw/codemirror-extensions-langs';
import styles from './QueryBuilder.module.scss';

const placeholder = 
  "PREFIX ga: <http://www.goldenagents.org/ontology>\n"+
  "  SELECT * where\n"+
  "  ?a ga:CreativeAgent\n"+
  "  ?a ga:hasName";

export function Editor() {
  return (
    <div>
      <h5 className={styles.header}>Manually edit your Sparql code</h5>
      <CodeMirror
          value={placeholder}
          height="200px"
          extensions={[langs.sparql()]}
        />
    </div>
  );
}
