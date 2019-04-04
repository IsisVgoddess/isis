package isis.incubator.command;

import java.util.List;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.services.command.CommandDtoServiceInternal;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.schema.cmd.v1.ActionDto;
import org.apache.isis.schema.cmd.v1.CommandDto;
import org.apache.isis.schema.cmd.v1.PropertyDto;
import org.springframework.stereotype.Service;

@Service
public class IncubatingCommandDtoServiceInternal implements CommandDtoServiceInternal {

	@Override
	public CommandDto asCommandDto(
			List<ObjectAdapter> targetAdapters, 
			ObjectAction objectAction,
			ObjectAdapter[] argAdapters) {
		
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CommandDto asCommandDto(
			List<ObjectAdapter> targetAdapters, 
			OneToOneAssociation association,
			ObjectAdapter valueAdapterOrNull) {
		
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addActionArgs(
			ObjectAction objectAction, 
			ActionDto actionDto, 
			ObjectAdapter[] argAdapters) {
		
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addPropertyValue(
			OneToOneAssociation property, 
			PropertyDto propertyDto, 
			ObjectAdapter valueAdapter) {
		// TODO Auto-generated method stub
		
	}

}
