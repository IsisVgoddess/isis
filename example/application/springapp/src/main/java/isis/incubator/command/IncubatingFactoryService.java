package isis.incubator.command;

import org.apache.isis.applib.services.factory.FactoryService;
import org.springframework.stereotype.Service;

@Service
public class IncubatingFactoryService implements FactoryService {

	@Override
	public <T> T instantiate(Class<T> domainClass) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> T mixin(Class<T> mixinClass, Object mixedIn) {
		// TODO Auto-generated method stub
		return null;
	}

}
