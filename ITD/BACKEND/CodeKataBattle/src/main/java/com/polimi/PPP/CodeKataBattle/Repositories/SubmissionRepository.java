package com.polimi.PPP.CodeKataBattle.Repositories;

import com.polimi.PPP.CodeKataBattle.Model.Submission;
import com.polimi.PPP.CodeKataBattle.Model.SubmissionStateEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubmissionRepository extends JpaRepository<Submission, Long> {
    // Repository methods as needed

    List<Submission> findAllByState(SubmissionStateEnum state);
}
