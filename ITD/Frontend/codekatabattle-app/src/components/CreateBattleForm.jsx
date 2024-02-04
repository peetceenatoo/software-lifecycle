import React, { useState } from 'react';
import DatePicker from 'react-datepicker';
import 'react-datepicker/dist/react-datepicker.css';
import api from '../utilities/api';
import { Card, Form, Button, Container, InputGroup, Alert } from 'react-bootstrap';
import { useNavigate } from 'react-router-dom';


const CreateBattleForm = ({ tournamentId }) => {
    const [battleName, setBattleName] = useState('');
    const [subscriptionDeadline, setSubscriptionDeadline] = useState(new Date());
    const [endBattleDeadline, setEndBattleDeadline] = useState(new Date());
    const [minGroupSize, setMinGroupSize] = useState('');
    const [maxGroupSize, setMaxGroupSize] = useState('');
    const [codingLanguage, setCodingLanguage] = useState('');
    const [manualScoring, setManualScoring] = useState(false);
    const [fileProject, setFileProject] = useState(null);
    const [fileTests, setFileTests] = useState(null);

    
    const navigate = useNavigate();
  
    const handleSubmit = async (event) => {
      event.preventDefault();
      // Construct form data
      const formData = new FormData();

      let jsonBattle = {
        name: battleName,
        subscriptionDeadline: subscriptionDeadline.toISOString(),
        submissionDeadline: endBattleDeadline.toISOString(),
        minStudentsInGroup: minGroupSize,
        maxStudentsInGroup: maxGroupSize,
        programmingLanguage: codingLanguage,
        manualScoring: manualScoring
      }

      formData.append(
        'battle', 
        new Blob([JSON.stringify(jsonBattle)], { type: 'application/json' })
      );

      if (fileProject) {
        formData.append('codeZip', fileProject);
      }
      if (fileTests) {
        formData.append('testZip', fileTests);
      }
  
      

      try {
        // Send a POST request
        const response = await api.post(`/tournaments/${tournamentId}/createBattle`, formData, {
          headers: {
            'Content-Type': 'multipart/form-data',
          },
        });
        console.log(response.data);
        alert('Battle created');
        navigate(0);
        // Handle the response here (e.g., show a success message, redirect, etc.)
      } catch (error) {
        console.error(error.response.data);
        alert('Error creating battle: ' + error.response.data.message)
        // Handle the error here (e.g., show an error message)
      }
    };
  
    const handleFileChangeProject = (event) => {
      setFileProject(event.target.files[0]);
    };

    const handleFileChangeTests = (event) => {
      setFileTests(event.target.files[0]);
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
                                showTimeSelect
                                timeFormat="HH:mm"
                                timeIntervals={15}
                                timeCaption="time"
                                dateFormat="MMMM d, yyyy h:mm aa"
                                minDate={new Date()}
                                placeholderText="Select date"
                                className="form-control"
                            />

                </Form.Group>
      
                <Form.Group className="mb-3">
                  <Form.Label className='m-2'>Submission Battle Deadline</Form.Label>
                  <DatePicker
                    selected={endBattleDeadline}
                    onChange={(date) => setEndBattleDeadline(date)}
                    showTimeSelect
                    timeFormat="HH:mm"
                    timeIntervals={15}
                    timeCaption="time"
                    dateFormat="MMMM d, yyyy h:mm aa"
                    minDate={new Date()}
                    placeholderText="Select date"
                    className="form-control"
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
                  <Form.Label>Upload CodeKata Project</Form.Label>
                  <Form.Control type="file" onChange={handleFileChangeProject} />
                </Form.Group>

                <Form.Group controlId="formFile" className="mb-3">
                  <Form.Label>Upload CodeKata Tests</Form.Label>
                  <Form.Control type="file" onChange={handleFileChangeTests} />
                </Form.Group>
      
                <Form.Group className="mb-3">
                  <Form.Label>Coding Language</Form.Label>
                  <Form.Select
                    value={codingLanguage}
                    onChange={(e) => setCodingLanguage(e.target.value)}
                  >
                    <option>Select language</option>
                    <option value="JAVA">Java</option>
                    {/* ... other options ... */}
                  </Form.Select>
                </Form.Group>

                <Alert variant="warning">
                  <Alert.Heading>Correctness of ZIP files</Alert.Heading>
                  <p>
                    They MUST be Maven Projects, including the pom.xml file, the src folders, and maven wrapper.
                    Remember to check .mvn folder is included.
                  </p>
                </Alert>


                <Form.Group className="mb-3">
                  <Form.Check
                    type="checkbox"
                    label="Manual Scoring"
                  />
                </Form.Group>
      
                <Form.Group className="mb-3">
                  <Form.Check
                    type="checkbox"
                    label="Security Scoring"
                    disabled
                  />
                </Form.Group>

                <Form.Group className="mb-3">
                  <Form.Check
                    type="checkbox"
                    label="Reliability Scoring"
                    disabled
                  />
                </Form.Group>

                <Form.Group className="mb-3">
                  <Form.Check
                    type="checkbox"
                    label="Maintanaibility Scoring"
                    disabled
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
