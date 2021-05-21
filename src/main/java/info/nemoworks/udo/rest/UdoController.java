package info.nemoworks.udo.rest;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import graphql.ExecutionResult;
import graphql.GraphQL;
import info.nemoworks.udo.graphql.graphqlBuilder.GraphQLBuilder;
import info.nemoworks.udo.graphql.schemaParser.SchemaTree;
import info.nemoworks.udo.model.UdoType;
import info.nemoworks.udo.service.UdoService;
import info.nemoworks.udo.service.UdoServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin
@RequestMapping("/api")
public class UdoController {

    private static final Logger logger = LoggerFactory.getLogger(UdoController.class);

    private final UdoService udoService;

    private GraphQL graphQL;
    private GraphQLBuilder graphQlBuilder;

    @Autowired
    public UdoController(GraphQLBuilder graphQlBuilder, UdoService udoService) {
        this.graphQL = graphQlBuilder.createGraphQl();
        this.graphQlBuilder = graphQlBuilder;
        this.udoService = udoService;
    }

    @CrossOrigin
    @PostMapping(value = "/documents/query")
    public ResponseEntity query(@RequestBody String query) {
        ExecutionResult result = graphQL.execute(query);
        logger.info("errors: " + result.getErrors());
        if (result.getErrors().isEmpty())
            return ResponseEntity.ok(result.getData());
        else return ResponseEntity.badRequest().body(result.getErrors());
    }

    @GetMapping("/schemas")
    public List<UdoType> allUdoTypes() {
        logger.info("find all udoTypes...");
//        Gson gson = new Gson();
        return udoService.getAllTypes();
    }

    @PostMapping("/schemas")
    public UdoType createUdoType(@RequestBody JsonObject params) {
        logger.info("now saving a new udotype...");
        String name = params.get("schemaName").getAsString();
        JsonObject content = params.get("schemaContent").getAsJsonObject();

        UdoType udoType = new UdoType(content);
        SchemaTree schemaTree = new SchemaTree().createSchemaTree(new Gson()
                .fromJson(udoType.getSchema().toString(), JsonObject.class));
        this.graphQL = graphQlBuilder.addSchemaInGraphQL(schemaTree);
        try {
            return udoService.saveOrUpdateType(udoType);
        } catch (UdoServiceException e) {
            e.printStackTrace();
        }
        return null;
    }

    @DeleteMapping("/schemas/{udoi}")
    public List<UdoType> deleteUdoType(@PathVariable String udoi){
        logger.info("now deleting udoType " + udoi + "...");
        UdoType udoType = udoService.getTypeById(udoi);
        SchemaTree schemaTree = new SchemaTree().createSchemaTree(new Gson()
                .fromJson(udoType.getSchema().toString(), JsonObject.class));
        this.graphQL = graphQlBuilder.deleteSchemaInGraphQl(schemaTree);
        try {
            udoService.deleteTypeById(udoi);
        } catch (UdoServiceException e) {
            e.printStackTrace();
        }
        return udoService.getAllTypes();
    }

    @GetMapping("/schemas/{udoi}")
    public UdoType getUdoTypeById(@PathVariable String udoi) {
        logger.info("now finding UdoType by udoi...");
//        Gson gson = new Gson();
        return udoService.getTypeById(udoi);
    }

    @PutMapping("/schemas/{udoi}")
    public UdoType updateUdoType(@RequestBody JsonObject params, @PathVariable String udoi){
//        String udoi = params.getString("udoi");
        logger.info("now updating schema " + udoi + "...");
//        String name = params.get("schemaName").getAsString();
        JsonObject content = params.get("schemaContent").getAsJsonObject();
//        Gson gson = new Gson();
        return null;
    }

}
