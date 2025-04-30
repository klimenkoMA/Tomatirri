package accountingApp.service;

import accountingApp.entity.TomatirriUser;
import accountingApp.entity.Role;
import accountingApp.repository.TomatirriUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
class CustomUserDetailsService implements UserDetailsService {

    final Logger logger = LoggerFactory.getLogger(CustomUserDetailsService.class);
    private final TomatirriUserRepository userRepository;
    private final TomatirriUserService tomatirriUserService;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public CustomUserDetailsService(TomatirriUserRepository userRepository
            , TomatirriUserService tomatirriUserService
            , PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.tomatirriUserService = tomatirriUserService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String userName) throws UsernameNotFoundException {
        try {

            try {
                List<TomatirriUser> tomatirriUserList = tomatirriUserService.getAllAppUsers();
                for (TomatirriUser user : tomatirriUserList
                ) {
                    if (user.getUserName().equals("admin")) {
                        throw new Exception();
                    }
                }

                Set<Role> roleSet = new HashSet<>();
                roleSet.add(Role.ADMIN);
                roleSet.add(Role.USER);
                TomatirriUser admin = new TomatirriUser("admin", "1", true, roleSet);
                tomatirriUserService.createUser(admin, "1");
            } catch (Exception exception) {
                logger.warn("CustomUserDetailsService.loadUserByUsername: INITIALISATION");
            }

            // Получаем пользователя из репозитория
            TomatirriUser tomatirriUser = userRepository.findByUserName(userName).orElseThrow(() ->
                    new UsernameNotFoundException("User not found"));

            if (!tomatirriUser.isActive()) {
                throw new Exception("User " + tomatirriUser.getUserName() + " isn't active!");
            }

            // Логируем успешную авторизацию
            logger.warn("CustomUserDetailsService.loadUserByUsername " +
                    "Successful authorization with user: " + userName);

            String roles;

            if (tomatirriUser.getRoles().isEmpty()) {
                roles = "USER";
            } else {
                roles = tomatirriUser
                        .getRoles()
                        .stream()
                        .iterator()
                        .next()
                        .getAuthority();
            }

            if (tomatirriUser.getUserName().equals("admin")) {
                roles = "ADMIN";
            }
            logger.warn("CustomUserDetailsService.loadUserByUsername " +
                    "User's role is : " + roles);

            // Создаем UserDetails
            return User
                    .builder()
                    .username(tomatirriUser.getUserName())
                    .password(tomatirriUser.getUserPass())
                    .roles(roles)
                    .build();

        } catch (UsernameNotFoundException e) {
            logger.error("User not found: " + userName, e);
            throw e;
        } catch (Exception e) {
            logger.error("An error occurred while loading user by username: " + userName, e);
            throw new UsernameNotFoundException(e.toString());
        }
    }

    /**
     * Метод для шифрования паролей всех пользователей, находящихся в БД на
     * данный момент. Применялся 1 раз, по окончанию тестирования работы системы
     * безопасности аутентификации
     */
    private void bCryptEncode() {

        List<TomatirriUser> tomatirriUserList = tomatirriUserService.getAllAppUsers();
        TomatirriUser user;
        String password;

        for (TomatirriUser tomatirriUser : tomatirriUserList) {
            user = tomatirriUser;
            password = user.getUserPass();
            user.setUserPass(passwordEncoder.encode(password));
            userRepository.save(user);
        }

    }


}
