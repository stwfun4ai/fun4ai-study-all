**Spring源码：**

1. IOC创建过程
2. bean实例化过程
3. 循环依赖
4. AOP
5. 事件发布监听









# Bean的生命周期

 Bean工厂实现应该尽可能支持标准的Bean生命周期接口。完整的初始化方法及其标准顺序是:

- BeanNameAware's {@code setBeanName}
* BeanClassLoaderAware's {@code setBeanClassLoader}
- BeanFactoryAware's {@code setBeanFactory}
- EnvironmentAware's {@code setEnvironment}
- EmbeddedValueResolverAware's {@code setEmbeddedValueResolver}
- ResourceLoaderAware's {@code setResourceLoader}   (only applicable when running in an application context)
- ApplicationEventPublisherAware's {@code setApplicationEventPublisher}   (only applicable when running in an application context)
- MessageSourceAware's {@code setMessageSource}   (only applicable when running in an application context)
- ApplicationContextAware's {@code setApplicationContext}   (only applicable when running in an application context)
- ServletContextAware's {@code setServletContext}   (only applicable when running in a web application context)
- {@code postProcessBeforeInitialization} methods of BeanPostProcessors
- InitializingBean's {@code afterPropertiesSet}
- a custom init-method definition
- {@code postProcessAfterInitialization} methods of BeanPostProcessors





```java
/**
* <p>Bean factory implementations should support the standard bean lifecycle interfaces
* as far as possible. The full set of initialization methods and their standard order is:
* <ol>
* <li>BeanNameAware's {@code setBeanName}
* <li>BeanClassLoaderAware's {@code setBeanClassLoader}
* <li>BeanFactoryAware's {@code setBeanFactory}
* <li>EnvironmentAware's {@code setEnvironment}
* <li>EmbeddedValueResolverAware's {@code setEmbeddedValueResolver}
* <li>ResourceLoaderAware's {@code setResourceLoader}
* (only applicable when running in an application context)
* <li>ApplicationEventPublisherAware's {@code setApplicationEventPublisher}
* (only applicable when running in an application context)
* <li>MessageSourceAware's {@code setMessageSource}
* (only applicable when running in an application context)
* <li>ApplicationContextAware's {@code setApplicationContext}
* (only applicable when running in an application context)
* <li>ServletContextAware's {@code setServletContext}
* (only applicable when running in a web application context)
* <li>{@code postProcessBeforeInitialization} methods of BeanPostProcessors
* <li>InitializingBean's {@code afterPropertiesSet}
* <li>a custom init-method definition
* <li>{@code postProcessAfterInitialization} methods of BeanPostProcessors
* </ol>
*
* <p>On shutdown of a bean factory, the following lifecycle methods apply:
* <ol>
* <li>{@code postProcessBeforeDestruction} methods of DestructionAwareBeanPostProcessors
* <li>DisposableBean's {@code destroy}
* <li>a custom destroy-method definition
* </ol>
*/
```

![image-20210111011145251](images/spring bean lifecycle.png)



BeanDefinitionReader



BeanFactory



PostProcessor



FactoryBean 生成特殊的对象

- isSingleton
- getObject
- getObjectType





Environment 调用系统环境的属性值

`System.getenv();`

`System.getProperties();`





# 三级缓存

```java
/** Cache of singleton objects: bean name to bean instance. */
	private final Map<String, Object> singletonObjects = new ConcurrentHashMap<>(256);

	/** Cache of singleton factories: bean name to ObjectFactory. */
	private final Map<String, ObjectFactory<?>> singletonFactories = new HashMap<>(16);

	/** Cache of early singleton objects: bean name to bean instance. */
	private final Map<String, Object> earlySingletonObjects = new HashMap<>(16);
```



- 一级缓存`singletonObjects`：成品对象，实例化和初始化都完成

- 二级缓存`earlySingletonObjects`：半成品对象，实例化完成但未初始化


- 三级缓存`singletonFactories`：lambda表达式（获取对象的一个匿名内部类）	回调机制






我们刚开始创建对象时有一个循环，正常先创建A再创建B；但是如果有循环依赖的话，在创建A的过程中就要把B的对象创建了。



1. 每次获取bean对象的时候先从一级缓存中获取值。
2. 创建B对象的目的是在给A填充属性的时候发现需要B对象，所以顺带创建了B对象。













二级缓存也可以解决循环依赖问题，前提是没有使用aop不需要创建代理对象。

**三级缓存是为了解决代理过程中的循环依赖问题。**



总结：我们在获取对象时通过name获取，如果原始对象和代理对象同时存在的话，无法选择获取哪一个。使用lambda表达式代表了一种回调机制，当需要使用当前对象的时候，通过lambda表达式追踪返回一个确定的最终版本对象，而不需要判断有几个对象，因为是替换的过程，最终只会有一个对象。















































