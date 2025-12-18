package com.krakenplugins.example.script;

import com.google.inject.Inject;
import com.kraken.api.Context;

public abstract class AbstractTask implements Task {
    @Inject
    protected Context ctx;
}
