import React from "react";
import { Container, Row, Col, Button } from "react-bootstrap";
import Header from "../components/Header";
import api from "../utilities/api";

import { useParams } from "react-router-dom";
import { useEffect, useState } from "react";
import BattleList from "../components/BattleList";
import '../styles/TournamentPage.css';
import CreateBattleForm from "../components/CreateBattleForm";
import { RankingTournament } from "../components/RankingTournament";


function TournamentPageViewEducator() {
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
          <Header/>
          <Row style={{ padding: '20px' }}>
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
                <h1 className="status">Status: {tournament.state}</h1>
              </div>
            </Col>
            <Col md={1}></Col>
          </Row>
          <Row style={{ padding: '20px' }}>
            <Col md={1}></Col>
            <Col md={4} className="my-auto d-flex flex-row">
              <h3 className="m-2">Educators Invited</h3>
              <Button >show</Button>
            </Col>
          </Row>
          {Object.keys(tournament).length > 0 && ( // Check if tournament is not empty
          <Row style={{ padding: '20px' }}>
          <Col md={1}></Col>
          <Col md={4} className="my-auto">
            <CreateBattleForm tournamentId={tournamentId} />
          </Col>
              <Col md={1}></Col>
              <Col md={2} className="my-auto">
                <BattleList
                  type="Managed"
                  name="Managed Battles"
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

export default TournamentPageViewEducator;