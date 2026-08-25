package cm.ndicsonlabs.bossapp.ui;

import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.spring.security.AuthenticationContext;

@Route("login")
@AnonymousAllowed
public class LoginView extends Div  implements BeforeEnterObserver {
    private final LoginForm loginForm;
    private final AuthenticationContext authenticationContext;

    public LoginView(AuthenticationContext authenticationContext) {
        this.authenticationContext = authenticationContext;
        loginForm = new LoginForm();
        loginForm.setAction("login");

        H2 title = new H2("Institution Finance Login");

        add(title, loginForm);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (authenticationContext.isAuthenticated()){
            event.forwardTo("");
        }
        // Inform the user if they entered incorrect credentials
        if (event.getLocation().getQueryParameters().getParameters().containsKey("error")) {
            loginForm.setError(true);
        }
    }
}