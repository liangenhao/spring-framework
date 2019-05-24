---
title: 07-Spring源码学习-深入学习BeanPostProcessor接口
date: 2019-05-24
categories: 源码学习
tags: [Spring]
---

回顾创建Bean过程中[初始化Bean](05.8.6-Spring源码学习-IOC-加载Bean-创建Bean-初始化Bean.md)的第二步：后置处理器应用。

下面深入学习`BeanPostProcessor`接口。

## BeanPostProcessor作用

- `BeanPostProcessor`可以理解是Spring的工厂钩子。

  > 其他的钩子例如：`Aware`、`InitializingBean`、`DisposableBean`等。

- 在 Bean 完成实例化后，初始化前后，`BeanPostProcessor`可以对Bean进行一些配置、增加一些自己的处理逻辑。

## BeanPostProcessor使用场景

- 处理标记接口实现类
- 当前对象提供代理实现（例如 AOP）

## BeanPostProcessor调用时机

回顾创建Bean过程中[初始化Bean](05.8.6-Spring源码学习-IOC-加载Bean-创建Bean-初始化Bean.md)，初始化Bean步骤：

1. 激活 Aware 方法。
2. 后处理器的初始化前处理
3. 激活用户自定义的 init 方法`invokeInitMethods()`
4. 后处理器的初始化后处理

所以BeanPostProcessor调用时机调用时机：在Bean实例化后，在bean初始化前后调用。

## BeanPostProcessor基本原理

```java
public interface BeanPostProcessor { 
	@Nullable
	default Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
		return bean;
	}

	@Nullable
	default Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		return bean;
	}
}
```

### 普通BeanFactory手动注册BeanPostProcessor

由于普通的`BeanFactory`在 `BeanFactory#getBean(...)` 方法的过程中根本就没有将我们自定义的`BeanPostProcessor`注入到`beanPostProcessors` 集合中。所以一般普通的`BeanFactory`是不支持自动注册`BeanPostProcessor`的，需要我们**手动调用** `#addBeanPostProcessor(BeanPostProcessor beanPostProcessor)` 方法进行注册。注册后的`BeanPostProcessor`**适用于所有该`BeanFactory`创建的 bean**：

```java
ClassPathResource resource = new ClassPathResource("spring.xml");
DefaultListableBeanFactory factory = new DefaultListableBeanFactory();
BeanPostProcessorTest beanPostProcessorTest = new BeanPostProcessorTest();
// 手动调用注册
factory.addBeanPostProcessor(beanPostProcessorTest);
XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(factory);
reader.loadBeanDefinitions(resource);

BeanPostProcessorTest test = (BeanPostProcessorTest) factory.getBean("beanPostProcessorTest");
```

### ApplicationContext自动注册BeanPostProcessor

`ApplicationContext`可以在其 bean 定义中自动检测所有的`BeanPostProcessor`并自动完成注册，同时将他们应用到随后创建的任何 Bean 中。

`ApplicationContext`实现自动注册的原因，在于我们构造一个`ApplicationContext`实例对象的时候会调用 `#registerBeanPostProcessors(ConfigurableListableBeanFactory beanFactory)` 方法，将检测到的`BeanPostProcessor`注入到`ApplicationContext`容器中，同时应用到该容器创建的 bean 中：

```java
// org.springframework.context.support.AbstractApplicationContext#registerBeanPostProcessors
protected void registerBeanPostProcessors(ConfigurableListableBeanFactory beanFactory) {
  PostProcessorRegistrationDelegate.registerBeanPostProcessors(beanFactory, this);
}

// org.springframework.context.support.PostProcessorRegistrationDelegate#registerBeanPostProcessors(org.springframework.beans.factory.config.ConfigurableListableBeanFactory, org.springframework.context.support.AbstractApplicationContext)
public static void registerBeanPostProcessors(
  ConfigurableListableBeanFactory beanFactory, AbstractApplicationContext applicationContext) {

  // 获取所有的 BeanPostProcessor 的 beanName
  // 这些 beanName 都已经全部加载到容器中去，但是没有实例化
  String[] postProcessorNames = beanFactory.getBeanNamesForType(BeanPostProcessor.class, true, false);

  // Register BeanPostProcessorChecker that logs an info message when
  // a bean is created during BeanPostProcessor instantiation, i.e. when
  // a bean is not eligible for getting processed by all BeanPostProcessors.
  // 记录所有的beanProcessor数量
  int beanProcessorTargetCount = beanFactory.getBeanPostProcessorCount() + 1 + postProcessorNames.length;
  // 注册 BeanPostProcessorChecker，它主要是用于在 BeanPostProcessor 实例化期间记录日志
  beanFactory.addBeanPostProcessor(new BeanPostProcessorChecker(beanFactory, beanProcessorTargetCount));

  // Separate between BeanPostProcessors that implement PriorityOrdered,
  // Ordered, and the rest.
  // 用于保存PriorityOrdered类型的bean，PriorityOrdered 保证顺序
  List<BeanPostProcessor> priorityOrderedPostProcessors = new ArrayList<>();
  // 用于保存MergedBeanDefinitionPostProcessor类型的bean
  List<BeanPostProcessor> internalPostProcessors = new ArrayList<>();
  // 用于保存Ordered类型的beanName，保证顺序
  List<String> orderedPostProcessorNames = new ArrayList<>();
  // 用于保存没有顺序的beanName
  List<String> nonOrderedPostProcessorNames = new ArrayList<>();
  // 遍历，对BeanPostProcessor进行分类
  for (String ppName : postProcessorNames) {
    if (beanFactory.isTypeMatch(ppName, PriorityOrdered.class)) {
      BeanPostProcessor pp = beanFactory.getBean(ppName, BeanPostProcessor.class);
      priorityOrderedPostProcessors.add(pp);
      if (pp instanceof MergedBeanDefinitionPostProcessor) {
        internalPostProcessors.add(pp);
      }
    }
    else if (beanFactory.isTypeMatch(ppName, Ordered.class)) {
      orderedPostProcessorNames.add(ppName);
    }
    else {
      nonOrderedPostProcessorNames.add(ppName);
    }
  }

  // First, register the BeanPostProcessors that implement PriorityOrdered.
  // 第一步，注册所有实现了 PriorityOrdered 的 BeanPostProcessor
  // 先排序
  sortPostProcessors(priorityOrderedPostProcessors, beanFactory);
  // 后注册，即自动注册，调用addBeanPostProcessor方法
  registerBeanPostProcessors(beanFactory, priorityOrderedPostProcessors);

  // Next, register the BeanPostProcessors that implement Ordered.
  // 第二步，注册所有实现了 Ordered 的 BeanPostProcessor
  List<BeanPostProcessor> orderedPostProcessors = new ArrayList<>(orderedPostProcessorNames.size());
  for (String ppName : orderedPostProcessorNames) {
    BeanPostProcessor pp = beanFactory.getBean(ppName, BeanPostProcessor.class);
    orderedPostProcessors.add(pp);
    if (pp instanceof MergedBeanDefinitionPostProcessor) {
      internalPostProcessors.add(pp);
    }
  }
  // 先排序
  sortPostProcessors(orderedPostProcessors, beanFactory);
  // 后注册，即自动注册，调用addBeanPostProcessor方法
  registerBeanPostProcessors(beanFactory, orderedPostProcessors);

  // Now, register all regular BeanPostProcessors.
  // 第三步注册所有无序的 BeanPostProcessor
  List<BeanPostProcessor> nonOrderedPostProcessors = new ArrayList<>(nonOrderedPostProcessorNames.size());
  for (String ppName : nonOrderedPostProcessorNames) {
    BeanPostProcessor pp = beanFactory.getBean(ppName, BeanPostProcessor.class);
    nonOrderedPostProcessors.add(pp);
    if (pp instanceof MergedBeanDefinitionPostProcessor) {
      internalPostProcessors.add(pp);
    }
  }
  // 注册，无需排序
  registerBeanPostProcessors(beanFactory, nonOrderedPostProcessors);

  // Finally, re-register all internal BeanPostProcessors.
  // 最后，注册所有的 MergedBeanDefinitionPostProcessor 类型的 BeanPostProcessor
  // 先排序
  sortPostProcessors(internalPostProcessors, beanFactory);
  // 后注册，即自动注册，调用addBeanPostProcessor方法
  registerBeanPostProcessors(beanFactory, internalPostProcessors);

  // Re-register post-processor for detecting inner beans as ApplicationListeners,
  // moving it to the end of the processor chain (for picking up proxies etc).
  // 加入ApplicationListenerDetector（探测器）
  // 重新注册 BeanPostProcessor 以检测内部 bean，因为 ApplicationListeners 将其移动到处理器链的末尾
  beanFactory.addBeanPostProcessor(new ApplicationListenerDetector(applicationContext));
}
```

步骤：

1. 首先，`beanFactory`获取到注册到该 BeanFactory 中所有`BeanPostProcessor`接口的Bean。
2. 迭代这些bean，按照`PriorityOrdered`、`Ordered`、无序的bean进行分类。
3. 按照分类，分别进行排序（无序的不用排序）和注册。

> 这里的自动注册，就是`ApplicationContext`帮我们调用了`addBeanPostProcessor`方法。

## 参考资料

- 芋道源码 精尽 Spring 源码分析