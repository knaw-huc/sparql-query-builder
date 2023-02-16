import type {ReactNode, Ref} from 'react';

export type MotionProps = {
  children: ReactNode;
  refProps?: Ref<HTMLDivElement>;
  className?: string;
}
