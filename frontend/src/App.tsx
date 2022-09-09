import React from 'react';
import { Counter } from './features/counter/Counter';
import { ServerStatus } from './features/serverstatus/ServerStatus';
import { Sse } from './features/sse/Sse';
import { 
  useSseRegisterQuery, 
  useSseSubscribeAgentStateQuery, 
  useSseSubscribeMessagesQuery } from './features/sse/sseApi';
import './styles/App.scss';
import { useAppSelector } from './app/hooks';
import { getUuid } from './features/uuid/uuidSlice';
import { Query } from './features/query/Query';
import { useGetAgentQuery, useGetAgentListQuery } from './features/agent/agentApi';

function App() {
  const uuid = useAppSelector(getUuid);
  const agent = useGetAgentQuery(undefined).data;
  const agentList = useGetAgentListQuery(undefined).data;
  const sseRegister = useSseRegisterQuery({uuid: uuid, userAgentId: agent?.uuid}, {skip: agent ? false : true});
  const sseAgentStatus = useSseSubscribeAgentStateQuery({uuid: uuid}, {skip: agent ? false : true});
  const sseAgentMessages = useSseSubscribeMessagesQuery({uuid: uuid, userAgentId: agent?.uuid}, {skip: agent ? false : true});

  return (
    <div className="App">
      <ServerStatus />
      <Counter />
      <p>UUID: {uuid}</p>
      <h5>Agent</h5>
      <div><pre>{JSON.stringify(agent, null, 2) }</pre></div>
      <h5>Agent List</h5>
      <div><pre>{JSON.stringify(agentList, null, 2) }</pre></div>
      <Query />
    </div>
  );
}

export default App;
