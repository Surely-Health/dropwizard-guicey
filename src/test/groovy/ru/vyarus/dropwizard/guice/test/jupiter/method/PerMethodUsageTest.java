package ru.vyarus.dropwizard.guice.test.jupiter.method;

import io.dropwizard.Application;
import io.dropwizard.Configuration;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import ru.vyarus.dropwizard.guice.GuiceBundle;
import ru.vyarus.dropwizard.guice.test.jupiter.ext.TestGuiceyAppExtension;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * @author Vyacheslav Rusakov
 * @since 03.06.2022
 */
public class PerMethodUsageTest {

    @RegisterExtension
    TestGuiceyAppExtension extension = TestGuiceyAppExtension.forApp(App.class).create();

    @Inject
    Bean bean;

    @Test
    void testPerMethodStartup() {
        Assertions.assertEquals(0, bean.value);
        bean.value = 3;
    }

    @Test
    void testPerMethodStartup2() {
        Assertions.assertEquals(0, bean.value);
        bean.value = 2;
    }

    @Singleton
    public static class Bean {
        public int value = 0;
    }

    public static class App extends Application<Configuration> {

        @Override
        public void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder().build());
        }

        @Override
        public void run(Configuration configuration, Environment environment) throws Exception {
        }
    }
}