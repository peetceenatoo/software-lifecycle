import React from 'react';
import { Button, ListGroup } from 'react-bootstrap';

const TournamentListItem = ({ id, nameTournament, subscriptionDeadline, role }) => {
  return (
    <ListGroup.Item className="d-flex justify-content-between align-items-start">
      <div className="ms-2 me-auto">
        <div className="fw-bold">#{id} - {nameTournament}</div>
        Subscription Deadline: {subscriptionDeadline}
      </div>
      <Button className="me-2" variant="info">Info</Button>
      <Button variant="info">Join</Button>
    </ListGroup.Item>
  );
};

export default TournamentListItem;
