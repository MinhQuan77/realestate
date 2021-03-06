package com.project.realestate.service;

import com.project.realestate.entity.User;
import com.project.realestate.exception.ConfirmationException;
import com.project.realestate.exception.TokenInvalidException;
import com.project.realestate.exception.UserNotFoundException;
import com.project.realestate.exception.UsernameExistException;
import com.project.realestate.repository.UsersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    PasswordEncoder encoder = new BCryptPasswordEncoder();

//    @Autowired
    @Qualifier("userRepository")
    UsersRepository usersRepository;

    @Autowired
    EmailService emailService;

    @Autowired
    JwtTokenService jwtTokenService;

    @Override
    public User getUserById(int id) throws UserNotFoundException {
        User user = usersRepository.findUserById(id);
        if( user== null) {
            throw new UserNotFoundException("User does not exist");
        }
        return user;
    }

    @Override
    public void updateFirstToken(User updateUser) throws UsernameExistException {
        User user = usersRepository.findUsersByUsername(updateUser.getUsername());
        if(user != null ) {
            throw new UsernameExistException("Username is exist");
        }
        usersRepository.save(user);

    }

    @Override
    public void register(User user) {
        saveUser(user);
        emailService.sendMailConfirmation(user);
    }

    @Override
    public void confirmRegistration(String token) throws TokenInvalidException, UsernameExistException, ConfirmationException, UserNotFoundException {
        int userId = Integer.parseInt(jwtTokenService.verifyToken(token));
        User user = getUserById(userId);
        if(user == null) {
            throw new TokenInvalidException("Token invalid");
        }
        user.setAccessToken(token);
        updateFirstToken(user);
    }

    @Override
    public void saveUser(User updateUser) {
        User user = usersRepository.findUsersByUsername(updateUser.getUsername());
        updateUser.setPassword(encoder.encode(updateUser.getPassword()));
        if(user == null)
            throw new UsernameNotFoundException("Username does not exist");
        usersRepository.save(updateUser);
    }

    @Override
    public void resetPassword(String token) throws TokenInvalidException, UsernameExistException, UserNotFoundException, ConfirmationException {
        int userId = Integer.parseInt(jwtTokenService.verifyToken(token));
        User user = getUserById(userId);
        usersRepository.save(user);
    }

}
