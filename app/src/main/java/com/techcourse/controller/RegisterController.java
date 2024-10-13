package com.techcourse.controller;

import com.interface21.context.stereotype.Controller;
import com.interface21.transaction.support.TransactionManager;
import com.interface21.web.bind.annotation.RequestMapping;
import com.interface21.web.bind.annotation.RequestMethod;
import com.interface21.webmvc.servlet.ModelAndView;
import com.interface21.webmvc.servlet.view.JspView;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.service.AppUserService;
import com.techcourse.service.TxUserService;
import com.techcourse.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

@Controller
public class RegisterController {

    private final UserService userService;

    public RegisterController() {
        DataSource dataSource = DataSourceConfig.getInstance();
        UserDao userDao = new UserDao(dataSource);
        UserHistoryDao userHistoryDao = new UserHistoryDao(dataSource);
        UserService appUserService = new AppUserService(userDao, userHistoryDao);
        TransactionManager transactionManager = new TransactionManager(dataSource);
        this.userService = new TxUserService(appUserService, transactionManager);
    }

    @RequestMapping(value = "/register", method = RequestMethod.POST)
    public ModelAndView register(final HttpServletRequest request, final HttpServletResponse response) {
        final var user = new User(2,
                request.getParameter("account"),
                request.getParameter("password"),
                request.getParameter("email"));
        userService.insert(user);

        return new ModelAndView(new JspView("redirect:/index.jsp"));
    }

    @RequestMapping(value = "/register", method = RequestMethod.GET)
    public ModelAndView view(final HttpServletRequest request, final HttpServletResponse response) {
        return new ModelAndView(new JspView("/register.jsp"));
    }
}
