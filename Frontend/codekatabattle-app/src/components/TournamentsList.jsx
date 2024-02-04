import React, { useState, useEffect } from 'react';
import axios from 'axios';
import { ListGroup, InputGroup, FormControl, Button } from 'react-bootstrap';
import TournamentListItemEnrolled from './TournamentListItems/TournamentListItemEnrolled';
import TournamentListItemManaged from './TournamentListItems/TournamentListItemManaged';
import TournamentListItemOngoing from './TournamentListItems/TournamentListItemOngoing';
import api from '../utilities/api';

const TournamentsList = ({ type, name, refreshKey}) => {
  const [tournaments, setTournaments] = useState([]);
  const [searchTerm, setSearchTerm] = useState('');

  // Function to fetch tournaments based on search term
  const searchTournaments = async () => {
    try {
      let response = null;
      console.log(searchTerm);
      if (searchTerm == '') {
        response = await api.get('/tournaments/state/ONGOING');
      }else 
        response = await api.get(`/tournaments/search/${searchTerm}`);
      setTournaments(response.data);
    } catch (error) {
      console.error('Error searching for tournaments', error);
    }
  };

  const fetchTournaments = async () => {
    try {
      let response = null;
      if(type == 'Ongoing') {
        //response = await api.get('/tournaments/state/ONGOING'); // Update the API endpoint as needed
        response = await api.get('/tournaments');
      }else if(type == 'Managed/Enrolled') {
        if(localStorage.getItem('role') == 'ROLE_STUDENT')
          response = await api.get('/tournaments/enrolled'); // Update the API endpoint as needed
        else
          response = await api.get('/tournaments/managed'); // Update the API endpoint as needed
      }
      console.log(response.data);
      setTournaments(response.data);
    } catch (error) {
      console.error('Error fetching tournaments', error);
    }
  };

  useEffect(() => {
    fetchTournaments();
  }, []);

  useEffect(() => {
    // Function to fetch tournaments list
    fetchTournaments();
  }, [refreshKey]);

   // Function to handle the search button click
   const handleSearchClick = () => {
      searchTournaments();
  };

  const formatDateTime = (dateTime) => {
    return new Date(dateTime).toLocaleString();
  };


  return (
    <div>
      <h2>{name}</h2>
      <InputGroup className="mb-3">
        <FormControl
          placeholder="Search"
          aria-label="Search"
          value={searchTerm}
          onChange={(e) => setSearchTerm(e.target.value)}
        />
        <Button variant="outline-primary" id="button-search" onClick={handleSearchClick}>
          Search
        </Button>
      </InputGroup>
      <ListGroup className="scrollable">
        {tournaments.map((tournament) => {
          if (type === 'Ongoing') {
            return (
              <TournamentListItemOngoing
                key={tournament.id}
                id={tournament.id}
                nameTournament={tournament.name}
                subscriptionDeadline={tournament.deadline}
                role={localStorage.getItem('role')}
                status={tournament.state}
              />
            );
          } else if (type === 'Managed/Enrolled') {
            if(localStorage.getItem('role') === 'ROLE_STUDENT') {
              return (
                <TournamentListItemEnrolled
                  key={tournament.id}
                  id={tournament.id}
                  nameTournament={tournament.name}
                  ranking= 'X'
                />
              );
            } else {
              return (
                <TournamentListItemManaged
                  key={tournament.id}
                  id={tournament.id}
                  nameTournament={tournament.name}
                  status={tournament.state}
                />
              );
            }
          }
        })}
      </ListGroup>
    </div>
  );
};

export default TournamentsList;
