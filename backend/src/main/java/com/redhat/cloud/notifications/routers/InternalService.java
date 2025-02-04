package com.redhat.cloud.notifications.routers;

import com.redhat.cloud.notifications.db.ApplicationResources;
import com.redhat.cloud.notifications.db.BundleResources;
import com.redhat.cloud.notifications.db.StatusResources;
import com.redhat.cloud.notifications.models.Application;
import com.redhat.cloud.notifications.models.Bundle;
import com.redhat.cloud.notifications.models.CurrentStatus;
import com.redhat.cloud.notifications.models.EventType;
import com.redhat.cloud.notifications.oapi.OApiFilter;
import com.redhat.cloud.notifications.recipients.User;
import com.redhat.cloud.notifications.routers.models.RenderEmailTemplateRequest;
import com.redhat.cloud.notifications.routers.models.RenderEmailTemplateResponse;
import com.redhat.cloud.notifications.templates.EmailTemplateService;
import com.redhat.cloud.notifications.utils.ActionParser;
import io.smallrye.mutiny.Uni;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.hibernate.reactive.mutiny.Mutiny;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.RedirectionException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.List;
import java.util.UUID;

import static com.redhat.cloud.notifications.Constants.INTERNAL;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN;

@Path(INTERNAL)
public class InternalService {

    @Inject
    BundleResources bundleResources;

    @Inject
    ApplicationResources appResources;

    @Inject
    StatusResources statusResources;

    @Inject
    OApiFilter oApiFilter;

    @Inject
    EmailTemplateService emailTemplateService;

    @Inject
    ActionParser actionParser;

    @Inject
    Mutiny.SessionFactory sessionFactory;

    @GET
    @Path("/")
    public void httpRoot() {
        throw new RedirectionException(Response.Status.OK, URI.create("index.html"));
    }

    @GET
    @Path("/openapi.json")
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<String> serveInternalOpenAPI() {
        return oApiFilter.serveOpenApi(OApiFilter.INTERNAL);
    }

    @POST
    @Path("/bundles")
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    public Uni<Bundle> createBundle(@NotNull @Valid Bundle bundle) {
        return sessionFactory.withSession(session -> {
            return bundleResources.createBundle(bundle);
        });
    }

    @GET
    @Path("/bundles")
    @Produces(APPLICATION_JSON)
    public Uni<List<Bundle>> getBundles() {
        return sessionFactory.withSession(session -> {
            // Return configured with types?
            return bundleResources.getBundles();
        });
    }

    @GET
    @Path("/bundles/{bundleId}")
    @Produces(APPLICATION_JSON)
    public Uni<Bundle> getBundle(@PathParam("bundleId") UUID bundleId) {
        return sessionFactory.withSession(session -> {
            return bundleResources.getBundle(bundleId)
                    .onItem().ifNull().failWith(new NotFoundException());
        });
    }

    @PUT
    @Path("/bundles/{bundleId}")
    @Consumes(APPLICATION_JSON)
    @Produces(TEXT_PLAIN)
    public Uni<Response> updateBundle(@PathParam("bundleId") UUID bundleId, @NotNull @Valid Bundle bundle) {
        return sessionFactory.withSession(session -> {
            return bundleResources.updateBundle(bundleId, bundle)
                    .onItem().transform(rowCount -> {
                        if (rowCount == 0) {
                            return Response.status(Response.Status.NOT_FOUND).build();
                        } else {
                            return Response.ok().build();
                        }
                    });
        });
    }

    @DELETE
    @Path("/bundles/{bundleId}")
    @Produces(APPLICATION_JSON)
    public Uni<Boolean> deleteBundle(@PathParam("bundleId") UUID bundleId) {
        return sessionFactory.withSession(session -> {
            return bundleResources.deleteBundle(bundleId);
        });
    }

    @GET
    @Path("/bundles/{bundleId}/applications")
    @Produces(APPLICATION_JSON)
    public Uni<List<Application>> getApplications(@PathParam("bundleId") UUID bundleId) {
        return sessionFactory.withSession(session -> {
            return bundleResources.getApplications(bundleId);
        });
    }

    @POST
    @Path("/applications")
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    public Uni<Application> createApplication(@NotNull @Valid Application app) {
        return sessionFactory.withSession(session -> {
            return appResources.createApp(app);
        });
    }

    @GET
    @Path("/applications/{appId}")
    @Produces(APPLICATION_JSON)
    public Uni<Application> getApplication(@PathParam("appId") UUID appId) {
        return sessionFactory.withSession(session -> {
            return appResources.getApplication(appId)
                    .onItem().ifNull().failWith(new NotFoundException());
        });
    }

    @PUT
    @Path("/applications/{appId}")
    @Consumes(APPLICATION_JSON)
    @Produces(TEXT_PLAIN)
    public Uni<Response> updateApplication(@PathParam("appId") UUID appId, @NotNull @Valid Application app) {
        return sessionFactory.withSession(session -> {
            return appResources.updateApplication(appId, app)
                    .onItem().transform(rowCount -> {
                        if (rowCount == 0) {
                            return Response.status(Response.Status.NOT_FOUND).build();
                        } else {
                            return Response.ok().build();
                        }
                    });
        });
    }

    @DELETE
    @Path("/applications/{appId}")
    @Produces(APPLICATION_JSON)
    public Uni<Boolean> deleteApplication(@PathParam("appId") UUID appId) {
        return sessionFactory.withSession(session -> {
            return appResources.deleteApplication(appId);
        });
    }

    @GET
    @Path("/applications/{appId}/eventTypes")
    @Produces(APPLICATION_JSON)
    public Uni<List<EventType>> getEventTypes(@PathParam("appId") UUID appId) {
        return sessionFactory.withSession(session -> {
            return appResources.getEventTypes(appId);
        });
    }

    @POST
    @Path("/eventTypes")
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    public Uni<EventType> createEventType(@NotNull @Valid EventType eventType) {
        return sessionFactory.withSession(session -> {
            return appResources.createEventType(eventType);
        });
    }

    @PUT
    @Path("/eventTypes/{eventTypeId}")
    @Consumes(APPLICATION_JSON)
    @Produces(TEXT_PLAIN)
    public Uni<Response> updateEventType(@PathParam("eventTypeId") UUID eventTypeId, @NotNull @Valid EventType eventType) {
        return sessionFactory.withSession(session -> {
            return appResources.updateEventType(eventTypeId, eventType)
                    .onItem().transform(rowCount -> {
                        if (rowCount == 0) {
                            return Response.status(Response.Status.NOT_FOUND).build();
                        } else {
                            return Response.ok().build();
                        }
                    });
        });
    }

    @DELETE
    @Path("/eventTypes/{eventTypeId}")
    @Produces(APPLICATION_JSON)
    public Uni<Boolean> deleteEventType(@PathParam("eventTypeId") UUID eventTypeId) {
        return sessionFactory.withSession(session -> {
            return appResources.deleteEventTypeById(eventTypeId);
        });
    }

    @PUT
    @Path("/status")
    @Consumes(APPLICATION_JSON)
    public Uni<Void> setCurrentStatus(@NotNull @Valid CurrentStatus status) {
        return sessionFactory.withSession(session -> {
            return statusResources.setCurrentStatus(status);
        });
    }

    @POST
    @Path("/templates/email/render")
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    @APIResponses(value = {
            @APIResponse(responseCode = "200", content = {
                    @Content(schema = @Schema(title = "RenderEmailTemplateResponseSuccess", implementation = RenderEmailTemplateResponse.Success.class))
            }),
            @APIResponse(responseCode = "400", content = {
                    @Content(schema = @Schema(title = "RenderEmailTemplateResponseError", implementation = RenderEmailTemplateResponse.Error.class))
            })
    })
    public Uni<Response> renderEmailTemplate(@NotNull @Valid RenderEmailTemplateRequest renderEmailTemplateRequest) {
        User user = createInternalUser();

        String payload = renderEmailTemplateRequest.getPayload();
        return actionParser.fromJsonString(payload)
                .onItem().transformToUni(action -> Uni.combine().all().unis(
                    emailTemplateService
                            .compileTemplate(renderEmailTemplateRequest.getSubjectTemplate(), "subject")
                            .onItem().transformToUni(templateInstance -> emailTemplateService.renderTemplate(
                            user,
                            action,
                            templateInstance
                    )),
                    emailTemplateService
                            .compileTemplate(renderEmailTemplateRequest.getBodyTemplate(), "body")
                            .onItem().transformToUni(templateInstance -> emailTemplateService.renderTemplate(
                            user,
                            action,
                            templateInstance
                    ))
                ).asTuple()
        ).onItem().transform(titleAndBody -> Response.ok(new RenderEmailTemplateResponse.Success(titleAndBody.getItem1(), titleAndBody.getItem2())).build())
        .onFailure().recoverWithItem(throwable -> Response.status(Response.Status.BAD_REQUEST).entity(new RenderEmailTemplateResponse.Error(throwable.getMessage())).build());
    }

    private User createInternalUser() {
        User user = new User();
        user.setUsername("jdoe");
        user.setEmail("jdoe@jdoe.com");
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setActive(true);
        user.setAdmin(false);
        return user;
    }
}
