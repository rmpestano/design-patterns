package org.jboss.forge.commands;

import org.jboss.forge.addon.parser.java.facets.JavaSourceFacet;
import org.jboss.forge.addon.projects.ProjectFactory;
import org.jboss.forge.addon.projects.Projects;
import org.jboss.forge.addon.resource.FileResource;
import org.jboss.forge.addon.ui.command.AbstractUICommand;
import org.jboss.forge.addon.ui.context.UIBuilder;
import org.jboss.forge.addon.ui.context.UIContext;
import org.jboss.forge.addon.ui.context.UIExecutionContext;
import org.jboss.forge.addon.ui.context.UIValidationContext;
import org.jboss.forge.addon.ui.input.UIInput;
import org.jboss.forge.addon.ui.metadata.UICommandMetadata;
import org.jboss.forge.addon.ui.metadata.WithAttributes;
import org.jboss.forge.addon.ui.result.Result;
import org.jboss.forge.addon.ui.result.Results;
import org.jboss.forge.addon.ui.util.Categories;
import org.jboss.forge.addon.ui.util.Metadata;
import org.jboss.forge.roaster.Roaster;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.utils.DesignPatterns;

import javax.inject.Inject;

public class BuilderCommand extends AbstractUICommand {

	@Inject
	ProjectFactory projectFactory;

	@Inject
	@WithAttributes(label = "Java Class", required = true, requiredMessage = "Choose a Java file")
	private UIInput<FileResource<?>> javaFile;

	@Override
	public UICommandMetadata getMetadata(UIContext context) {
		return Metadata.forCommand(BuilderCommand.class).name("Builder")
				.category(Categories.create("Design Patterns"));
	}

	@Override
	public void initializeUI(UIBuilder builder) throws Exception {

		builder.add(javaFile);
	}


	@Override
	public Result execute(UIExecutionContext context) throws Exception {
		FileResource<?> selectedFile = (FileResource<?>)javaFile.getValue();
		JavaClassSource javaClassSource = DesignPatterns.createBuilder((JavaClassSource) Roaster.parse(selectedFile.getUnderlyingResourceObject()));
		JavaSourceFacet facet = Projects.getSelectedProject(projectFactory,context.getUIContext()).getFacet(JavaSourceFacet.class);
		facet.saveJavaSource(javaClassSource);
		return Results.success("Builder created for class "+selectedFile.getName());
	}

	@Override
	public void validate(UIValidationContext validator) {
		super.validate(validator);
		FileResource<?> selectedFile = (FileResource<?>)javaFile.getValue();
		if(!selectedFile.getName().endsWith(".java")){
			validator.addValidationError(javaFile,
					"The selected file must be Java file");
		}
	}
}