package com.techcourse.controller;

import com.interface21.context.stereotype.Controller;
import com.interface21.jdbc.core.JdbcTemplate;
import com.interface21.web.bind.annotation.RequestMapping;
import com.interface21.web.bind.annotation.RequestMethod;
import com.interface21.webmvc.servlet.ModelAndView;
import com.interface21.webmvc.servlet.view.JsonView;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.dao.UserDaoImpl;
import com.techcourse.dao.UserHistoryDaoImpl;
import com.techcourse.service.AppUserServiceImpl;
import com.techcourse.service.TxUserService;
import com.techcourse.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;

    public UserController() {
        userService = new TxUserService(
                new AppUserServiceImpl(
                        new UserDaoImpl(new JdbcTemplate(DataSourceConfig.getInstance())),
                        new UserHistoryDaoImpl(new JdbcTemplate(DataSourceConfig.getInstance()))
                )
        );
    }

    @RequestMapping(value = "/api/user", method = RequestMethod.GET)
    public ModelAndView show(final HttpServletRequest request, final HttpServletResponse response) {
        final var account = request.getParameter("account");
        log.debug("user id : {}", account);

        final var modelAndView = new ModelAndView(new JsonView());
        final var user = userService.findByAccount(account);

        modelAndView.addObject("user", user);
        return modelAndView;
    }
}
