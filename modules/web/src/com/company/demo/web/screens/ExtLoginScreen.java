package com.company.demo.web.screens;

import com.company.demo.auth.TwoFactorLoginPasswordCredentials;
import com.haulmont.cuba.gui.Route;
import com.haulmont.cuba.gui.components.Action;
import com.haulmont.cuba.gui.components.TextField;
import com.haulmont.cuba.gui.screen.Subscribe;
import com.haulmont.cuba.gui.screen.UiController;
import com.haulmont.cuba.gui.screen.UiDescriptor;
import com.haulmont.cuba.security.auth.AbstractClientCredentials;
import com.haulmont.cuba.security.auth.Credentials;
import com.haulmont.cuba.security.auth.LoginPasswordCredentials;
import com.haulmont.cuba.security.global.LoginException;
import com.haulmont.cuba.web.app.login.LoginScreen;

import javax.inject.Inject;


@Route(path = "login", root = true)
@UiController("login-screen")
@UiDescriptor("ext-login-screen.xml")
public class ExtLoginScreen extends LoginScreen {

    @Inject
    private TextField<Integer> authKeyField;

    @Override
    protected void doLogin(Credentials credentials) throws LoginException {
        if (credentials instanceof LoginPasswordCredentials) {
            AbstractClientCredentials clientCred = new TwoFactorLoginPasswordCredentials((LoginPasswordCredentials)credentials, authKeyField.getValue());
            super.doLogin(clientCred);
        } else {
            throw new LoginException("Invalid credentials type");
        }
    }

    @Subscribe("submit")
    public void onSubmit(Action.ActionPerformedEvent event) {
        login();
    }


}