package com.polimi.PPP.CodeKataBattle.Utilities;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GitHubAPITest {

    @Test
    void createRepository() {

        IGitHubAPI gitHubAPI = new GitHubAPI();
        gitHubAPI.pushFolder("Jacopopiazza","new_repo_With_puglio", "/Users/japo/Downloads/Puglio", "First commit by CKB");

    }
}