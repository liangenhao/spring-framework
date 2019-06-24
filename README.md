# <img src="src/docs/asciidoc/images/spring-framework.png" width="80" height="80"> Spring Framework

## Spring Framework 源码学习

项目 fork 时间：2019年4月2日

release version：v5.1.6.RELEASE

development version：5.2.0.BUILD-SNAPSHOT

### 学习进度

- IOC：完成。

  > 只学习了解析对xml配置文件的资源加载。还没有学习对注解模式的资源加载。

- AOP：未开始

- 未完待续...

### 学习笔记

[Spring 源码学习](source_docs)

## 学习笔记目录

- [01-环境搭建](source_docs/01-Spring源码学习-环境搭建.md)
- [02-IOC-总览](source_docs/02-Spring源码学习-IOC-总览.md)
- [03-IOC-资源定位](source_docs/03-Spring源码学习-IOC-资源定位.md)
- [04-IOC-资源装载](source_docs/04-Spring源码学习-IOC-资源装载.md)
  - [04.1-IOC-资源装载-获取XML验证模式](source_docs/04.1-Spring源码学习-IOC-资源装载-获取XML验证模式.md)
  - [04.2-IOC-资源装载-获取Document对象](source_docs/04.2-Spring源码学习-IOC-资源装载-获取Document对象.md)
  - [04.3-IOC-资源装载-注册BeanDefinition](source_docs/04.3-Spring源码学习-IOC-资源装载-注册BeanDefinition.md)
    - [04.3.1-IOC-资源装载-解析默认标签-bean](source_docs/04.3.1-Spring源码学习-IOC-资源装载-解析默认标签-<bean>.md)
    - [04.3.2-IOC-资源装载-解析默认标签-alias](source_docs/04.3.2-Spring源码学习-IOC-资源装载-解析默认标签-<alias>.md)
    - [04.3.3-IOC-资源装载-解析默认标签-import](source_docs/04.3.3-Spring源码学习-IOC-资源装载-解析默认标签-<import>.md)
    - [04.3.4-IOC-资源装载-解析默认标签-嵌套的beans](source_docs/04.3.4-Spring源码学习-IOC-资源装载-解析默认标签-嵌套的<beans>.md)
    - [04.3.5-IOC-资源装载-解析自定义标签](source_docs/04.3.5-Spring源码学习-IOC-资源装载-解析自定义标签.md)
- [05-IOC-加载Bean](source_docs/05-Spring源码学习-IOC-加载Bean.md)
  - [05.1-IOC-加载Bean-转换对应beanName](source_docs/05.1-Spring源码学习-IOC-加载Bean-转换对应beanName.md)
  - [05.2-IOC-加载Bean-尝试从缓存中加载单例](source_docs/05.2-Spring源码学习-IOC-加载Bean-尝试从缓存中加载单例.md)
  - [05.3-IOC-加载Bean-原型模式的依赖检查](source_docs/05.3-Spring源码学习-IOC-加载Bean-原型模式的依赖检查.md)
  - [05.4-IOC-加载Bean-从parentBeanFactory中加载bean](source_docs/05.4-Spring源码学习-IOC-加载Bean-从parentBeanFactory中加载bean.md)
  - [05.5-IOC-加载Bean-typeCheckOnly](source_docs/05.5-Spring源码学习-IOC-加载Bean-typeCheckOnly.md)
  - [05.6-IOC-加载Bean-获取RootBeanDefinition](source_docs/05.6-Spring源码学习-IOC-加载Bean-获取RootBeanDefinition.md)
  - [05.7-IOC-加载Bean-处理依赖](source_docs/05.7-Spring源码学习-IOC-加载Bean-处理依赖.md)
  - [05.8-IOC-加载Bean-创建不同scope的Bean](source_docs/05.8-Spring源码学习-IOC-加载Bean-创建不同scope的Bean.md)
    - [05.8.1-IOC-加载Bean-准备创建Bean](source_docs/05.8.1-Spring源码学习-IOC-加载Bean-准备创建Bean.md)
    - [05.8.2-IOC-加载Bean-创建Bean-创建Bean的实例](source_docs/05.8.2-Spring源码学习-IOC-加载Bean-创建Bean-创建Bean的实例.md)
    - [05.8.4-IOC-加载Bean-创建Bean-循环依赖处理](source_docs/05.8.4-Spring源码学习-IOC-加载Bean-创建Bean-循环依赖处理.md)
    - [05.8.5--IOC-加载Bean-创建Bean-属性填充](source_docs/05.8.5-Spring源码学习-IOC-加载Bean-创建Bean-属性填充.md)
    - [05.8.6-IOC-加载Bean-创建Bean-初始化Bean](source_docs/05.8.6-Spring源码学习-IOC-加载Bean-创建Bean-初始化Bean.md)
    - [05.8.7-IOC-加载Bean-创建Bean-注册DisposableBean](source_docs/05.8.7-Spring源码学习-IOC-加载Bean-创建Bean-注册DisposableBean.md)
- [06-深入学习Aware接口](source_docs/06-Spring源码学习-深入学习Aware接口.md)
- [07-深入学习BeanPostProcessor接口](source_docs/07-Spring源码学习-深入学习BeanPostProcessor接口.md)
- [08-深入学习InitializingBean和init-method](source_docs/08-Spring源码学习-深入学习InitializingBean和init-method.md)
- [09-深入学习Bean的生命周期](source_docs/09-Spring源码学习-深入学习Bean的生命周期.md)
- [10-深入学习BeanFactoryPostProcessor接口](source_docs/10-Spring源码学习-深入学习BeanFactoryPostProcessor接口.md)
  - [10.1-内置实现PropertyPlaceholderConfigurer](source_docs/10.1-Spring源码学习-深入学习BeanFactoryPostProcessor接口-内置实现PropertyPlaceholderConfigurer.md)
  - [10.2-内置实现PropertyOverrideConfigurer](source_docs/10.2-Spring源码学习-深入学习BeanFactoryPostProcessor接口-内置实现PropertyOverrideConfigurer.md)
  - [11-容器功能扩展-ApplicationContext相关接口](source_docs/11-Spring源码学习-容器功能扩展-ApplicationContext相关接口.md)
  - [12-容器功能扩展-ApplicationContext的refresh()方法](source_docs/12-Spring源码学习-容器功能扩展-ApplicationContext的refresh()方法.md)
- 总结
  - [从Spring源码看编程规范](source_docs/Spring源码学习-总结-从Spring源码看编程规范.md)
  - [从Spring源码看设计模式](source_docs/Spring源码学习-总结-从Spring源码看设计模式.md)


### 参考资料

整个学习过程，参考 芋道源码 精尽 Spring 源码分析 进行，并在相应的源码上进行标注解释，同时也会参考《Spring源码深度解析 第2版》书籍的相关内容。

- 芋道源码 精尽 Spring 源码分析 
- 《Spring源码深度解析 第2版》

------


This is the home of the Spring Framework: the foundation for all [Spring projects](https://spring.io/projects). Collectively the Spring Framework and the family of Spring projects is often referred to simply as "Spring". 

Spring provides everything required beyond the Java programming language for creating enterprise applications for a wide range of scenarios and architectures. Please read the [Overview](https://docs.spring.io/spring/docs/current/spring-framework-reference/overview.html#spring-introduction) section as reference for a more complete introduction.

## Code of Conduct

This project is governed by the [Spring Code of Conduct](CODE_OF_CONDUCT.adoc). By participating, you are expected to uphold this code of conduct. Please report unacceptable behavior to spring-code-of-conduct@pivotal.io.

## Access to Binaries

For access to artifacts or a distribution zip, see the [Spring Framework Artifacts](https://github.com/spring-projects/spring-framework/wiki/Spring-Framework-Artifacts) wiki page.

## Documentation

The Spring Framework maintains reference documentation ([published](https://docs.spring.io/spring-framework/docs/current/spring-framework-reference/) and [source](src/docs/asciidoc)), Github [wiki pages](https://github.com/spring-projects/spring-framework/wiki), and an
[API reference](https://docs.spring.io/spring-framework/docs/current/javadoc-api/). There are also [guides and tutorials](https://spring.io/guides) across Spring projects.

## Build from Source

See the [Build from Source](https://github.com/spring-projects/spring-framework/wiki/Build-from-Source) Wiki page and the [CONTRIBUTING.md](CONTRIBUTING.md) file.

## Stay in Touch

Follow [@SpringCentral](https://twitter.com/springcentral), [@SpringFramework](https://twitter.com/springframework), and its [team members](https://twitter.com/springframework/lists/team/members) on Twitter. In-depth articles can be found at [The Spring Blog](https://spring.io/blog/), and releases are announced via our [news feed](https://spring.io/blog/category/news).

## License

The Spring Framework is released under version 2.0 of the [Apache License](https://www.apache.org/licenses/LICENSE-2.0).
