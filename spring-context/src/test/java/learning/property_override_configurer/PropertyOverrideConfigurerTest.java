package learning.property_override_configurer;

import learning.property_placeholder_configurer.StudentService;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * PropertyOverrideConfigurer 示例
 * @author enhao
 */
public class PropertyOverrideConfigurerTest {

	public static void main(String[] args) {
		ApplicationContext context = new ClassPathXmlApplicationContext("property_override_configurer_test.xml");

		StudentService studentService = (StudentService) context.getBean("student");
		System.out.println("student name:" + studentService.getName());
	}
}
