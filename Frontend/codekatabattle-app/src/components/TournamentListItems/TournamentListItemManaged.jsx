import React from 'react';
import { Button, ListGroup } from 'react-bootstrap';
import { useNavigate } from 'react-router-dom';

const TournamentListItemManaged = ({ id, nameTournament, status }) => {
  const navigate = useNavigate();

  const handleInfoClick = () => {
    navigate(`/tournament/${id}`);
  };

  return (
    <ListGroup.Item className="d-flex justify-content-between align-items-start">
      <div className="ms-2 me-auto">
        <div className="fw-bold">#{id} - {nameTournament}</div>
        <div>Status: {status}</div>
      </div>
      <Button className="me-2" onClick={handleInfoClick}>Info</Button>
    </ListGroup.Item>
  );
};

export default TournamentListItemManaged;
