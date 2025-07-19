package com.trustsphere.ejb.service;

import com.trustsphere.core.entity.User;
import com.trustsphere.core.entity.Role;
import com.trustsphere.core.enums.UserStatus;
import com.trustsphere.ejb.remote.UserServiceRemote;
import com.trustsphere.ejb.dao.UserDAO;
import com.trustsphere.core.dto.UserDTO;
import com.trustsphere.ejb.exception.UserNotFoundException;
import java.util.List;
import java.util.stream.Collectors;

import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;

@Stateless
@RolesAllowed({"ROLE_ADMIN"})
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class UserServiceBean implements UserServiceRemote {

    @EJB
    private UserDAO userDAO;

    @Override
    public UserDTO createUser(UserDTO dto) {
        User user = mapToEntity(dto);
        User created = userDAO.create(user);
        return mapToDTO(created);
    }

    @Override
    public UserDTO getUserById(String id) throws  UserNotFoundException {
        User user = userDAO.findById(id);
        if (user == null) {
            throw new UserNotFoundException(id);
        }
        return mapToDTO(user);
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public List<UserDTO> listActiveUsers() {
        return userDAO.findAll().stream()
                .filter(user -> user.getStatus() == UserStatus.ACTIVE)
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void updateStatus(String id, UserStatus status) throws UserNotFoundException {
        User user = userDAO.findById(id);
        if (user == null) {
            throw new UserNotFoundException(id);
        }
        user.setStatus(status);
        //User updated =
                userDAO.update(user);
        //return updated != null;
    }

    private User mapToEntity(UserDTO dto) {
        User user = new User();
        user.setEmail(dto.getEmail());
        user.setFullName(dto.getFullName());
        user.setStatus(dto.getStatus());
        return user;
    }

    private UserDTO mapToDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setFullName(user.getFullName());
        dto.setStatus(user.getStatus());
        //dto.setCreatedAt(user.getCreatedAt());
        //dto.setUpdatedAt(user.getUpdatedAt());
        dto.setRoleNames(user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet()));
        return dto;
    }
}