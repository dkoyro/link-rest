package com.nhl.link.rest.unit;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.sql.SQLException;

import javax.ws.rs.core.Application;
import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;

import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.configuration.server.ServerRuntime;
import org.apache.cayenne.query.EJBQLQuery;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.inmemory.InMemoryTestContainerFactory;
import org.glassfish.jersey.test.spi.TestContainerException;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

import com.nhl.link.rest.runtime.LinkRestBuilder;

/**
 * A main superclass of LinkRest unit tests that require full stack Jersey
 * container.
 */
public abstract class JerseyTestOnDerby extends JerseyTest {

	protected static ServerRuntime runtime;
	protected static DerbyManager derbyAssembly;

	@BeforeClass
	public static void setUpClass() throws IOException, SQLException {
		derbyAssembly = new DerbyManager("target/derby");
		runtime = new ServerRuntime("cayenne-linkrest-tests.xml");
	}

	@AfterClass
	public static void tearDownClass() throws IOException, SQLException {
		runtime.shutdown();
		runtime = null;

		derbyAssembly.shutdown();
		derbyAssembly = null;
	}

	protected ObjectContext context;

	@Before
	public void before() {
		this.context = runtime.newContext();

		context.performGenericQuery(new EJBQLQuery("delete from E4"));
		context.performGenericQuery(new EJBQLQuery("delete from E3"));
		context.performGenericQuery(new EJBQLQuery("delete from E2"));
		context.performGenericQuery(new EJBQLQuery("delete from E5"));
		context.performGenericQuery(new EJBQLQuery("delete from E6"));
		
		context.performGenericQuery(new EJBQLQuery("delete from E7"));
		context.performGenericQuery(new EJBQLQuery("delete from E9"));
		context.performGenericQuery(new EJBQLQuery("delete from E8"));
		
		context.performGenericQuery(new EJBQLQuery("delete from E11"));
		context.performGenericQuery(new EJBQLQuery("delete from E10"));
		
		context.performGenericQuery(new EJBQLQuery("delete from E12E13"));
		context.performGenericQuery(new EJBQLQuery("delete from E12"));
		context.performGenericQuery(new EJBQLQuery("delete from E13"));

	}

	public JerseyTestOnDerby() throws TestContainerException {
		super(new InMemoryTestContainerFactory());
	}

	protected String urlEnc(String queryParam) {
		try {
			// URLEncoder replaces spaces with "+"... Those are not decoded
			// properly by Jersey in 'uriInfo.getQueryParameters()' (TODO: why?)
			return URLEncoder.encode(queryParam, "UTF-8").replace("+", "%20");
		} catch (UnsupportedEncodingException e) {

			// unexpected... we know that UTF-8 is present
			throw new RuntimeException(e);
		}
	}

	@Override
	public Application configure() {

		Feature lrFeature = doConfigure().build().getFeature();

		Feature unitFeature = new Feature() {

			@Override
			public boolean configure(FeatureContext context) {
				doAddResources(context);
				return true;
			}
		};

		return new ResourceConfig().register(unitFeature).register(lrFeature);
	}

	protected LinkRestBuilder doConfigure() {
		return new LinkRestBuilder().cayenneRuntime(runtime);
	}

	protected abstract void doAddResources(FeatureContext context);
}
