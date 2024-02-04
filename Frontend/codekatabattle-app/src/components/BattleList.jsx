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

  const formatDateTime = (dateTime) => {
  return new Date(dateTime).toLocaleString();
  };


  return (
    <div>
      <h2>{name}</h2>
      <ListGroup className="scrollable">
        {battles.map((battle) => {
          if (type === 'Ongoing') {
            return (
              <BattleListItemOngoing
                key={battle.id}
                id={battle.id}
                nameTournament={battle.name}
                subscriptionDeadline={formatDateTime(battle.deadline)}
                role={localStorage.getItem('role')}
                status={battle.state}
              />
            );
          } else if (type === 'Managed/Enrolled') {
            if(localStorage.getItem('role') === 'ROLE_STUDENT') {
              return (
                <BattleListItemInfo
                  key={battle.id}
                  id={battle.id}
                  score= 'X'
                />
              );
            } else {
              return (
                <BattleListItemInfo
                key={battle.id}
                id={battle.id}
                score= 'X'
                />
              );
            }
          }
        })}
      </ListGroup>
    </div>
  );
};

export default BattleList;
