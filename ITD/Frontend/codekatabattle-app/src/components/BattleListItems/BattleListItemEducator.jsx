import React from 'react';
import { Button, ListGroup } from 'react-bootstrap';
import { useNavigate } from 'react-router-dom';

const BattleListItemEducator = ({ id, nameBattle, status }) => {
    const navigate = useNavigate();

    const handleInfoClick = () => {
      navigate(`/battle/${id}`);
    };

  return (
    <ListGroup.Item className="d-flex justify-content-between align-items-start">
      <div className="ms-2 me-auto">
        <div className="fw-bold">#{id} - {nameBattle}</div>
      Status: {status}
      </div>
      <Button className="me-2" onClick={handleInfoClick}>Info</Button>
    </ListGroup.Item>
  );
};

export default BattleListItemEducator;
