import Tooltip from 'react-bootstrap/Tooltip';
import OverlayTrigger from 'react-bootstrap/OverlayTrigger';

export const Tip = ({id, content, triggerElement}: any) => {
  const renderTooltip = (props: any) => (
    <Tooltip id={id} {...props}>
      {content}
    </Tooltip>
  );

  return (
    <OverlayTrigger
      delay={{show: 50, hide: 200}}
      overlay={renderTooltip}>
      {triggerElement}
    </OverlayTrigger>
  );
}