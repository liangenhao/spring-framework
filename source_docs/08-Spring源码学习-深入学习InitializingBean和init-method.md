---
title: 08-Spring源码学习-深入学习InitializingBean和init-method
date: 2019-05-26
categories: 源码学习
tags: [Spring]
---

回顾创建Bean过程中[初始化Bean](05.8.6-Spring源码学习-IOC-加载Bean-创建Bean-初始化Bean.md)的第三步：激活用户自定义的 init 方法。

这个过程涉及了`InitializingBean`接口和`init-method`方法。

## InitializingBean

`InitializingBean`接口仅包含了一个方法`afterPropertiesSet()`：

```java
public interface InitializingBean {

	void afterPropertiesSet() throws Exception;

}
```

Spring 在完成实例化后，设置完所有属性，进行 `Aware`接口和`BeanPostProcessor`前置处理之后，会接着检测当前 bean 对象是否实现了`InitializingBean`接口。如果是，则会调用其 `#afterPropertiesSet()` 方法，进一步调整 bean 实例对象的状态。

> 回看[初始化Bean](05.8.6-Spring源码学习-IOC-加载Bean-创建Bean-初始化Bean.md)第三步中的`invokeInitMethods()`方法，先检测bean是否实现了`InitializingBean`接口，如果是，则调用`afterPropertiesSet()`方法。

虽然该接口为 Spring 容器的扩展性立下了汗马功劳，但是如果真的让我们的业务对象来实现这个接口就显得不是那么的友好了，Spring 的一个核心理念就是无侵入性，但是如果我们业务类实现这个接口就显得 Spring 容器具有侵入性了。所以 Spring 还提供了另外一种实现的方式：**`init-method` 方法**。

## init-method

`init-method`属性用于在 bean 初始化时指定执行方法，可以用来替代实现`InitializingBean`接口。

`init-method`属性与`InitializingBean`接口相比无侵入性。

`init-method`属性的调用时机在`InitializingBean`接口调用完毕后执行。

`init-method`属性可以在配置文件中指定：

```xml
<bean id="initializingBeanTest" class="org.springframework.core.test.InitializingBeanTest"
      init-method="setOtherName">
  <property name="name" value="chenssy 1 号"/>
</bean>
```

> `init-method="setOtherName"`表示`setOtherName()`方法为初始化方法。

或者在Java Config中使用注解：

```java
@Bean(initMethod="setOtherName")
```

我们可以使用 `<beans>`标签的 `default-init-method` 属性来统一指定初始化方法，这样就省了需要在每个 `<bean>` 标签中都设置 `init-method` 这样的繁琐工作了。比如在 `default-init-method` 规定所有初始化操作全部以 `initBean()` 命名：

```xml
<beans xmlns="http://www.springframework.org/schema/beans"
		xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
		xsi:schemaLocation="http://www.springframework.org/schema/beans https://www.springframework.org/schema/beans/spring-beans-3.0.xsd"
    default-init-method="initBean">
</beans>
```





## 参考资料

- 芋道源码 精尽 Spring 源码分析