package br.dev.ampliar.caipora.config;

import br.dev.ampliar.caipora.CaiporaApplication;
import br.dev.ampliar.caipora.repos.DepartmentRepository;
import br.dev.ampliar.caipora.repos.DeployRepository;
import br.dev.ampliar.caipora.repos.RoleRepository;
import br.dev.ampliar.caipora.repos.SoftwareRepository;
import br.dev.ampliar.caipora.repos.StakeholderRepository;
import br.dev.ampliar.caipora.repos.UserRepository;
import br.dev.ampliar.caipora.repos.VersionRepository;
import io.restassured.RestAssured;
import io.restassured.config.SessionConfig;
import io.restassured.http.ContentType;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlMergeMode;
import org.springframework.util.StreamUtils;
import org.testcontainers.containers.MSSQLServerContainer;


/**
 * Abstract base class to be extended by every IT test. Starts the Spring Boot context with a
 * Datasource connected to the Testcontainers Docker instance. The instance is reused for all tests,
 * with all data wiped out before each test.
 */
@SpringBootTest(
        classes = CaiporaApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@ActiveProfiles("it")
@Sql({"/data/clearAll.sql", "/data/departmentData.sql", "/data/roleData.sql", "/data/userData.sql"})
@SqlMergeMode(SqlMergeMode.MergeMode.MERGE)
public abstract class BaseIT {

    @ServiceConnection
    private static final MSSQLServerContainer mSSQLServerContainer = new MSSQLServerContainer("mcr.microsoft.com/mssql/server:2022-latest");
    public static final String ADMIN = "admin";
    public static final String ITOPS = "itops";
    public static final String OBSERVER = "observer";
    public static final String PASSWORD = "Bootify!";
    private static final HashMap<String, String> webFormsSessions = new HashMap<>();

    static {
        mSSQLServerContainer.acceptLicense()
                .withReuse(true)
                .start();
    }

    @LocalServerPort
    public int serverPort;

    @Autowired
    public DeployRepository deployRepository;

    @Autowired
    public UserRepository userRepository;

    @Autowired
    public RoleRepository roleRepository;

    @Autowired
    public VersionRepository versionRepository;

    @Autowired
    public SoftwareRepository softwareRepository;

    @Autowired
    public StakeholderRepository stakeholderRepository;

    @Autowired
    public DepartmentRepository departmentRepository;

    @PostConstruct
    public void initRestAssured() {
        RestAssured.port = serverPort;
        RestAssured.urlEncodingEnabled = false;
        RestAssured.config = RestAssured.config().sessionConfig(new SessionConfig().sessionIdName("SESSION"));
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }

    public String readResource(final String resourceName) {
        try {
            return StreamUtils.copyToString(getClass().getResourceAsStream(resourceName), StandardCharsets.UTF_8);
        } catch (final IOException io) {
            throw new UncheckedIOException(io);
        }
    }

    public String webFormsSession(final String username) {
        String webFormsSession = webFormsSessions.get(username);
        if (webFormsSession == null) {
            // init session
            webFormsSession = RestAssured
                    .given()
                        .accept(ContentType.HTML)
                    .when()
                        .get("/login")
                    .sessionId();

            // perform login
            webFormsSession = RestAssured
                    .given()
                        .sessionId(webFormsSession)
                        .csrf("/login")
                        .accept(ContentType.HTML)
                        .contentType(ContentType.URLENC)
                        .formParam("name", username)
                        .formParam("password", PASSWORD)
                    .when()
                        .post("/login")
                    .sessionId();
            webFormsSessions.put(username, webFormsSession);
        }
        return webFormsSession;
    }

}
