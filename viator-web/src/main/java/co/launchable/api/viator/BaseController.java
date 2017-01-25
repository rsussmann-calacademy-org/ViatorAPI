package co.launchable.api.viator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;

/**
 * Created by michaelmcelligott on 1/10/14.
 */
@PropertySource("classpath:viator.properties")
public class BaseController {
    @Autowired
    Environment env;

    public BaseController() {}
}
