---
title: 12-Spring源码学习-容器功能扩展-ApplicationContext的refresh()方法
date: 2019-06-02
categories: 源码学习
tags: [Spring]
---

`refresh()`方法是`ApplicationContext`体系中最关键的一个方法。

`refresh()`方法定义在`ConfigurableApplicationContext`中，实现在`AbstractApplicationContext`中。

`refresh()`方法作用：刷新 Spring 的应用上下文。

```java
// org.springframework.context.support.AbstractApplicationContext#refresh
public void refresh() throws BeansException, IllegalStateException {
  synchronized (this.startupShutdownMonitor) {
    // Prepare this context for refreshing.
    // 准备上下文以进行刷新
    prepareRefresh();

    // Tell the subclass to refresh the internal bean factory.
    // 初始化BeanFactory，并进行xml文件读取
    ConfigurableListableBeanFactory beanFactory = obtainFreshBeanFactory();

    // Prepare the bean factory for use in this context.
    // 准备BeanFactory，对BeanFactory进行各种功能填充
    prepareBeanFactory(beanFactory);

    try {
      // Allows post-processing of the bean factory in context subclasses.
      // 子类覆盖方法做额外的处理，即子类处理自定义的 BeanFactoryPostProcess
      postProcessBeanFactory(beanFactory);

      // Invoke factory processors registered as beans in the context.
      // 激活各种 BeanFactoryPostProcessor
      invokeBeanFactoryPostProcessors(beanFactory);

      // Register bean processors that intercept bean creation.
      // 注册拦截Bean创建BeanPostProcessor，这里只是注册，真正的调用在getBean的时候
      registerBeanPostProcessors(beanFactory);

      // Initialize message source for this context.
      // 为上下文初始化Message源，即不同语言的消息体，国际化处理
      initMessageSource();

      // Initialize event multicaster for this context.
      // 初始化应用消息广播器，并放入 applicationEventMulticaster bean 中
      initApplicationEventMulticaster();

      // Initialize other special beans in specific context subclasses.
      // 留给子类来初始化其他的bean
      onRefresh();

      // Check for listener beans and register them.
      // 在所有注册的bean中查找 Listener bean，注册到消息广播器中
      registerListeners();

      // Instantiate all remaining (non-lazy-init) singletons.
      // 初始化剩下的单实例（非延迟加载）
      finishBeanFactoryInitialization(beanFactory);

      // Last step: publish corresponding event.
      // 完成刷新过程，通知生命周期处理器 lifecycleProcessor 刷新过程
      // 同时发出 ContextRefreshEvent 通知别人
      finishRefresh();
    }

    catch (BeansException ex) {
      if (logger.isWarnEnabled()) {
        logger.warn("Exception encountered during context initialization - " +
                    "cancelling refresh attempt: " + ex);
      }

      // Destroy already created singletons to avoid dangling resources.
      // 销毁已经创建的bean
      destroyBeans();

      // Reset 'active' flag.
      // 重置容器激活标签
      cancelRefresh(ex);

      // Propagate exception to caller.
      throw ex;
    }

    finally {
      // Reset common introspection caches in Spring's core, since we
      // might not ever need metadata for singleton beans anymore...
      resetCommonCaches();
    }
  }
}
```

## 环境准备-prepareRefresh

【作用】：上下文刷新前准备工作。对系统的环境变量或系统属性进行准备和校验。

在某种情况下项目的使用需要读取某些系统变量， 而这个变量的设置很可能会影响着系统的正确性，如环境变量中必须设置某个值才能运行，否则不能运行，这个时候可以在这里加这个校验，重写 `initPropertySources`方法就好了。

```java
protected void prepareRefresh() {
  // Switch to active.
  // 设置开始时间
  this.startupDate = System.currentTimeMillis();
  // 设置状态：非关闭状态、激活状态。
  this.closed.set(false);
  this.active.set(true);

  if (logger.isDebugEnabled()) {
    if (logger.isTraceEnabled()) {
      logger.trace("Refreshing " + this);
    }
    else {
      logger.debug("Refreshing " + getDisplayName());
    }
  }

  // Initialize any placeholder property sources in the context environment.
  // 初始化上下文环境中的占位符属性来源（由子类实现）
  initPropertySources();

  // Validate that all properties marked as required are resolvable:
  // see ConfigurablePropertyResolver#setRequiredProperties
  // 对属性进行必要的校验
  getEnvironment().validateRequiredProperties();

  // Store pre-refresh ApplicationListeners...
  // 存储预刷新的 ApplicationListener
  if (this.earlyApplicationListeners == null) {
    this.earlyApplicationListeners = new LinkedHashSet<>(this.applicationListeners);
  }
  else {
    // Reset local application listeners to pre-refresh state.
    this.applicationListeners.clear();
    this.applicationListeners.addAll(this.earlyApplicationListeners);
  }

  // Allow for the collection of early ApplicationEvents,
  // to be published once the multicaster is available...
  // 允许收集早起的 ApplicationEvent
  this.earlyApplicationEvents = new LinkedHashSet<>();
}
```

该方法最重要的两个方法：`initPropertySources()`和`validateRequiredProperties()`。

### initPropertySources

用户可以根据需要重写该方法，进行个性化的属性处理以及设置。

### validateRequiredProperties

该方法对属性进行验证。

### 应用

例如，如果我们工程在运行过程中某个值（VAR）需要从系统环境变量中获取。如果没有这个值就没法工作。则可以使用一下方式：

首先我们对`ClassPathXmlApplicationContext`进行扩展：

```java
public class MyClassPathXmlApplicationContext extends ClassPathXmlApplicationContext {
  public MyClassPathXmlApplicationContext(String... configLocations) {
    super(configLocations);
  }
  
  protected void initPropertySources() {
    // 添加验证请求
    getEnvironment().setRequiredProperties("VAR");
  }
}
```

在`initPropertySources`方法中，添加验证请求。

然后在`validateRequiredProperties`方法中会校验，如果检测到如果没有`VAR`这个环境变量。则抛出异常。

## 加载BeanFactory-obtainFreshBeanFactory

【作用】：创建并初始化`BeanFactory`。

`ApplicationContext`是对`BeanFactory`的功能上的扩展。不但包含了`BeanFactory`的全部功能，还在其基础上添加了大量的扩展应用。

**通过`obtainFreshBeanFactory()`方法，`ApplicationContext`就拥有了`BeanFactory`的全部功能。**

```java
// org.springframework.context.support.AbstractApplicationContext#obtainFreshBeanFactory
protected ConfigurableListableBeanFactory obtainFreshBeanFactory() {
  // 刷新 BeanFactory
  refreshBeanFactory();
  // 获取 BeanFactory
  return getBeanFactory();
}

// org.springframework.context.support.AbstractRefreshableApplicationContext#refreshBeanFactory
@Override
protected final void refreshBeanFactory() throws BeansException {
  // 若已经有 BeanFactory 对象了，销毁它们的bean和BeanFactory
  if (hasBeanFactory()) {
    destroyBeans();
    closeBeanFactory();
  }
  try {
    // 创建 DefaultListableBeanFactory
    DefaultListableBeanFactory beanFactory = createBeanFactory();
    // 为了序列化指定id，如果需要的话，让这个BeanFactory从id反序列化到BeanFactory对象
    beanFactory.setSerializationId(getId());
    // 定制 BeanFactory，设置相关属性，包括是否允许覆盖同名称的不同定义的对象以及循环依赖以及设置@Autowired和@Qualifier注解解析器
    customizeBeanFactory(beanFactory);
    // 初始化 DocumentReader，进行xml文件读取和解析
    // 该方法有不同的实现：
    // AbstractXmlApplicationContext，进行xml文件读取和解析
    // AnnotationConfigWebApplicationContext, 进行注解的解析。
    loadBeanDefinitions(beanFactory);
    synchronized (this.beanFactoryMonitor) {
      this.beanFactory = beanFactory;
    }
  }
  catch (IOException ex) {
    throw new ApplicationContextException("I/O error parsing bean definition source for " + getDisplayName(), ex);
  }
}
```

步骤：

1. 创建`DefaultListableBeanFactory`对象。

   > 关于`DefaultListableBeanFactory` 参考 [总览](02-Spring源码学习-IOC-总览.md)

2. 指定序列化id。

3. 定制`BeanFactory`。

4. 加载`BeanDefinition`。

   > 这里就是容器的资源装载，关于xml的解析，详见 [资源装载](04-Spring源码学习-IOC-资源装载.md)。
   
5. 将创建好的 bean 的引用赋值给`ApplicationContext`。

## 填充BeanFactory功能-prepareBeanFactory

```java
protected void prepareBeanFactory(ConfigurableListableBeanFactory beanFactory) {
  // Tell the internal bean factory to use the context's class loader etc.
  // 设置 beanFactory 的 classLoader
  beanFactory.setBeanClassLoader(getClassLoader());
  // 设置 beanFactory 的表达式语言处理器，默认可以使用#{bean.xxx}的形式来调用相关属性值
  beanFactory.setBeanExpressionResolver(new StandardBeanExpressionResolver(beanFactory.getBeanClassLoader()));
  // 为 beanFactory 增加一个默认的 PropertyEditor
  beanFactory.addPropertyEditorRegistrar(new ResourceEditorRegistrar(this, getEnvironment()));

  // Configure the bean factory with context callbacks.
  // 为 beanFactory 增加 ApplicationContextAwareProcessor
  beanFactory.addBeanPostProcessor(new ApplicationContextAwareProcessor(this));
  // 设置 beanFactory 忽略自动装配的接口
  beanFactory.ignoreDependencyInterface(EnvironmentAware.class);
  beanFactory.ignoreDependencyInterface(EmbeddedValueResolverAware.class);
  beanFactory.ignoreDependencyInterface(ResourceLoaderAware.class);
  beanFactory.ignoreDependencyInterface(ApplicationEventPublisherAware.class);
  beanFactory.ignoreDependencyInterface(MessageSourceAware.class);
  beanFactory.ignoreDependencyInterface(ApplicationContextAware.class);

  // beanfactory interface not registered as resolvable type in a plain factory.
  // MessageSource registered (and found for autowiring) as a bean.
  // 设置几个自动装配的特殊规则
  beanFactory.registerResolvableDependency(BeanFactory.class, beanFactory);
  beanFactory.registerResolvableDependency(ResourceLoader.class, this);
  beanFactory.registerResolvableDependency(ApplicationEventPublisher.class, this);
  beanFactory.registerResolvableDependency(ApplicationContext.class, this);

  // Register early post-processor for detecting inner beans as ApplicationListeners.
  beanFactory.addBeanPostProcessor(new ApplicationListenerDetector(this));

  // Detect a LoadTimeWeaver and prepare for weaving, if found.
  // 增加对AspectJ的支持
  if (beanFactory.containsBean(LOAD_TIME_WEAVER_BEAN_NAME)) {
    beanFactory.addBeanPostProcessor(new LoadTimeWeaverAwareProcessor(beanFactory));
    // Set a temporary ClassLoader for type matching.
    beanFactory.setTempClassLoader(new ContextTypeMatchClassLoader(beanFactory.getBeanClassLoader()));
  }

  // Register default environment beans.
  // 注册默认的系统环境bean
  if (!beanFactory.containsLocalBean(ENVIRONMENT_BEAN_NAME)) {
    beanFactory.registerSingleton(ENVIRONMENT_BEAN_NAME, getEnvironment());
  }
  if (!beanFactory.containsLocalBean(SYSTEM_PROPERTIES_BEAN_NAME)) {
    beanFactory.registerSingleton(SYSTEM_PROPERTIES_BEAN_NAME, getEnvironment().getSystemProperties());
  }
  if (!beanFactory.containsLocalBean(SYSTEM_ENVIRONMENT_BEAN_NAME)) {
    beanFactory.registerSingleton(SYSTEM_ENVIRONMENT_BEAN_NAME, getEnvironment().getSystemEnvironment());
  }
}
```

## postProcessBeanFactory

`postProcessBeanFactory()`提供给子类覆盖的额外处理。在`AbstractApplicationContext`中是默认的空实现。

例如在`AbstractRefreshableWebApplicationContext`中的实现如下：

```java
// org.springframework.web.context.support.AbstractRefreshableWebApplicationContext#postProcessBeanFactory
protected void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) {
  // 添加 ServletContextAwareProcessor
  beanFactory.addBeanPostProcessor(new ServletContextAwareProcessor(this.servletContext, this.servletConfig));
  // 忽略 ServletContextAware 和 ServletConfigAware
  beanFactory.ignoreDependencyInterface(ServletContextAware.class);
  beanFactory.ignoreDependencyInterface(ServletConfigAware.class);

  // 注册 WEB 应用特定的域（scope）到 beanFactory 中，以便 WebApplicationContext 可以使用它们。比如 “request” , “session” , “globalSession” , “application”
  WebApplicationContextUtils.registerWebApplicationScopes(beanFactory, this.servletContext);
  // 注册 WEB 应用特定的 Environment bean 到 beanFactory 中，以便WebApplicationContext 可以使用它们。如：”contextParameters”, “contextAttributes”
  WebApplicationContextUtils.registerEnvironmentBeans(beanFactory, this.servletContext, this.servletConfig);
}
```

## invokeBeanFactoryPostProcessors

`invokeBeanFactoryPostProcessors()`方法的作用是激活各种`BeanFactoryPostProcessor`。

> 关于`BeanFactoryPostProcessor`，详见 [深入学习BeanFactoryPostProcessor接口](10-Spring源码学习-深入学习BeanFactoryPostProcessor接口.md)

该方法将具体操作委托给`PostProcessorRegistrationDelegate`类的`invokeBeanFactoryPostProcessors()`方法执行：

```java
// 详见 : 
// org.springframework.context.support.PostProcessorRegistrationDelegate#invokeBeanFactoryPostProcessors(org.springframework.beans.factory.config.ConfigurableListableBeanFactory, java.util.List<org.springframework.beans.factory.config.BeanFactoryPostProcessor>)
```

> 对所有的`BeanDefinitionRegistryPostProcessors` 、手动注册的`BeanFactoryPostProcessor`以及通过配置文件方式的`BeanFactoryPostProcessor`按照`PriorityOrdered`、`Ordered`、`no ordered`三种方式分开处理、调用。



## registerBeanPostProcessors

注册拦截Bean创建`BeanPostProcessor`，这里只是注册，真正的调用在`getBean()`的时候。

和上一步`invokeBeanFactoryPostProcessors()`一样，该方法委托给了`PostProcessorRegistrationDelegate`类的`registerBeanPostProcessors()`方法执行：

详见：[深入学习BeanPostProcessor接口](07-Spring源码学习-深入学习BeanPostProcessor接口.md) 的`ApplicationContext自动注册BeanPostProcessor`小节。

## initMessageSource

> 初始化上下文中的资源文件，如国际化文件的处理等。

详见 [ApplicationContext相关接口](11-Spring源码学习-容器功能扩展-ApplicationContext相关接口.md)中的`MessageSource`。

## initApplicationEventMulticaster

```java
protected void initApplicationEventMulticaster() {
  ConfigurableListableBeanFactory beanFactory = getBeanFactory();
  // 如果存在 applicationEventMulticaster bean，则获取赋值
  if (beanFactory.containsLocalBean(APPLICATION_EVENT_MULTICASTER_BEAN_NAME)) {
    this.applicationEventMulticaster =
      beanFactory.getBean(APPLICATION_EVENT_MULTICASTER_BEAN_NAME, ApplicationEventMulticaster.class);
    if (logger.isTraceEnabled()) {
      logger.trace("Using ApplicationEventMulticaster [" + this.applicationEventMulticaster + "]");
    }
  }
  else {
    // 没有则新建 SimpleApplicationEventMulticaster，并完成 bean 的注册
    this.applicationEventMulticaster = new SimpleApplicationEventMulticaster(beanFactory);
    beanFactory.registerSingleton(APPLICATION_EVENT_MULTICASTER_BEAN_NAME, this.applicationEventMulticaster);
    if (logger.isTraceEnabled()) {
      logger.trace("No '" + APPLICATION_EVENT_MULTICASTER_BEAN_NAME + "' bean, using " +
                   "[" + this.applicationEventMulticaster.getClass().getSimpleName() + "]");
    }
  }
}
```



## onRefresh

> 留给子类来初始化其他的bean。

该方法需要在所有单例 bean 初始化之前调用。

## registerListeners

> 在所有注册的bean中查找Listener bean，注册到消息广播器中。

## finishBeanFactoryInitialization

> 初始化剩下的单实例（非延迟加载）。

```java
protected void finishBeanFactoryInitialization(ConfigurableListableBeanFactory beanFactory) {
  // Initialize conversion service for this context.
  // 初始化转换器
  if (beanFactory.containsBean(CONVERSION_SERVICE_BEAN_NAME) &&
      beanFactory.isTypeMatch(CONVERSION_SERVICE_BEAN_NAME, ConversionService.class)) {
    beanFactory.setConversionService(
      beanFactory.getBean(CONVERSION_SERVICE_BEAN_NAME, ConversionService.class));
  }

  // Register a default embedded value resolver if no bean post-processor
  // (such as a PropertyPlaceholderConfigurer bean) registered any before:
  // at this point, primarily for resolution in annotation attribute values.
  // 如果之前没有注册 bean 后置处理器（例如PropertyPlaceholderConfigurer），则注册默认的解析器
  if (!beanFactory.hasEmbeddedValueResolver()) {
    beanFactory.addEmbeddedValueResolver(strVal -> getEnvironment().resolvePlaceholders(strVal));
  }

  // Initialize LoadTimeWeaverAware beans early to allow for registering their transformers early.
  // 初始化 LoadTimeWeaverAware
  String[] weaverAwareNames = beanFactory.getBeanNamesForType(LoadTimeWeaverAware.class, false, false);
  for (String weaverAwareName : weaverAwareNames) {
    getBean(weaverAwareName);
  }

  // Stop using the temporary ClassLoader for type matching.
  // 停止使用临时ClassLoader进行类型匹配。
  beanFactory.setTempClassLoader(null);

  // Allow for caching all bean definition metadata, not expecting further changes.
  // 允许缓存所有bean定义元数据，而不期望进一步的更改。
  beanFactory.freezeConfiguration();

  // Instantiate all remaining (non-lazy-init) singletons.
  // 实例化所有剩余（非延迟初始化）单例。
  beanFactory.preInstantiateSingletons();
}
```



## finishRefresh

> 完成刷新过程，通知生命周期处理器`lifecycleProcessor`刷新过程，同时发出`ContextRefreshEvent`通知别人。

Spring 中提供了`Lifecycle`接口，`Lifecycle`接口包含`start/stop`方法，该接口保证在启动时调用`start`方法开始生命周期，并在Spring关闭时调用`stop`方法结束生命周期。

`finishRefresh()`方法保证了这一功能。

```java
protected void finishRefresh() {
  // Clear context-level resource caches (such as ASM metadata from scanning).
  // 清除上下文级资源缓存（例如来自扫描的ASM元数据）。
  clearResourceCaches();

  // Initialize lifecycle processor for this context.
  // 为此上下文初始化生命周期处理器。
  initLifecycleProcessor();

  // Propagate refresh to lifecycle processor first.
  // 首先,将刷新传播到生命周期处理器.
  getLifecycleProcessor().onRefresh();

  // Publish the final event.
  // 发布最终活动
  publishEvent(new ContextRefreshedEvent(this));

  // Participate in LiveBeansView MBean, if active.
  // 如果处于活动状态，请参与LiveBeansView MBean。
  LiveBeansView.registerApplicationContext(this);
}
```

主要是调用 `LifecycleProcessor#onRefresh()` ，并且发布事件。

关于`publishEvent()`，详见 [ApplicationContext相关接口](11-Spring源码学习-容器功能扩展-ApplicationContext相关接口.md) 的 `ApplicationEventPublisher`小节。





## 参考资料

- 芋道源码 精尽 Spring 源码分析
- 《Spring源码深度解析 第2版》