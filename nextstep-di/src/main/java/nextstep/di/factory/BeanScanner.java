package nextstep.di.factory;

import nextstep.annotation.Bean;
import nextstep.annotation.ComponentScan;
import nextstep.annotation.Configuration;
import nextstep.di.factory.instantiation.ConstructorInstantiation;
import nextstep.di.factory.instantiation.MethodInstantiation;
import org.reflections.Reflections;
import org.springframework.beans.BeanUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public class BeanScanner {
    private static final int PATH_EMPTY_COUNT = 0;

    private final Reflections reflection;

    public BeanScanner(Object... basePackage) {
        this.reflection = generateReflection(basePackage);
    }

    private Reflections generateReflection(Object... basePackage) {
        return new Reflections(findBasePackage(basePackage));
    }

    private Object[] findBasePackage(Object... basePackage) {
        Object[] basePackagePaths = Arrays.stream(basePackage)
            .filter(object -> object instanceof Class)
            .filter(object -> ((Class) object).isAnnotationPresent(ComponentScan.class))
            .map(object -> (ComponentScan) ((Class) object).getAnnotation(ComponentScan.class))
            .map(ComponentScan::basePackages)
            .distinct()
            .toArray();
        if (basePackagePaths.length != PATH_EMPTY_COUNT) {
            return basePackagePaths;
        }
        return basePackage;
    }

    public BeanCreateMatcher scanBean(Class<? extends Annotation>... annotation) {
        BeanCreateMatcher beanCreateMatcher = scanConstructorBean(annotation);
        return scanMethodBean(beanCreateMatcher);
    }

    private BeanCreateMatcher scanConstructorBean(Class<? extends Annotation>... annotations) {
        BeanCreateMatcher beanCreateMatcher = new BeanCreateMatcher();
        for (Class<? extends Annotation> annotation : annotations) {
            Set<Class<?>> typesAnnotatedWith = reflection.getTypesAnnotatedWith(annotation);
            typesAnnotatedWith.forEach(clazz -> beanCreateMatcher.put(clazz, new ConstructorInstantiation(clazz)));
        }
        return beanCreateMatcher;
    }

    private BeanCreateMatcher scanMethodBean(BeanCreateMatcher beanCreateMatcher) {
        Set<Class<?>> typesAnnotatedWith = reflection.getTypesAnnotatedWith(Configuration.class);
        typesAnnotatedWith.forEach(configurationClazz -> registerMethodBean(beanCreateMatcher, configurationClazz));
        return beanCreateMatcher;
    }

    private void registerMethodBean(BeanCreateMatcher beanCreateMatcher, Class<?> configurationClazz) {
        Set<Method> methods = Arrays.stream(configurationClazz.getDeclaredMethods())
            .filter(method -> method.isAnnotationPresent(Bean.class))
            .collect(Collectors.toSet());
        for (Method method : methods) {
            beanCreateMatcher.put(method.getReturnType(),
                new MethodInstantiation(method, BeanUtils.instantiateClass(configurationClazz)));
        }
    }
}
