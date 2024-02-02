import React from "react";
import { Container, Row, Col, Form, Button, InputGroup } from "react-bootstrap";
import axios from "axios";
import Header from "../components/Header";
import TournamentsListOngoing from "../components/OngoingTournamentsList";
import TournamentListManagedEnrolled from "../components/ManagedEnrolledTournamentsList";


function HomePage() {
    return (
        <Container fluid className="px-0">
            <Header />
            <Row className="min-vh-100">
                <Col md={1}></Col>
                <Col md={4} className="my-auto">
                    <TournamentsListOngoing />
                </Col>
                <Col md={2}></Col>
                <Col md={4} className="my-auto">
                    <TournamentListManagedEnrolled />
                </Col>
                <Col md={1}></Col>
            </Row>
        </Container>
    );
    
    }

export default HomePage;