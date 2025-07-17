package com.trustsphere.ejb.api;

import com.trustsphere.core.enums.UserStatus;
import com.trustsphere.ejb.dto.UserDTO;
import jakarta.ejb.Remote;

import java.util.List;

@Remote
public interface UserServiceRemote {

    UserDTO createUser(UserDTO dto);

    UserDTO getUserById(String id);

    List<UserDTO> listActiveUsers();

    void updateStatus(String id, UserStatus status);
}