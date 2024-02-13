import React from 'react';
import { Container, Row, Col, Form, Button, InputGroup } from 'react-bootstrap';
import axios from 'axios';
import { useNavigate, Link } from 'react-router-dom';

function SignupPage() {
  const navigate = useNavigate(); // Correct use of useNavigate hook

  const handleSignupSubmit = (event) => {
    event.preventDefault(); // Prevent the default form submit action

    if(!event.target.elements.formHorizontalRadios1.checked && !event.target.elements.formHorizontalRadios2.checked){
      alert("Role choice is mandatory")
      return
    }

    const userData = {
      name: event.target.elements.formBasicName.value,
      surname: event.target.elements.formBasicSurname.value,
      username: event.target.elements.formBasicUsername.value,
      email: event.target.elements.formBasicEmail.value,
      password: event.target.elements.formBasicPassword.value,
      linkBio: event.target.elements.formBasicLinkBio.value,
      roleName: event.target.elements.formHorizontalRadios1.checked ? 'ROLE_STUDENT' : 'ROLE_EDUCATOR',
      // Add other form fields as necessary
    };

    signup(userData); // Call the signup function with the user data
  };

  const signup = (userData) => {
    axios.post('https://codekatabattle.it:8443/api/users/signup', userData)
      .then(response => {
        console.log('Signup Success:', response.data); // Log the success response
        alert('Signup successful!'); // Alert the user to the success
        navigate('/login'); // Navigate to the login page on success
      })
      .catch(error => {
        //console.error('Signup Error:', error.response ? error.response.data : error.message); // Log any errors
        console.log('Signup Error:' + (error.response.data.message ? error.response.data.message : error)); // Log any errors (specifically the error message
        alert('Signup Error:' + (error.response.data.message ? error.response.data.message : error)); // Alert the user to any errors
      });
  };

  return (
    <Container fluid className="min-vh-100">
      <Row className="min-vh-100">
        <Col md={1}></Col> {/* Left spacing */}

        <Col md={4} className="my-auto px-4">
          <h2>Create an account</h2>
          <p>Start your journey!</p>
          <Form onSubmit={handleSignupSubmit}> {/* Form submission handler */}
            <Form.Group className="mb-3" controlId="formBasicName">
              <Form.Label>Name</Form.Label>
              <Form.Control type="text" placeholder="Enter your name" required />
            </Form.Group>

            <Form.Group className="mb-3" controlId="formBasicSurname">
              <Form.Label>Surname</Form.Label>
              <Form.Control type="text" placeholder="Enter your surname" required />
            </Form.Group>

            <Form.Group className="mb-3" controlId="formBasicUsername">
              <Form.Label>Username</Form.Label>
              <Form.Control type="text" placeholder="Enter your username" required />
            </Form.Group>

            <Form.Group className="mb-3" controlId="formBasicLinkBio">
              <Form.Label>Link Bio</Form.Label>
              <Form.Control type="text" placeholder="Enter your LinkBio" required />
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
            Already have an account? <Link to="/login">Log In</Link>
          </div>
        </Col>

        <Col md={1}></Col> {/* Right spacing */}

        <Col md={6} className="bg-primary text-white d-flex align-items-center justify-content-center px-5 rounded-end">
          <div>
            <h1 className="display-1 font-weight-bold">CodeKataBattle</h1>
            <h1 className="display-5 font-weight-bold">Begin Your Journey to Coding Excellence</h1>
            <h1 className="display-5">Ready for Your Next Kata?</h1>
          </div>
        </Col>
      </Row>
    </Container>
  );
}

export default SignupPage;
