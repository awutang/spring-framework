/**
 * Author: Tang Yuqian
 * Date: 2022/3/25
 */
package test;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;

/**
 * 在上面的例子中，我们会发现没有添加 @Configuraion注解时dmzService被创建了两次。
 *
 * 这是因为第一次创建是被 Spring 容器所创建的，Spring 调用这个 dmzService() 创建了一个 Bean 被放入了单例池中(没有添加其它配置默认是单例的)。
 * 第二次创建是 Spring 容器在创建 a 时调用了a()，而 a() 又调用了 dmzService() 方法。
 * 这样的话，就出现问题了。
 *
 * 第一，对于 dmzService 而言，它被创建了两次，打破了单例的条件。
 *
 * 第二，对于 a 而言，它所依赖的 dmzService 不是 Spring 所管理的，而是直接调用的一个普通的 java 方法创建的普通对象。这个对象没有被 Spring 对象管理，首先它的域(Scope)定义失效了，其次它没有经过一个完整的生命周期，那么我们所定义所有的 Bean 的后置处理器都没有作用到它身上，其中就包括了完成 AOP 的后置处理器，所以 AOP 也失效了。
 */
@ComponentScan("com.dmz.source.code")
@Configuration
public class Config {
	@Bean
	public A a() {
		return new A(dmzService());
	}

	@Bean
	public DmzService dmzService() {
		return new DmzService();
	}
}

public class A {
	public A(DmzService dmzService) {
		System.out.println("create A by dmzService");
	}
}

@Component
public class DmzService {
	public DmzService() {
		System.out.println("create dmzService");
	}
}


public class Main {
	public static void main(String[] args) {
		System.setProperty(DebuggingClassWriter.DEBUG_LOCATION_PROPERTY, "F:\\code");  //实例化一个增强器，也就是cglib中的一个class generator
		Enhancer eh = new Enhancer();  //设置目标类
		 eh.setSuperclass(Target.class);  // 设置拦截对象
		 eh.setCallbacks(new Callback[]{new Interceptor(), NoOp.INSTANCE});
		 eh.setCallbackFilter(new CallbackFilter() {
		 @Override
		 public int accept(Method method) {
		 if(method.getName().equals("g"))    // 这里返回的是上面定义的callback数组的下标，0就是我们的Interceptor对象，1是内置的NoOp对象，代表不做任何操作
		 return 0;    else return 1;   }  });  // 生成代理类并返回一个实例
		 Target t = (Target) eh.create();
		 t.f();
		 t.g(); }}


