package com.revolut.mtt.app;

import com.revolut.mtt.controller.AccountController;
import com.revolut.mtt.controller.UserController;
import com.revolut.mtt.modules.SchemaInit;
import com.revolut.mtt.modules.TransactionSupport;
import org.jooby.Jooby;
import org.jooby.jdbc.Jdbc;
import org.jooby.json.Jackson;

/**
 * Main Jooby class running REST service
 */
public class App extends Jooby {

    {
        // modules
        use(new Jdbc());
        use(new SchemaInit());
        use(new Jackson());
        use(new TransactionSupport());

        // controllers
        use(UserController.class);
        use(AccountController.class);
    }

    public static void main(String[] args) {
        run(App::new, args);
    }
}
