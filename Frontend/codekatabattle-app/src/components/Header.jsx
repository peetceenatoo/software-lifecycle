import React from 'react';
import { Navbar, Nav, Form, FormControl, Button, Container, Row, Col } from 'react-bootstrap';

const Header = () => {
  return (
    <Navbar bg="primary" variant="dark" expand="lg" className="px-0">
      <Container fluid className="px-0">
        <Row className="w-100 gx-0">
          <Col md={1} lg={1} /> {/* Spazio vuoto a sinistra */}
          <Col md={10} lg={10}>
            <Row>
              <Col xs={12} lg={3}>
                <Navbar.Brand href="#home">CodeKataBattle</Navbar.Brand>
              </Col>
              <Col xs={12} lg={6} className="d-flex align-items-center justify-content-center">
                <Form className='d-flex flex-row justify-content-center' >
                  <Col xs={12} lg={7} className="d-flex align-items-center justify-content-center">
                    <FormControl type="text" placeholder="Search" style={{ width: 'auto' }} />
                  </Col>
                  <Col xs={12} lg={6} className="d-flex align-items-center justify-content-center">
                  <Button variant="outline-light">Search</Button>
                  </Col>
                </Form>
              </Col>
              <Col xs={12} lg={3} className="d-flex justify-content-end">
                <Nav>
                  <Nav.Link href="#welcome">Welcome Luciano</Nav.Link>
                  <Nav.Link href="#home">Home</Nav.Link>
                  <Nav.Link href="#profile">Profile</Nav.Link>
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
