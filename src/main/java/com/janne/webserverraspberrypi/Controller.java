package com.janne.webserverraspberrypi;


import com.janne.webserverraspberrypi.services.GameCoverService;
import com.janne.webserverraspberrypi.services.deviceManager.DeviceService;
import com.janne.webserverraspberrypi.websockets.AudioWebsocketService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;

@RestController
public class Controller {

    private final DeviceService deviceService;
    private final GameCoverService gameCoverService;
    private final Util util;
    private final AudioWebsocketService websocketHandler;
    @Value("${api.callback}")
    private String callbackUrl;

    public Controller(GameCoverService gameCoverService, Util util, AudioWebsocketService websocketHandler, DeviceService deviceService) {
        this.gameCoverService = gameCoverService;
        this.util = util;
        this.websocketHandler = websocketHandler;
        this.deviceService = deviceService;
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

    @GetMapping("/device/{device}/{action}")
    public ResponseEntity executeAction(@PathVariable("device") String deviceId, @PathVariable("action") String action, @RequestParam String password) {
        System.out.println("Running on: " + deviceId + " " + action);
        if (!util.checkPassword(password)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String res = deviceService.executeAction(deviceId, action);
        if (res == null) {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok().body(res);
    }

    @GetMapping("/get_status")
    public ResponseEntity<String> getStatus(@RequestParam String deviceId, @RequestParam String serviceName, @RequestParam String password) {
        if (!util.checkPassword(password)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return ResponseEntity.ok(deviceService.isServiceRunning(deviceId, serviceName) ? "1" : "0");
    }

    @GetMapping("/execute_action")
    public ResponseEntity executeAction(@RequestParam String deviceId, @RequestParam String service, @RequestParam String action, @RequestParam String password) {
        if (!util.checkPassword(password)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        switch (action) {
            case "start":
                deviceService.startService(deviceId, service);
                break;
            case "stop":
                deviceService.stopService(deviceId, service);
                break;
            case "restart":
                deviceService.restartService(deviceId, service);
                break;
            default:
                return ResponseEntity.badRequest().body("Invalid operation (start/stop/restart); given: " + action);
        }

        return ResponseEntity.ok("Action Send");
    }

    @GetMapping("/load_cover")
    public ResponseEntity loadCover(@RequestParam String url, @RequestParam String password) {
        if (!util.checkPassword(password)) {
            return new ResponseEntity("Password Mismatch", HttpStatus.UNAUTHORIZED);
        }

        websocketHandler.sendInformation("albumCover", url, "discord", System.currentTimeMillis());
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
