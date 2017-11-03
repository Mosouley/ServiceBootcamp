/**
 * signature Programme
 */
package com.bootcampWS.rest.controllers;

import com.bootcampWS.entities.Programme;
import com.bootcampWS.entities.Programmes;
import com.bootcampWS.jpa.ProgrammeRepository;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

/**
 *
 * @author soul
 */
@Path("v0/programmes")
@Api(value = "programmes", description = "web service on all the programs available")
public class ProgrammeRessource {


     //instanciation d'un bailleur repository
    ProgrammeRepository programmeRepository = new ProgrammeRepository("wsprogrammepu");

    //Annotation JAX-RS2
    @Context
    UriInfo uriInfo;

    /**
     *
     * @param start
     * @param size
      * @return
     */
    @GET
//    @Path("/list")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "allow to retrieve all the bailleurs and scroll to the previous and the next thanks to Hateoas")
    public Programmes getProgrammes(@ApiParam(value = "start",required = true)  @QueryParam("start") int start,                  //
            @QueryParam("size") @DefaultValue("2") int size  ) throws SQLException {

        /**
         * //definition des URI permettant d'obtenir le previous et le next de
         * tout programme //aussi les parametres de pagination sont son inclus
         * dans la requete http avec un minimum de 2
         */
        //Determination du Builder
        UriBuilder builder = uriInfo.getAbsolutePathBuilder();
        builder.queryParam("start", "{start}");
        builder.queryParam("size", "{size}");
//
//        UriBuilder nextLinkBuilder = uriInfo.getAbsolutePathBuilder();
//        nextLinkBuilder.queryParam("start", 5);
//        nextLinkBuilder.queryParam("size", 10);
//        URI next = nextLinkBuilder.build();
        List<Programme> programmes = programmeRepository.findAll();

        ArrayList<Link> links = new ArrayList<>();
        ArrayList<Programme> list = new ArrayList<>();
        synchronized (programmes) {
            int i = 0;
            for (Programme programme : programmes) {
                if (i >= start && i < start + size) {
                    list.add(programme);
                }
                i++;
            }
        }
//
        // next link
        if (start + size < programmes.size()) {
            int next = start + size;
            URI nextUri = builder.clone().build(next, size);
            Link nextLink = Link.fromUri(nextUri)
                    .rel("next").type("application/json").build();
            links.add(nextLink);
        }
        // previous link
        if (start > 0) {
            int previous = start - size;
            if (previous < 0) {
                previous = 0;
            }
            URI previousUri = builder.clone().build(previous, size);
            Link previousLink = Link.fromUri(previousUri)
                    .rel("previous")
                    .type("application/json").build();
            links.add(previousLink);
        }

        //         List<Projet> projets;
        //Pour chaque bailleur definir son link
        //pour chaque bailleur faire un lien vers sa liste de projets
        //pour chaque bailleur faire un lien vers sa liste de projets
        //mise en oeuvre de l'implementation Hateoas
        //boucle for sur chaque bailleur
        //Introduire son champ self (lui-meme) qui fait reference à son lien
        for (Programme programme : programmes) {
            //pour chaque bailleur dans la liste
            //trouver l'URI vers sa ressource provenant de la recherche par id se trouvant dans la methode getbyId

//    UriBuilder builder = UriBuilder.fromUri(uriInfo.getRequestUri());
////        builder.host("{hostname}");
//        builder.path(BailleurRestController.class,"getById");
//        UriBuilder clone = builder.clone();
//            URI uri = clone.build(uriInfo.getPath(), bailleur.getId());
            Link lien = Link.fromUri(uriInfo.getBaseUriBuilder()
                    .path(getClass())
                    .path(getClass(), "getById")
                    .build(programme.getReference()))
                    .rel(programme.getNom())
                    .type("GET").build();
            programme.setSelf(lien);
            Response.accepted(programme)
                    .links(programme.getSelf())
                    .build();
            //setter pour fixer le lien vers cette ressource
            programme.setSelf(lien);
        }
        Programmes listProgrammes = new Programmes();
        //on la remplit de la liste provenant de la base de donnée
        listProgrammes.setProgrammes(list);
        //on lui met ses liens
        listProgrammes.setLinks(links);
        //on retourne la reponse à la requete
        return listProgrammes;

//    
    }

    @GET
    @Path("/programme/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getById(@PathParam("id") int ref) throws SQLException {

        Programme programme = programmeRepository.findByPropertyUnique("reference", ref);

        if (programme != null) {
            programme.setSelf(
                    Link.fromUri(uriInfo.getAbsolutePath())
                    .rel("self")
                    .type("GET")
                    .build());
            return Response.accepted(programme).links(programme.getSelf()).build();
        } else {
            return Response.status(404).entity(programme).build();
        }
    }

    @GET
    @Path("/programme/param/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getByIdParam(@PathParam("id") int ref, @QueryParam("fields") String fields) throws SQLException, IllegalArgumentException, IllegalAccessException, IntrospectionException, InvocationTargetException {
        String[] fieldArray = fields.split(",");
        Programme programme = programmeRepository.findByPropertyUnique("reference", ref);
        //  Bailleur bailleurResponse=new Bailleur();
        Map<String, Object> responseMap = new HashMap<>();
//
        PropertyDescriptor[] propertyDescriptors = Introspector.getBeanInfo(Programme.class).getPropertyDescriptors();

        for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {

            Method method = propertyDescriptor.getReadMethod();
            if (check(fieldArray, propertyDescriptor.getName())) {
                responseMap.put(propertyDescriptor.getName(), method.invoke(programme));
            }
        }
        return Response.status(200).entity(responseMap).build();
    }

//    @POST
//    @Path("/create")
////    @Produces(MediaType.)
//    @Consumes(MediaType.APPLICATION_JSON)
//    public Response create(Bailleur bailleur) {
//        String output = " Félicitations objet créé avec succès : ";
//        try {
//            bailleurRepository.create(bailleur);
//            return Response.status(200).entity(output + bailleur.getNom()).build();
//        } catch (SQLException ex) {
//            return Response.status(404).entity("Erreur: Objet non créé").build();
//        }
//    }
//
//    @PUT
//    @Path("/update")
////    @Produces(MediaType.)
//    @Consumes(MediaType.APPLICATION_JSON)
//    public Response update(Bailleur bailleur) {
//        String output = " Félicitations Mise à jour effectuée avec succès pour : ";
//        try {
//            bailleurRepository.update(bailleur);
//            return Response.status(200).entity(output + bailleur.getNom()).build();
//        } catch (SQLException ex) {
//            return Response.status(404).entity("Erreur: Objet non mis à jour").build();
//        }
//
//    }
//
    private boolean check(String[] fields, String field) {

        for (String field1 : fields) {
            if (field.equals(field1)) {
                return true;
            }
        }
        return false;
    }
}
