package com.lubover.singularity.user.controller;

import com.lubover.singularity.user.entity.User;
import com.lubover.singularity.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public Map<String, Object> register(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String password = request.get("password");
        String nickname = request.get("nickname");

        User user = userService.register(username, password, nickname);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", user);
        return result;
    }

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String password = request.get("password");

        User user = userService.login(username, password);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", user);
        return result;
    }

    @GetMapping("/{id}")
    public Map<String, Object> getUserById(@PathVariable Long id) {
        User user = userService.getUserById(id);
        Map<String, Object> result = new HashMap<>();
        result.put("success", user != null);
        result.put("data", user);
        return result;
    }

    @GetMapping("/list")
    public Map<String, Object> getAllUsers() {
        List<User> users = userService.getAllUsers();
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", users);
        return result;
    }

    @PutMapping("/{id}")
    public Map<String, Object> updateUser(@PathVariable Long id, @RequestBody Map<String, Object> request) {
        String password = (String) request.get("password");
        String nickname = (String) request.get("nickname");
        String role = (String) request.get("role");
        BigDecimal balance = request.get("balance") != null ? new BigDecimal(request.get("balance").toString()) : null;

        User user = userService.updateUser(id, password, nickname, role, balance);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", user);
        return result;
    }

    @DeleteMapping("/{id}")
    public Map<String, Object> deleteUser(@PathVariable Long id) {
        boolean deleted = userService.deleteUser(id);
        Map<String, Object> result = new HashMap<>();
        result.put("success", deleted);
        result.put("message", deleted ? "User deleted successfully" : "User not found");
        return result;
    }

    @PostMapping("/{id}/recharge")
    public Map<String, Object> recharge(@PathVariable Long id, @RequestBody Map<String, Object> request) {
        BigDecimal amount = new BigDecimal(request.get("amount").toString());
        boolean success = userService.recharge(id, amount);
        Map<String, Object> result = new HashMap<>();
        result.put("success", success);
        result.put("message", success ? "Recharge successful" : "Recharge failed");
        return result;
    }

    @PostMapping("/{id}/deduct")
    public Map<String, Object> deduct(@PathVariable Long id, @RequestBody Map<String, Object> request) {
        BigDecimal amount = new BigDecimal(request.get("amount").toString());
        boolean success = userService.deduct(id, amount);
        Map<String, Object> result = new HashMap<>();
        result.put("success", success);
        result.put("message", success ? "Deduct successful" : "Deduct failed");
        return result;
    }
}
