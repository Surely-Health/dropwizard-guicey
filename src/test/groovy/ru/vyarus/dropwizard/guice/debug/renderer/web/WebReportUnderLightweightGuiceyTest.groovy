package ru.vyarus.dropwizard.guice.debug.renderer.web

import io.dropwizard.Application
import io.dropwizard.Configuration
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.bundle.lookup.PropertyBundleLookup
import ru.vyarus.dropwizard.guice.debug.renderer.web.support.GuiceWebModule
import ru.vyarus.dropwizard.guice.debug.renderer.web.support.UserServletsBundle
import ru.vyarus.dropwizard.guice.debug.report.web.MappingsConfig
import ru.vyarus.dropwizard.guice.debug.report.web.WebMappingsRenderer
import ru.vyarus.dropwizard.guice.module.GuiceyConfigurationInfo
import ru.vyarus.dropwizard.guice.test.spock.UseGuiceyApp
import spock.lang.Specification

import javax.inject.Inject

/**
 * @author Vyacheslav Rusakov
 * @since 24.10.2019
 */
@UseGuiceyApp(App)
class WebReportUnderLightweightGuiceyTest extends Specification {

    static {
        System.clearProperty(PropertyBundleLookup.BUNDLES_PROPERTY)
    }

    @Inject
    Environment environment
    @Inject
    GuiceyConfigurationInfo info
    WebMappingsRenderer renderer

    void setup() {
        renderer = new WebMappingsRenderer(environment, info)
    }

    def "Check complete render"() {

        expect:
        render(new MappingsConfig()
                .showMainContext()
                .showGuiceMappings()
                .showDropwizardMappings()) == """

    MAIN /
    ├── filter     /custom/*                    CustomMappingFilter          (r.v.d.g.d.r.w.s.UserServletsBundle)      STOPPED    [ERROR]
    ├── filter     /async/*             async   AsyncFilter                  (r.v.d.g.d.r.w.s.UserServletsBundle)      STOPPED    [REQUEST]
    ├── filter     /both/*                      BothFilter                   (r.v.d.g.d.r.w.s.UserServletsBundle)      STOPPED    [REQUEST]
    ├── filter     /1/*                         MainFilter                   (r.v.d.g.d.r.w.s.UserServletsBundle)      STOPPED    [REQUEST]
    ├── filter     /2/*                         --"--
    │
    ├── filter     /*                   async   GuiceFilter                  (c.g.inject.servlet)                      STOPPED    [REQUEST]
    │   ├── guicefilter     /1/*                         GFilter                        r.v.d.g.d.r.w.support.GuiceWebModule
    │   ├── guicefilter     /1/abc?/.*           regex   GRegexFilter                   r.v.d.g.d.r.w.support.GuiceWebModule
    │   ├── guicefilter     /1/foo                       instance of GFilterInstance    r.v.d.g.d.r.w.support.GuiceWebModule
    │   ├── guiceservlet    /2/*                         GServlet                       r.v.d.g.d.r.w.support.GuiceWebModule
    │   ├── guiceservlet    /2/abc?/             regex   GRegexServlet                  r.v.d.g.d.r.w.support.GuiceWebModule
    │   └── guiceservlet    /2/foo                       instance of GServletInstance   r.v.d.g.d.r.w.support.GuiceWebModule
    │
    ├── servlet    /foo                         MainServlet                  (r.v.d.g.d.r.w.s.UserServletsBundle)      STOPPED
    │   └── filter                                  TargetServletFilter          (r.v.d.g.d.r.w.s.UserServletsBundle)      STOPPED    [REQUEST]
    │
    ├── servlet    /bar                         --"--
    ├── servlet    /both                        BothServlet                  (r.v.d.g.d.r.w.s.UserServletsBundle)      STOPPED
    └── servlet    /async               async   AsyncServlet                 (r.v.d.g.d.r.w.s.UserServletsBundle)      STOPPED
""" as String;
    }

    static class App extends Application<Configuration> {

        @Override
        void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder()
                    .bundles(new UserServletsBundle())
                    .modules(new GuiceWebModule())
                    .printWebMappings()
                    .build())
        }

        @Override
        void run(Configuration configuration, Environment environment) throws Exception {
        }
    }


    String render(MappingsConfig config) {
        renderer.renderReport(config).replaceAll("\r", "").replaceAll(" +\n", "\n")
    }
}