package isis.incubator.command;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.function.Supplier;

import org.apache.isis.applib.services.background.CommandSchedulerService;
import org.apache.isis.applib.services.command.Command;
import org.apache.isis.core.metamodel.adapter.ObjectAdapterProvider;
import org.apache.isis.core.metamodel.services.command.CommandDtoServiceInternal;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;

import lombok.Builder;
import lombok.NonNull;

@Builder
class IncubatingCommandInvocationHandler<T> implements InvocationHandler {
	
	@NonNull private final CommandSchedulerService commandSchedulerService;
	@NonNull private final T target;
    private final Object mixedInIfAny;
    @NonNull private final SpecificationLoader specificationLoader;
    @NonNull private final CommandDtoServiceInternal commandDtoServiceInternal;
    @NonNull private final Supplier<Command> toplevelCommandSupplier;
    @NonNull private final Supplier<ObjectAdapterProvider> adapterProviderSupplier;

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		// TODO Auto-generated method stub
		return null;
	}
	
}
