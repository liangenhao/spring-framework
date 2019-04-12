---
title: 02-Spring源码学习-IOC-总览
date: 2019-04-04
categories: 源码学习
tags: [Spring]
---



## 核心类

> 内容来自于《Spring源码深度解析 第2版》P23。

### DefaultListableBeanFactory

**`DefaultListableBeanFactory` 是整个bean加载的核心部分，是 Spring 注册及加载 bean 的默认实现。**

![容器加载相关类图](images/容器加载相关类图.png)

> 类图中各个类的作用，可看《Spring源码深度解析 第2版》P24-25。

`DefaultListableBeanFactory`有唯一的子类`XmlBeanFactory`，使用了自定义的XML读取器`XmlBeanDefinitionReader`，实现了个性化的`BeanDefinitionReader`读取。

不过从 Spring 3.1 开始，`XmlBeanFactory`被标记为过时，推荐使用`DefaultListableBeanFactory`和`XmlBeanDefinitionReader`共同完成。

### XmlBeanDefinitionReader

Xml 配置文件读取是 Spring 中重要的功能，配置读取相关类图如下：

![配置文件读取相关类图](images/配置文件读取相关类图.png)

## IOC 容器装载执行步骤

使用`DefaultListableBeanFactory`和`XmlBeanDefinitionReader`使用IOC容器代码如下：

```java
ClassPathResource resource = new ClassPathResource("bean.xml"); // 1
DefaultListableBeanFactory factory = new DefaultListableBeanFactory(); // 2
XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(factory); // 3
reader.loadBeanDefinitions(resource); // 4
```

> 1. 获取资源。
> 2. 获取 `BeanFactory`。
> 3. 根据新建的 `BeanFactory` 创建一个 `BeanDefinitionReader` 对象，该 Reader 对象为资源的**解析器**。
> 4. 装载资源。

分为三个步骤：

1. **资源定位**，通常用外部资源（配置文件）来描述bean对象，因此是首先定位到外部这个资源（Resource）。

   > 详见： [Spring源码学习-IOC-资源定位](03-Spring源码学习-IOC-资源定位.md)

2. **装载**，就是 `BeanDefinition` 的载入。使用`BeanDefinitionReader`读取解析`Resource`资源，将bean表示成`BeanDefinition `。

   > 详见： [Spring源码学习-IOC-资源装载](04-Spring源码学习-IOC-资源装载.md)

   - 在 IoC 容器内部维护着一个 `BeanDefinition` Map 的数据结构。
   - 在配置文件中每一个 `<bean>` 都对应着一个 `BeanDefinition` 对象。

3. **注册**，向 IoC 容器注册在第二步解析好的 `BeanDefinition`，这个过程是通过 `BeanDefinitionRegistry `接口来实现的。在 IoC 容器内部其实是将第二个过程解析得到的 `BeanDefinition `注入到一个 `HashMap `容器中，IoC 容器就是通过这个 `HashMap` 来维护这些 `BeanDefinition` 的。

   > 详见：[Spring源码学习-IOC-资源装载-解析默认标签-bean](04.3.1-Spring源码学习-IOC-资源装载-解析默认标签-<bean>.md) 的 `注册解析的BeanDefinition` 小节。

   - 注意：这个过程并没有完成依赖注入（Bean 创建），Bean 创建是发生在应用第一次调用 `#getBean(...)` 方法，向容器索要 Bean 时。
   - 当然我们可以通过设置预处理，即对某个 Bean 设置 `lazyinit = false` 属性，那么这个 Bean 的依赖注入就会在容器初始化的时候完成。

> 简单的说，上面步骤的结果是，XML Resource => XML Document => BeanDefinition 



## bean的加载

当对XML配置文件解析成`BeanDefinition`，并将`BeanDefinition`注册到`BeanDefinitionRegistry`后，就是bean如何加载。

TODO

## 容器的功能扩展

TODO



## 参考资料

- 芋道源码 精尽 Spring 源码分析
- 《Spring源码深度解析 第2版》