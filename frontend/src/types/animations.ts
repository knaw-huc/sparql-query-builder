import {HTMLMotionProps} from 'framer-motion';
import type {ReactNode, Ref} from 'react';

export interface MotionDivProps extends HTMLMotionProps<'div'> {
  children: ReactNode;
  refProps?: Ref<HTMLDivElement>;
}
