import React, { useState } from 'react';
import { useAppSelector, useAppDispatch } from '../../app/hooks';
import { useSseSubscribeQueryProgressQuery, useSseRegisterQuery } from '../sse/sseApi';
import { useSendQueryQuery, useGetAgentQuery, useGetAgentListQuery } from '../agent/agentApi';
import { getUuid } from '../uuid/uuidSlice';
import styles from './Query.module.scss';


export const Query = () => {
  const uuid = useAppSelector(getUuid);
  const agent = useGetAgentQuery(undefined).data;
  const agentList = useGetAgentListQuery(undefined).data;

  const TempDemoQuery = {
    "query":"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\nPREFIX ga: <https://data.goldenagents.org/ontology/>\n\nSELECT DISTINCT ?creativeact ?subeventof WHERE {\n\t?creativeact rdf:type ga:CreativeAct . \n\t?creativeact ga:subEventOf ?subeventof\n}",
    "queryType":"USER_QUERY",
    "selectedSources": agentList?.length > 0 && agentList.map( (agent: any) => agent.agentType === 'DB' && agent.uuid),
  };

  const query = useSendQueryQuery({userAgentId: agent?.uuid, post: TempDemoQuery}, {skip: agentList ? false : true}).data;
  const sseRegister = useSseRegisterQuery({uuid: uuid, userAgentId: agent?.uuid, queryId: query?.queryID}, {skip: query && agent ? false : true});
  const sseQueryProgress = useSseSubscribeQueryProgressQuery({uuid: uuid, queryId: query?.queryID}, {skip: query ? false : true});

  console.log(sseRegister);

  return (
    <div>
     Query wordt gestuurd
    </div>
  )
}