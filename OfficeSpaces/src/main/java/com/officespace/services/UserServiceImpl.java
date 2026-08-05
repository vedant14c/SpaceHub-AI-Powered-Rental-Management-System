package com.officespace.services;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.officespace.daos.UserDao;
import com.officespace.dtos.LoginResponseDto;
import com.officespace.entities.User;
import com.officespace.security.JwtService;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class UserServiceImpl {

	@Autowired
	private PasswordEncoder passwordEncoder;
	@Autowired
	private UserDao userDao;
	@Autowired
	private JwtService jwtService;


	public String register(User user) {
		if (userDao.findByEmail(user.getEmail()) != null)
			return "User already exists";

		if (user.getRole() == null || user.getRole() == com.officespace.entities.Role.ADMIN) {
			user.setRole(com.officespace.entities.Role.USER);
		}

		user.setPassword(passwordEncoder.encode(user.getPassword()));
		user.setIsActive(true);
		user.setCreatedAt(LocalDateTime.now());
	    user.setUpdatedAt(LocalDateTime.now());
		userDao.save(user);
		return "Registration Successful";
	}

	public LoginResponseDto login(User dto) {
		User user = userDao.findByEmail(dto.getEmail());
		LoginResponseDto response = new LoginResponseDto();

		if (user != null && passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
			if (!Boolean.TRUE.equals(user.getIsActive())) {
				response.setMessage("Account is deactivated");
				return response;
			}
			String token = jwtService.generateToken(user);
			response.setUserId(user.getId());
			response.setName(user.getName());
			response.setEmail(user.getEmail());
			response.setRole(user.getRole().name());
			response.setToken(token);
			response.setMessage("Login Successful");
			return response;
		}

		response.setMessage("Invalid Email or Password");
		return response;
	}
	public User activateUser(int id) {

	    User user = userDao.findById(id).orElseThrow();
	    user.setIsActive(true);
	    return userDao.save(user);
	}

	public User deactivateUser(int id) {

	    User user = userDao.findById(id).orElseThrow();
	    user.setIsActive(false);
	    return userDao.save(user);
	}

	public User updateUserRole(int id, com.officespace.entities.Role role) {
	    User user = userDao.findById(id).orElseThrow();
	    user.setRole(role);
	    return userDao.save(user);
	}
}