import { Container, Row, Col, Button } from "react-bootstrap";
import Header from "../components/Header";
import api from "../utilities/api";
import { onclickButtonNotYetImplemented } from '../utilities/alerting';

import { useParams } from "react-router-dom";
import { useEffect, useState } from "react";
import '../styles/TournamentPage.css';
import BattleInfoCard from "../components/BattleInfoCard";
import CodeSubmissions from "../components/CodeSubmissions";
import { RankingBattle } from "../components/RankingBattle";
import CodeSubmissionsStudent from "../components/CodeSubmissionStudent";


function BattlePageViewEducator() {
    const { battleId } = useParams();
    const [battle, setBattle] = useState({});

    useEffect(() => {
        console.log("mbareee");
      const fetchBattle = async () => {
        try {
          const response = await api.get(`/battles/${battleId}`);
          setBattle(response.data);
          console.log(response.data);
        } catch (error) {
          console.error('Error fetching battle info', error);
        }
      };
  
      if (battleId) {
        fetchBattle();
      }
    }, [battleId]);
  

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
          {battle.state == "CONSOLIDATION" &&
            <Row>
              <Col md={1}></Col>
              <Col md={3}>
              <button onClick={onclickButtonNotYetImplemented} className="btn btn-danger">Manage Consolidation</button>
              </Col>
            </Row>
          }
          <Row style={{ padding: '20px' }}>
            <Col md={1}></Col>
            <Col md={3} className="my-auto">
              <BattleInfoCard battle={battle}/>
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
          </Container>
            )}
        </Container>
      );
}

export default BattlePageViewEducator;