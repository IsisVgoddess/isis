package isis.incubator;

import java.util.List;
import java.util.stream.Stream;

import org.apache.isis.applib.IsisApplibModule;
import org.apache.isis.commons.internal.cdi._CDI;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.commons.internal.context._Context;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.config.AppConfig;
import org.apache.isis.config.registry.IsisBeanTypeRegistry;
import org.apache.isis.core.metamodel.MetamodelModule;
import org.apache.isis.core.runtime.RuntimeModule;
import org.apache.isis.core.runtime.services.RuntimeServicesModule;
import org.apache.isis.core.runtime.services.i18n.po.TranslationServicePo;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;

import lombok.val;
import springapp.boot.web.SpringAppManifest;

@Configuration 
@ComponentScan(
		basePackageClasses= {
				IsisIncubatorModule.class,
				IsisApplibModule.class,
				MetamodelModule.class,
				RuntimeModule.class,
				RuntimeServicesModule.class},
		includeFilters= {
				@Filter(type = FilterType.CUSTOM, classes= {BeanScanInterceptorForSpring.class})
		})
public class IsisBoot implements ApplicationContextAware {
	
	//TODO @Autowired AppConfig appConfig;

	@Override
	public void setApplicationContext(ApplicationContext springContext) throws BeansException {
	    
	    val appConfig = new SpringAppManifest();
	    
	    System.out.println("!!!!!!!!!!!!!!!!!!! finalizing config");
	    appConfig.isisConfiguration();
	    System.out.println("!!!!!!!!!!!!!!!!!!! finalized config");
	    
	    _Context.putSingleton(AppConfig.class, appConfig);
	    
	    // init CDI
	    _CDI.init(()->
	    	Stream.concat(
	    			Stream.of(
	    					appConfig.getClass(),
	    					TranslationServicePo.class //FIXME [2033] cannot provision with @Singleton
	    					),
	    			IsisBeanTypeRegistry.current().streamCdiManaged())
	    );
	    
//	    System.out.println("!!!!!!!!!!!!!!!!!!! verify core services");
//	    verifyCoreServices();
	    
		// TODO Auto-generated method stub
		System.out.println("!!!!!!!!!!!!!!!!!!! TODO bootstrap isis from context");
		
		springContext.getBeansOfType(Object.class).forEach(
		(k, v)->{
			
			if(!v.getClass().getName().startsWith("org.apache.isis") ||
					!v.getClass().getName().startsWith("domain")
					) {
				return;
			}
			
			System.out.println(String.format("key='%s', value='%s'", k, v));
		});
		
	}
	
	private final List<Class<?>> coreServices = _Lists.of(
			
			org.apache.isis.config.IsisConfiguration.class,
			org.apache.isis.applib.services.registry.ServiceRegistry.class, 
			org.apache.isis.core.runtime.system.session.IsisSessionFactory.class, 

			org.apache.isis.core.security.authentication.manager.AuthenticationManager.class, 
			org.apache.isis.core.security.authorization.manager.AuthorizationManager.class,
			org.apache.isis.core.security.authentication.AuthenticationSessionProvider.class,

			org.apache.isis.core.metamodel.specloader.SpecificationLoader.class,

			//org.apache.isis.applib.services.eventbus.EventBusService.class,
			org.apache.isis.applib.services.factory.FactoryService.class,

			org.apache.isis.applib.services.i18n.LocaleProvider.class,
			org.apache.isis.applib.services.i18n.TranslationsResolver.class,
			org.apache.isis.applib.services.i18n.TranslationService.class,
			org.apache.isis.applib.services.message.MessageService.class,

			org.apache.isis.applib.services.repository.RepositoryService.class,
			org.apache.isis.applib.services.title.TitleService.class,
			org.apache.isis.applib.services.user.UserService.class,
			org.apache.isis.applib.services.xactn.TransactionService.class,

			org.apache.isis.core.metamodel.services.persistsession.ObjectAdapterService.class,

			org.apache.isis.applib.services.wrapper.WrapperFactory.class,
			org.apache.isis.core.runtime.services.bookmarks.BookmarkServiceInternalDefault.class,

			org.apache.isis.applib.services.homepage.HomePageProviderService.class
			
			);
	
	private void verifyCoreServices() {
		
		coreServices.stream()
		.forEach(type->{
			
			System.out.println("!!! Core Service check: " + type);
			
			val beans = _CDI.select(type);
			if(!beans.isCardinalityOne()) {
				throw _Exceptions.unrecoverable("Core Service not known to CDI: " + type );
			}
		});
		
	}
	

	
//	@Bean
//	public SpecificationLoader specificationLoader(IsisSessionProducerBean isisSessionProducerBean) {
//	    return isisSessionProducerBean.produceSpecificationLoader();
//	}
	
}
