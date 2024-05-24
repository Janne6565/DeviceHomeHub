package com.janne.webserverraspberrypi;


import com.janne.webserverraspberrypi.services.GameCoverService;
import com.janne.webserverraspberrypi.websockets.ServiceManagerWebsocket;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import com.janne.webserverraspberrypi.websockets.AudioWebsocketHandler;

@RestController
public class Controller {

    @Value("${credentials.password-hash}")
    private String storedPasswordHash;

    @Value("${api.callback}")
    private String callbackUrl;

    private final GameCoverService gameCoverService;

    private final Util util;

    private final AudioWebsocketHandler websocketHandler;

    public Controller(GameCoverService gameCoverService, Util util, AudioWebsocketHandler websocketHandler) {
        this.gameCoverService = gameCoverService;
        this.util = util;
        this.websocketHandler = websocketHandler;
    }

    @GetMapping("/")
    public String index() {
        return "Server Online!";
    }

    @PostMapping("/turn-on")
    public ResponseEntity pcTurnOn(@RequestBody String userProvidedPassword) {
        if (util.checkPassword(userProvidedPassword)) {
            WebClient webClient = WebClient.create();
            String responseBody = webClient.get()
                    .uri(callbackUrl)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            return new ResponseEntity<>("Password Matched", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Password Mismatch", HttpStatus.UNAUTHORIZED);
        }
    }

    @GetMapping("/execute_action")
    public ResponseEntity executeAction(@RequestParam String deviceId, @RequestParam String service, @RequestParam String action, @RequestParam String password) {

        if (!util.checkPassword(password)) {
            return new ResponseEntity("Password Mismatch", HttpStatus.UNAUTHORIZED);
        }

        ServiceManagerWebsocket.executeAction(deviceId, service, action);
        return new ResponseEntity("Action Send", HttpStatus.OK);
    }

    @GetMapping("/load_cover")
    public ResponseEntity loadCover(@RequestParam String url, @RequestParam String password) {
        if (!util.checkPassword(password)) {
            return new ResponseEntity("Password Mismatch", HttpStatus.UNAUTHORIZED);
        }

        websocketHandler.sendInformation("albumCover", url, "discord");
        return new ResponseEntity("Album Cover Send", HttpStatus.OK);
    }
    
    
    @GetMapping("/load_game")
    public ResponseEntity loadGame(@RequestParam String url, @RequestParam String password) {
        if (!util.checkPassword(password)) {
            return new ResponseEntity("Password Mismatch", HttpStatus.UNAUTHORIZED);
        } else {
            gameCoverService.dumpGameCover(url);
            return new ResponseEntity("Game Cover Send", HttpStatus.OK);
        }
    }
}
