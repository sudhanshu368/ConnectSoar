package com.connectsoar.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class SupabaseConfig {

    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.anon-key}")
    private String supabaseAnonKey;

    @Value("${supabase.service-role-key:}")
    private String supabaseServiceRoleKey;

    @Bean
    public RestClient supabaseRestClient(RestClient.Builder builder) {
        return builder
                .baseUrl(supabaseUrl + "/auth/v1")
                .defaultHeader("apikey", supabaseAnonKey)
                .defaultHeader("Authorization", "Bearer " + supabaseAnonKey)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    @Bean
    public RestClient supabasePostgrestRestClient(RestClient.Builder builder) {
        String authHeader = (supabaseServiceRoleKey != null && !supabaseServiceRoleKey.isBlank())
                ? supabaseServiceRoleKey : supabaseAnonKey;
        return builder
                .baseUrl(supabaseUrl + "/rest/v1")
                .defaultHeader("apikey", supabaseAnonKey)
                .defaultHeader("Authorization", "Bearer " + authHeader)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }
}
