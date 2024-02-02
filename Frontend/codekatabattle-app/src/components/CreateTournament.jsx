import React, { useState } from 'react';
import { Card, Form, Button, Row, Col, Container, ListGroup } from 'react-bootstrap';
import DatePicker from 'react-datepicker';
import 'react-datepicker/dist/react-datepicker.css';
import Modal from 'react-bootstrap/Modal';
import axios from 'axios'; // Make sure to import axios
import api from '../utilities/api';

const CreateTournament = () => {
    const [show, setShow] = useState(false);
    const [startDate, setStartDate] = useState(new Date());
    const [tournamentName, setTournamentName] = useState(''); // State for tournament name
    const [invitations, setInvitations] = useState([]);
    const [username, setUsername] = useState('');

    const handleClose = () => setShow(false);
    const handleShow = () => {
        setUsername(''); // Clear the username input when opening the modal
        setShow(true);
    };

    const addInvitation = () => {
        if (username && !invitations.includes(username)) {
            setInvitations([...invitations, username]);
            setUsername(''); // Clear input after adding
        }
    };

    const deleteInvitation = (usernameToDelete) => {
        setInvitations(invitations.filter((uname) => uname !== usernameToDelete));
    };

    const handleSubmit = (event) => {
        event.preventDefault();
      
        // Prepare the data to send
        const data = {
          tournamentName: tournamentName,
          registrationDeadline: startDate.toISOString(),
          educatorsInvited: invitations,
        };
      
        api.post('/tournaments/create', data)
          .then(response => {
            console.log('Tournament created successfully:', response.data);
            // You might want to clear the form here or provide some notification to the user
            handleClose(); // Close the modal after submitting
          })
          .catch(error => {
            console.error('Error creating tournament:', error);
            // Handle specific error scenarios here, potentially showing error messages to the user
          });
      };

    return (
        <Container>
            <Modal show={show} onHide={handleClose} backdrop="static" keyboard={false}>
                <Modal.Header closeButton>
                    <Modal.Title>Invite Educators</Modal.Title>
                </Modal.Header>
                <Modal.Body>
                    <Form.Control
                        type="text"
                        placeholder="Enter the username of the educator"
                        name="invitations"
                        value={username}
                        onChange={(e) => setUsername(e.target.value)}
                        onKeyPress={(e) => e.key === 'Enter' && addInvitation()}
                    />
                    <ListGroup className="mt-3">
                        {invitations.map((invite, index) => (
                            <ListGroup.Item key={index}>
                                {invite}
                                <Button
                                    variant="danger"
                                    size="sm"
                                    onClick={() => deleteInvitation(invite)}
                                    style={{ float: 'right' }}
                                >
                                    Delete
                                </Button>
                            </ListGroup.Item>
                        ))}
                    </ListGroup>
                </Modal.Body>
                <Modal.Footer>
                    <Button variant="secondary" onClick={handleClose}>
                        Close
                    </Button>
                    <Button variant="primary" onClick={addInvitation}>
                        Add
                    </Button>
                </Modal.Footer>
            </Modal>
        <Card className="my-3">
            <Card.Body>
                <Card.Title>Create a Tournament</Card.Title>
                <Form onSubmit={handleSubmit}>
                    <Form.Group as={Row} className="mb-3">
                        <Form.Label column sm="3">
                            Subscription Deadline
                        </Form.Label>
                        <Col sm="9">
                            <DatePicker
                                selected={startDate}
                                onChange={(date) => setStartDate(date)}
                                showTimeSelect
                                timeFormat="HH:mm"
                                timeIntervals={15}
                                timeCaption="time"
                                dateFormat="MMMM d, yyyy h:mm aa"
                                minDate={new Date()}
                                className="form-control"
                            />
                        </Col>
                    </Form.Group>
                    <Form.Group as={Row} className="mb-3">
                            <Form.Label column sm="3">
                                Tournament Name
                            </Form.Label>
                            <Col sm="9">
                                <Form.Control
                                    type="text"
                                    placeholder="Enter tournament name"
                                    name="tournamentName"
                                    value={tournamentName} // Use the state for value
                                    onChange={(e) => setTournamentName(e.target.value)} // Update the state on change
                                />
                            </Col>
                        </Form.Group>
                    <Row>
                        <Col sm={{ span: 9, offset: 3 }}>
                            <Button variant="primary" type="button">
                                Add a Badge
                            </Button>
                            {' '}
                            <Button variant="secondary" type="button" onClick={handleShow}>
                                Invite an educator
                            </Button>
                        </Col>
                    </Row>
                    <Row className="mt-3">
                        <Col sm={{ span: 9, offset: 3 }}>
                            <Button variant="success" type="submit">
                                Create
                            </Button>
                        </Col>
                    </Row>
                </Form>
            </Card.Body>
        </Card>
        </Container>
    );
};

export default CreateTournament;
