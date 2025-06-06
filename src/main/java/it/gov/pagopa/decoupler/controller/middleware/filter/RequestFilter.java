package it.gov.pagopa.decoupler.controller.middleware.filter;

import io.quarkus.vertx.web.RouteFilter;
import io.vertx.ext.web.RoutingContext;
import it.gov.pagopa.decoupler.service.middleware.configuration.PrimitiveInfoRegistry;
import it.gov.pagopa.decoupler.service.model.PrimitiveInfo;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;

@ApplicationScoped
@Priority(1)
public class RequestFilter {

  private static final String REQUEST_REROUTED_FLAG = "soap-rerouted";

  private final Logger log;

  private final PrimitiveInfoRegistry primitiveRegistry;

  public RequestFilter(Logger log, PrimitiveInfoRegistry primitiveRegistry) {

    this.log = log;
    this.primitiveRegistry = primitiveRegistry;
  }

  @RouteFilter(100)
  void filter(RoutingContext ctx) {

    // If request was already re-routed, skip directly on next
    // component of the filter chain
    if (ctx.get(REQUEST_REROUTED_FLAG) != null) {
      ctx.next();
      return;
    }

    ctx.put(REQUEST_REROUTED_FLAG, true);
    String redirectionUri = ctx.request().uri();
    if (!redirectionUri.endsWith("/")) {
      redirectionUri += "/";
    }

    String primitive = ctx.request().getHeader("soapaction");
    if (primitive != null) {
      ctx.put(REQUEST_REROUTED_FLAG, true);
      PrimitiveInfo primitiveInfo = this.primitiveRegistry.fromPrimitive(primitive);
      redirectionUri += primitiveInfo.getInternalSubpath() + "/" + primitive;
      ctx.reroute(redirectionUri);
      return;
    }

    //
    else {
      ctx.put(REQUEST_REROUTED_FLAG, true);
      String subpath = ctx.request().uri().replaceAll("^/+", "").replaceAll("/+$", "");
      PrimitiveInfo primitiveInfo = this.primitiveRegistry.fromPath(subpath);
      redirectionUri = "/" + primitiveInfo.getInternalSubpath() + "/" + subpath;
      ctx.reroute(redirectionUri);
      return;
    }

    // ctx.next();
  }

  private void setEndpointIfSoapRequest(RoutingContext ctx) {

    String soapAction = ctx.request().getHeader("soapAction");
    if (soapAction != null) {
      ctx.reroute(ctx.request().uri() + soapAction);
    }
  }
}
