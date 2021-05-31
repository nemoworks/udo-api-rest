package info.nemoworks.udo.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import graphql.ExecutionResult;
import graphql.GraphQL;
import info.nemoworks.udo.graphql.graphqlBuilder.GraphQLBuilder;
import info.nemoworks.udo.graphql.schemaParser.SchemaTree;
import info.nemoworks.udo.model.Udo;
import info.nemoworks.udo.model.UdoType;
import info.nemoworks.udo.service.UdoService;
import info.nemoworks.udo.service.UdoServiceException;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin
@RequestMapping("/api")
@Slf4j
public class UdoController {

//    private static final Logger log = LoggerFactory.getLogger(UdoController.class);

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
        log.info("query: " + query);
        log.info("errors: " + result.getErrors());
        if (result.getErrors().isEmpty()) {
            return ResponseEntity.ok(result.getData());
        } else {
            return ResponseEntity.badRequest().body(result.getErrors());
        }
    }

    @GetMapping("/schemas")
    public List<UdoType> allUdoTypes() {
        log.info("find all udoTypes...");
//        Gson gson = new Gson();
        return udoService.getAllTypes();
    }

    @PostMapping("/schemas")
    public UdoType createUdoType(@RequestBody JsonObject params) {
        log.info("now saving a new udotype...");
//        String name = params.get("schemaName").getAsString();
        JsonObject content = (JsonObject) params.get("content");
        UdoType udoType = new UdoType(content);
        if (content.has("properties")) {
            SchemaTree schemaTree = new SchemaTree().createSchemaTree(new Gson()
                .fromJson(udoType.getSchema().toString(), JsonObject.class));
            this.graphQL = graphQlBuilder.addSchemaInGraphQL(schemaTree);
        }
        try {
            return udoService.saveOrUpdateType(udoType);
        } catch (UdoServiceException e) {
            e.printStackTrace();
        }
        return null;
    }

    @PostMapping("/documents")
    public Udo createUdoByUri(@RequestParam String uri, @RequestParam String id)
        throws UdoServiceException, InterruptedException, JsonProcessingException {
        log.info("now creating udo " + id + "by uri: " + uri + "...");
        udoService.createUdoByUri(uri, id);
        Udo udo = udoService.getUdoById(id);
        while (udo == null) {
            udo = udoService.getUdoById(id);
            Thread.sleep(1000);
        }
        UdoType udoType = udo.inferType();
//        JsonObject schema = udoType.getSchema();
//        schema.addProperty("title", id);
//        udoType.setSchema(schema);
        SchemaTree schemaTree = new SchemaTree().createSchemaTree(new Gson()
            .fromJson(udoType.getSchema().toString(), JsonObject.class));
        this.graphQL = graphQlBuilder.addSchemaInGraphQL(schemaTree);
        return udo;
    }

    @DeleteMapping("/schemas/{udoi}")
    public List<UdoType> deleteUdoType(@PathVariable String udoi) {
        log.info("now deleting udoType " + udoi + "...");
        UdoType udoType = udoService.getTypeById(udoi);
        if (udoType.getSchema().has("properties")) {
            SchemaTree schemaTree = new SchemaTree().createSchemaTree(new Gson()
                .fromJson(udoType.getSchema().toString(), JsonObject.class));
            this.graphQL = graphQlBuilder.deleteSchemaInGraphQl(schemaTree);
        }
        try {
            udoService.deleteTypeById(udoi);
        } catch (UdoServiceException e) {
            e.printStackTrace();
        }
        return udoService.getAllTypes();
    }

    @GetMapping("/schemas/{udoi}")
    public UdoType getUdoTypeById(@PathVariable String udoi) {
        log.info("now finding UdoType by udoi " + udoi + "...");
//        Gson gson = new Gson();
        return udoService.getTypeById(udoi);
    }

    @PutMapping("/schemas/{udoi}")
    public UdoType updateUdoType(@RequestBody JsonObject params, @PathVariable String udoi) {
//        String udoi = params.getString("udoi");
        log.info("now updating schema " + udoi + "...");
        JsonObject content = (JsonObject) params.get("content");
        UdoType udoType = new UdoType(content);
        udoType.setId(udoi);
        if (content.has("properties")) {
            SchemaTree schemaTree = new SchemaTree().createSchemaTree(new Gson()
                .fromJson(udoType.getSchema().toString(), JsonObject.class));
            this.graphQL = graphQlBuilder.addSchemaInGraphQL(schemaTree);
        }
        try {
            return udoService.saveOrUpdateType(udoType);
        } catch (UdoServiceException e) {
            e.printStackTrace();
        }
        return null;
    }

}
