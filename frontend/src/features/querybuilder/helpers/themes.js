import {tags as t} from '@lezer/highlight';
import {createTheme} from '@uiw/codemirror-themes';
import type {Theme} from 'react-select';

// theme for code editor
export const gaDark = createTheme({
  theme: 'dark',
  settings: {
    background: '#0d1117',
    foreground: '#c9d1d9',
    caret: '#c9d1d9',
    selection: '#003d73',
    selectionMatch: '#003d73',
    lineHighlight: '#36334280',
  },
  styles: [
    {tag: [t.comment, t.bracket], color: '#8b949e'},
    {tag: [t.className, t.propertyName], color: '#d2a8ff'},
    {tag: [t.variableName, t.attributeName, t.number, t.operator], color: '#efc501'},
    {tag: [t.keyword, t.typeName, t.typeOperator, t.typeName], color: '#fff'},
    {tag: [t.string, t.meta, t.regexp], color: '#a5d6ff'},
    {tag: [t.name, t.quote], color: '#7ee787'},
    {tag: [t.heading], color: '#d2a8ff', fontWeight: 'bold'},
    {tag: [t.emphasis], color: '#d2a8ff', fontStyle: 'italic'},
    {tag: [t.deleted], color: '#ffdcd7', backgroundColor: 'ffeef0'},
  ],
});

// theme for the selection boxes
export const selectorTheme = (theme: Theme) => ({
  ...theme,
  borderRadius: 3,
  colors: {
    ...theme.colors,
    primary25: '#efc501',
    primary: 'black',
  }
});