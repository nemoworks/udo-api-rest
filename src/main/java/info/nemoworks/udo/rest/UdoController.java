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
import info.nemoworks.udo.storage.UdoNotExistException;
import info.nemoworks.udo.storage.UdoPersistException;
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
        System.out.println(ResponseEntity.ok(result.getData()));
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

    @GetMapping("/documents")
    public List<Udo> allUdos() {
        log.info("find all udos...");
        return udoService.getAllUdos();
    }

    @GetMapping("/documents/{udoi}")
    public Udo getUdoById(@PathVariable String udoi) {
        log.info("find udo by id: " + udoi + " ...");
        return udoService.getUdoById(udoi);
    }

    @PostMapping("/schemas")
    public UdoType createUdoType(@RequestBody JsonObject params) {
        log.info("now saving a new udotype...");
        JsonObject content = (JsonObject) params.get("content");
        String name = params.get("name").getAsString();
        UdoType udoType = new UdoType(content);
        if (content.has("properties")) {
            SchemaTree schemaTree = new SchemaTree().createSchemaTree(new Gson()
                .fromJson(udoType.getSchema().toString(), JsonObject.class), name);
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
    public Udo createUdoByUri(@RequestParam String uri, @RequestParam String name,
        @RequestParam String location, @RequestParam String uriType)
        throws UdoServiceException, InterruptedException, JsonProcessingException, UdoNotExistException, UdoPersistException {
        log.info("now creating udo by uri: " + uri + "...");
        String id = udoService.createUdoByUri(uri, location, uriType);
        Udo udo = udoService.getUdoById(id);
        while (udo == null) {
            udo = udoService.getUdoById(id);
            Thread.sleep(1000);
        }
//        System.out.println(udo.getContextInfo().getContext("location"));
//        System.out.println(udo.getUri());
//        UdoType udoType = udo.inferType();
//        System.out.println(udo.inferType());
        UdoType udoType = udo.getType();
        JsonObject schema = udoType.getSchema();
        schema.addProperty("title", name);
        udoType.setSchema(schema);
        udoService.saveOrUpdateType(udoType);
//        ContextInfo contextInfo = udo.getContextInfo();
//        contextInfo.addContext("location", location);
//        udo.setContextInfo(contextInfo);
//        udo.setType(udoType);
//        udoService.saveOrUpdateUdo(udo);
        SchemaTree schemaTree = new SchemaTree().createSchemaTree(new Gson()
            .fromJson(udo.inferType().getSchema().toString(), JsonObject.class), name);
        this.graphQL = graphQlBuilder.addSchemaInGraphQL(schemaTree);
        return udo;
    }

    @DeleteMapping("/schemas/{udoi}/{name}")
    public List<UdoType> deleteUdoTypeWithName(@PathVariable String udoi,
        @PathVariable String name) {
        log.info("now deleting udoType " + udoi + "...");
        UdoType udoType = udoService.getTypeById(udoi);
        if (udoType.getSchema().has("properties")) {
            SchemaTree schemaTree = new SchemaTree().createSchemaTree(new Gson()
                .fromJson(udoType.getSchema().toString(), JsonObject.class), name);
            this.graphQL = graphQlBuilder.deleteSchemaInGraphQl(schemaTree);
        }
        try {
            udoService.deleteTypeById(udoi);
        } catch (UdoServiceException e) {
            e.printStackTrace();
        }
        return udoService.getAllTypes();
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

    @PutMapping("/schemas/{udoi}/{name}")
    public UdoType updateUdoTypeWithName(@RequestBody JsonObject params,
        @PathVariable String udoi, @PathVariable String name) {
//        String udoi = params.getString("udoi");
        log.info("now updating schema " + udoi + "...");
//        JsonObject content = (JsonObject) params.get("content");
        UdoType udoType = new UdoType(params);
        udoType.setId(udoi);
        if (params.has("properties")) {
            SchemaTree schemaTree = new SchemaTree().createSchemaTree(new Gson()
                .fromJson(udoType.getSchema().toString(), JsonObject.class), name);
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
