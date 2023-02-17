import {motion} from 'framer-motion';
import type {MotionDivProps} from '../../types/animations';

export const FadeDiv = (props: MotionDivProps) =>
  <motion.div
    {...props}
    initial={{opacity: 0}}
    animate={{opacity: 1}}
    exit={{opacity: 0}}
    ref={props.refProps}>
    {props.children}
  </motion.div>
  
// This is a bit of a hack, giving invalid x and y values, to make framer motion ignore parent display state
export const FadeDivFixed = (props: MotionDivProps) =>
  <motion.div
    {...props}
    initial={{opacity: 0}}
    animate={{opacity: 1}}
    exit={{opacity: 0}}
    ref={props.refProps}
    style={{x: "-", y: "-"}}>
    {props.children}
  </motion.div>

export const SlideInDiv = (props: MotionDivProps) =>
  <motion.div
    initial={{y: "100%"}}
    animate={{y: 0}}
    exit={{y: "100%"}}
    ref={props.refProps}>
    {props.children}
  </motion.div>