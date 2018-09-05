package org.infinispan.online.service.caching;

import io.fabric8.openshift.client.OpenShiftClient;
import org.arquillian.cube.openshift.impl.requirement.RequiresOpenshift;
import org.arquillian.cube.requirement.ArquillianConditionalRunner;
import org.infinispan.online.service.endpoint.HotRodTester;
import org.infinispan.online.service.endpoint.RESTTester;
import org.infinispan.online.service.scaling.ScalingTester;
import org.infinispan.online.service.utils.DeploymentHelper;
import org.infinispan.online.service.utils.OpenShiftClientCreator;
import org.infinispan.online.service.utils.OpenShiftCommandlineClient;
import org.infinispan.online.service.utils.OpenShiftHandle;
import org.infinispan.online.service.utils.ReadinessCheck;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.net.MalformedURLException;
import java.net.URL;

@RunWith(ArquillianConditionalRunner.class)
@RequiresOpenshift
public class DatagridServiceTest {

   private static final String SERVICE_NAME = "datagrid-service";

   URL hotRodService;
   URL restService;
   HotRodTester hotRodTester;
   OpenShiftClient client = OpenShiftClientCreator.getClient();

   ReadinessCheck readinessCheck = new ReadinessCheck();
   OpenShiftHandle handle = new OpenShiftHandle(client);

   ScalingTester scalingTester = new ScalingTester();
   OpenShiftCommandlineClient commandlineClient = new OpenShiftCommandlineClient();

   @Deployment
   public static Archive<?> deploymentApp() {
      return ShrinkWrap
         .create(WebArchive.class, "test.war")
         .addAsLibraries(DeploymentHelper.testLibs())
         .addPackage(CachingServiceTest.class.getPackage())
         .addPackage(ReadinessCheck.class.getPackage())
         .addPackage(ScalingTester.class.getPackage())
         .addPackage(HotRodTester.class.getPackage());
   }

   @Before
   public void before() throws MalformedURLException {
      readinessCheck.waitUntilAllPodsAreReady(client);
      hotRodService = handle.getServiceWithName(SERVICE_NAME + "-hotrod");
      restService = handle.getServiceWithName(SERVICE_NAME + "-https");
      URL hotRodService = handle.getServiceWithName(SERVICE_NAME + "-hotrod");
      hotRodTester = new HotRodTester(SERVICE_NAME, hotRodService, client);
   }

   @Test
   public void should_read_and_write_through_hotrod_endpoint() {
      hotRodTester.putGetTest();
   }

   @Test
   public void should_create_permanent_caches() {
      hotRodTester.createNamedCache("custom", "replicated");
      hotRodTester.namedCachePutGetTest("custom");

      scalingTester.scaleDownStatefulSet(0, SERVICE_NAME, client, commandlineClient, readinessCheck);
      scalingTester.scaleUpStatefulSet(1, SERVICE_NAME, client, commandlineClient, readinessCheck);

      hotRodTester.namedCachePutGetTest("custom");
   }

}
