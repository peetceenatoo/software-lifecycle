import React from 'react';
import { Button, ListGroup } from 'react-bootstrap';
import axios from 'axios';
import api from '../../utilities/api';


const TournamentListItemOngoing = ({ id, nameTournament, subscriptionDeadline, role, status }) => {

  const handleEnroll = async () => {
    api.post(`/tournaments/${id}/enroll`, {})
      .then((response) => {
        console.log(response);
        console.log('Enrolled in tournament', response.data);
      })
      .catch((error) => {
        console.error('Error enrolling in tournament', error);
      });
  }

  const isDeadlinePassed = new Date(subscriptionDeadline) < new Date();

  return (
    <ListGroup.Item className="d-flex justify-content-between align-items-start">
      <div className="ms-2 me-auto">
        <div className="fw-bold">#{id} - {nameTournament}</div>
        <div style={{ color: isDeadlinePassed ? 'red' : 'green' }}>
          {isDeadlinePassed ? status : subscriptionDeadline}
        </div>
      </div>
      <Button className="me-2" >Info</Button>
      {/* Only show Join button if user is a student */}
      {role === 'ROLE_STUDENT' && <Button  variant={isDeadlinePassed ? 'secondary' : 'primary'} onClick={handleEnroll} disabled={isDeadlinePassed}>Join</Button>}
    </ListGroup.Item>
  );
};

export default TournamentListItemOngoing;
