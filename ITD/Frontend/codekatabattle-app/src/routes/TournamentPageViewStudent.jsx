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
        <Container fluid className="px-0 min-vh-100">
          <Header className="header-margin"/>
          <Row>
            <Col md={1}></Col>
            <Col md={3}>
              <div className="ms-2 me-auto">
                {/* Apply the heading class here */}
                <h1 className="heading">Tournament #{tournament.id} - {tournament.name}</h1>

              </div>
            </Col>
            <Col md={4}></Col>
            <Col md={3}>
              <div className="ms-2 me-auto">
                {/* Apply the status-ongoing class here */}
                <h3>Status: {tournament.state}</h3>
              </div>
            </Col>
            <Col md={1}></Col>
          </Row>
          {Object.keys(tournament).length > 0 && ( // Check if tournament is not empty
            <Row>
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
              <Col md={2} className="my-auto">
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