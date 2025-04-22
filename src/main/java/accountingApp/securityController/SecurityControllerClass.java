package accountingApp.securityController;

import accountingApp.entity.TomatirriUser;
import accountingApp.entity.Role;
import accountingApp.service.AppUserService;
import accountingApp.usefulmethods.Checker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.*;

@RequestMapping
@Controller
public class SecurityControllerClass {

    final Logger logger = LoggerFactory.getLogger(SecurityControllerClass.class);

    @Autowired(required = false)
    private AppUserService service;

    @Autowired
    private Checker checker;

    @Bean
    public static Checker checker() {
        return new Checker();
    }


    @GetMapping("/")
    @PreAuthorize("isAuthenticated()")
    public String getHome() {
        return "main";
    }

    @GetMapping("/login")
    public String getLogin() {
        return "login";
    }


    @GetMapping("/users")
    public String getUsers(Model model) {
        List<TomatirriUser> tomatirriUserList = service.getAllAppUsers();
        Role[] rolesArray = Role.values();
        List<String> rolesList = new ArrayList<>();
        for (Role r : rolesArray
        ) {
            rolesList.add(r.getAuthority());
        }

        List<String> isActiveList = new ArrayList<>();
        isActiveList.add("active");
        isActiveList.add("blocked");

        model.addAttribute("isActiveList", isActiveList);
        model.addAttribute("rolesList", rolesList);
        model.addAttribute("appUserList", tomatirriUserList);
        return "users";
    }

    @PostMapping("/adduser")
    public String addNewUser(@RequestParam String userName
            , @RequestParam String userPass
            , @RequestParam String isActive
            , @RequestParam String roles
            , Model model
    ) {

        if (checker.checkAttribute(userName)
                || checker.checkAttribute(userPass)
                || checker.checkAttribute(isActive)
                || checker.checkAttribute(roles)) {
            logger.warn("SecurityControllerClass.addNewUser():" +
                    " Attribute has a null value!");
            return getUsers(model);
        }

        String userNameWithoutSpaces = userName.trim();
        String userPassWithoutSpaces = userPass.trim();
        String isActiveWithoutSpaces = isActive.trim();
        String rolesWithoutSpaces = roles.trim();

        try {
            TomatirriUser user;
            Set<Role> rolesSet = new HashSet<>();
            boolean isActiveUser;
            isActiveUser = isActiveWithoutSpaces.toLowerCase(Locale.ROOT).equals("active");

            if (rolesWithoutSpaces.toLowerCase(Locale.ROOT).equals("admin")) {
                rolesSet.add(Role.ADMIN);
            }
            rolesSet.add(Role.USER);

            user = new TomatirriUser(userNameWithoutSpaces
                    , userPassWithoutSpaces
                    , isActiveUser
                    , rolesSet);
            service.createUser(user, userPassWithoutSpaces);
            return getUsers(model);

        } catch (Exception e) {
            logger.error("*** SecurityControllerClass.addNewUser():  WRONG DB VALUES " +
                    "OR EMPTY ATTRS *** " + e.getMessage() + " " + e.toString());
            e.printStackTrace();
            return getUsers(model);
        }
    }

    @PostMapping("/updateuser")
    public String updateAppUser(@RequestParam String id
            , @RequestParam String userName
            , @RequestParam String userPass
            , @RequestParam String isActive
            , @RequestParam String roles
            , Model model
    ) {
        if (checker.checkAttribute(id)
                || checker.checkAttribute(userName)
                || checker.checkAttribute(userPass)
                || checker.checkAttribute(isActive)
                || checker.checkAttribute(roles)) {
            logger.warn("SecurityControllerClass.updateAppUser():" +
                    " Attribute has a null value!");
            return getUsers(model);
        }

        String userIdWithoutSpaces = id.trim();
        String userNameWithoutSpaces = userName.trim();
        String userPassWithoutSpaces = userPass.trim();
        String isActiveWithoutSpaces = isActive.trim();
        String rolesWithoutSpaces = roles.trim();

        try {
            long idCheck = Long.parseLong(userIdWithoutSpaces);
            if (idCheck <= 0) {
                logger.warn("SecurityControllerClass.updateAppUser():" +
                        " WRONG ID FORMAT");
                return getUsers(model);
            } else if (!userNameWithoutSpaces.equals("")
                    && !userPassWithoutSpaces.equals("")
                    && !isActiveWithoutSpaces.equals("")
                    && !rolesWithoutSpaces.equals("")
            ) {

                TomatirriUser user;
                Set<Role> rolesSet = new HashSet<>();
                boolean isActiveUser;
                isActiveUser = isActiveWithoutSpaces.toLowerCase(Locale.ROOT).equals("active");

                if (rolesWithoutSpaces.toLowerCase(Locale.ROOT).equals("admin")) {
                    rolesSet.add(Role.ADMIN);
                }
                rolesSet.add(Role.USER);

                user = new TomatirriUser(idCheck
                        , userNameWithoutSpaces
                        , userPassWithoutSpaces
                        , isActiveUser
                        , rolesSet);
                service.updateUser(user, userPassWithoutSpaces);
            }
            return getUsers(model);

        } catch (Exception e) {
            logger.error("*** SecurityControllerClass.updateAppUser()::  WRONG DB VALUES " +
                    "OR EMPTY ATTRS *** " + e.getMessage() + " " + e.toString());
            e.printStackTrace();
            return getUsers(model);
        }
    }

    @PostMapping("/deleteuser")
    public String deleteAppUser(@RequestParam String id
            , Model model
    ) {
        if (checker.checkAttribute(id)
        ) {
            logger.warn("SecurityControllerClass.deleteAppUser():" +
                    " Attribute has a null value!");
            return getUsers(model);
        }

        String userIdWithoutSpaces = id.trim();

        try {
            long idCheck = Long.parseLong(userIdWithoutSpaces);
            if (idCheck <= 0) {
                logger.warn("SecurityControllerClass.deleteAppUser():" +
                        " WRONG ID FORMAT");
                return getUsers(model);
            }
            service.deleteUser(idCheck);
            return getUsers(model);
        } catch (Exception e) {
            logger.error("*** SecurityControllerClass.deleteAppUser():  WRONG DB VALUES " +
                    "OR EMPTY ATTRS *** " + e.getMessage() + " " + e.toString());
            e.printStackTrace();
            return getUsers(model);
        }
    }

    @PostMapping("/finduserbyname")
    public String findAppUser(@RequestParam String userName
            , Model model
    ) {
        if (checker.checkAttribute(userName)
        ) {
            logger.warn("SecurityControllerClass.findAppUser():" +
                    " Attribute has a null value!");
            return getUsers(model);
        }

        String userIdWithoutSpaces = userName.trim();

        try {
            long idCheck = Long.parseLong(userIdWithoutSpaces);
            if (idCheck <= 0 || checker.checkAttribute(idCheck + "")) {
                logger.warn("SecurityControllerClass.findAppUser():" +
                        " WRONG ID FORMAT");
                return getUsers(model);
            }
            List<TomatirriUser> tomatirriUserList = service.findUserById(idCheck);
            model.addAttribute("appUserList", tomatirriUserList);
            return "users";
        } catch (Exception e) {
            try {
                Optional<TomatirriUser> appUserOptional =
                        service.findUserByName(userIdWithoutSpaces);
                List<TomatirriUser> tomatirriUserList = new ArrayList<>();
                appUserOptional.ifPresent(tomatirriUserList::add);
                model.addAttribute("appUserList", tomatirriUserList);
                logger.debug("SecurityControllerClass.findAppUser(): " +
                        "found an AppUser by fio  *** " + e.getMessage());
                return "users";
            } catch (Exception e1) {
                logger.error("*** SecurityControllerClass.findAppUser():  WRONG DB VALUES " +
                        "OR EMPTY ATTRS *** " + e.getMessage() + " " + e.toString());
                e1.printStackTrace();
                return getUsers(model);
            }
        }
    }

    @PostMapping("/finduserbyattrs")
    public String findAppUserByAttrs(@RequestParam String attrs
            , Model model
    ) {
        if (checker.checkAttribute(attrs)
        ) {
            logger.warn("SecurityControllerClass.findAppUserByAttrs():" +
                    " Attribute has a null value!");
            return getUsers(model);
        }

        String attrsWithoutSpaces = attrs.trim().toLowerCase(Locale.ROOT);
        try {
            List<TomatirriUser> users = service.getAllAppUsers();
            List<TomatirriUser> tomatirriUserList = new ArrayList<>();

            for (TomatirriUser us : users
            ) {
                if ((us.getId() + "").contains(attrsWithoutSpaces)) {
                    tomatirriUserList.add(us);
                } else if (us.getUserName().toLowerCase(Locale.ROOT)
                        .contains(attrsWithoutSpaces)) {
                    tomatirriUserList.add(us);
                } else if (us.getIsActiveForView().toLowerCase(Locale.ROOT)
                        .contains(attrsWithoutSpaces)) {
                    tomatirriUserList.add(us);
                } else if (us.getRoles().stream()
                        .anyMatch(descr -> descr.getAuthority().toLowerCase(Locale.ROOT)
                                .contains(attrsWithoutSpaces))) {
                    tomatirriUserList.add(us);
                }
            }

            model.addAttribute("appUserList", tomatirriUserList);

            if (tomatirriUserList.isEmpty()) {
                logger.debug("*** ITStaffController.getItStaffListByAttrs():  DATA NOT FOUND IN DB***");
                return getUsers(model);
            }

            return "users";
        } catch (Exception e) {
            logger.error("*** SecurityControllerClass.findAppUserByAttrs():  WRONG DB VALUES " +
                    "OR EMPTY ATTRS *** " + e.getMessage() + " " + e.toString());
            e.printStackTrace();
            return getUsers(model);
        }

    }

}
