---
title: Spring源码学习-总结-从Spring源码看编程规范
date: 2019-04-23
categories: 源码学习
tags: [Spring]
---



## 命名

【形容词 + 名词】：

`DefaultListableBeanFactory`

`ConfigurableListableBeanFactory`：`Configurable`：可配置的，即表示可修改，所以一般有该名称的类里都有`set*`方法或者`add*`方法。



## 方法

Spring 源码中，一个真正干活的函数一般都是以`do`开头的。

示例：

【`XmlBeanDefinitionReader#loadBeanDefinitions(EncodedResource)`】：

加载`BeanDefinition`方法`loadBeanDefinitions()`。而真正注册的逻辑在`doLoadBeanDefinitions()`方法中执行。

【`DefaultBeanDefinitionDocumentReader#registerBeanDefinitions`】：

注册`BeanDefinition`方法`registerBeanDefinitions`。真正的逻辑在方法`doRegisterBeanDefinitions`中进行。

总结：

例如`loadBeanDefinitions()`方法只是从全局做些统筹的工作。真正干活的是`do`开头的方法。

## 变量

在方法中，**当将一个示例变量赋值给一个局部变量；或者将型参赋值给一个局部变量。**则这个局部变量的命名规则通常是`*ToUse`。

示例：

```java
// org.springframework.beans.factory.xml.XmlBeanDefinitionReader#getValidationModeForResource
protected int getValidationModeForResource(Resource resource) {
  // 获取指定的验证模式
  int validationModeToUse = getValidationMode();
  // 如果不是自动验证模式，直接返回
  if (validationModeToUse != VALIDATION_AUTO) {
    return validationModeToUse;
  }
  // 自动验证模式：
  // 如果文件具有DOCTYPE定义，则使用DTD验证，否则假设XSD验证。
  int detectedMode = detectValidationMode(resource);
  // 如果不是自动验证模式，直接返回
  if (detectedMode != VALIDATION_AUTO) {
    return detectedMode;
  }
  // 如果仍然是自动验证模式，则使用默认验证模式：XSD
  // Hmm, we didn't get a clear indication... Let's assume XSD,
  // since apparently no DTD declaration has been found up until
  // detection stopped (before finding the document's root tag).
  return VALIDATION_XSD;
}
```

