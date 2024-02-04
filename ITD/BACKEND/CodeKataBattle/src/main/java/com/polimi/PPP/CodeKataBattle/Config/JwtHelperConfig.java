package com.polimi.PPP.CodeKataBattle.Config;

import com.polimi.PPP.CodeKataBattle.Security.JwtHelper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JwtHelperConfig {
    @Bean
    public JwtHelper jwtHelper() {
        // You might need to pass secret key or other properties
        return new JwtHelper(/* constructor parameters */);
    }
}
