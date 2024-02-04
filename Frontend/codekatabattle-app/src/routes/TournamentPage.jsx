import React, { Component } from 'react';
import { useParams } from 'react-router-dom';
import axios from 'axios';
import { Button, Card, ListGroup } from 'react-bootstrap';
import { useNavigate } from 'react-router-dom';
import TournamentPageViewStudent from './TournamentPageViewStudent';
import TournamentPageViewEducator from './TournamentPageViewEducator';

function TournamentPage() {
    const role = localStorage.getItem('role');
    const { tournamentId } = useParams();

    return (
        <>
          {role === 'ROLE_STUDENT' && <TournamentPageViewStudent tournamentId={tournamentId} />}
          {role === 'ROLE_EDUCATOR' && <TournamentPageViewEducator tournamentId={tournamentId} />}
        </>
      );
}

export default TournamentPage;