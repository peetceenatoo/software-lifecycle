import React from "react";
import { Container, Row, Col } from "react-bootstrap";
import Header from "../components/Header";
import api from "../utilities/api";

import { useParams } from "react-router-dom";
import { useEffect, useState } from "react";
import BattleList from "../components/BattleList";
import '../styles/TournamentPage.css';
import { RankingTournament } from "../components/RankingTournament";

function TournamentPageViewStudent() {
    const { tournamentId } = useParams();
    const [tournament, setTournament] = useState({});

    useEffect(() => {
      const fetchTournament = async () => {
        try {
          const response = await api.get(`/tournaments/${tournamentId}`);
          setTournament(response.data);
        } catch (error) {
          console.error('Error fetching tournament info', error);
        }
      };
  
      if (tournamentId) {
        fetchTournament();
      }
    }, [tournamentId]);
  

    return (
        <Container fluid className="px-0">
          <Header className="header-margin"/>
          <Row className="min-vh-100">
            <Col md={1}></Col>
            <Col md={3}>
              <div className="ms-2 me-auto">
                {/* Apply the heading class here */}
                <div className="heading">#{tournament.id} - {tournament.name}</div>
              </div>
            </Col>
            <Col md={4}></Col>
            <Col md={3}>
              <div className="ms-2 me-auto">
                {/* Apply the status-ongoing class here */}
                <div className="status">Status: {tournament.state}</div>
              </div>
            </Col>
            <Col md={1}></Col>
          </Row>
          {Object.keys(tournament).length > 0 && ( // Check if tournament is not empty
            <Row className="min-vh-100">
              <Col md={1}></Col>
              <Col md={3} className="my-auto">
                <BattleList
                  type="Ongoing"
                  name="Available Battles"
                  tournamentId={tournamentId}
                />
              </Col>
              <Col md={1}></Col>
              <Col md={3} className="my-auto">
                <BattleList
                  type="Enrolled"
                  name="Enrolled Battles"
                  tournamentId={tournamentId}
                />
              </Col>
              <Col md={1}></Col>
              <Col md={3} className="my-auto">
                <RankingTournament
                  tournamentId={tournamentId}
                />
              </Col>
            </Row>
          )}
        </Container>
      );
}

export default TournamentPageViewStudent;