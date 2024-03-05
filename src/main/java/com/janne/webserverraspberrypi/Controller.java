package com.janne.webserverraspberrypi;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

@RestController
public class Controller {

    @Value("${credentials.password-hash}")
    private String storedPasswordHash;

    @Value("${api.callback}")
    private String callbackUrl;
    private final BCryptPasswordEncoder passwordEncoder;

    public Controller(BCryptPasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/")
    public String index() {
        return "Server Online!";
    }

    @PostMapping("/turn-on")
    public ResponseEntity pcTurnOn(@RequestBody String userProvidedPassword) {
        userProvidedPassword = userProvidedPassword.replace("%", "!");
        System.out.println("Matching: " + userProvidedPassword + " to: " + storedPasswordHash);
        if (passwordEncoder.matches(userProvidedPassword, storedPasswordHash)) {
            WebClient webClient = WebClient.create();
            String responseBody = webClient.get()
                    .uri(callbackUrl)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            return new ResponseEntity<>("Password Matched", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Password Mismatch", HttpStatus.MULTI_STATUS);
        }
    }
}
