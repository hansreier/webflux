package webflux.controller;


import static org.assertj.core.api.Assertions.assertThat;
import static webflux.util.FileUtilities.*;
import static webflux.util.FileUtilities.createDocBytes;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;

import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import webflux.config.AppTestConfig;
import webflux.domain.Document;

@ContextConfiguration(classes = AppTestConfig.class)
@WebFluxTest(controllers = DbController.class)
@AutoConfigureWebTestClient(timeout = "360000")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Rollback(false)
public class DbControllerTest {

    private static final Logger LOG = LoggerFactory.getLogger(DbControllerTest.class);

    private static final int NO_FILES = 1;
    private static final int KBYTES_SMALL = 400;

    @Autowired
    private WebTestClient webTestClient;

    @BeforeAll
    private static void startup() throws Exception {
        generateFile(KBYTES_SMALL);
        generateFiles(KBYTES_SMALL, NO_FILES);
    }

    @Test
    @Order(1)
    public void testCreateDocuments() {
        LOG.info("Creating documents in db test");
        int noDocuments = 3;
        for (int i = 0; i < noDocuments; i++) {
            Document document = new Document();
            document.setCreated(LocalDateTime.now());
            document.setDocumentType("Test");
            Flux<Document> savedDocuments =
                    webTestClient.post().uri("/db/create")
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(Mono.just(document), Document.class)
                            .exchange()
                            .expectStatus().isOk()
                            .returnResult(Document.class).getResponseBody();
            Document saved = savedDocuments.blockLast();
            if (saved != null) {
                LOG.info("Generert nøkkel: {}", saved.getDocumentKey());
            } else {
                LOG.info("Ingen nøkler funnet");
            }
        }
    }

    @Test
    @Order(2)
    public void testUpdateAndReadDocument() throws IOException {
        String fileName = RESOURCE_DIR + "BetalingGen.txt";
        generateFile(fileName, FILE_TEXT, KBYTES_SMALL);
        byte[] docBytes = createDocBytes(fileName);
        LOG.info("Update document test");
        Document document = new Document();
        document.setDocumentKey(1L);
        document.setDocument(docBytes);
        Flux<Document> savedDocument =
                webTestClient.post().uri("/db/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(Mono.just(document), Document.class)
                        .exchange()
                        .expectStatus().isOk()
                        .returnResult(Document.class).getResponseBody();
        Document saved = savedDocument.blockLast();
        assertThat(saved).isNotNull();
        LOG.info("Oppdatert forekomst: {}", saved.getDocumentKey());
        LOG.info("Read document");
        int id = 1;
        Flux<Document> result =
                webTestClient.get().uri("/db/" + id)
                        .exchange()
                        .expectStatus().isOk()
                        .returnResult(Document.class).getResponseBody();
        Document read = result.blockLast();
        assertThat(read).isNotNull();
        LOG.info("Document read {}", read.getDocumentKey());
        byte[] content = read.getDocument();
        assertThat(content).isEqualTo(docBytes);
        String text = new String(content, StandardCharsets.UTF_8);
        LOG.info("Document contents {}", text);
    }


    @Test
    @Order(2)
    public void testUpdateDocument() {
        LOG.info("Update document test");
        Document document = new Document();
        document.setDocumentKey(1L);
        document.setCreated(LocalDateTime.now());
        document.setDocumentType("Test");
        Flux<Document> savedDocument =
                webTestClient.post().uri("/db/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(Mono.just(document), Document.class)
                        .exchange()
                        .expectStatus().isOk()
                        .returnResult(Document.class).getResponseBody();
        Document saved = savedDocument.blockLast();
        assertThat(saved).isNotNull();
        LOG.info("Oppdatert forekomst: {}", saved.getDocumentKey());
    }

    @Test
    @Order(3)
    public void testDeleteDocument() {
        LOG.info("Delete document instance test");
        int id = 2;
        Flux<Object> result =
                webTestClient.delete().uri("/db/delete/" + id)
                        .exchange()
                        .expectStatus().isOk()
                        .returnResult(Object.class).getResponseBody();
        result.blockLast();
        LOG.info("Deleted or not existing");
    }

    @Test
    @Order(4)
    public void testDBFileUpload() {
        LOG.info("Test web client for file upload started");
        String fileName = RESOURCE_DIR + "BetalingGen.txt";
        //  String fileName = RESOURCE_DIR +"Betaling.txt";
        File file = new File(fileName);
        Flux<Document> savedDocument =
                webTestClient.post()
                        .uri("/db/uploadToDb")
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .body(BodyInserters.fromMultipartData(fromFile(file)))
                        .exchange()
                        .expectStatus().isOk()
                        .returnResult(Document.class).getResponseBody();
        Document saved = savedDocument.blockLast();
        LOG.info("completed test ");
        byte[] docBytes = saved.getDocument();
        Long id = saved.getDocumentKey();
        LOG.info(new String(saved.getDocument(), StandardCharsets.UTF_8));
        LOG.info("end result");

        assertThat(saved).isNotNull();

        if (id != null) {
            LOG.info("Oppdatert forekomst: {}", saved.getDocumentKey());
            LOG.info("Read document");
            Flux<Document> result =
                    webTestClient.get().uri("/db/" + id)
                            .exchange()
                            .expectStatus().isOk()
                            .returnResult(Document.class).getResponseBody();
            Document read = result.blockLast();
            assertThat(read).isNotNull();
            LOG.info("Document read {}", read.getDocumentKey());
            byte[] content = read.getDocument();
            assertThat(content).isEqualTo(docBytes);
            String text = new String(content, StandardCharsets.UTF_8);
            LOG.info("Document contents {}", text);
        } else
            LOG.error("Document not written to db, id is not returned");
    }

    @Test
    @Order(5)
    public void readAllDocuments() {
        LOG.info("Read all documents test");
        Flux<Document> savedDocuments =
                webTestClient.get().uri("/db/all")
                        .exchange()
                        .expectStatus().isOk()
                        .returnResult(Document.class).getResponseBody();
        savedDocuments.subscribe(s -> LOG.info("Key: {}", s.getDocumentKey()));
    }

    private MultiValueMap<String, HttpEntity<?>> fromFile(File file) {
        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("file", new FileSystemResource(file));
        return builder.build();
    }

}
