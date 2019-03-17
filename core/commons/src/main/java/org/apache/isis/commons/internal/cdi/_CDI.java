/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.isis.commons.internal.cdi;

import static org.apache.isis.commons.internal.base._NullSafe.isEmpty;
import static org.apache.isis.commons.internal.base._NullSafe.stream;
import static org.apache.isis.commons.internal.base._With.requires;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nullable;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.CDI;
import javax.enterprise.inject.spi.CDIProvider;
import javax.enterprise.util.AnnotationLiteral;
import javax.inject.Qualifier;

import org.apache.isis.commons.internal.base._NullSafe;
import org.apache.isis.commons.internal.context._Context;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.commons.internal.functions._Functions.CheckedRunnable;
import org.apache.isis.core.plugins.ioc.IocPlugin;

import lombok.val;

/**
 * <h1>- internal use only -</h1>
 * <p>
 * Framework internal CDI support.
 * <p>
 * <b>WARNING</b>: Do <b>NOT</b> use any of the classes provided by this package! <br/>
 * These may be changed or removed without notice!
 *
 * @since 2.0.0-M2
 */
public final class _CDI {

    /**
     * Bootstrap CDI if not already present.
     * @param onDiscover - Packages of the specified (stream of) classes will be scanned and found classes 
     * will be added to the set of bean classes for the synthetic bean archive. 
     */
    public static void init(Supplier<Stream<Class<?>>> onDiscover) {
        
        if(cdi().isPresent()) {
            return;
        }
        
        requires(onDiscover, "onDiscover");
        
        // plug in the provider
        final CDIProvider standaloneCDIProvider = IocPlugin.get().getCDIProvider(onDiscover.get());
        CDI.setCDIProvider(standaloneCDIProvider);

        // verify
        if(!cdi().isPresent()) {
            throw _Exceptions.unrecoverable("Could not resolve an instance of CDI.");
        }
        
        // proper CDI lifecycle support utilizing the fact that WELD provides a WeldContainer that 
        // implements AutoCloseable, which we can put on the _Context, such that when _Context.clear()
        // is called, gets properly closed
        final CheckedRunnable onClose = () -> ((AutoCloseable)CDI.current()).close();
        _Context.putSingleton(_CDI_Lifecycle.class, _CDI_Lifecycle.of(onClose));
        
    }
    
    /**
     * Get the CDI BeanManager for the current context.
     * @return non-null
     * @throws RuntimeException - if no BeanManager could be resolved
     */
    public static BeanManager getBeanManager() {
        return cdi().map(CDI::getBeanManager)
                .orElseThrow(()->_Exceptions.unrecoverable("Could not resolve a BeanManager."));
    }
    
    /**
     * Obtains a child Instance for the given required type and additional required qualifiers. 
     * @param subType
     * @param qualifiers
     * @return an optional, empty if passed two instances of the same qualifier type, or an 
     * instance of an annotation that is not a qualifier type
     */
    public static <T> Optional<T> getManagedBean(final Class<T> subType, Collection<Annotation> qualifiers) {
        return getInstance(subType, qualifiers)
                .map(instance->tryGet(instance::get));
    }

    /**
     * Obtains a child Instance for the given required type and additional required qualifiers. 
     * @param subType
     * @param qualifiers
     * @return an optional, empty if passed two instances of the same qualifier type, or an 
     * instance of an annotation that is not a qualifier type
     */
    public static <T> Optional<T> getManagedBean(final Class<T> subType) {
        return getInstance(subType)
                .map(instance->tryGet(instance::get));
    }
    
    /**
     * Obtains a child Instance for the given required type and additional required qualifiers. 
     * @param subType
     * @param qualifiers
     * @return an optional, empty if passed two instances of the same qualifier type, or an 
     * instance of an annotation that is not a qualifier type
     */
    public static <T> Optional<Instance<T>> getInstance(final Class<T> subType, Collection<Annotation> qualifiers) {
        if(isEmpty(qualifiers)) {
            return getInstance(subType);
        }
        
        final Annotation[] _qualifiers = qualifiers.toArray(new Annotation[] {});
        
        return cdi()
                .map(cdi->tryGet(()->cdi.select(subType, _qualifiers)));
    }
    
    /**
     * Obtains a child Instance for the given required type and additional required qualifiers. 
     * @param subType
     * @param qualifiers
     * @return an optional, empty if passed two instances of the same qualifier type, or an 
     * instance of an annotation that is not a qualifier type
     */
    public static <T> Optional<Instance<T>> getInstance(final Class<T> subType) {
        return cdi()
                .map(cdi->tryGet(()->cdi.select(subType)));
    }
    
    
    /**
     * Filters the input array into a collection, such that only annotations are retained, 
     * that are valid qualifiers for CDI.
     * @param annotations
     * @return non-null
     */
    public static List<Annotation> filterQualifiers(final Annotation[] annotations) {
        return stream(annotations)
        .filter(_CDI::isQualifier)
        .collect(Collectors.toList());
    }
    
    /**
     * @param annotation
     * @return whether or not the annotation is a valid qualifier for CDI
     */
    public static boolean isQualifier(Annotation annotation) {
        if(annotation==null) {
            return false;
        }
        return annotation.annotationType().getAnnotationsByType(Qualifier.class).length>0;
    }
    
    // -- GENERIC SINGLETON RESOLVING
    
    /**
     * @return framework's managed singleton
     * @throws NoSuchElementException - if the singleton is not resolvable
     */
    public static <T> T getSingleton(Class<T> type) {
        // first lookup CDI, then lookup _Context; the latter to support unit testing 
        return _CDI.getManagedBean(type)
                .orElseGet(()->getMockedSingleton(type));
    }
        
    // -- UNIT TESTING SUPPORT
    
//    public static void putMockedSingletons(Collection<?> mockedServices) {
//        stream(mockedServices)
//        .forEach(_CDI::putMockedSingleton);
//    }
//    
//    public static <T> T putMockedSingleton(T mockedService) {
//        return putMockedSingleton(_Casts.uncheckedCast(mockedService.getClass()), mockedService);
//    }
//    
//    public static <T> T putMockedSingleton(Class<T> type, T mockedService) {
//        _Context.putSingleton(type, mockedService);
//        return mockedService;
//    }
    
    private static <T> T getMockedSingleton(Class<T> type) {
        requires(type, "type");        
        return _Context.getElseFail(type);
    }
    
    // -- ENUMERATE BEANS
    
    public final static AnnotationLiteral<Any> QUALIFIER_ANY = 
            new AnnotationLiteral<Any>() {
        private static final long serialVersionUID = 1L;};
    
    public static Stream<Bean<?>> streamAllBeans() {
		BeanManager beanManager = _CDI.getBeanManager();
        Set<Bean<?>> beans = beanManager.getBeans(Object.class, _CDI.QUALIFIER_ANY);
        return beans.stream();
	}
    
    // -- FACTORIES
    
	public static class InstanceFactory {

		public static <T> Instance<T> empty() {
			return new _CDI_EmptyInstance<>();
		}
		
		public static <T> Instance<T> singleton(@Nullable T pojo) {
			if(pojo==null) {
				return empty();
			}
			return _CDI_SingletonInstance.of(pojo);
		}

		public static <T> Instance<T> ambiguous(@Nullable Collection<T> collection) {
			val size = _NullSafe.size(collection);
			if(size==0) {
				return empty();
			} 
			if(size==1) {
				if(collection instanceof List) {
					// just to reduce heap pollution when collection is a list
					return singleton(((List<T>)collection).get(0));
				} 
				return singleton(collection.iterator().next());
			}
			return _CDI_AmbiguousInstance.of(collection);
		}

        public static <T, R> Instance<R> mapElements(Instance<T> instance, Function<T, R> elementMapper) {
            
            if(instance.isUnsatisfied()) {
                return empty();
            }
            if(instance.isResolvable()) {
                val element = instance.get();
                return singleton(elementMapper.apply(element));
            }
            val elements = instance.stream()
            .map(elementMapper)
            .collect(Collectors.toList());
            return _CDI_AmbiguousInstance.of(elements);
        }
		
	}
        
    // -- HELPER
    
    private _CDI() {}
    
    /**
     * Get the CDI instance that provides access to the current container. 
     * @return an optional
     */
    private static Optional<CDI<Object>> cdi() {
        try {
            CDI<Object> cdi = CDI.current();
            return Optional.ofNullable(cdi);
        } catch (Exception e) {
            return Optional.empty();
        }
    }
    
    private static <T> T tryGet(final Supplier<T> supplier) {
        try { 
            return supplier.get();  
        } catch (Exception e) {
            return null;
        }
    }
	


}
