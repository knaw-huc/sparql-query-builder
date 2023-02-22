import {forwardRef} from 'react';
import type {Ref} from 'react';
import {motion} from 'framer-motion';
import type {HTMLMotionProps} from 'framer-motion';

export const FadeDiv = forwardRef(({children, ...props}: HTMLMotionProps<'div'>, ref: Ref<HTMLDivElement>) =>
  <motion.div
    {...props}
    initial={{opacity: 0}}
    animate={{opacity: 1}}
    exit={{opacity: 0}}
    ref={ref}
    transition={{
      duration: 0.15, 
      ease: "easeOut",
    }}>
    {children}
  </motion.div>
);

export const SlideInDiv = forwardRef(({children, ...props}: HTMLMotionProps<'div'>, ref: Ref<HTMLDivElement>) =>
  <motion.div
    {...props}
    initial={{y: "100%"}}
    animate={{y: 0}}
    exit={{y: "100%"}}
    ref={ref}
    transition={{
      duration: 0.3,
      ease: "easeOut",
    }}>
    {children}
  </motion.div>
);

export const LayoutMotionDiv = ({children, ...props}: HTMLMotionProps<'div'>) =>
  <motion.div
    {...props}
    layout="position"
    transition={{
      duration: 0.2,
      ease: "easeOut",
    }}>
    {children}
  </motion.div>