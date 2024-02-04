import React, { Component } from 'react';
import { useParams } from 'react-router-dom';
import axios from 'axios';
import { Button, Card, ListGroup } from 'react-bootstrap';
import { useNavigate } from 'react-router-dom';
import BattlePageViewStudent from './BattlePageViewStudent';
import BattlePageViewEducator from './BattlePageViewEducator';

function BattlePage() {
    const role = localStorage.getItem('role');
    const { tournamentId } = useParams();
    const { battleId } = useParams();

    return (
        <>
          {role === 'ROLE_STUDENT' && <BattlePageViewStudent tournamentId={tournamentId} battleId = {battleId}/>}
          {role === 'ROLE_EDUCATOR' && <BattlePageViewEducator tournamentId={tournamentId} battleId = {battleId}/>}
        </>
      );
}

export default BattlePage;