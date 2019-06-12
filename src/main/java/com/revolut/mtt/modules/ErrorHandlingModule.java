package com.revolut.mtt.modules;

import com.google.inject.Binder;
import com.typesafe.config.Config;
import org.jooby.Env;
import org.jooby.Jooby;

public class ErrorHandlingModule implements Jooby.Module {

    @Override
    public void configure(final Env env, final Config conf, final Binder binder) throws Throwable {

    }
}
