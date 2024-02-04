import React from 'react';
import { Button, ListGroup } from 'react-bootstrap';
import axios from 'axios';
import api from '../../utilities/api';
import { useNavigate } from 'react-router-dom';

const TournamentListItemOngoing = ({ id, nameTournament, subscriptionDeadline, role, status }) => {

  const navigate = useNavigate();

  const handleInfoClick = () => {
    navigate(`/tournament/${id}`);
  };

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

  const formatDateTime = (dateTime) => {
    return new Date(dateTime).toLocaleString();
  };

  const isDeadlinePassed = new Date(subscriptionDeadline) < new Date();

  return (
    <ListGroup.Item className="d-flex justify-content-between align-items-start">
      <div className="ms-2 me-auto">
        <div className="fw-bold">#{id} - {nameTournament}</div>
        <div style={{ color: isDeadlinePassed ? 'red' : 'green' }}>
          {isDeadlinePassed ? status : formatDateTime(subscriptionDeadline)}
        </div>
      </div>
      <Button className="me-2" onClick={handleInfoClick}>Info</Button>
      {/* Only show Join button if user is a student */}
      {role === 'ROLE_STUDENT' && <Button  variant={isDeadlinePassed ? 'secondary' : 'primary'} onClick={handleEnroll} disabled={isDeadlinePassed}>Join</Button>}
    </ListGroup.Item>
  );
};

export default TournamentListItemOngoing;
