import React, { useState, useEffect } from 'react';
import axios from 'axios';
import { Table } from 'react-bootstrap';
import api from '../utilities/api';

const RankingTournamentItem = ({ rank, name, score }) => (
  <tr>
    <td>{rank}</td>
    <td>{name}</td>
    <td>{score}</td>
  </tr>
);

const RankingTournament = ({ tournamentId }) => {
  const [rankings, setRankings] = useState([]);

  useEffect(() => {
    const fetchRanking = async () => {
      try {
        const response = await api.get(`tournaments/${tournamentId}/ranking`);
        // Check if response.data is an array before setting it to state
        if (Array.isArray(response.data)) {
          setRankings(response.data);
        } else {
          console.error("Received data is not an array:", response.data);
          setRankings([]); // Set to empty array or handle as needed
        }
      } catch (error) {
        console.error("Error fetching tournament rankings:", error);
      }
    };

    fetchRanking();
  }, [tournamentId]);

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
              <RankingTournamentItem
                key={index}
                rank={index + 1}
                name={item.name}
                score={item.score}
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

export { RankingTournament, RankingTournamentItem };
