package org.example.springboot_redis_active.controller;

import org.example.springboot_redis_active.model.User;
import org.example.springboot_redis_active.repository.Userrepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private Userrepository userRepository;

    @Autowired
    private JmsTemplate jmsTemplate;

    //creating a user
    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User user) {
        // Save to Redis
        User savedUser = userRepository.save(user);
        // Send message to ActiveMQ
        jmsTemplate.convertAndSend("users.queue", "New user created: " + savedUser.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(savedUser);
    }

    //fetching user by id
    @GetMapping("/{id}")
    public ResponseEntity getUser(@PathVariable String id) {
         Optional<User> user=userRepository.findById(id);
         if(user.isPresent()) {
             jmsTemplate.convertAndSend("users.queue", "User : " + user.get());
             return ResponseEntity.status(HttpStatus.FOUND).body(user.get());
         }else{
//             jmsTemplate.convertAndSend("users.queue", "User not found with the id: "+id);
//             return ResponseEntity.status(HttpStatus.NOT_FOUND).body("user not found with the ID: "+ id);
             throw new RuntimeException("User not found with id: " + id);
         }
    }

    //fetching all users
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        Iterable<User> users = userRepository.findAll();
        List<User> userList = (List<User>) users;
        if (userList.isEmpty()) {
            jmsTemplate.convertAndSend("users.queue", "No users found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } else {
            StringBuilder userNames = new StringBuilder("Users found names are: ");
            for (User user : userList) {
                userNames.append(user.getName()).append(", "); 
            }
            
            userNames.setLength(userNames.length() - 2);

            jmsTemplate.convertAndSend("users.queue", userNames.toString());
            return ResponseEntity.status(HttpStatus.OK).body(userList); 
        }
    }

//deleting user by id
    @DeleteMapping("/{id}")
    public ResponseEntity deleteUser(@PathVariable String id) {
        Optional<User> user=userRepository.findById(id);
        if(user.isPresent()) {
            userRepository.delete(user.get());
            jmsTemplate.convertAndSend("users.queue", "user deleted with the" +" ID "+user.get().getId());
            return ResponseEntity.status(HttpStatus.ACCEPTED).body("User with this id : "+id + " has been delete successfuly");

        }else{
//            jmsTemplate.convertAndSend("users.queue", "user not found with the " +" ID "+id);
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User with the id : "+id + " not found");
            throw new RuntimeException("User not found with id: " + id);
        }

    }

    //deleting all users
    @DeleteMapping
    public ResponseEntity deleteAllUsers() {
        Iterable<User> users = userRepository.findAll();
        List<User> userList = (List<User>) users;
        if (userList.isEmpty()) {
//            jmsTemplate.convertAndSend("users.queue", "No users found");
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("no users found");
            throw new RuntimeException("Users are empty");
        } else {
            userRepository.deleteAll();
            jmsTemplate.convertAndSend("users.queue", "all User deleted");
            return ResponseEntity.status(HttpStatus.ACCEPTED).body("all User deleted");
        }
    }

    //updating user by id
    @PutMapping("/{id}")
    public ResponseEntity updateUser(@PathVariable String id, @RequestBody User user) {
        // Check if user exists
        Optional<User> existingUserOptional = userRepository.findById(id);
        if (existingUserOptional.isPresent()) {
            User existingUser = existingUserOptional.get();
            String oldname=existingUser.getName();
            existingUser.setName(user.getName()); // Update fields as necessary
            User updatedUser = userRepository.save(existingUser);
            jmsTemplate.convertAndSend("users.queue", "User updated for the id:" + id +" 'NAME' "+ oldname +" to "+ updatedUser.getName());
//            return updatedUser;
            return ResponseEntity.status(HttpStatus.OK).body("the updated user is " + updatedUser);
        } else {
            // Handle user not found
            throw new RuntimeException("User not found with id: " + id);
            //jmsTemplate.convertAndSend("users.queue", "The User not found with the id: "+ id);
            //return ResponseEntity.status(HttpStatus.NOT_FOUND).body("The user not found with ID: " + id);
        }
    }
}




