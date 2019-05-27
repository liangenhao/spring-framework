package learning.property_placeholder_configurer;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * PropertyPlaceholderConfigurer 示例：动态加载配置文件
 *
 * @author enhao
 */
public class PropertyPlaceholderConfigurerTest {

	public static void main(String[] args) {
		ApplicationContext context = new ClassPathXmlApplicationContext("property_placeholder_configurer_test.xml");

		StudentService studentService = (StudentService) context.getBean("studentService");
		System.out.println("student name:" + studentService.getName());
	}
}
