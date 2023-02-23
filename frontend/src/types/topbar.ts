import type {ReactNode} from 'react';

export type MenuWrapProps = {
  children: ReactNode[];
  isSmall: boolean;
  expanded: boolean;
}

export type DropperProps = {
  children: ReactNode;
  number: number;
}

export type ToggleProps = {
  title: string;
  open: boolean;
  onClick: () => void;
  id: string;
}

export type MenuProps = {
  children: ReactNode[];
  closeMenu: () => void;
  open: boolean;
  isSmall: boolean;
}

export type ItemProps = {
  href: string;
  title: string;
}