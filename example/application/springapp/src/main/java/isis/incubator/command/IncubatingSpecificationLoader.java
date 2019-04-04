package isis.incubator.command;

import java.util.List;

import org.apache.isis.core.metamodel.spec.ObjectSpecId;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.core.metamodel.specloader.specimpl.IntrospectionState;
import org.springframework.stereotype.Service;

@Service
public class IncubatingSpecificationLoader implements SpecificationLoader {

	@Override
	public void init() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void shutdown() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<ObjectSpecification> currentSpecifications() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ObjectSpecification lookupBySpecId(ObjectSpecId objectSpecId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void reloadSpecification(Class<?> domainType) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public ObjectSpecification loadSpecification(Class<?> domainType, IntrospectionState upTo) {
		// TODO Auto-generated method stub
		return null;
	}

}
