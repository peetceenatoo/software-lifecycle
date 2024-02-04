import React, { useState, useEffect } from 'react';
import axios from 'axios';
import { ListGroup, InputGroup, FormControl, Button } from 'react-bootstrap';

import api from '../utilities/api';
import BattleListItemOngoing from './BattleListItems/BattleListItemOngoing';
import BattleListItemInfo from './BattleListItems/BattleListItemInfo';


const BattleList = ({ type, name, tournamentId}) => {
  const [battles, setBattles] = useState([]);

  useEffect(() => {
    const fetchBattles = async () => {
      try {
        let response = null;
        if(localStorage.getItem('role') == 'ROLE_STUDENT') {
          if(type == 'Ongoing') {
            response = await api.get(`/tournaments/${tournamentId}/battles`);
          }else if(type == 'Enrolled') {
            response = await api.get(`/tournaments/${tournamentId}/battles/enrolled`);
          }else if(type == 'Ended') {
            response = await api.get(`/tournaments/${tournamentId}/battles/state/ENDED`);
          } 
        }else {
          response = await api.get(`/tournaments/${tournamentId}/battles`);
        }
        console.log(response.data);
        setBattles(response.data);
      } catch (error) {
        console.error('Error fetching Battles', error);
      }
    };


    fetchBattles();
  }, []);

  

  return (
    <div>
      <h2>{name}</h2>
      <ListGroup className="scrollable">
        {battles.length > 0 ? (
          battles.map((battle) => {
            if (type === 'Ongoing') {
              return (
                <BattleListItemOngoing
                  key={battle.id}
                  battleId={battle.id}
                  battleState={battle.state}
                  nameBattle={battle.name}
                  nameTournament={battle.name}
                  subscriptionDeadline={battle.submissionDeadline}
                  role={localStorage.getItem('role')}
                  status={battle.state}
                />
              );
            } else if (type === 'Managed' || type==='Enrolled') {
              if(localStorage.getItem('role') === 'ROLE_STUDENT') {
                return (
                  <BattleListItemInfo
                    key={battle.id}
                    id={battle.id}
                    nameBattle={battle.name}
                    score= 'X'
                  />
                );
              } else {
                return (
                  <BattleListItemInfo
                  key={battle.id}
                  id={battle.id}
                  nameBattle={battle.name}
                  score = {null}
                  />
                );
              }
            }
          })
        ) : 
        (
          // If there are no battles, display a message
          <ListGroup.Item>No battles available</ListGroup.Item>
        )}
      </ListGroup>
    </div>
  );
};

export default BattleList;
