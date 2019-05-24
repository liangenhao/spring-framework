---
title: 06-Spring源码学习-深入学习Aware接口
date: 2019-05-23
categories: 源码学习
tags: [Spring]
---

回顾创建Bean过程中[初始化Bean](05.8.6-Spring源码学习-IOC-加载Bean-创建Bean-初始化Bean.md)的第一步：激活`Aware`方法。

下面深入学习`Aware`接口。

## Aware接口

### Aware接口的作用

1. 标记接口。
2. **实现了该接口的bean是具有被Spring容器通知的能力**，通知的方式采用**回调**的方式。
3. `Aware`接口是一个空接口，实际的方法签名由各个子接口来确定。

### Aware接口的命名规则

子接口的实现通常**只会有一个单参数的`set`方法**。**该`set`方法的命名规则为`set`+去掉接口命中的`Aware`后缀。**

> 例如`BeanNameAware`接口的set方法：`setBeanName()`。
>
> `ApplicationContextAware`接口的set方法：`setApplicationContext()`。

### Aware接口是如何工作的

`Aware`的子接口提供一个`SetXxx`方法，该方法的作用就是设置属性值的。

我们可以从[初始化Bean](05.8.6-Spring源码学习-IOC-加载Bean-创建Bean-初始化Bean.md)中的激活Aware方法看到它是如何工作的:

```java
// org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory#invokeAwareMethods
private void invokeAwareMethods(final String beanName, final Object bean) {
  if (bean instanceof Aware) {
    if (bean instanceof BeanNameAware) {
      ((BeanNameAware) bean).setBeanName(beanName);
    }
    if (bean instanceof BeanClassLoaderAware) {
      ClassLoader bcl = getBeanClassLoader();
      if (bcl != null) {
        ((BeanClassLoaderAware) bean).setBeanClassLoader(bcl);
      }
    }
    if (bean instanceof BeanFactoryAware) {
      ((BeanFactoryAware) bean).setBeanFactory(AbstractAutowireCapableBeanFactory.this);
    }
  }
}
```

> 其他的`Aware`子类也类似。

## Aware子类

常用的`Aware`子类：

- `LoadTimeWeaverAware`：加载Spring Bean时织入第三方模块，如AspectJ
- `BeanClassLoaderAware`：加载Spring Bean的类加载器
- `BootstrapContextAware`：资源适配器BootstrapContext，如JCA,CCI
- `ResourceLoaderAware`：底层访问资源的加载器
- `BeanFactoryAware`：声明BeanFactory
- `PortletConfigAware`：PortletConfig
- `PortletContextAware`：PortletContext
- `ServletConfigAware`：ServletConfig
- `ServletContextAware`：ServletContext
- `MessageSourceAware`：国际化
- `ApplicationEventPublisherAware`：应用事件
- `NotificationPublisherAware`：JMX通知
- `BeanNameAware`：声明Spring Bean的名字

## 总结

`Aware`真正的含义：是 Spring 容器在初始化主动检测当前 bean 是否实现了 Aware 接口，如果实现了则回调其 set 方法将相应的参数设置给该 bean ，这个时候该 bean 就从 Spring 容器中取得相应的资源。



## 参考资料

- 芋道源码 精尽 Spring 源码分析