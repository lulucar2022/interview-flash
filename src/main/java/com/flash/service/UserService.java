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

/**
 * 用户管理Service
 * 负责用户的增删改查等业务逻辑
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) // 默认事务只读，提升性能
public class UserService {

    private final UserRepository userRepository;

    /**
     * 根据ID查询用户
     * @param id 用户ID
     * @return 用户DTO
     */
    public UserDTO getUserById(Long id) {
        return userRepository.findById(id)
                .map(this::convertToDTO)
                .orElseThrow(() -> BusinessException.notFound("用户不存在"));
    }

    /**
     * 根据用户名查询用户
     * @param username 用户名
     * @return 用户DTO
     */
    public UserDTO getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .map(this::convertToDTO)
                .orElseThrow(() -> BusinessException.notFound("用户不存在"));
    }

    /**
     * 查询所有用户
     * @return 用户DTO列表
     */
    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * 创建用户
     * 需要校验用户名和邮箱是否已存在
     * 
     * @param dto 创建用户请求参数
     * @return 创建成功的用户DTO
     */
    @Transactional // 写操作需要开启事务
    public UserDTO createUser(CreateUserDTO dto) {
        // 检查用户名是否已存在
        if (userRepository.existsByUsername(dto.getUsername())) {
            throw BusinessException.badRequest("用户名已存在");
        }
        // 检查邮箱是否已使用
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw BusinessException.badRequest("邮箱已被使用");
        }

        // 构建用户实体
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setPassword(dto.getPassword());
        // 如果没有设置显示名，则使用用户名
        user.setDisplayName(dto.getDisplayName() != null ? dto.getDisplayName() : dto.getUsername());

        // 保存到数据库
        return convertToDTO(userRepository.save(user));
    }

    /**
     * 更新用户信息
     * 
     * @param id 用户ID
     * @param dto 更新请求参数
     * @return 更新后的用户DTO
     */
    @Transactional
    public UserDTO updateUser(Long id, CreateUserDTO dto) {
        // 先查询用户是否存在
        User user = userRepository.findById(id)
                .orElseThrow(() -> BusinessException.notFound("用户不存在"));

        // 检查用户名是否与其他用户冲突
        if (!user.getUsername().equals(dto.getUsername()) && userRepository.existsByUsername(dto.getUsername())) {
            throw BusinessException.badRequest("用户名已存在");
        }
        // 检查邮箱是否与其他用户冲突
        if (!user.getEmail().equals(dto.getEmail()) && userRepository.existsByEmail(dto.getEmail())) {
            throw BusinessException.badRequest("邮箱已被使用");
        }

        // 更新用户信息
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setPassword(dto.getPassword());
        if (dto.getDisplayName() != null) {
            user.setDisplayName(dto.getDisplayName());
        }

        return convertToDTO(userRepository.save(user));
    }

    /**
     * 删除用户
     * 
     * @param id 用户ID
     */
    @Transactional
    public void deleteUser(Long id) {
        // 检查用户是否存在
        if (!userRepository.existsById(id)) {
            throw BusinessException.notFound("用户不存在");
        }
        userRepository.deleteById(id);
    }

    /**
     * 实体转DTO
     * 
     * @param user 用户实体
     * @return 用户DTO
     */
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
