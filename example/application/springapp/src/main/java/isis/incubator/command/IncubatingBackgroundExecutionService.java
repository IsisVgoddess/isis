package isis.incubator.command;

import static org.apache.isis.commons.internal.base._Casts.uncheckedCast;

import java.lang.reflect.InvocationHandler;

import javax.inject.Inject;

import org.apache.isis.applib.services.background.BackgroundExecutionService;
import org.apache.isis.applib.services.background.CommandSchedulerService;
import org.apache.isis.applib.services.command.CommandContext;
import org.apache.isis.applib.services.factory.FactoryService;
import org.apache.isis.commons.internal._Constants;
import org.apache.isis.core.commons.lang.ArrayExtensions;
import org.apache.isis.core.metamodel.services.command.CommandDtoServiceInternal;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.core.metamodel.specloader.classsubstitutor.ProxyEnhanced;
import org.apache.isis.core.plugins.codegen.ProxyFactory;
import org.springframework.stereotype.Service;

@Service
public class IncubatingBackgroundExecutionService implements BackgroundExecutionService {

	@Override
    public <T> T execute(final T domainObject) {
        final Class<T> cls = uncheckedCast(domainObject.getClass());
        final InvocationHandler methodHandler = newMethodHandler(domainObject, null);
        return newProxy(cls, null, methodHandler);
    }

    @Override
    public <T> T executeMixin(Class<T> mixinClass, Object mixedIn) {
        final T mixin = factoryService.mixin(mixinClass, mixedIn);
        final InvocationHandler methodHandler = newMethodHandler(mixin, mixedIn);
        return newProxy(mixinClass, mixedIn, methodHandler);
    }

    private <T> T newProxy(
            final Class<T> cls,
            final Object mixedInIfAny,
            final InvocationHandler methodHandler) {

        final Class<?>[] interfaces = ArrayExtensions.combine(
                cls.getInterfaces(),
                new Class<?>[] { ProxyEnhanced.class });

        final boolean initialize = mixedInIfAny!=null;


        final Class<?>[] constructorArgTypes = initialize ? new Class<?>[] {mixedInIfAny.getClass()} : _Constants.emptyClasses;
        final Object[] constructorArgs = initialize ? new Object[] {mixedInIfAny} : _Constants.emptyObjects;

        final ProxyFactory<T> proxyFactory = ProxyFactory.builder(cls)
                .interfaces(interfaces)
                .constructorArgTypes(constructorArgTypes)
                .build();

        return initialize
                ? proxyFactory.createInstance(methodHandler, constructorArgs)
                        : proxyFactory.createInstance(methodHandler, false)
                        ;
    }

    /**
     *
     * @param target - the object that is proxied, either a domain object or a mixin around a domain object
     * @param mixedInIfAny - if target is a mixin, then this is the domain object that is mixed-in to.
     */
    private <T> InvocationHandler newMethodHandler(final T target, final Object mixedInIfAny) {

    	return IncubatingCommandInvocationHandler.builder()
    			.commandSchedulerService(commandSchedulerService)
    			.target(target)
    			.mixedInIfAny(mixedInIfAny)
    			.specificationLoader(specificationLoader)
    			.commandDtoServiceInternal(commandDtoServiceInternal)
    			.toplevelCommandSupplier(new CommandContext())
    			.build();
        
    }


    // //////////////////////////////////////

    //@Inject @Any private Instance<BackgroundCommandService> backgroundCommandServices;
    @Inject private CommandSchedulerService commandSchedulerService;
    @Inject private CommandDtoServiceInternal commandDtoServiceInternal;
    //@Inject private CommandContext commandContext;
    @Inject private FactoryService factoryService;
    @Inject private SpecificationLoader specificationLoader;

}
