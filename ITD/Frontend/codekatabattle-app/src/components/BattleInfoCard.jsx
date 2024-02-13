import React, { useState, useEffect } from 'react';
import axios from 'axios';
import { Card, Button, Form, DropdownButton, Dropdown } from 'react-bootstrap';
import api from '../utilities/api';
import { onclickButtonNotYetImplemented } from '../utilities/alerting';

const BattleInfoCard = ({ battle }) => {
  if (!battle) { // Check if battle is defined
    console.log('Battle is undefined');
    return null; // Don't render anything if battle is undefined
  }

  const handleClickRepo = () => {
    // Use window.open(url, target) to open a new tab

      window.open('https://github.com/'+ battle.repositoryLink, '_blank', 'noopener,noreferrer');
    
  };

  console.log("BattleInfoCard");
  console.log(battle)

  const formatDateTime = (date) => {
    //const dateTime = new Date(date);
    //return dateTime.toISOString();
    return date
  }

  return (
    <Card className="mb-2">
      <Card.Header>Info</Card.Header>
      <Card.Body>
        <Card.Title>{battle.name}</Card.Title>
        <Form>
          <Form.Group controlId="subscriptionDeadline">
            <Form.Label className="m-2" >Subscription Deadline</Form.Label>
            <Form.Control
              type="text"
              placeholder={formatDateTime(battle.subscriptionDeadline)}
              readOnly
            />
          </Form.Group>
          <Form.Group controlId="submissionDeadline">
            <Form.Label className="m-2" >End Battle Deadline</Form.Label>
            <Form.Control
              type="text"
              placeholder={formatDateTime(battle.submissionDeadline)}
              readOnly
            />
          </Form.Group>
          <Form.Group controlId="groupSize">
            <Form.Label className="m-2">
              MinStudents: {battle.minStudentsInGroup}, MaxStudents: {battle.maxStudentsInGroup}
            </Form.Label>
          </Form.Group>
          <Button variant="primary" onClick={handleClickRepo} disabled={battle.state != "ONGOING"}>Show codekata</Button>
          <Form.Group controlId="programmingLanguage">
            <Button className="m-2" disabled={true}>
              {battle.programmingLanguage}
            </Button>
          </Form.Group>
          <Form.Group  controlId="manualScoring">
            <Form.Check className="m-2" 
              type="checkbox" 
              label="Manual Scoring" 
              checked={battle.manualScoringRequired}
              readOnly
            />
          </Form.Group>
        </Form>
        <Card.Text>Status: {battle.state}</Card.Text>
      </Card.Body>
    </Card>
  );
};

export default BattleInfoCard;
