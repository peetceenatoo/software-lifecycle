import React from 'react';
import { Button, ListGroup } from 'react-bootstrap';

const BattleListItemInfo = ({ id, nameBattle, score }) => {
  return (
    <ListGroup.Item className="d-flex justify-content-between align-items-start">
      <div className="ms-2 me-auto">
        <div className="fw-bold">#{id} - {nameBattle}</div>
      {score === null ? "Not yet scored" : `Score: ${score}`} 
      </div>
      <Button className="me-2" >Info</Button>
    </ListGroup.Item>
  );
};

export default BattleListItemInfo;
