package isis.incubator.command;

import org.apache.isis.applib.services.background.CommandSchedulerService;
import org.apache.isis.applib.services.command.Command;
import org.apache.isis.schema.cmd.v1.CommandDto;
import org.springframework.stereotype.Service;

@Service
public class CommandSchedulerServiceInMemory implements CommandSchedulerService {

	@Override
	public void schedule(
			CommandDto dto, 
			Command parentCommand, 
			String targetClassName, 
			String targetActionName,
			String targetArgs) {
		
		// TODO Auto-generated method stub
		
	}

}
