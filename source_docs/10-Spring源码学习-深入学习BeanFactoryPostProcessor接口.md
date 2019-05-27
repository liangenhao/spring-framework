---
title: 10-Spring源码学习-深入学习BeanFactoryPostProcessor接口
date: 2019-05-28
categories: 源码学习
tags: [Spring]
---

> 可以类比 [BeanPostProcessor接口](07-Spring源码学习-深入学习BeanPostProcessor接口.md) 。

## BeanFactoryPostProcessor作用

```java
@FunctionalInterface
public interface BeanFactoryPostProcessor {
  
   void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException;
}
```

1. 一种容器扩展机制。
2. 【作用时机】：工作于`BeanDefinition`加载完成之后，Bean 实例化之前。
3. 【作用】：给了我们**在 Bean 实例化之前最后一次修改`BeanDefinition`的机会**，我们可以利用这个机会对`BeanDefinition`来进行一些额外的操作，比如更改某些 bean 的一些属性，给某些 Bean 增加一些其他的信息等等操作。
4. 【注意】：在 `postProcessBeanFactory(...)` 方法中，千万不能进行 Bean 的实例化工作，因为这样会导致 Bean 过早实例化，会产生严重后果，**我们始终需要注意的是`BeanFactoryPostProcessor`是与`BeanDefinition`打交道的，如果想要与 Bean 打交道，请使用`BeanPostProcessor`** 。



## BeanFactoryPostProcessor原理

### 普通BeanFactory手动注册BeanFactoryPostProcessor

和`BeanPostProcessor`接口一样，普通的`BeanFactory`不支持自动注册`BeanFactoryPostProcessor`，需要主动进行注册调用：

```java
MyBeanFactoryPostProcessor beanFactoryPostProcessor1 = new MyBeanFactoryPostProcessor();
beanFactoryPostProcessor1.postProcessBeanFactory(factory);
```

### ApplicationContext自动注册BeanFactoryPostProcessor

和`BeanPostProcessor`接口一样，`ApplicationContext`自动识别配置文件中的 BeanFactoryPostProcessor 并且完成注册和调用，我们只需要简单的配置声明即可。

## BeanFactoryPostProcessor示例

定义自定义的`BeanFactoryPostProcessor_1`：

```java
public class BeanFactoryPostProcessor_1 implements BeanFactoryPostProcessor,Ordered {

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        System.out.println("调用 BeanFactoryPostProcessor_1 ...");

        System.out.println("容器中有 BeanDefinition 的个数：" + beanFactory.getBeanDefinitionCount());

        // 获取指定的 BeanDefinition
        BeanDefinition bd = beanFactory.getBeanDefinition("studentService");

        MutablePropertyValues pvs = bd.getPropertyValues();

        pvs.addPropertyValue("name","chenssy1");
        pvs.addPropertyValue("age",15);
    }

    @Override
    public int getOrder() {
        return 1;
    }
}
```

配置文件：

```xml
<bean id="studentService" class="org.springframework.core.service.StudentService">
    <property name="name" value="chenssy"/>
    <property name="age" value="10"/>
</bean>

<bean class="org.springframework.core.test.BeanFactoryPostProcessor_1"/>
```

测试：

```java
ApplicationContext context = new ClassPathXmlApplicationContext("spring.xml");

StudentService studentService = (StudentService) context.getBean("studentService");
System.out.println("student name:" + studentService.getName() + "-- age:" + studentService.getAge());
```

结果：

```
调用 BeanFactoryPostProcessor_1 ...
容器中有 BeanDefinition 的个数：2
student name:chenssy1-- age:15
```

## 内置的BeanFactoryPostProcessor实现

Spring 为我们提供了几个常用的`BeanFactoryPostProcessor`：

- `PropertyPlaceholderConfigurer`：允许我们在 XML 配置文件/注解中使用占位符并将这些占位符所代表的资源单独配置到简单的 properties 文件中来加载。

  > 详见：[内置实现PropertyPlaceholderConfigurer](10.1-Spring源码学习-深入学习BeanFactoryPostProcessor接口-内置实现PropertyPlaceholderConfigurer.md)
  >
  > 注意：Spring 5.2 以后，使用`PropertySourcesPlaceholderConfigurer`替代`PropertyPlaceholderConfigurer`。通过利用`Environment`和`PropertySource`机制使得其更灵活。

- `PropertyOverrideConfigurer`：允许我们使用占位符来明确表明bean 定义中的 property 与 properties 文件中的各配置项之间的对应关系。

  > 详见：TODO



## 参考资料

- 芋道源码 精尽 Spring 源码分析