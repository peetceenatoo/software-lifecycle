import React from 'react';
import { Button, ListGroup } from 'react-bootstrap';

const TournamentListItemOngoing = ({ id, nameTournament, subscriptionDeadline, role }) => {
  return (
    <ListGroup.Item className="d-flex justify-content-between align-items-start">
      <div className="ms-2 me-auto">
        <div className="fw-bold">#{id} - {nameTournament}</div>
        Subscription Deadline: {subscriptionDeadline}
      </div>
      <Button className="me-2" variant="info">Info</Button>
      {/* Only show Join button if user is a student */}
      {role === 'ROLE_STUDENT' && <Button variant="info">Join</Button>}
    </ListGroup.Item>
  );
};

export default TournamentListItemOngoing;
