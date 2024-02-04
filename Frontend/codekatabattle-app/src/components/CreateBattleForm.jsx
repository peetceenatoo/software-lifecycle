import React, { useState } from 'react';
import DatePicker from 'react-datepicker';
import 'react-datepicker/dist/react-datepicker.css';
import api from '../utilities/api';
import { Card, Form, Button, Container, InputGroup } from 'react-bootstrap';


const CreateBattleForm = ({ tournamentId }) => {
    const [battleName, setBattleName] = useState('');
    const [subscriptionDeadline, setSubscriptionDeadline] = useState(new Date());
    const [endBattleDeadline, setEndBattleDeadline] = useState(new Date());
    const [minGroupSize, setMinGroupSize] = useState('');
    const [maxGroupSize, setMaxGroupSize] = useState('');
    const [codingLanguage, setCodingLanguage] = useState('');
    const [manualScoring, setManualScoring] = useState(false);
    const [file, setFile] = useState(null);
  
    const handleSubmit = async (event) => {
      event.preventDefault();
      // Construct form data
      const formData = new FormData();
      formData.append('battleName', battleName);
      formData.append('subscriptionDeadline', subscriptionDeadline.toISOString());
      formData.append('endBattleDeadline', endBattleDeadline.toISOString());
      formData.append('minGroupSize', minGroupSize);
      formData.append('maxGroupSize', maxGroupSize);
      formData.append('codingLanguage', codingLanguage);
      formData.append('manualScoring', manualScoring);
      if (file) {
        formData.append('file', file);
      }
  
      try {
        // Send a POST request
        const response = await axios.post(`/${tournamentId}/createBattle`, formData, {
          headers: {
            'Content-Type': 'multipart/form-data',
          },
        });
        console.log(response.data);
        // Handle the response here (e.g., show a success message, redirect, etc.)
      } catch (error) {
        console.error('Error submitting form', error);
        // Handle the error here (e.g., show an error message)
      }
    };
  
    const handleFileChange = (event) => {
      setFile(event.target.files[0]);
    };

    return (
        <Container>
          <Card>
            <Card.Body>
            <Card.Title>Create a Battle</Card.Title>
              <Form onSubmit={handleSubmit}>
                <Form.Group className="mb-3 d-flex fle-row">
                  <Form.Control
                    type="text"
                    placeholder="Enter battle name"
                    value={battleName}
                    onChange={(e) => setBattleName(e.target.value)}
                  />
                </Form.Group>
      
                <Form.Group className="mb-3">
                  <Form.Label className='m-2'>Subscription Deadline</Form.Label>
                  <DatePicker
                    selected={subscriptionDeadline}
                    onChange={(date) => setSubscriptionDeadline(date)}
                    className="form-control"
                    placeholderText="Select date"
                  />
                </Form.Group>
      
                <Form.Group className="mb-3">
                  <Form.Label className='m-2'>End Battle Deadline</Form.Label>
                  <DatePicker
                    selected={endBattleDeadline}
                    onChange={(date) => setEndBattleDeadline(date)}
                    className="form-control"
                    placeholderText="Select date"
                  />
                </Form.Group>
      
                <InputGroup className="mb-3">
                  <Form.Control
                    type="number"
                    placeholder="Min group size"
                    value={minGroupSize}
                    onChange={(e) => setMinGroupSize(e.target.value)}
                  />
                  <InputGroup.Text> - </InputGroup.Text>
                  <Form.Control
                    type="number"
                    placeholder="Max group size"
                    value={maxGroupSize}
                    onChange={(e) => setMaxGroupSize(e.target.value)}
                  />
                </InputGroup>
      
                <Form.Group controlId="formFile" className="mb-3">
                  <Form.Label>Upload Codekata</Form.Label>
                  <Form.Control type="file" onChange={handleFileChange} />
                </Form.Group>
      
                <Form.Group className="mb-3">
                  <Form.Label>Coding Language</Form.Label>
                  <Form.Select
                    value={codingLanguage}
                    onChange={(e) => setCodingLanguage(e.target.value)}
                  >
                    <option>Select language</option>
                    <option value="python">Python</option>
                    <option value="javascript">JavaScript</option>
                    <option value="java">Java</option>
                    {/* ... other options ... */}
                  </Form.Select>
                </Form.Group>
      
                <Form.Group className="mb-3">
                  <Form.Check
                    type="checkbox"
                    label="Manual Scoring"
                    checked={manualScoring}
                    onChange={(e) => setManualScoring(e.target.checked)}
                  />
                </Form.Group>
      
                <Button variant="primary" type="submit">
                  Create
                </Button>
              </Form>
            </Card.Body>
          </Card>
        </Container>
      );
      
};

export default CreateBattleForm;
