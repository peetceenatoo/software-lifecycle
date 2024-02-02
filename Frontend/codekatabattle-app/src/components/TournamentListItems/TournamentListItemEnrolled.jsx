import React from 'react';
import { Button, ListGroup } from 'react-bootstrap';

const TournamentListItemEnrollled = ({ id, nameTournament, ranking }) => {
  return (
    <ListGroup.Item className="d-flex justify-content-between align-items-start">
      <div className="ms-2 me-auto">
        <div className="fw-bold">#{id} - {nameTournament}</div>
        Current Ranking: {ranking}
      </div>
      <Button className="me-2" variant="info">Info</Button>
    </ListGroup.Item>
  );
};

export default TournamentListItemEnrollled;
