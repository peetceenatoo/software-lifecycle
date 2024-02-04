import React from "react";
import { Container, Row, Col, Button } from "react-bootstrap";
import Header from "../components/Header";
import api from "../utilities/api";

import { useParams } from "react-router-dom";
import { useEffect, useState, useRef } from "react";
import '../styles/TournamentPage.css';
import BattleInfoCard from "../components/BattleInfoCard";
import CodeSubmissions from "../components/CodeSubmissions";
import CodeSubmissionsStudent from "../components/CodeSubmissionStudent";
import { RankingBattle } from "../components/RankingBattle";
import { useNavigate } from "react-router-dom";


function BattlePageViewStudent() {
    const renderCount = useRef(0);
    renderCount.current += 1;
    const { battleId } = useParams();
    const [battle, setBattle] = useState({});

    const navigate = useNavigate();

    useEffect(() => {
        console.log(`Rendered: ${renderCount.current} times`);
      const fetchBattle = async () => {
        try {
          const response = await api.get(`/battles/${battleId}`);
          setBattle(response.data);
          console.log(response.data);
        } catch (error) {
          console.error('Error fetching battle info', error);
          alert('You dont have the permission to view this battle' );
          navigate('/');
        }
      };
  
      if (battleId) {
        fetchBattle();
      }
    }, [battleId]);

    const getGithubToken = async () => {
      try {
        const response = await api.get(`/battles/${battleId}/getGithubToken`);
        console.log(response.data);
        alert('In the next alert you will see the GitHub token. Please copy it and use it to submit your code, remember that it will only be shown once. Store it in the secrets (in the example JWT_TOKEN) of your GitHub reository.\nAn example of GitHub Action can be found at https://shorturl.at/gtXZ4');
        alert('GitHub token: ' + response.data.token);
      } catch (error) {
        alert(error.response.data.message)
        console.error('Error fetching battle info', error);
      }
    }

    return (
      <Container fluid className="px-0 min-vh-100">
          <Header/>
          <Row style={{ padding: '20px' }}>
            <Col md={1}></Col>
            <Col md={3}>
              <div className="ms-2 me-auto">
                {/* Apply the heading class here */}
                <h1 className="heading">Battle #{battleId}</h1>
              </div>
            </Col>
            <Col md={4}></Col>
            <Col md={3}>
              <div className="ms-2 me-auto">
                {/* Apply the status-ongoing class here */}
                {/*<h1 className="status">Status: {tournament.state}</h1>*/}
              </div>
            </Col>
            <Col md={1}></Col>
          </Row>
          {Object.keys(battle).length > 0 && ( 
            <Container>
          <Row style={{ padding: '20px' }}>
          <Col md={1}></Col>
          <Col md={3} className="my-auto">
            <BattleInfoCard battle={battle.battle}/>
          </Col>
              <Col md={1}></Col>
              <Col md={3} className="my-auto">
                <CodeSubmissionsStudent battleId={battleId}/>
              </Col>
              <Col md={1}></Col>
              <Col md={2} className="my-auto">
                <RankingBattle battleId={battleId}/> 
              </Col>

              </Row>
              {battle.battle.state == "ONGOING" &&
                <Row>
                <Col md={1}></Col>
                <Col md={3}>
                <button onClick={getGithubToken} className="btn btn-danger">GitHub Token</button>
                </Col>
                </Row>
              } 
            </Container>
            )}
        </Container>
      );
}

export default BattlePageViewStudent;