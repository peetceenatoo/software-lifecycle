import React, { useState } from 'react';
import { Button, Container, ListGroup, Modal, Form } from 'react-bootstrap';
import axios from 'axios';
import api from '../../utilities/api';

// TODO: check the numbers of invitations




const BattleListItemOngoing = ({ battleId,  battleState, nameBattle, subscriptionDeadline, role, status }) => {
  const [show, setShow] = useState(false);
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

  const handleEnroll = async () => {

    const data = {
      educatorsInvited: invitations,
    };


    api.post(`/tournaments/${battleId}/enroll`, data)
      .then((response) => {
        console.log(response);
        console.log('Enrolled in tournament', response.data);
      })
      .catch((error) => {
        console.error('Error enrolling in tournament', error);
      });
  }

  const isDeadlinePassed = new Date(subscriptionDeadline) < new Date();

  const formatDateTime = (dateTime) => {
    return new Date(dateTime).toLocaleString();
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
                        placeholder="Enter the username of the student"
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
    <ListGroup.Item className="d-flex justify-content-between align-items-start">
      <div className="ms-2 me-auto">
        <div className="fw-bold">#{battleId} - {nameBattle}</div>
        <div style={{ color: isDeadlinePassed ? 'red' : 'green' }}>
        {battleState } - {isDeadlinePassed ? status : formatDateTime(subscriptionDeadline)}
        </div>
      </div>
      <Button className="me-2" >Info</Button>
      {/* Only show Join button if user is a student */}
      {role === 'ROLE_STUDENT' && <Button  variant={isDeadlinePassed ? 'secondary' : 'primary'} onClick={handleEnroll} disabled={isDeadlinePassed}>Join</Button>}
    </ListGroup.Item>
    </Container>
  );
};

export default BattleListItemOngoing;
