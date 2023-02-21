import {motion} from 'framer-motion';
import type {MotionDivProps} from '../../types/animations';

export const FadeDiv = ({refProps, children, ...props}: MotionDivProps) =>
  <motion.div
    {...props}
    initial={{opacity: 0}}
    animate={{opacity: 1}}
    exit={{opacity: 0}}
    ref={refProps}>
    {children}
  </motion.div>

export const SlideInDiv = ({refProps, children, ...props}: MotionDivProps) =>
  <motion.div
    {...props}
    initial={{y: "100%"}}
    animate={{y: 0}}
    exit={{y: "100%"}}
    ref={refProps}>
    {children}
  </motion.div>