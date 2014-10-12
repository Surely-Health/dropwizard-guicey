package ru.vyarus.dropwizard.guice

import com.google.common.collect.Sets
import com.google.inject.Injector
import com.google.inject.Key
import com.google.inject.TypeLiteral
import io.dropwizard.Configuration
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment
import io.dropwizard.testing.junit.DropwizardAppRule
import org.junit.Rule
import ru.vyarus.dropwizard.guice.module.installer.feature.LifeCycleInstaller
import ru.vyarus.dropwizard.guice.module.installer.feature.ManagedInstaller
import ru.vyarus.dropwizard.guice.module.installer.feature.ResourceInstaller
import ru.vyarus.dropwizard.guice.module.installer.feature.TaskInstaller
import ru.vyarus.dropwizard.guice.module.installer.feature.eager.EagerSingletonInstaller
import ru.vyarus.dropwizard.guice.module.installer.feature.health.HealthCheckInstaller
import ru.vyarus.dropwizard.guice.module.installer.feature.plugin.PluginInstaller
import ru.vyarus.dropwizard.guice.module.installer.feature.provider.JerseyProviderInstaller
import ru.vyarus.dropwizard.guice.module.installer.internal.FeaturesHolder
import ru.vyarus.dropwizard.guice.support.AutoScanApplication
import ru.vyarus.dropwizard.guice.support.TestConfiguration
import ru.vyarus.dropwizard.guice.support.feature.*

/**
 * Dummy test.
 *
 * @author Vyacheslav Rusakov
 * @since 31.08.2014
 */
class AutoScanModeTest extends AbstractTest {

    @Rule
    DropwizardAppRule<TestConfiguration> RULE =
            new DropwizardAppRule<TestConfiguration>(AutoScanApplication.class, 'src/test/resources/ru/vyarus/dropwizard/guice/config.yml');

    def "Check auto scan configuration"() {

        when: "application started"
        Injector injector = GuiceBundle.getInjector()
        FeaturesHolder holder = injector.getInstance(FeaturesHolder.class);
        Bootstrap bootstrap = injector.getInstance(Bootstrap.class);

        then: "environment binding done"
        bootstrap
        injector.getExistingBinding(Key.get(Environment))
        injector.getExistingBinding(Key.get(Configuration))
        injector.getExistingBinding(Key.get(TestConfiguration))

        then: "all installers found"
        holder.installers.size() == 8

        then: "command found"
        bootstrap.getCommands().size() == 2
        def dummyCmd = bootstrap.getCommands().find { it.class == DummyCommand }
        dummyCmd
        dummyCmd.service

        then: "task found"
        holder.getFeatures(TaskInstaller) == [DummyTask]
        injector.getExistingBinding(Key.get(DummyTask))

        then: "resource found"
        holder.getFeatures(ResourceInstaller) == [DummyResource]
        injector.getExistingBinding(Key.get(DummyResource))

        then: "managed found"
        holder.getFeatures(ManagedInstaller) == [DummyManaged]
        injector.getExistingBinding(Key.get(DummyManaged))

        then: "lifecycle found"
        holder.getFeatures(LifeCycleInstaller) == [DummyLifeCycle]
        injector.getExistingBinding(Key.get(DummyLifeCycle))

        then: "jersey provider found"
        Sets.newHashSet(holder.getFeatures(JerseyProviderInstaller)) == [DummyExceptionMapper, DummyJerseyProvider] as Set
        injector.getExistingBinding(Key.get(DummyExceptionMapper))
        injector.getExistingBinding(Key.get(DummyJerseyProvider))

        then: "health check found"
        holder.getFeatures(HealthCheckInstaller) == [DummyHealthCheck]
        injector.getExistingBinding(Key.get(DummyHealthCheck))

        then: "eager found"
        holder.getFeatures(EagerSingletonInstaller) == [DummyService]

        then: "plugins found"
        Sets.newHashSet(holder.getFeatures(PluginInstaller)) == [DummyPlugin1, DummyPlugin2, DummyNamedPlugin1, DummyNamedPlugin2] as Set
        injector.getInstance(Key.get(new TypeLiteral<Set<PluginInterface>>() {})).size() == 2
        injector.getInstance(Key.get(new TypeLiteral<Map<DummyPluginKey, PluginInterface>>() {})).size() == 2
    }
}