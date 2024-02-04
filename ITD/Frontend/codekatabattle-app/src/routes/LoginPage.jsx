import React from 'react';
import { Container, Row, Col, Form, Button, InputGroup } from 'react-bootstrap';
import axios from 'axios';
import { useNavigate, Link } from 'react-router-dom';


function LoginPage() {

  const navigate = useNavigate();

  const handleLoginSubmit = (event) => {
    console.log(event.target.elements.formBasicEmail.value);
    console.log(event.target.elements.formBasicPassword.value);
    event.preventDefault();
    const credentials = {
      email: event.target.elements.formBasicEmail.value,
      password: event.target.elements.formBasicPassword.value,
    };
    login(credentials);
  };

  const login = (credentials) => {
    axios.post('https://localhost:8443/api/users/login', credentials)
      .then(response => {
        console.log('Login Success:', response.data);
        const token = response.data.token;
        localStorage.setItem('token', token);
        localStorage.setItem('role', response.data.role);
        localStorage.setItem('username', response.data.username);
        navigate('/');
      })

      .catch(error => {
        console.error('Login Error:', error.response ? error.response.data : error.message);
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
          <h1>Log In</h1>
          <Form onSubmit={handleLoginSubmit}>
            <Form.Group className="mb-3" controlId="formBasicEmail">
              <Form.Label>Email / Username</Form.Label>
              <Form.Control type="email" placeholder="Enter your email or your username" required />
            </Form.Group>

            <Form.Group className="mb-3" controlId="formBasicPassword">
              <Form.Label>Password</Form.Label>
                <Form.Control type="password" placeholder="Password" required />
            </Form.Group>

            <fieldset>
              <Form.Group as={Row} className="mb-3">
                <Col sm={7}>
                  <Form.Check
                    type="checkbox"
                    label="Remember me"
                    name="formHorizontalRadios"
                    id="formHorizontalRadios1"
                  />
                </Col>
                <Col sm={5}>
                  <a href="#forgotpassword">Forgot password?</a>
                </Col>

              </Form.Group>

              <Form.Group as={Row} className="mb-3">
                
              </Form.Group>

            </fieldset>


            <Button variant="primary" type="submit">
              Sign in
            </Button>
          </Form>
          <div className="mt-3">
            Don't have an account? <Link to="/signup">Sign up</Link>
          </div>
        </Col>

        {/* Right spacing */}
        <Col md={1}></Col>

        {/* Right column for the information panel */}
        <Col md={6} className="bg-primary text-white d-flex align-items-center justify-content-center px-5 rounded-end">
          <div>
            <h1 className="display-1 font-weight-bold">CodeKataBattle</h1>
            <h1 className="display-5 font-weight-bold">Begin Your Journey to Coding Excellence</h1>
            <h1 className="display-5 ">Ready for Your Next Kata?</h1>
          </div>
        </Col>
      </Row>
    </Container>
  );
}

export default LoginPage;
