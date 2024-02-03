import React from 'react';
import { Button, ListGroup } from 'react-bootstrap';

const TournamentListItemMananaged = ({ id, nameTournament, status }) => {
  return (
    <ListGroup.Item className="d-flex justify-content-between align-items-start">
      <div className="ms-2 me-auto">
        <div className="fw-bold">#{id} - {nameTournament}</div>
        Status: {status}
      </div>
      <Button className="me-2">End</Button>
      <Button>Info</Button>
    </ListGroup.Item>
  );
};

export default TournamentListItemMananaged;
