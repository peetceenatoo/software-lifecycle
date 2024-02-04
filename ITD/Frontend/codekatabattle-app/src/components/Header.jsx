import React from 'react';
import { Navbar, Nav, Form, FormControl, Button, Container, Row, Col } from 'react-bootstrap';
import { useNavigate } from 'react-router-dom';
import {onclickButtonNotYetImplemented} from '../utilities/alerting'

const Header = () => {
  const navigate = useNavigate();

  const goToHome = () => {
    navigate('/');
  }

  return (
    <Navbar bg="primary" variant="dark" expand="lg" className="px-0">
      <Container fluid className="px-0">
        <Row className="w-100 gx-0">
          <Col md={1} lg={1} /> {/* Spazio vuoto a sinistra */}
          <Col md={10} lg={10}>
            <Row>
              <Col xs={12} lg={3}>
                <Navbar.Brand>CodeKataBattle</Navbar.Brand>
              </Col>
              <Col xs={12} lg={5} className="d-flex align-items-center justify-content-center">
                <Form className='d-flex flex-row justify-content-center' >
                  <Col xs={12} lg={7} className="d-flex align-items-center justify-content-center">
                    <FormControl type="text" placeholder="Search" style={{ width: 'auto' }} />
                  </Col>
                  <Col xs={12} lg={6} className="d-flex align-items-center justify-content-center">
                  <Button variant="outline-light" onClick={onclickButtonNotYetImplemented}>Search</Button>
                  </Col>
                </Form>
              </Col>
              <Col xs={12} lg={4} className="d-flex justify-content-end">
                <Nav>
                  <Nav.Link>Welcome {localStorage.getItem('username')}</Nav.Link>
                  <Nav.Link onClick={goToHome}>Home</Nav.Link>
                  <Nav.Link onClick={onclickButtonNotYetImplemented}>Profile</Nav.Link>
                </Nav>
              </Col>
            </Row>
          </Col>
          <Col md={1} lg={1} /> {/* Spazio vuoto a destra */}
        </Row>
      </Container>
    </Navbar>
  );
};

export default Header;
