package org.thevlad.web.esignature.config;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.config.ServiceLocatorFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.thevlad.web.esignature.service.TemplateProviderType;


@Configuration
public class TemplateProviderConfig {

	  @Bean("templateProviderFactory")
	  public FactoryBean serviceLocatorFactoryBean() {
	    ServiceLocatorFactoryBean factoryBean = new ServiceLocatorFactoryBean();
	    factoryBean.setServiceLocatorInterface(TemplateProviderType.TemplateProviderFactory.class);
	    return factoryBean;
	  }

}
