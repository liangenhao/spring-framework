---
title: 05-Spring源码学习-IOC-加载Bean
date: 2019-04-16
categories: 源码学习
tags: [Spring]
---

```java
MyTestBean bean = (MyTestBean)factory.getBean("myTestBean");
```

在容器初始化完成后，显示或者隐式地调用 `BeanFactory#getBean(String name)` 方法时，则会触发加载 Bean 阶段。

`DefaultListableBeanFactory`的**`getBean(String)`方法的实现，来自于`AbstractBeanFactory`的默认实现。**



## AbstractBeanFactory#getBean(String)

```java
// org.springframework.beans.factory.support.AbstractBeanFactory#getBean(java.lang.String)
public Object getBean(String name) throws BeansException {
  // name : 要获取的 bean 的名称
  // requiredType : 要获取 bean 的类型
  // args : 创建 Bean 时传递的参数。这个参数仅限于创建 Bean 时使用
  // typeCheckOnly : 是否仅做类型检查，false：表示除了做类型检查，还要做些其他的事情
  return doGetBean(name, null, null, false);
}
protected <T> T doGetBean(final String name, @Nullable final Class<T> requiredType,
                          @Nullable final Object[] args, boolean typeCheckOnly) throws BeansException {
  // 1. 转换对应的beanName
  // 1.1 返回 bean 名称，剥离工厂引用前缀(&)。
  // 1.2 如果 name 是 alias ，则获取对应映射的 beanName 。
  final String beanName = transformedBeanName(name);
  Object bean;

  // 2. 尝试从缓存中或者实例工厂中加载单例bean
  // 单例在Spring的同一个容器中只会被创建一次，后续再获取Bean，就直接从单例缓存中获取了。
  // 因为在创建单例bean的时候会存在依赖注入的情况，而在创建依赖的时候为了避免循环依赖，
  // 在Spring中创建bean的原则是不等bean创建完成就会将bean的ObjectFactory提早曝光加入到缓存中，
  // 一旦下一个bean创建时候需要依赖上一个bean则直接使用ObjectFactory
  // Eagerly check singleton cache for manually registered singletons.
  Object sharedInstance = getSingleton(beanName);
  // 从缓存/实例工厂中获取到单例bean
  if (sharedInstance != null && args == null) {
    if (logger.isTraceEnabled()) {
      if (isSingletonCurrentlyInCreation(beanName)) {
        logger.trace("Returning eagerly cached instance of singleton bean '" + beanName +
                     "' that is not fully initialized yet - a consequence of a circular reference");
      }
      else {
        logger.trace("Returning cached instance of singleton bean '" + beanName + "'");
      }
    }
    // 从缓存中得到了bean的原始状态，则需要对bean进行实例化
    // 例如：我们需要对工厂bean进行处理，得到的其实是工厂bean的初始状态，
    // 但是我们真正需要的是工厂bean中定义的factory-method方法中返回的bean。
    // getObjectForBeanInstance 方法就是完成这个工作的
    bean = getObjectForBeanInstance(sharedInstance, name, beanName, null);
  }

  else {
    // Fail if we're already creating this bean instance:
    // We're assumably within a circular reference.
    // 3. 原型模式的依赖检查
    // 只有在单例模式下才会尝试解决循环依赖
    // 原型模式下，如果存在循环依赖：
    // A中有B属性，B中有A属性，那么当依赖注入时，就会产生当A还未创建完的时候，
    // 因为对于B的创建再次返回创建A，造成循环依赖，直接抛出异常
    // 为什么原型模式不处理循环依赖：因为单例模式是使用缓存的，但原型模式无法使用缓存的。
    if (isPrototypeCurrentlyInCreation(beanName)) {
      throw new BeanCurrentlyInCreationException(beanName);
    }

    // Check if bean definition exists in this factory.
    BeanFactory parentBeanFactory = getParentBeanFactory();
    // 4. 如果当前容器中所有已经加载的beanDefinition中不包括beanName，则从 parentBeanFactory（父容器） 中查找
    if (parentBeanFactory != null && !containsBeanDefinition(beanName)) {
      // Not found -> check parent.
      // 转换对应beanName，并且若name以&开头，则beanName也加上&
      String nameToLookup = originalBeanName(name);
      // 如果，父类容器为 AbstractBeanFactory ，直接递归查找
      if (parentBeanFactory instanceof AbstractBeanFactory) {
        return ((AbstractBeanFactory) parentBeanFactory).doGetBean(
          nameToLookup, requiredType, args, typeCheckOnly);
      }
      // 用明确的 args 从 parentBeanFactory 中，获取 Bean 对象
      else if (args != null) {
        // Delegation to parent with explicit args.
        return (T) parentBeanFactory.getBean(nameToLookup, args);
      }
      // 用明确的 requiredType 从 parentBeanFactory 中，获取 Bean 对象
      else if (requiredType != null) {
        // No args -> delegate to standard getBean method.
        return parentBeanFactory.getBean(nameToLookup, requiredType);
      }
      // 直接使用 nameToLookup 从 parentBeanFactory 获取 Bean 对象
      else {
        return (T) parentBeanFactory.getBean(nameToLookup);
      }
    }
    // 5. 如果不是仅仅做类型检查则是创建bean，这里要进行记录
    if (!typeCheckOnly) {
      markBeanAsCreated(beanName);
    }

    try {
      // 6. 从容器中获取 beanName 相应的 GenericBeanDefinition 对象，并将其转换为 RootBeanDefinition 对象
      // 转换的同时，如果父类 bean 不为空的话，则会一并合并父类的属性。
      final RootBeanDefinition mbd = getMergedLocalBeanDefinition(beanName);
      // 检查给定的合并的 BeanDefinition
      checkMergedBeanDefinition(mbd, beanName, args);

      // Guarantee initialization of beans that the current bean depends on.
      String[] dependsOn = mbd.getDependsOn();
      // 7. 如果存在依赖，则需要递归实例化依赖的bean
      if (dependsOn != null) {
        for (String dep : dependsOn) {
          // 若给定的依赖 bean 已经注册为依赖给定的 bean
          // 即循环依赖的情况，抛出 BeanCreationException 异常
          if (isDependent(beanName, dep)) {
            throw new BeanCreationException(mbd.getResourceDescription(), beanName,
                                            "Circular depends-on relationship between '" + beanName + "' and '" + dep + "'");
          }
          // 缓存依赖调用
          registerDependentBean(dep, beanName);
          try {
            // 递归处理依赖 Bean
            getBean(dep);
          }
          catch (NoSuchBeanDefinitionException ex) {
            throw new BeanCreationException(mbd.getResourceDescription(), beanName,
                                            "'" + beanName + "' depends on missing bean '" + dep + "'", ex);
          }
        }
      }

      // 8. mbd 本身 bean 的实例化：不同作用域的 Bean 实例化

      // Create bean instance.
      // 单例模式
      if (mbd.isSingleton()) {
        sharedInstance = getSingleton(beanName, () -> {
          try {
            // 创建一个bean
            return createBean(beanName, mbd, args);
          }
          catch (BeansException ex) {
            // Explicitly remove instance from singleton cache: It might have been put there
            // eagerly by the creation process, to allow for circular reference resolution.
            // Also remove any beans that received a temporary reference to the bean.
            destroySingleton(beanName);
            throw ex;
          }
        });
        // 实例化bean
        bean = getObjectForBeanInstance(sharedInstance, name, beanName, mbd);
      }
      // 原型模式：创建一个新的
      else if (mbd.isPrototype()) {
        // It's a prototype -> create a new instance.
        Object prototypeInstance = null;
        try {
          // 创建前，记录prototypesCurrentlyInCreation的值，为了原型模式的依赖检查
          beforePrototypeCreation(beanName);
          prototypeInstance = createBean(beanName, mbd, args);
        }
        finally {
          // 创建后，删除记录prototypesCurrentlyInCreation的值。
          afterPrototypeCreation(beanName);
        }
        // 实例化
        bean = getObjectForBeanInstance(prototypeInstance, name, beanName, mbd);
      }
      // 其他作用域
      else {
        // 从 BeanDefinition 中获取作用域
        String scopeName = mbd.getScope();
        final Scope scope = this.scopes.get(scopeName);
        if (scope == null) {
          throw new IllegalStateException("No Scope registered for scope name '" + scopeName + "'");
        }
        try {
          Object scopedInstance = scope.get(beanName, () -> {
            // 创建前，记录prototypesCurrentlyInCreation的值，为了原型模式的依赖检查
            beforePrototypeCreation(beanName);
            try {
              return createBean(beanName, mbd, args);
            }
            finally {
              // 创建后，删除记录prototypesCurrentlyInCreation的值。
              afterPrototypeCreation(beanName);
            }
          });
          // 实例化
          bean = getObjectForBeanInstance(scopedInstance, name, beanName, mbd);
        }
        catch (IllegalStateException ex) {
          throw new BeanCreationException(beanName,
                                          "Scope '" + scopeName + "' is not active for the current thread; consider " +
                                          "defining a scoped proxy for this bean if you intend to refer to it from a singleton",
                                          ex);
        }
      }
    }
    catch (BeansException ex) {
      cleanupAfterBeanCreationFailure(beanName);
      throw ex;
    }
  }

  // 9. 类型转换
  // Check if required type matches the type of the actual bean instance.
  // 检查需要的类型是否符合 bean 的实际类型
  if (requiredType != null && !requiredType.isInstance(bean)) {
    try {
      // 执行转换
      T convertedBean = getTypeConverter().convertIfNecessary(bean, requiredType);
      if (convertedBean == null) {
        // 转换失败，抛出 BeanNotOfRequiredTypeException 异常
        throw new BeanNotOfRequiredTypeException(name, requiredType, bean.getClass());
      }
      return convertedBean;
    }
    catch (TypeMismatchException ex) {
      if (logger.isTraceEnabled()) {
        logger.trace("Failed to convert bean '" + name + "' to required type '" +
                     ClassUtils.getQualifiedName(requiredType) + "'", ex);
      }
      throw new BeanNotOfRequiredTypeException(name, requiredType, bean.getClass());
    }
  }
  // requiredType == null 或者 requiredType 不符合 bean 的实际类型
  // 若requiredType == null ，返回的就是 Object 对象
  // 直接强转
  return (T) bean;
}
```

### 转换对应beanName

详见 [转换对应beanName](05.1-Spring源码学习-IOC-加载Bean-转换对应beanName.md)

### 尝试从缓存中加载单例

详见 [尝试从缓存中加载单例](05.2-Spring源码学习-IOC-加载Bean-尝试从缓存中加载单例.md)

### 原型模式的依赖检查

TODO



## 参考资料

- 芋道源码 精尽 Spring 源码分析
- 《Spring源码深度解析 第2版》