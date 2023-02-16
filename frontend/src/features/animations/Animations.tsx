import {motion} from 'framer-motion';
import type {MotionProps} from '../../types/animations';

export const FadeDiv = ({children, refProps, className}: MotionProps) =>
  <motion.div
    initial={{opacity: 0}}
    animate={{opacity: 1}}
    exit={{opacity: 0}}
    className={className}
    ref={refProps}>
    {children}
  </motion.div>

export const SlideDiv = ({children, refProps, className}: MotionProps) =>
  <motion.div
    initial={{y: "100%"}}
    animate={{y: 0}}
    exit={{y: "100%"}}
    className={className}
    ref={refProps}>
    {children}
  </motion.div>