import React, { useState, useEffect } from 'react';
import axios from 'axios';
import { Table } from 'react-bootstrap';
import api from '../utilities/api';

const RankingBattleItem = ({ rank, name, score }) => (
  <tr>
    <td>{rank}</td>
    <td>{name}</td>
    <td>{score}</td>
  </tr>
);

const RankingBattle = ({ battleId }) => {
  const [rankings, setRankings] = useState([]);

  useEffect(() => {
    const fetchRanking = async () => {
      try {
        const response = await api.get(`battles/${battleId}/ranking`);
        // Check if response.data is an array before setting it to state
        if (Array.isArray(response.data)) {
          setRankings(response.data);
        } else {
          console.error("Received data is not an array:", response.data);
          setRankings([]); // Set to empty array or handle as needed
        }
      } catch (error) {
        console.error("Error fetching battle rankings:", error);
      }
    };

    fetchRanking();
  }, [battleId]);

  


  return (
    <div>
      <h3>Ranking</h3>
      <Table striped bordered hover>
        <thead>
          <tr>
            <th>#</th>
            <th>Name</th>
            <th>Score</th>
          </tr>
        </thead>
        <tbody>
          {rankings.length > 0 ? (
            rankings.map((item, index) => (
              <RankingBattleItem
                key={index}
                rank={index + 1}
                name={' '.concat(item.usernames)}
                score={item.highestScore}
              />
            ))
          ) : ( 
            <tr>
              <td colSpan="3">No rankings available</td>
            </tr>
          )}
        </tbody>
      </Table>
    </div>
  );
};

export { RankingBattle, RankingBattleItem};
