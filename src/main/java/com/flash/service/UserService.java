package com.flash.service;

import com.flash.dto.CreateUserDTO;
import com.flash.dto.UserDTO;
import com.flash.entity.User;
import com.flash.exception.BusinessException;
import com.flash.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;

    public UserDTO getUserById(Long id) {
        return userRepository.findById(id)
                .map(this::convertToDTO)
                .orElseThrow(() -> BusinessException.notFound("用户不存在"));
    }

    public UserDTO getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .map(this::convertToDTO)
                .orElseThrow(() -> BusinessException.notFound("用户不存在"));
    }

    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public UserDTO createUser(CreateUserDTO dto) {
        if (userRepository.existsByUsername(dto.getUsername())) {
            throw BusinessException.badRequest("用户名已存在");
        }
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw BusinessException.badRequest("邮箱已被使用");
        }

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setPassword(dto.getPassword());
        user.setDisplayName(dto.getDisplayName() != null ? dto.getDisplayName() : dto.getUsername());

        return convertToDTO(userRepository.save(user));
    }

    @Transactional
    public UserDTO updateUser(Long id, CreateUserDTO dto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> BusinessException.notFound("用户不存在"));

        if (!user.getUsername().equals(dto.getUsername()) && userRepository.existsByUsername(dto.getUsername())) {
            throw BusinessException.badRequest("用户名已存在");
        }
        if (!user.getEmail().equals(dto.getEmail()) && userRepository.existsByEmail(dto.getEmail())) {
            throw BusinessException.badRequest("邮箱已被使用");
        }

        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setPassword(dto.getPassword());
        if (dto.getDisplayName() != null) {
            user.setDisplayName(dto.getDisplayName());
        }

        return convertToDTO(userRepository.save(user));
    }

    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw BusinessException.notFound("用户不存在");
        }
        userRepository.deleteById(id);
    }

    private UserDTO convertToDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setDisplayName(user.getDisplayName());
        dto.setCreatedAt(user.getCreatedAt().toString());
        return dto;
    }
}
