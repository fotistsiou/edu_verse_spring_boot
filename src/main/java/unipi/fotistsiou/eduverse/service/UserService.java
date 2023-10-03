package unipi.fotistsiou.eduverse.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import unipi.fotistsiou.eduverse.entity.Role;
import unipi.fotistsiou.eduverse.entity.User;
import unipi.fotistsiou.eduverse.repository.UserRepository;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final RoleService roleService;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService (
            UserRepository userRepository,
            RoleService roleService,
            PasswordEncoder passwordEncoder
    ){
        this.userRepository = userRepository;
        this.roleService = roleService;
        this.passwordEncoder = passwordEncoder;
    }

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    public Optional<User> findOneByEmail(String email) {
        return userRepository.findOneByEmail(email);
    }

    public User saveUser(User user, String role) {
        if (user.getId() == null) {
            if (user.getRoles().isEmpty()) {
                Set<Role> roles = new HashSet<>();
                roleService.findRoleByName(role).ifPresent(roles::add);
                user.setRoles(roles);
            }
            String am = generateAm(user.getId(), role);
            if (am != null) {
                user.setAm(am);
            }
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    public void addAm(User user, String role) {
        String am = null;
        if (user.getId() != null) {
            am = generateAm(user.getId(), role);
            if (am != null) {
                user.setAm(am);
            }
        }
        userRepository.updateAm(user.getId(), am);
    }

    public void updateUser(User user) {
        userRepository.updateUser(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getTelephone()
        );
    }

    private String generateAm(Long userId, String role) {
        String prefix = role.contains("ROLE_PROFESSOR") ? "eduprof" : "edustud";
        String userIdStr = String.format("%05d", userId);
        return prefix + userIdStr;
    }
}