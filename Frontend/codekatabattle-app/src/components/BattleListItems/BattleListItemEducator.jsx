import React from 'react';
import { Button, ListGroup } from 'react-bootstrap';

const BattleListItemEducator = ({ id, nameBattle, status }) => {
  return (
    <ListGroup.Item className="d-flex justify-content-between align-items-start">
      <div className="ms-2 me-auto">
        <div className="fw-bold">#{id} - {nameBattle}</div>
      Status: {status}
      </div>
      <Button className="me-2" >Info</Button>
    </ListGroup.Item>
  );
};

export default BattleListItemEducator;
