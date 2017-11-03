/**
 * embedded server class
 * permet de définir le web.xml par un code
 *
 */
package com.bootcampWS.rest.application;

import io.swagger.jaxrs.config.DefaultJaxrsConfig;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.servlet.ServletContainer;

/**
 *
 * @author soul
 */
public class MyServer {

    public static void main(String[] args) throws Exception
  {
      Server server = new Server(9090);
      ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
      context.setContextPath("/");
      server.setHandler(context);

      // Setup API resources
      ServletHolder apiServlet = context.addServlet(ServletContainer.class, "/rest/*");
      apiServlet.setInitOrder(1);
      apiServlet.setInitParameter("com.sun.jersey.config.property.packages", "com.bootcampWS.rest.controllers;io.swagger.jaxrs.json;io.swagger.jaxrs.listing");

      // Setup Swagger servlet
      ServletHolder swaggerServlet = context.addServlet(DefaultJaxrsConfig.class, "/swagger-core");
      swaggerServlet.setInitOrder(2);
      swaggerServlet.setInitParameter("api.version", "1.0.0");

      // Setup Swagger-UI static resources
      String resourceBasePath ="/webapp";  // HateoasApp.class.getResource("/webapp").toExternalForm();
      context.setWelcomeFiles(new String[] {"index.html"});
      context.setResourceBase(resourceBasePath);
      context.addServlet(new ServletHolder(new DefaultServlet()), "/*");

      server.start();
//      server.dumpStdErr();
      server.join();
  }
}
