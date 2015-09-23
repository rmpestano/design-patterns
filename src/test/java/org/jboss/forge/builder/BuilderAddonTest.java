package org.jboss.forge.builder;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.addon.facets.FacetFactory;
import org.jboss.forge.addon.projects.Project;
import org.jboss.forge.addon.projects.ProjectFactory;
import org.jboss.forge.addon.resource.DirectoryResource;
import org.jboss.forge.addon.resource.FileResource;
import org.jboss.forge.addon.resource.Resource;
import org.jboss.forge.addon.shell.test.ShellTest;
import org.jboss.forge.addon.ui.result.Failed;
import org.jboss.forge.addon.ui.result.Result;
import org.jboss.forge.addon.ui.test.UITestHarness;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.AddonDependencies;
import org.jboss.forge.arquillian.archive.AddonArchive;
import org.jboss.forge.roaster.Roaster;
import org.jboss.forge.roaster.model.JavaType;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.junit.Assert;
import org.junit.Test;

import javax.inject.Inject;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(Arquillian.class)
public class BuilderAddonTest {

	private Project project;


	@Inject
	private ProjectFactory projectFactory;

	@Inject
	private FacetFactory facetFactory;

	@Inject
	private ShellTest shellTest;

	@Inject
	private UITestHarness uiTestHarness;

	@Deployment
	@AddonDependencies
	public static AddonArchive getDeployment() {
		return ShrinkWrap.create(AddonArchive.class).addBeansXML();
	}

	@Before
	public void setUp() throws IOException {
		project = projectFactory.createTempProject();
	}

	@Test
	public void shouldCreateBuilderFromPersonClass() throws TimeoutException, FileNotFoundException {
		shellTest.getShell().setCurrentResource(project.getRoot());
		addPerson();
		Result result = shellTest.execute("builder --javaFile src/main/java/com/model/Person.java", 20, TimeUnit.SECONDS);
		Assert.assertThat(result, not(instanceOf(Failed.class)));
		Resource personBuilder = project.getRoot().reify(DirectoryResource.class).getChild("src/main/java/com/model").getChild("PersonBuilder.java");
		assertThat(personBuilder.exists(), is(true));

		JavaClassSource builderSource = (JavaClassSource) Roaster.parse((File) personBuilder.getUnderlyingResourceObject());
		assertThat(builderSource.hasField("person"),is(true));
		assertThat(builderSource.hasMethodSignature("build"), is(true));
	}

	@Test
	@Ignore
	public void shouldNotCreateBuilderFromNonJavaFile() throws TimeoutException, FileNotFoundException {
		shellTest.getShell().setCurrentResource(project.getRoot());
		addInvalidFile();
		Result result = null;
		try{
		  result = shellTest.execute("builder --javaFile src/main/java/com/model/Person.php", 20, TimeUnit.SECONDS);
		}catch (TimeoutException ex){
			//result is null forge isn't returning when there are validation errors...
			assertThat(result, is(instanceOf(Failed.class)));
			assertThat(result.getMessage(), is(equalTo("The selected file must be Java file")));
		}
	}

	private void addPerson() throws FileNotFoundException {
		DirectoryResource resource = project.getRoot().reify(DirectoryResource.class).getOrCreateChildDirectory("src").
				getOrCreateChildDirectory("main").
				getOrCreateChildDirectory("java").
				getOrCreateChildDirectory("com").
				getOrCreateChildDirectory("model");
		FileResource<?> person = (FileResource<?>) resource.getChild("Person.java");
		person.setContents(new FileInputStream(new File(Paths.get("").toAbsolutePath() + "/target/test-classes/Person.java")));
	}

	private void addInvalidFile() throws FileNotFoundException {
		DirectoryResource resource = project.getRoot().reify(DirectoryResource.class).getOrCreateChildDirectory("src").
				getOrCreateChildDirectory("main").
				getOrCreateChildDirectory("java").
				getOrCreateChildDirectory("com").
				getOrCreateChildDirectory("model");
		FileResource<?> person = (FileResource<?>) resource.getChild("Person.php");
		person.setContents(new FileInputStream(new File(Paths.get("").toAbsolutePath() + "/target/test-classes/Person.java")));
	}
}