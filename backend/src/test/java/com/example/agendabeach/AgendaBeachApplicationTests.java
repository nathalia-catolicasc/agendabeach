package com.example.agendabeach;

import static org.junit.jupiter.api.Assertions.*;

import com.AgendaBeachApplication;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.*;
import org.springframework.jdbc.core.JdbcTemplate;

import tools.jackson.databind.*;

import java.net.URI;
import java.net.http.*;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;

@SpringBootTest(
        classes = {AgendaBeachApplication.class, AgendaBeachApplicationTests.FixedTime.class},
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
            "spring.flyway.schemas=agendabeach_backend_test",
            "spring.flyway.init-sqls=CREATE EXTENSION IF NOT EXISTS btree_gist WITH SCHEMA public",
            "spring.flyway.default-schema=agendabeach_backend_test",
            "spring.jpa.properties.hibernate.default_schema=agendabeach_backend_test",
            "spring.datasource.hikari.connection-init-sql=SET search_path TO"
                    + " agendabeach_backend_test,public"
        })
class AgendaBeachApplicationTests {
    @TestConfiguration
    static class FixedTime {
        @Bean
        @Primary
        Clock testClock() {
            return Clock.fixed(Instant.parse("2026-10-21T06:00:00Z"), ZoneOffset.UTC);
        }
    }

    @LocalServerPort int port;
    @Autowired JdbcTemplate db;
    @Autowired ObjectMapper json;
    HttpClient client = HttpClient.newHttpClient();

    record Reply(int status, JsonNode body) {}

    record Account(long id, String token) {}

    Account alice, bob, admin;
    long court;

    @Test
    void profileUpdatePreservesCreditChangedByConcurrentTransaction() throws Exception {
        db.update("UPDATE users SET credit_balance=10 WHERE id=?", alice.id());
        try (var pool = Executors.newSingleThreadExecutor();
                var connection = db.getDataSource().getConnection()) {
            connection.setAutoCommit(false);
            try {
                int lockOwner;
                try (var statement = connection.createStatement();
                        var result = statement.executeQuery("SELECT pg_backend_pid()")) {
                    assertTrue(result.next());
                    lockOwner = result.getInt(1);
                }
                try (var lock =
                        connection.prepareStatement("SELECT id FROM users WHERE id=? FOR UPDATE")) {
                    lock.setLong(1, alice.id());
                    try (var result = lock.executeQuery()) {
                        assertTrue(result.next());
                    }
                }
                var update =
                        pool.submit(
                                () ->
                                        request(
                                                "PUT",
                                                "/users/" + alice.id(),
                                                alice.token(),
                                                "{\"name\":\"Updated client\"}"));
                long deadline = System.nanoTime() + Duration.ofSeconds(5).toNanos();
                boolean blocked = false;
                while (System.nanoTime() < deadline) {
                    blocked =
                            db.queryForObject(
                                    "SELECT count(*) > 0 FROM pg_stat_activity WHERE"
                                        + " datname=current_database() AND ? ="
                                        + " ANY(pg_blocking_pids(pid)) AND wait_event_type='Lock'"
                                        + " AND query LIKE '%users%'",
                                    Boolean.class, lockOwner);
                    if (blocked) break;
                    Thread.sleep(20);
                }
                assertTrue(
                        blocked,
                        "Profile update must reach the locked user before the credit changes");
                try (var credit =
                        connection.prepareStatement(
                                "UPDATE users SET credit_balance=40 WHERE id=?")) {
                    credit.setLong(1, alice.id());
                    credit.executeUpdate();
                }
                connection.commit();
                var response = update.get(10, TimeUnit.SECONDS);
                assertEquals(200, response.status());
                assertEquals("Updated client", response.body().get("name").asText());
                assertEquals(
                        40,
                        db.queryForObject(
                                "SELECT credit_balance FROM users WHERE id=?",
                                Integer.class,
                                alice.id()));
            } finally {
                connection.rollback();
            }
        }
    }

    @BeforeEach
    void setup() throws Exception {
        db.execute("TRUNCATE bookings, auth_tokens, users, courts RESTART IDENTITY CASCADE");
        db.update("UPDATE pricing_policy SET regular_price=60, peak_price=100");
        db.update("DELETE FROM holidays");
        alice = register("alice", "52998224725", false);
        bob = register("bob", "11144477735", false);
        admin = register("admin", "12345678909", true);
        var created =
                request(
                        "POST",
                        "/courts",
                        admin.token(),
                        "{\"name\":\"Court A\",\"type\":\"OPEN\"}");
        assertEquals(200, created.status());
        court = created.body().get("id").asLong();
    }

    Account register(String name, String cpf, boolean administrator) throws Exception {
        var response =
                request(
                        "POST",
                        "/users",
                        null,
                        "{\"name\":\""
                                + name
                                + "\",\"email\":\""
                                + name
                                + "@example.com\",\"password\":\"password123\",\"cpf\":\""
                                + cpf
                                + "\"}");
        assertEquals(200, response.status(), response.body().toString());
        long id = response.body().get("id").asLong();
        if (administrator) db.update("UPDATE users SET profile='ADMIN' WHERE id=?", id);
        var login =
                request(
                        "POST",
                        "/users/login",
                        null,
                        "{\"email\":\"" + name + "@example.com\",\"password\":\"password123\"}");
        assertEquals(200, login.status());
        return new Account(id, login.body().get("accessToken").asText());
    }

    Reply request(String method, String path, String token, String body) throws Exception {
        var builder =
                HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
                        .timeout(Duration.ofSeconds(15))
                        .header("Content-Type", "application/json");
        if (token != null) builder.header("Authorization", "Bearer " + token);
        var result =
                client.send(
                        builder.method(
                                        method,
                                        body == null
                                                ? HttpRequest.BodyPublishers.noBody()
                                                : HttpRequest.BodyPublishers.ofString(body))
                                .build(),
                        HttpResponse.BodyHandlers.ofString());
        return new Reply(
                result.statusCode(),
                result.body().isBlank() ? json.nullNode() : json.readTree(result.body()));
    }

    Reply book(Account account, String start, boolean credit) throws Exception {
        return bookOnCourt(account, start, credit, court);
    }

    Reply bookOnCourt(Account account, String start, boolean credit, long targetCourt)
            throws Exception {
        var end = LocalDateTime.parse(start).plusHours(1);
        return request(
                "POST",
                "/bookings",
                account.token(),
                "{\"courtId\":"
                        + targetCourt
                        + ",\"startTime\":\""
                        + start
                        + "\",\"endTime\":\""
                        + end
                        + "\",\"useCredit\":"
                        + credit
                        + "}");
    }

    @Test
    void secureIdentityOwnershipRolesAndTokenExpiry() throws Exception {
        var forged =
                HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/users"))
                        .header("X-User-Id", "3")
                        .build();
        assertEquals(401, client.send(forged, HttpResponse.BodyHandlers.ofString()).statusCode());
        assertEquals(403, request("GET", "/users/" + alice.id(), bob.token(), null).status());
        assertEquals(403, request("GET", "/users", alice.token(), null).status());
        assertEquals(
                403,
                request("PUT", "/pricing", alice.token(), "{\"regularPrice\":1,\"peakPrice\":1}")
                        .status());
        var booking = book(alice, "2026-10-23T06:00:00", false);
        assertEquals(200, booking.status());
        String path = "/bookings/" + booking.body().get("id").asLong();
        assertEquals(403, request("POST", path + "/cancel", bob.token(), null).status());
        assertEquals(403, request("GET", path, bob.token(), null).status());
        assertEquals(403, request("PUT", path, bob.token(), "{}").status());
        assertEquals(403, request("DELETE", path, bob.token(), null).status());
        assertEquals(200, request("POST", "/users/logout", alice.token(), null).status());
        assertEquals(401, request("GET", "/users/" + alice.id(), alice.token(), null).status());
        db.update("UPDATE auth_tokens SET expires_at='2026-10-20'");
        assertEquals(401, request("GET", "/users", admin.token(), null).status());
    }

    @Test
    void pricingHolidaysHoursQuotaAndAvailability() throws Exception {
        assertEquals(
                200,
                request("PUT", "/pricing", admin.token(), "{\"regularPrice\":70,\"peakPrice\":110}")
                        .status());
        var b = book(alice, "2026-10-22T06:00:00", false);
        assertEquals(200, b.status());
        assertEquals(70, b.body().get("courtPrice").asInt());
        assertEquals(
                200,
                request(
                                "PUT",
                                "/pricing/holidays",
                                admin.token(),
                                "{\"date\":\"2026-10-23\",\"description\":\"Local holiday\"}")
                        .status());
        assertEquals(
                110, book(alice, "2026-10-23T06:00:00", false).body().get("courtPrice").asInt());
        assertEquals(
                110, book(alice, "2026-10-24T06:00:00", false).body().get("courtPrice").asInt());
        assertEquals(409, book(alice, "2026-10-25T06:00:00", false).status());
        assertEquals(400, book(bob, "2026-10-22T05:00:00", false).status());
        assertEquals(
                400,
                request(
                                "POST",
                                "/bookings",
                                bob.token(),
                                "{\"courtId\":"
                                        + court
                                        + ",\"startTime\":\"2026-10-22T06:00:00\",\"endTime\":\"2026-10-22T07:30:00\"}")
                        .status());
        var slots =
                request(
                        "GET",
                        "/bookings/availability?date=2026-10-22&courtId=" + court,
                        bob.token(),
                        null);
        assertEquals(200, slots.status());
        assertEquals(17, slots.body().size());
        assertFalse(slots.body().get(0).get("available").asBoolean());
        assertTrue(slots.body().get(1).get("available").asBoolean());
        assertEquals("2026-10-22T23:00:00", slots.body().get(16).get("endTime").asText());
        for (int i = 0; i < 5; i++) {
            long start = System.nanoTime();
            assertEquals(
                    200,
                    request(
                                    "GET",
                                    "/bookings/availability?date=2026-10-22&courtId=" + court,
                                    bob.token(),
                                    null)
                            .status());
            assertTrue(
                    System.nanoTime() - start < Duration.ofSeconds(2).toNanos(),
                    "Availability exceeded 2s in local smoke test");
        }
    }

    @Test
    void cancellationPersistsCreditsAndAllowsFutureUseWithoutDuplicateCredit() throws Exception {
        var b =
                book(
                        alice,
                        "2026-10-22T06:00:00",
                        false); // exactly 24 hours: credit, not cash refund
        long id = b.body().get("id").asLong();
        var cancelled = request("POST", "/bookings/" + id + "/cancel", alice.token(), null);
        assertEquals(200, cancelled.status());
        assertEquals(0, cancelled.body().get("refundAmount").asInt());
        assertEquals(30, cancelled.body().get("creditAmount").asInt());
        assertEquals(
                409, request("POST", "/bookings/" + id + "/cancel", alice.token(), null).status());
        assertEquals(
                30,
                request("GET", "/users/me/credits", alice.token(), null)
                        .body()
                        .get("creditBalance")
                        .asInt());
        var next = book(alice, "2026-10-23T06:00:00", true);
        assertEquals(200, next.status());
        assertEquals(
                0,
                request("GET", "/users/me/credits", alice.token(), null)
                        .body()
                        .get("creditBalance")
                        .asInt());
        var refund =
                request(
                        "POST",
                        "/bookings/" + next.body().get("id").asLong() + "/cancel",
                        alice.token(),
                        null);
        assertEquals(30, refund.body().get("refundAmount").asInt());
        assertEquals(30, refund.body().get("creditAmount").asInt());
    }

    @Test
    void simultaneousSameSlotHasOneWinnerAndQuotaSerializesAcrossCourts() throws Exception {
        try (var pool = Executors.newFixedThreadPool(2)) {
            var gate = new CountDownLatch(1);
            var one =
                    pool.submit(
                            () -> {
                                gate.await();
                                return book(alice, "2026-10-23T06:00:00", false).status();
                            });
            var two =
                    pool.submit(
                            () -> {
                                gate.await();
                                return book(bob, "2026-10-23T06:00:00", false).status();
                            });
            gate.countDown();
            var statuses =
                    new ArrayList<>(
                            List.of(one.get(20, TimeUnit.SECONDS), two.get(20, TimeUnit.SECONDS)));
            statuses.sort(Integer::compare);
            assertEquals(List.of(200, 409), statuses);
        }
        db.execute("TRUNCATE bookings RESTART IDENTITY");
        assertEquals(200, book(alice, "2026-10-23T07:00:00", false).status());
        assertEquals(200, book(alice, "2026-10-23T08:00:00", false).status());
        long secondCourt =
                request(
                                "POST",
                                "/courts",
                                admin.token(),
                                "{\"name\":\"Court B\",\"type\":\"COVERED\"}")
                        .body()
                        .get("id")
                        .asLong();
        try (var pool = Executors.newFixedThreadPool(2)) {
            var gate = new CountDownLatch(1);
            var one =
                    pool.submit(
                            () -> {
                                gate.await();
                                return book(alice, "2026-10-23T09:00:00", false).status();
                            });
            var two =
                    pool.submit(
                            () -> {
                                gate.await();
                                return bookOnCourt(alice, "2026-10-23T10:00:00", false, secondCourt)
                                        .status();
                            });
            gate.countDown();
            var statuses =
                    new ArrayList<>(
                            List.of(one.get(20, TimeUnit.SECONDS), two.get(20, TimeUnit.SECONDS)));
            statuses.sort(Integer::compare);
            assertEquals(List.of(200, 409), statuses);
        }
        assertEquals(
                3,
                db.queryForObject(
                        "SELECT count(*) FROM bookings WHERE user_id=? AND status='SCHEDULED'",
                        Integer.class,
                        alice.id()));
    }

    @Test
    void concurrentCancellationCreditsOnceAndDatabaseRejectsOverlaps() throws Exception {
        long id = book(alice, "2026-10-22T06:00:00", false).body().get("id").asLong();
        try (var pool = Executors.newFixedThreadPool(2)) {
            var gate = new CountDownLatch(1);
            Callable<Integer> cancel =
                    () -> {
                        gate.await();
                        return request("POST", "/bookings/" + id + "/cancel", alice.token(), null)
                                .status();
                    };
            var one = pool.submit(cancel);
            var two = pool.submit(cancel);
            gate.countDown();
            var statuses =
                    new ArrayList<>(
                            List.of(one.get(20, TimeUnit.SECONDS), two.get(20, TimeUnit.SECONDS)));
            statuses.sort(Integer::compare);
            assertEquals(List.of(200, 409), statuses);
        }
        assertEquals(
                30,
                db.queryForObject(
                        "SELECT credit_balance FROM users WHERE id=?", Integer.class, alice.id()));
        assertEquals(200, book(alice, "2026-10-23T06:00:00", false).status());
        assertThrows(
                org.springframework.dao.DataIntegrityViolationException.class,
                () ->
                        db.update(
                                "INSERT INTO"
                                    + " bookings(user_id,court_id,start_time,end_time,court_price)"
                                    + " VALUES (?,?,'2026-10-23 06:30','2026-10-23 07:30',60)",
                                bob.id(),
                                court));
    }

    @Test
    void cpfAndOpenApi() throws Exception {
        var health = request("GET", "/actuator/health", null, null);
        assertEquals(200, health.status());
        assertEquals("UP", health.body().get("status").asText());
        assertEquals(
                400,
                request(
                                "POST",
                                "/users",
                                null,
                                "{\"name\":\"X\",\"email\":\"x@example.com\",\"password\":\"password123\",\"cpf\":\"11111111111\"}")
                        .status());
        assertEquals(
                409,
                request(
                                "POST",
                                "/users",
                                null,
                                "{\"name\":\"X\",\"email\":\"x@example.com\",\"password\":\"password123\",\"cpf\":\"529.982.247-25\"}")
                        .status());
        var docs = request("GET", "/v3/api-docs", null, null);
        assertEquals(200, docs.status());
        assertTrue(docs.body().get("paths").has("/bookings/availability"));
        assertTrue(docs.body().get("components").get("securitySchemes").has("bearerAuth"));
    }

    @Test
    void upgradesExistingDatabaseAndPreservesLegacyUsers() throws Exception {
        String schema = "legacy_upgrade_test";
        db.execute("CREATE SCHEMA " + schema);
        try {
            try (var connection = db.getDataSource().getConnection()) {
                String previousSchema = connection.getSchema();
                try {
                    connection.setSchema(schema);
                    org.springframework.jdbc.datasource.init.ScriptUtils.executeSqlScript(
                            connection,
                            new org.springframework.core.io.ClassPathResource(
                                    "db/migration/V1__initial_schema.sql"));
                    connection
                            .createStatement()
                            .executeUpdate(
                                    "INSERT INTO users(name,email,password) VALUES"
                                        + " ('Legacy','legacy@example.com','unused-test-hash')");
                } finally {
                    connection.setSchema(previousSchema);
                }
            }
            var flyway =
                    org.flywaydb.core.Flyway.configure()
                            .dataSource(db.getDataSource())
                            .schemas(schema)
                            .defaultSchema(schema)
                            .baselineOnMigrate(true)
                            .baselineVersion("1")
                            .load();
            assertEquals(1, flyway.migrate().migrationsExecuted);
            assertEquals(
                    1,
                    db.queryForObject(
                            "SELECT count(*) FROM "
                                    + schema
                                    + ".users WHERE cpf IS NULL AND credit_balance=0",
                            Integer.class));
            assertEquals(
                    60,
                    db.queryForObject(
                            "SELECT regular_price FROM " + schema + ".pricing_policy",
                            Integer.class));
            assertEquals(0, flyway.migrate().migrationsExecuted);
        } finally {
            db.execute("DROP SCHEMA " + schema + " CASCADE");
        }
    }

    @Test
    void updateCannotBypassRulesAndLegacyUsersCanRegisterCpf() throws Exception {
        db.update("UPDATE users SET cpf=NULL WHERE id=?", alice.id());
        assertEquals(409, book(alice, "2026-10-23T06:00:00", false).status());
        assertEquals(
                200,
                request(
                                "PUT",
                                "/users/" + alice.id(),
                                alice.token(),
                                "{\"cpf\":\"529.982.247-25\"}")
                        .status());
        var b = book(alice, "2026-10-23T06:00:00", false);
        long id = b.body().get("id").asLong();
        String path = "/bookings/" + id;
        assertEquals(
                400,
                request("PUT", path, alice.token(), "{\"startTime\":\"2026-10-23T06:30:00\"}")
                        .status());
        assertEquals(
                200,
                request(
                                "PUT",
                                path,
                                alice.token(),
                                "{\"startTime\":\"2026-10-23T07:00:00\",\"endTime\":\"2026-10-23T08:00:00\"}")
                        .status());
        assertEquals(
                409,
                request(
                                "PUT",
                                path,
                                alice.token(),
                                "{\"startTime\":\"2026-10-23T18:00:00\",\"endTime\":\"2026-10-23T19:00:00\"}")
                        .status());
        assertEquals(200, request("DELETE", path, alice.token(), null).status());
        assertEquals(
                "CANCELLED",
                request("GET", path, alice.token(), null).body().get("status").asText());
        assertEquals(409, request("PUT", path, alice.token(), "{}").status());
    }
}
