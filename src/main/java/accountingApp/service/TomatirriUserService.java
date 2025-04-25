package accountingApp.service;

import accountingApp.entity.TomatirriUser;
import accountingApp.entity.Role;
import accountingApp.repository.TomatirriUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class TomatirriUserService {

    private final Logger logger = LoggerFactory.getLogger(TomatirriUserService.class);

    @Autowired
    private TomatirriUserRepository tomatirriUserRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Bean
    public static PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    public List<TomatirriUser> getAllAppUsers() {

        List<TomatirriUser> tomatirriUserList = new ArrayList<>();

        try {
            if (tomatirriUserRepository.findAll() == null) {
                TomatirriUser user = new TomatirriUser(1, "test"
                        , "test", true,
                        new HashSet<>(Collections.singleton(Role.USER)) {
                        });
                tomatirriUserList.add(user);
                return tomatirriUserList;
            }
        } catch (Exception e) {
            logger.warn("***AppUserService.getAllAppUsers() appUserRepository.findAll()" +
                    "return "
                    + e.getMessage());
            TomatirriUser user = new TomatirriUser(0, "test"
                    , "test", true,
                    new HashSet<>(Collections.singleton(Role.USER)) {
                    });
            tomatirriUserList.add(user);
            return tomatirriUserList;
        }

        return setIsActiveFromBooleanIntoString(tomatirriUserRepository.findAll());
    }

    public TomatirriUser createUser(TomatirriUser user, String password) {
        user.setUserPass(passwordEncoder.encode(password));
        logger.warn("AppUser " + user.getUserName() + " created!");
        return tomatirriUserRepository.save(user);
    }

    public TomatirriUser updateUser(TomatirriUser user, String password) {
        user.setUserPass(passwordEncoder.encode(password));
        logger.warn("AppUser " + user.getUserName() + " updated!");
        return tomatirriUserRepository.save(user);
    }

    public List<TomatirriUser> findUserById(long id) {
        return tomatirriUserRepository.findAppUserById(id);
    }

    public Optional<TomatirriUser> findUserByName(String userName) {
        return tomatirriUserRepository.findByUserName(userName);
    }

    public void deleteUser(long id) {
        TomatirriUser user = findUserById(id).get(0);
        logger.warn("AppUser " + user.getUserName() + " deleted!");
        tomatirriUserRepository.deleteById(id);
    }

    /**
     * Метод нужен для корректного отображения первых зарегистрированных пользователей.
     * Нужно убрать перед релизом и очисткой БД
     *
     * @param outerUserList
     * @return
     */
    private List<TomatirriUser> setIsActiveFromBooleanIntoString(List<TomatirriUser> outerUserList) {
        try {
            if (outerUserList == null) {
                throw new Exception("OuterUserList is NULL");
            }

            List<TomatirriUser> innerUserList = new ArrayList<>();
            for (TomatirriUser outerUser : outerUserList) {
                TomatirriUser innerUser;

                long userId = outerUser.getId();
                String userName = outerUser.getUserName();
                Set<Role> userRoles = outerUser.getRoles();

                innerUser = new TomatirriUser(userId,
                        userName,
                        outerUser.getUserPass(),
                        outerUser.isActive(),
                        userRoles);
                innerUserList.add(innerUser);
            }
            return innerUserList;
        } catch (Exception e) {
            logger.error("AppUserService.setIsActiveFromBooleanIntoString(): " +
                    e.getMessage());
            return null;
        }
    }

}
