import Tooltip from 'react-bootstrap/Tooltip';
import OverlayTrigger from 'react-bootstrap/OverlayTrigger';
import type {OverlayInjectedProps} from 'react-bootstrap/Overlay';
import type {TipProps} from '../../types/tooltip';

export const Tip = ({id, content, triggerElement}: TipProps) => {
  const renderTooltip = (props: OverlayInjectedProps) => 
    <Tooltip id={id} {...props}>
      {content}
    </Tooltip>

  return (
    <OverlayTrigger
      delay={{show: 50, hide: 200}}
      overlay={renderTooltip}>
      {triggerElement}
    </OverlayTrigger>
  );
}