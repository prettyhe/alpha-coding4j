<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:aop="http://www.springframework.org/schema/aop"
       xsi:schemaLocation="http://www.springframework.org/schema/beans
        http://www.springframework.org/schema/beans/spring-beans.xsd
        http://www.springframework.org/schema/aop
	    http://www.springframework.org/schema/aop/spring-aop.xsd">

    <aop:config proxy-target-class="${enable-proxy-target-class}">

        <aop:aspect ref="${aop-ref}" order="${aop-order}">
            <aop:${aop-advice} method="${aop-advice-method}"
                        pointcut="${aop-pointcut}"/>
        </aop:aspect>

    </aop:config>

</beans>