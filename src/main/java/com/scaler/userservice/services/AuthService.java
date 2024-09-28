package com.scaler.userservice.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.scaler.userservice.clients.KafkaProducerClient;
import com.scaler.userservice.dtos.EmailDto;
import com.scaler.userservice.exceptions.UserAlreadyExistsException;
import com.scaler.userservice.exceptions.UserNotFoundException;
import com.scaler.userservice.exceptions.WrongPasswordException;
import com.scaler.userservice.models.Session;
import com.scaler.userservice.models.SessionStatus;
import com.scaler.userservice.models.User;
import com.scaler.userservice.repositories.SessionRepository;
import com.scaler.userservice.repositories.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service("userService")
public class AuthService {

    private UserRepository userRepository;

    @Autowired
    private KafkaProducerClient kafkaProducerClient;
    private BCryptPasswordEncoder bCryptPasswordEncoder;
    //private SecretKey key = Jwts.SIG.HS256.key().build();
    //using custom key
    private SecretKey key = Keys.hmacShaKeyFor("namanisveryveryveryveryveryveryverycool"
                    .getBytes(StandardCharsets.UTF_8));
    private SessionRepository sessionRepository;

    @Autowired
    private ObjectMapper objectMapper;

    public AuthService(UserRepository userRepository,
                       BCryptPasswordEncoder bCryptPasswordEncoder,
                       SessionRepository sessionRepository) {
        this.userRepository = userRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.sessionRepository = sessionRepository;
    }

    public boolean signUp(String email, String password) throws UserAlreadyExistsException {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new UserAlreadyExistsException("User with email: " + email + " already exists");
        }

        User user = new User();

        user.setEmail(email);
        user.setPassword(bCryptPasswordEncoder.encode(password));

        userRepository.save(user);

        //send message to kafka for welcome email
        try {
            EmailDto emailDto = new EmailDto();
            emailDto.setTo(email);
            emailDto.setSubject("Welcome to Ecomm");
            emailDto.setBody("Have a pleaseant shopping experience!!");
            emailDto.setFrom("anuragbatch@gmail.com");

            kafkaProducerClient.sendMessage("user_signedIn", objectMapper.writeValueAsString(emailDto));
        }catch (JsonProcessingException e){
            throw new RuntimeException(e.getMessage());
        }

        return true;
    }

    public String login(String email, String password) throws UserNotFoundException, WrongPasswordException {
        Optional<User> userOptional = userRepository.findByEmail(email);

        if (userOptional.isEmpty()) {
            throw new UserNotFoundException("User with email: " + email + " not found.");
        }

        boolean matches = bCryptPasswordEncoder.matches(
                password,
                userOptional.get().getPassword()
        );

        if (matches) {
            String token =  createJwtToken(userOptional.get().getId(),
                    new ArrayList<>(),
                    userOptional.get().getEmail());

            Session session = new Session();
            session.setToken(token);
            session.setUser(userOptional.get());

            Calendar calendar = Calendar.getInstance();
            Date currentDate = calendar.getTime();

            calendar.add(Calendar.DAY_OF_MONTH, 30);
            Date datePlus30Days = calendar.getTime();
            session.setExpiringAt(datePlus30Days);

            sessionRepository.save(session);

            return token;
        } else {
            throw new WrongPasswordException("Wrong password.");
        }
    }

    private String createJwtToken(Long id, List<String> roles, String email) {
        Map<String, Object> dataInJwt = new HashMap<>();
        dataInJwt.put("user_id", id);
        dataInJwt.put("roles", roles);
        dataInJwt.put("email", email);

        Calendar calendar = Calendar.getInstance();
        Date currentDate = calendar.getTime();

        calendar.add(Calendar.DAY_OF_MONTH, 30);
        Date datePlus30Days = calendar.getTime();

        String token = Jwts.builder()
                .claims(dataInJwt)
                .expiration(datePlus30Days)
                .issuedAt(new Date())
                .signWith(key)
                .compact();

        return token;
    }

    public boolean logout(String token) throws Exception{
        try {
            Optional<Session> session = sessionRepository.findByToken(token);

            if (session.isEmpty()) {
                throw new Exception("Invalid session");
            }else{
                session.get().setSessionStatus(SessionStatus.INACTIVE);
                sessionRepository.save(session.get());
            }

            return true;
        }catch(Exception e){
            throw new Exception("Invalid session");
        }
    }

    public void updateRole() {
        System.out.println("Update Role");
    }

    public boolean validate(String token) {
        try {
            Jws<Claims> claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token);

            Date expiryAt = claims.getPayload().getExpiration();
            Long userId = claims.getPayload().get("user_id", Long.class);

        } catch (Exception e) {
            return false;
        }

        return true;
    }
}
