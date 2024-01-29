package com.polimi.PPP.CodeKataBattle.Evaluators;

import com.polimi.PPP.CodeKataBattle.DTOs.SubmissionDTO;
import com.polimi.PPP.CodeKataBattle.Exceptions.ErrorDuringEvaluationException;
import com.polimi.PPP.CodeKataBattle.Model.Submission;

public interface IEvaluator {

    Float scoreOfFunctionalTests(SubmissionDTO submission) throws ErrorDuringEvaluationException;

    Float scoreOfStaticAnalysis(SubmissionDTO submission) throws ErrorDuringEvaluationException ;
}
