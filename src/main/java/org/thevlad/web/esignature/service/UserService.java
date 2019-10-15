package org.thevlad.web.esignature.service;

import org.springframework.security.core.userdetails.UserDetailsService;
import org.thevlad.web.esignature.model.User;
import org.thevlad.web.esignature.web.dto.UserRegistrationDto;

public interface UserService extends UserDetailsService {

    User findByEmail(String email);

    User save(UserRegistrationDto registration);
}
