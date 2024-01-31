import React from 'react';
import { Container, Row, Col, Form, Button, InputGroup } from 'react-bootstrap';
import axios from 'axios';



function SignupPage() {

  const handleSignupSubmit = (event) => {
    event.preventDefault();
    const userData = {
      name: event.target.elements.formBasicName.value,
      username: event.target.elements.formBasicUsername.value,
      email: event.target.elements.formBasicEmail.value,
      password: event.target.elements.formBasicPassword.value,
      // Include other data as necessary
    };

    signup(userData);
  };

  const signup = (userData) => {
    axios.post('http://localhost:8080/signup', userData)
      .then(response => {
        console.log('Signup Success:', response.data);
        // Handle success here
      })
      .catch(error => {
        console.error('Signup Error:', error.response ? error.response.data : error.message);
        // Handle errors here
      });
  };


  return (
    <Container fluid className="min-vh-100">
      <Row className="min-vh-100">
        {/* Left spacing */}
        <Col md={1}></Col>

        {/* Main form content */}
        <Col md={4} className="my-auto px-4">
          <h2>Create an account</h2>
          <p>Start your journey!</p>
          <Form>
            <Form.Group className="mb-3" controlId="formBasicName">
              <Form.Label>Name</Form.Label>
              <Form.Control type="text" placeholder="Enter your name" required />
            </Form.Group>

            <Form.Group className="mb-3" controlId="formBasicUsername">
              <Form.Label>Username</Form.Label>
              <Form.Control type="text" placeholder="Enter your username" required />
            </Form.Group>

            <Form.Group className="mb-3" controlId="formBasicEmail">
              <Form.Label>Email</Form.Label>
              <InputGroup>
                <Form.Control type="email" placeholder="Enter your email" required />
                <InputGroup.Text id="basic-addon2">@</InputGroup.Text>
              </InputGroup>
            </Form.Group>

            <Form.Group className="mb-3" controlId="formBasicPassword">
              <Form.Label>Password</Form.Label>
              <Form.Control type="password" placeholder="Password" required />
            </Form.Group>

            <fieldset>
              <Form.Group as={Row} className="mb-3">
                <Col sm={10}>
                  <Form.Check
                    type="radio"
                    label="Student"
                    name="formHorizontalRadios"
                    id="formHorizontalRadios1"
                  />
                  <Form.Check
                    type="radio"
                    label="Educator"
                    name="formHorizontalRadios"
                    id="formHorizontalRadios2"
                  />
                </Col>
              </Form.Group>
            </fieldset>

            <Button variant="primary" type="submit">
              Get started
            </Button>
          </Form>
          <div className="mt-3">
            Already have an account? <a href="#login">Log in</a>
          </div>
        </Col>

        {/* Right spacing */}
        <Col md={1}></Col>

        {/* Right column for the information panel */}
        <Col md={6} className="bg-primary text-white d-flex align-items-center justify-content-center px-5 rounded-end">
          <div>
            <h1 class="display-1 font-weight-bold">CodeKataBattle</h1>
            <h1 class="display-5 font-weight-bold">Begin Your Journey to Coding Excellence</h1>
            <h1 class="display-5 ">Ready for Your Next Kata?</h1>
          </div>
        </Col>
      </Row>
    </Container>
  );
}

export default SignupPage;
