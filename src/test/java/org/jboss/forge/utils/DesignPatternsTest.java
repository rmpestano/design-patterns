package org.jboss.forge.utils;

import java.util.Date;

import org.jboss.forge.roaster.Roaster;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.JavaInterfaceSource;
import org.jboss.forge.roaster.model.source.MethodSource;
import org.junit.Assert;
import org.junit.Test;

public class DesignPatternsTest
{

   @Test
   public void testCreateDecorator() throws Exception
   {
      JavaInterfaceSource interfaceClass = Roaster
               .create(JavaInterfaceSource.class).setPackage("org.test.demo")
               .setName("CoolOMeter");
      MethodSource<JavaInterfaceSource> coolMethod = interfaceClass
               .addMethod().setReturnTypeVoid().setName("calculateCoolness");
      coolMethod.addParameter(String.class, "name");
      coolMethod.addParameter(Integer.class, "age");

      MethodSource<JavaInterfaceSource> uncoolMethod = interfaceClass
               .addMethod().setReturnTypeVoid().setName("calculateUnCoolness");
      uncoolMethod.addParameter(String.class, "name");
      uncoolMethod.addParameter(Integer.class, "age");
      JavaClassSource decorator = DesignPatterns.createDecorator(interfaceClass);
      Assert.assertNotNull(decorator);
      Assert.assertEquals("CoolOMeterDecorator", decorator.getName());
   }

   @Test
   public void testCreateBuilder() throws Exception
   {
      JavaClassSource javaClass = Roaster
               .create(JavaClassSource.class).setPackage("org.test.demo")
               .setName("Customer");
      javaClass.addProperty(String.class, "id");
      javaClass.addProperty(String.class, "name");
      javaClass.addProperty(Date.class, "birthDate");
      JavaClassSource builder = DesignPatterns.createBuilder(javaClass);
      Assert.assertNotNull(builder);
      Assert.assertEquals("CustomerBuilder", builder.getName());
   }

}
