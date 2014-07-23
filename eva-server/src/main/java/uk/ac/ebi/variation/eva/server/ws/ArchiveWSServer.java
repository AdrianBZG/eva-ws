package uk.ac.ebi.variation.eva.server.ws;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import javax.naming.NamingException;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import org.opencb.datastore.core.QueryResult;
import org.opencb.opencga.storage.variant.ArchiveDBAdaptor;
import uk.ac.ebi.variation.eva.lib.storage.metadata.ArchiveEvaproDBAdaptor;

/**
 *
 * @author Cristina Yenyxe Gonzalez Garcia <cyenyxe@ebi.ac.uk>
 */
@Path("/{version}/meta")
@Produces(MediaType.APPLICATION_JSON)
public class ArchiveWSServer extends EvaWSServer {
    
    private ArchiveDBAdaptor dbAdaptor;
    
    public ArchiveWSServer() {

    }

    public ArchiveWSServer(@DefaultValue("") @PathParam("version")String version, @Context UriInfo uriInfo, @Context HttpServletRequest hsr) 
            throws IOException, NamingException {
        super(version, uriInfo, hsr);
        dbAdaptor = new ArchiveEvaproDBAdaptor();
    }

    @GET
    @Path("/files/count")
    public Response countFiles() {
        return createOkResponse(dbAdaptor.countFiles());
    }
    
    @GET
    @Path("/species/count")
    public Response countSpecies() {
        return createOkResponse(dbAdaptor.countSpecies());
    }
    
    @GET
    @Path("/studies/count")
    public Response countStudies() {
        return createOkResponse(dbAdaptor.countStudies());
    }
    
    @GET
    @Path("/studies/stats")
    public Response getStudiesStats(@QueryParam("species") String species) {
        if (species != null && !species.isEmpty()) {
            queryOptions.put("species", Arrays.asList(species.split(",")));
        }
        
        QueryResult<Map.Entry<String, Integer>> resultSpecies = dbAdaptor.countStudiesPerSpecies(queryOptions);
        QueryResult<Map.Entry<String, Integer>> resultTypes = dbAdaptor.countStudiesPerType(queryOptions);
        
        QueryResult combinedQueryResult = new QueryResult();
        combinedQueryResult.setDbTime(resultSpecies.getDbTime() + resultTypes.getDbTime());
        
        JsonNodeFactory factory = new JsonNodeFactory(true);
        ObjectNode root = factory.objectNode();
        combinedQueryResult.addResult(root);
        combinedQueryResult.setNumTotalResults(combinedQueryResult.getNumResults());
        
        // Species
        ObjectNode speciesNode = factory.objectNode();
        for (Map.Entry<String, Integer> speciesCount : resultSpecies.getResult()) {
            speciesNode.put(speciesCount.getKey(), speciesCount.getValue());
        }
        root.put("species", speciesNode);
        
        // Types
        ObjectNode typesNode = factory.objectNode();
        for (Map.Entry<String, Integer> typesCount : resultTypes.getResult()) {
            typesNode.put(typesCount.getKey(), typesCount.getValue());
        }
        root.put("type", typesNode);
        
        return createOkResponse(combinedQueryResult);
    }
}