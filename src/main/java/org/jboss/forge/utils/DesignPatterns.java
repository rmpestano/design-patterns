package org.jboss.forge.utils;

import java.util.Iterator;
import java.util.List;

import org.jboss.forge.roaster.Roaster;
import org.jboss.forge.roaster.model.JavaType;
import org.jboss.forge.roaster.model.source.Import;
import org.jboss.forge.roaster.model.source.Importer;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.MethodHolderSource;
import org.jboss.forge.roaster.model.source.MethodSource;
import org.jboss.forge.roaster.model.source.ParameterSource;
import org.jboss.forge.roaster.model.source.PropertySource;

/**
 * Design patterns from GoF
 * 
 * @author George Gastaldi
 *
 */
public class DesignPatterns
{

   public static JavaClassSource createBuilder(JavaClassSource javaClass)
   {
      // Create Builder Class
      String builderClassName = javaClass.getName() + "Builder";
      JavaClassSource builderClass = Roaster.create(JavaClassSource.class)
               .setName(builderClassName)
               .setPackage(javaClass.getPackage());
      javaClass.getImports().forEach(i -> builderClass.addImport(i));
      builderClass.addField().setPrivate().setFinal(true).setType(javaClass).setName("obj")
               .setLiteralInitializer("new " + javaClass.getName() + "();");
      builderClass.addMethod()
               .setPublic().setStatic(true).setName("create").setReturnType(builderClass)
               .setBody("return new " + builderClassName + "();");
      builderClass.addMethod()
               .setConstructor(true).setPrivate().setBody("");

      builderClass.addMethod()
               .setPublic().setReturnType(javaClass).setName("build").setBody("return obj;");
      for (PropertySource<JavaClassSource> property : javaClass.getProperties())
      {
         String propertyName = property.getName();
         String upperCasedName = Character.toUpperCase(propertyName.charAt(0)) + propertyName.substring(1);
         MethodSource<JavaClassSource> method = builderClass.addMethod().setPublic().setReturnType(builderClass)
                  .setName("with" + upperCasedName);
         method.addParameter(property.getType().getQualifiedName(), "param");
         method.setBody("obj.set" + upperCasedName + "(param);return this;");
      }
      return builderClass;
   }

   @SuppressWarnings({ "unchecked", "rawtypes" })
   public static JavaClassSource createDecorator(JavaType<?> javaSource)
   {
      // Create Decorator Class
      String decoratorClassName = javaSource.getName() + "Decorator";
      JavaClassSource decoratorClass = Roaster.create(JavaClassSource.class)
               .setName(decoratorClassName)
               .setPackage(javaSource.getPackage());
      if (javaSource instanceof Importer)
      {
         List<Import> imports = ((Importer) javaSource).getImports();
         imports.forEach(i -> decoratorClass.addImport(i));
      }
      MethodSource<JavaClassSource> constructor = decoratorClass.addMethod()
               .setPublic().setConstructor(true);
      constructor.addParameter(javaSource, "delegate");
      constructor.setBody("this.delegate = delegate;");
      // Create the Delegate final field
      decoratorClass.addField().setPrivate().setFinal(true)
               .setType(javaSource).setName("delegate");

      if (javaSource instanceof MethodHolderSource)
      {
         List<MethodSource<?>> methods = ((MethodHolderSource) javaSource)
                  .getMethods();
         for (MethodSource method : methods)
         {
            MethodSource<JavaClassSource> decoratorMethod = decoratorClass
                     .addMethod().setPublic().setName(method.getName());
            StringBuilder sb = new StringBuilder();
            if (method.isReturnTypeVoid())
            {
               decoratorMethod.setReturnTypeVoid();
               sb.append("delegate.");
            }
            else
            {
               sb.append("return delegate.");
               decoratorMethod.setReturnType(method.getReturnType()
                        .getQualifiedName());
            }
            sb.append(method.getName()).append("(");

            List<ParameterSource<?>> parameters = method.getParameters();
            for (Iterator<ParameterSource<?>> iterator = parameters
                     .iterator(); iterator.hasNext();)
            {
               ParameterSource<JavaClassSource> param = (ParameterSource<JavaClassSource>) iterator
                        .next();
               sb.append(param.getName());
               if (iterator.hasNext())
               {
                  sb.append(",");
               }
               decoratorMethod.addParameter(param.getType()
                        .getQualifiedName(), param.getName());
            }
            sb.append(");");
            ((List<String>) method.getThrownExceptions())
                     .forEach(o -> decoratorMethod.addThrows(o));
            decoratorMethod.setBody(sb.toString());
         }
      }
      return decoratorClass;
   }

}
