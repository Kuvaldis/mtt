package com.revolut.mtt.app;

import com.revolut.mtt.controller.AccountController;
import com.revolut.mtt.controller.TransferController;
import com.revolut.mtt.controller.UserController;
import com.revolut.mtt.error.ErrorHandlingModule;
import com.revolut.mtt.database.SchemaInit;
import com.revolut.mtt.database.TransactionSupport;
import org.eclipse.jetty.server.RequestLog;
import org.jooby.Jooby;
import org.jooby.RequestLogger;
import org.jooby.jdbc.Jdbc;
import org.jooby.json.Jackson;

/**
 * Main Jooby class running REST service.
 */
public class App extends Jooby {

    {
        // modules
        use(new Jdbc());
        use(new SchemaInit());
        use(new Jackson());
        use(new TransactionSupport());
        use(new ErrorHandlingModule());
        use("*", new RequestLogger());

        // controllers
        use(UserController.class);
        use(AccountController.class);
        use(TransferController.class);
    }

    public static void main(String[] args) {
        run(App::new, args);
    }
}
