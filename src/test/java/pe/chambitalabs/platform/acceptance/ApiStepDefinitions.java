package pe.chambitalabs.platform.acceptance;

import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.mockito.Mockito;
import pe.chambitalabs.platform.certification.application.internal.outboundservices.BlockchainCertificateRegistry;
import pe.chambitalabs.platform.certification.application.internal.outboundservices.BlockchainCertificateRegistry.Issuance;
import pe.chambitalabs.platform.iam.application.internal.outboundservices.MailService;

import java.math.BigInteger;
import java.time.Duration;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import org.awaitility.Awaitility;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/** Reusable steps that drive the RESTful API like the web and mobile applications do. */
public class ApiStepDefinitions {

    private static final String PASSWORD = "Chambita2026!";
    private static final AtomicLong TOKEN_IDS = new AtomicLong(1);

    private final ScenarioContext context;
    private final MailService mailService;
    private final BlockchainCertificateRegistry blockchain;

    public ApiStepDefinitions(ScenarioContext context, MailService mailService,
                              BlockchainCertificateRegistry blockchain) {
        this.context = context;
        this.mailService = mailService;
        this.blockchain = blockchain;
    }

    @Before
    public void simulateSepolia() {
        Awaitility.pollInSameThread();
        Awaitility.setDefaultPollInterval(Duration.ofMillis(200));
        when(blockchain.isConfigured()).thenReturn(true);
        when(blockchain.contractAddress()).thenReturn("0x00000000000000000000000000000000000c4a3b");
        when(blockchain.issue(anyString(), anyString())).thenAnswer(invocation -> randomHash());
        when(blockchain.findIssuance(anyString())).thenAnswer(invocation ->
                Optional.of(new Issuance(true, BigInteger.valueOf(TOKEN_IDS.getAndIncrement()), null)));
    }

    // ---------- Actors ----------

    @Given("a client {string} is registered and signed in")
    public void aClientIsRegistered(String name) {
        var email = context.emailOf(name, "gmail.com");
        signUp(name, email, "CLIENT");
        signIn(name, email);
    }

    @Given("a student {string} registered with an institutional email")
    public void aStudentRegistered(String name) {
        var email = context.emailOf(name, "upc.edu.pe");
        signUp(name, email, "STUDENT");
        context.put(name + "Email", email);
    }

    @Given("a verified student {string} with the category {string}")
    public void aVerifiedStudent(String name, String category) {
        aStudentRegistered(name);
        var email = context.get(name + "Email");
        context.send("POST", "/api/v1/student-verifications/confirmations",
                "{\"email\":\"" + email + "\",\"code\":\"" + verificationCodeSentTo(email) + "\"}", null, null);
        assertThat(context.lastStatus()).isEqualTo(200);
        signIn(name, email);
        var student = context.actor(name);
        await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> {
            context.send("GET", "/api/v1/students/" + student.id(), null, student.token(), null);
            assertThat(context.lastStatus()).isEqualTo(200);
        });
        context.send("PUT", "/api/v1/students/" + student.id() + "/profile", """
                {"career":"Diseño Gráfico","cycle":7,"bio":"Diseño identidades para marcas pequeñas",
                 "skills":["Illustrator","Canva","Identidad visual"],"serviceCategories":["%s"]}
                """.formatted(category), student.token(), null);
        assertThat(context.lastStatus()).isEqualTo(200);
    }

    @Then("a verification code was sent to {string}")
    public void aVerificationCodeWasSent(String name) {
        assertThat(verificationCodeSentTo(context.get(name + "Email"))).matches("\\d{6}");
    }

    @When("{string} confirms the verification code")
    public void confirmsTheVerificationCode(String name) {
        var email = context.get(name + "Email");
        context.send("POST", "/api/v1/student-verifications/confirmations",
                "{\"email\":\"" + email + "\",\"code\":\"" + verificationCodeSentTo(email) + "\"}", null, null);
    }

    @When("{string} confirms the code {string}")
    public void confirmsTheCode(String name, String code) {
        context.send("POST", "/api/v1/student-verifications/confirmations",
                "{\"email\":\"" + context.get(name + "Email") + "\",\"code\":\"" + code + "\"}", null, null);
    }

    @Given("{string} signs in")
    public void signsIn(String name) {
        signIn(name, context.get(name + "Email"));
    }

    // ---------- Business flow shortcuts ----------

    @Given("{string} published a project {string} with budget {double}")
    public void publishedAProject(String client, String title, double budget) {
        context.send("POST", "/api/v1/projects", """
                {"title":"%s","description":"Vendo ropa juvenil y necesito una imagen profesional.",
                 "category":"GRAPHIC_DESIGN","requiredSkills":["Illustrator","Canva"],
                 "deliverables":["Logo","Paleta","Plantillas"],"budget":%s,"deadline":"2099-12-31"}
                """.formatted(title, budget), context.actor(client).token(), null);
        assertThat(context.lastStatus()).isEqualTo(201);
        context.put("projectId", context.read("$.id"));
        context.put("client", client);
    }

    @Given("{string} sent a proposal for {double} in {int} days")
    public void sentAProposal(String student, double price, int days) {
        context.send("POST", "/api/v1/projects/{projectId}/proposals",
                "{\"price\":" + price + ",\"deliveryDays\":" + days + ",\"message\":\"Te propongo tres bocetos.\"}",
                context.actor(student).token(), null);
        assertThat(context.lastStatus()).isEqualTo(201);
        context.put("proposalId", context.read("$.id"));
        context.put(student + "ProposalId", context.read("$.id"));
    }

    @Given("{string} accepted the proposal")
    public void acceptedTheProposal(String client) {
        context.send("POST", "/api/v1/projects/{projectId}/proposals/{proposalId}/acceptance", null,
                context.actor(client).token(), null);
        assertThat(context.lastStatus()).isEqualTo(200);
        var token = context.actor(client).token();
        await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> {
            context.send("GET", "/api/v1/engagements", null, token, null);
            List<String> ids = context.read("$[?(@.proposalId == '" + context.get("proposalId") + "')].id");
            assertThat(ids).hasSize(1);
            context.put("engagementId", ids.getFirst());
        });
    }

    @Given("{string} paid the engagement")
    public void paidTheEngagement(String client) {
        var token = context.actor(client).token();
        await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> {
            context.send("POST", "/api/v1/engagements/{engagementId}/payments", null, token, null);
            assertThat(context.lastStatus()).isEqualTo(200);
        });
        theEngagementIs("ACTIVE");
    }

    @Given("{string} delivered the work")
    public void deliveredTheWork(String student) {
        context.send("POST", "/api/v1/engagements/{engagementId}/deliverables",
                "{\"linkUrl\":\"https://drive.google.com/chambita-entrega\",\"note\":\"Entrega final\"}",
                context.actor(student).token(), null);
        assertThat(context.lastStatus()).isEqualTo(201);
    }

    @Given("{string} approved the work")
    public void approvedTheWork(String client) {
        context.send("POST", "/api/v1/engagements/{engagementId}/approval", null, context.actor(client).token(), null);
        assertThat(context.lastStatus()).isEqualTo(200);
        theEngagementIs("COMPLETED");
    }

    @Given("{string} rated {string} with {int} stars")
    public void ratedWithStars(String reviewer, String reviewee, int score) {
        var token = context.actor(reviewer).token();
        await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> {
            context.send("POST", "/api/v1/engagements/{engagementId}/reviews",
                    "{\"score\":" + score + ",\"comment\":\"Entregó a tiempo.\"}", token, null);
            assertThat(context.lastStatus()).isEqualTo(201);
        });
    }

    // ---------- Generic requests ----------

    @When("{string} sends a {word} request to {string}")
    public void sendsARequest(String actor, String method, String path) {
        context.send(method, path, null, context.actor(actor).token(), null);
    }

    @When("{string} sends a {word} request to {string} with:")
    public void sendsARequestWith(String actor, String method, String path, String body) {
        context.send(method, path, body, context.actor(actor).token(), null);
    }

    @When("an anonymous user sends a {word} request to {string} with:")
    public void anonymousSendsWith(String method, String path, String body) {
        context.send(method, path, body, null, null);
    }

    @When("an anonymous user sends a {word} request to {string} with the language {string} and:")
    public void anonymousSendsWithLanguage(String method, String path, String language, String body) {
        context.send(method, path, body, null, language);
    }

    @When("an anonymous user sends a {word} request to {string}")
    public void anonymousSends(String method, String path) {
        context.send(method, path, null, null, null);
    }

    // ---------- Assertions ----------

    @Then("the response status is {int}")
    public void theResponseStatusIs(int status) {
        assertThat(context.lastStatus()).as(context.lastBody()).isEqualTo(status);
    }

    @And("the response field {string} is {string}")
    public void theResponseFieldIs(String path, String expected) {
        Object value = context.read(path);
        assertThat(String.valueOf(value)).isEqualTo(context.resolve(expected));
    }

    @And("the response field {string} exists")
    public void theResponseFieldExists(String path) {
        Object value = context.read(path);
        assertThat(value).isNotNull();
    }

    @And("the response list {string} has {int} items")
    public void theResponseListHasItems(String path, int size) {
        List<Object> list = context.read(path);
        assertThat(list).hasSize(size);
    }

    @And("the response does not contain {string}")
    public void theResponseDoesNotContain(String text) {
        assertThat(context.lastBody()).doesNotContain(context.resolve(text));
    }

    @Then("the engagement is {string}")
    public void theEngagementIs(String status) {
        var engagementId = context.get("engagementId");
        await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> {
            context.send("GET", "/api/v1/engagements/" + engagementId, null, anyParticipantToken(), null);
            assertThat(context.lastStatus()).isEqualTo(200);
            assertThat((String) context.read("$.status")).isEqualTo(status);
        });
    }

    @Then("the payment of the engagement is {string}")
    public void thePaymentIs(String status) {
        await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> {
            context.send("GET", "/api/v1/engagements/{engagementId}/payments", null, anyParticipantToken(), null);
            assertThat(context.lastStatus()).isEqualTo(200);
            assertThat((String) context.read("$.status")).isEqualTo(status);
        });
    }

    @Then("the certificate of {string} is {string}")
    public void theCertificateIs(String student, String status) {
        var actor = context.actor(student);
        await().atMost(Duration.ofSeconds(20)).untilAsserted(() -> {
            context.send("GET", "/api/v1/students/" + actor.id() + "/certificates", null, actor.token(), null);
            List<String> statuses = context.read("$[*].status");
            assertThat(statuses).containsExactly(status);
        });
        context.put("certificateHash", context.read("$[0].certificateHash"));
    }

    @Then("the profile of {string} shows {int} certificate(s)")
    public void theProfileShowsCertificates(String student, int count) {
        var actor = context.actor(student);
        await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> {
            context.send("GET", "/api/v1/students/" + actor.id(), null, actor.token(), null);
            Integer certificates = context.read("$.certificateCount");
            assertThat(certificates).isEqualTo(count);
        });
    }

    // ---------- Helpers ----------

    private void signUp(String name, String email, String role) {
        context.send("POST", "/api/v1/authentication/sign-up", """
                {"email":"%s","password":"%s","fullName":"%s Test","role":"%s"}
                """.formatted(email, PASSWORD, name, role), null, null);
        assertThat(context.lastStatus()).as(context.lastBody()).isEqualTo(201);
        context.addActor(name, new ScenarioContext.Actor(context.read("$.id"), email, PASSWORD, null));
        context.put(name + "Email", email);
    }

    private void signIn(String name, String email) {
        context.send("POST", "/api/v1/authentication/sign-in",
                "{\"email\":\"" + email + "\",\"password\":\"" + PASSWORD + "\"}", null, null);
        assertThat(context.lastStatus()).isEqualTo(200);
        context.addActor(name, new ScenarioContext.Actor(context.read("$.id"), email, PASSWORD, context.read("$.token")));
    }

    private String anyParticipantToken() {
        return context.actor(context.get("client")).token();
    }

    private String verificationCodeSentTo(String email) {
        var codes = new java.util.ArrayList<String>();
        await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> {
            codes.clear();
            Mockito.mockingDetails(mailService).getInvocations().stream()
                    .filter(invocation -> invocation.getMethod().getName().equals("sendVerificationCode"))
                    .filter(invocation -> email.equals(invocation.getArgument(0)))
                    .forEach(invocation -> codes.add(invocation.getArgument(2)));
            assertThat(codes).isNotEmpty();
        });
        return codes.getLast();
    }

    private static String randomHash() {
        var bytes = new byte[32];
        ThreadLocalRandom.current().nextBytes(bytes);
        return "0x" + HexFormat.of().formatHex(bytes);
    }

}
