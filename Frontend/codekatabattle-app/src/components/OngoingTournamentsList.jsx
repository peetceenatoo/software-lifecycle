import React, { useState, useEffect } from 'react';
import axios from 'axios';
import { ListGroup, InputGroup, FormControl, Button } from 'react-bootstrap';
import TournamentListItem from './TournamentListItem';
import api from '../utilities/api';

const TournamentsListOngoing = () => {
  const [tournaments, setTournaments] = useState([]);
  const [searchTerm, setSearchTerm] = useState('');

  useEffect(() => {
    const fetchTournaments = async () => {
      try {
        const response = await api.get('/tournaments/state/ONGOING'); // Update the API endpoint as needed
        console.log(response.data);
        setTournaments(response.data);
      } catch (error) {
        console.error('Error fetching tournaments', error);
      }
    };

    fetchTournaments();
  }, []);

  // Function to format ZonedDateTime
  const formatDateTime = (dateTime) => {
    // Assuming the dateTime is in ISO 8601 format, you could parse and format it as needed
    // Example using toLocaleString, you might want to adjust based on your locale and formatting preferences
    return new Date(dateTime).toLocaleString();
  };

  return (
    <div>
      <h2>Ongoing Tournaments</h2>
      <InputGroup className="mb-3">
        <FormControl
          placeholder="Search"
          aria-label="Search"
          value={searchTerm}
          onChange={(e) => setSearchTerm(e.target.value)}
        />
        <Button variant="outline-secondary" id="button-search">
          Search
        </Button>
      </InputGroup>
      <ListGroup>
        {tournaments.map((tournament) => (
          <TournamentListItem
            key={tournament.id}
            id={tournament.id}
            nameTournament={tournament.name}
            subscriptionDeadline={formatDateTime(tournament.deadline)}
          />
        ))}
      </ListGroup>
    </div>
  );
};

export default TournamentsListOngoing;
