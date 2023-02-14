import CodeMirror from '@uiw/react-codemirror';
import {langs} from '@uiw/codemirror-extensions-langs';
import {gaDark} from '../helpers/themes';
import styles from './Editor.module.scss';
import {useAppSelector, useAppDispatch} from '../../../app/hooks';
import {setActiveQuery, selectActiveQuery} from '../queryBuilderSlice';
import {useTranslation} from 'react-i18next';

// TODO: Should typing here reset the QB?

export function Editor() {
  const query = useAppSelector(selectActiveQuery);
  const dispatch = useAppDispatch();
  const {t} = useTranslation(['querybuilder']);

  return (
    <div>
      <h5 className={styles.header}>{t('editor.header')}</h5>
      <p className={styles.note}>{t('editor.note')}</p>
      <CodeMirror
        value={query}
        height="20rem"
        extensions={[langs.sparql()]}
        theme={gaDark}
        onChange={(value: string) => dispatch(setActiveQuery(value))}
      />
    </div>
  );
}
