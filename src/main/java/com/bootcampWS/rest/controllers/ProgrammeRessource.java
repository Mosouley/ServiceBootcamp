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
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import static javax.ws.rs.HttpMethod.POST;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
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


     /**
      * instanciation d'un repository programme
      * permettant d'accéder à toutes les methodes d'appel aux données d'un programme
      */
    ProgrammeRepository programmeRepository = new ProgrammeRepository("WSProgrammePU");

    //Annotation JAX-RS2
    @Context
    UriInfo uriInfo;

    /**
     *URI d'acces à tous les programmes
     * @param start
     * @param size
      * @return
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "To retrieve all the programmes and scroll to the previous and the next thanks to Hateoas")
    public Programmes getProgrammes(@ApiParam(value = "start",required = true)  @QueryParam("start") int start,                  //
            @QueryParam("size") @DefaultValue("2") int size  ) throws SQLException {

        /**
         * //definition des URI permettant d'obtenir le previous et le next de
         * tout programme //aussi les parametres de pagination sont inclus
         * dans la requete http avec un minimum de 2
         */
        //Utilisation de la classe abstraite uribuilder
        UriBuilder builder = uriInfo.getAbsolutePathBuilder();
        builder.queryParam("start", "{start}");
        builder.queryParam("size", "{size}");

        //obtenir la liste des programmes de la base de donnees
        List<Programme> programmes = programmeRepository.findAll();

        //definissons une liste de liens (d'adresse d'acces à chaque programme)
        ArrayList<Link> links = new ArrayList<>();
        //creation d'une liste de programme, constituée seulement des programmes
        //respectant les criteres de start and size
        ArrayList<Programme> list = new ArrayList<>();

        //on fait remplir la liste des programmes respectant les queryparam de façon synchronisee pour eviter les appels concurrents

        synchronized (programmes) {
            int i = 0;
            for (Programme programme : programmes) {
                //on ajoute les programmes un a un à la liste jusqu'à ce que la taille definie soit  atteinte
                if (i >= start && i < start + size) {
                    list.add(programme);
                }
                i++;
            }
        }

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

        //Pour chaque programme definir son link
         //mise en oeuvre de l'implementation Hateoas
        //boucle for sur chaque programme
        //Introduire son champ self (lui-meme) qui fait reference à son lien
        for (Programme programme : programmes) {
            //pour chaque bailleur dans la liste
            //trouver l'URI vers sa ressource provenant de la recherche par id se trouvant dans la methode getbyId

            Link lien = Link.fromUri(uriInfo.getBaseUriBuilder()
                    .path(getClass())       //permet d'avoir l'uri de base
                    .path(getClass(), "getById")        //permet d'avoir l'URI de la methode recherche
                    .build(programme.getReference()))   //ajoute la reference du programme comme queryparam
                    .rel(programme.getNom())
                    .type("GET").build();               //faire le build pour resoudre le lien
            //definir le lien du programme ainsi obtenu
            programme.setSelf(lien);

            //pour chaque programme trouve lier le programme a son lien
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
  
    }

    @GET
    @Path("/programme/{ref}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getById(@PathParam("ref") int ref) throws SQLException {

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

    /**
     * methode de recherche sur un nombre limités de champs
     * @param ref
     * @param fields
     * @return
     * @throws SQLException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     * @throws IntrospectionException
     * @throws InvocationTargetException
     */
    @GET
    @Path("/programme/param/{ref}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getByIdParam(@PathParam("ref") int ref, @QueryParam("fields") String fields) throws SQLException, IllegalArgumentException, IllegalAccessException, IntrospectionException, InvocationTargetException {
       //Trouver les champs de recherche Separés par une virgule
        String[] fieldArray = fields.split(",");

        //retrouver le seul programme dont la reference est
        Programme programme = programmeRepository.findByPropertyUnique("reference", ref);
        //  Definissons un Map
        Map<String, Object> responseMap = new HashMap<>();

        //meta programming permettant d'obtenir tous les champs declares sur la classe programme
        PropertyDescriptor[] propertyDescriptors = Introspector.getBeanInfo(Programme.class).getPropertyDescriptors();

        //pour chaque proprete ou attribut parcourir ses methodes getter
        for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {

            Method method = propertyDescriptor.getReadMethod();

            //si la verification est concluante, donc le client souhaite obtenir ce champ
            if (check(fieldArray, propertyDescriptor.getName())) {
                responseMap.put(propertyDescriptor.getName(), method.invoke(programme));
            }

        }

        //A la fin de la boucle le Map contiendra tous les champs demandés leur nom, et le resultat de leur methode getter
        //on retourne donc la tableau de chmap dans la requête
        if(responseMap.isEmpty()){      //Aucun des champs indiques n'appartient au service programme
            return Response.noContent().build();
        }else{
        return Response.status(200).entity(responseMap).build();
        }
    }

    @POST
    @Path("/create")
//    @Produces(MediaType.)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response create(Programme programme) {
        String output = " Félicitations objet créé avec succès : ";
        try {
            programmeRepository.create(programme);
            return Response.status(200).entity(output + programme.getNom()).build();
        } catch (SQLException ex) {
            return Response.status(404).entity("Erreur: Objet non créé").build();
        }
    }
//
    @PUT
    @Path("/update")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response update(Programme programme) {
        String output = " Félicitations Mise à jour effectuée avec succès pour : ";
        try {
            programmeRepository.update(programme);
            return Response.status(200).entity(output + programme.getNom()).build();
        } catch (SQLException ex) {
            return Response.status(404).entity("Erreur: Objet non mis à jour").build();
        }

    }
//
    /**
     * methode permettant de verifier si un attribut fait partie d'une liste de champ indiqués
     * @param fields
     * @param field
     * @return
     */
    private boolean check(String[] fields, String field) {

        for (String field1 : fields) {
            if (field.equals(field1)) {
                return true;
            }
        }
        return false;
    }
}
