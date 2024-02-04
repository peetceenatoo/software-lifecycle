import React, { useState, useEffect } from 'react';
import axios from 'axios';
import { ListGroup, Button, Container, Badge } from 'react-bootstrap';
import api from '../utilities/api';
import { onclickButtonNotYetImplemented } from '../utilities/alerting'; // Replace './alerting' with the path to the file 

const CodeSubmissions = ({ battleId }) => {
  const [submissions, setSubmissions] = useState([]);

  useEffect(() => {
    const fetchSubmissions = async () => {
        try {
          const response = await api.get(`/battles/${battleId}/submissions`);
          setSubmissions(response.data); // Check if response.data is an array
          console.log(response.data);
        } catch (error) {
          console.error('Error fetching submissions:', error);
          setSubmissions([]); // Set submissions to an empty array in case of error
        }
      };

    fetchSubmissions();
  }, [battleId]);

  return (
    <Container>
    <h3>Submissions</h3>
    <ListGroup>
      {submissions.map((submission, index) => (
        <ListGroup.Item key={index} className="d-flex justify-content-between align-items-center">
          <div>GROUP: #{submission.groupId}</div> {/* Assuming repositoryUrl contains the username/repository format */}
          <div>
           Score: <Badge bg="primary">{submission.automaticScore}</Badge>
          </div>
          <Button variant="primary" onClick={onclickButtonNotYetImplemented}>
            Show
          </Button>
        </ListGroup.Item>
      ))}
    </ListGroup>
    </Container>
  );
};

export default CodeSubmissions;
