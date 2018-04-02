import com.spring.login.exception.AuthorizationException
import com.spring.login.model.LoginResponse
import com.spring.login.model.Payload
import com.spring.login.service.LoginService
import com.spring.login.web.controller.LoginController
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import rx.Observable
import spock.lang.Shared
import spock.lang.Specification

class LoginControllerSpec extends Specification {

    @Shared
    LoginController loginController

    @Shared
    LoginService loginServiceMock

    @Shared
    MockMvc mockMvc

    def setup() {
        loginServiceMock = Mock(LoginService)
        loginController = new LoginController(loginServiceMock)
        mockMvc = MockMvcBuilders.standaloneSetup(loginController).build()
    }

    def 'test successful login'() {
        setup:
        LoginResponse expectedLoginResponse = new LoginResponse()
        expectedLoginResponse.setUserId("A1234567")
        expectedLoginResponse.setEmployeeNumber("A1234567")
        expectedLoginResponse.setDisplayName("User Name")
        expectedLoginResponse.setEmail_id("user.name@bestbuy.com")
        expectedLoginResponse.setFirstName("User")
        expectedLoginResponse.setLastName("Name")
        expectedLoginResponse.setEmployeeStatus("Active")
        expectedLoginResponse.setEnterpriseRole("Admin")
        Observable<LoginResponse> expectedObservable = Observable.just(expectedLoginResponse)

        String payload = "{\"userId\":\"A1234567\",\"password\":\"password\"}"

        when:
        def mockMvcResponse = mockMvc.perform(MockMvcRequestBuilders.post("/login").content(payload).contentType("application/json")).andReturn()
        def loginResponse = mockMvcResponse.getAsyncResult().getAt(0)
        def response = mockMvcResponse.getResponse()

        then:
        1 * loginServiceMock.login(_ as Payload) >> expectedObservable
        loginResponse?.getUserId() == expectedObservable.toBlocking().single().userId
        response?.getStatus() == 200
    }

    def 'test login failure'() {
        setup:
        final String AUTH_FAILED = "authorization.failed"
        loginServiceMock.login(new Payload("A1234567", "wrongPassword")) >> {
            throw new AuthorizationException(AUTH_FAILED)
        }
        String payload = "{\"userId\":\"A1234567\",\"password\":\"wrongPassword\"}"

        when:
        def mockMvcFailureResponse = mockMvc.perform(MockMvcRequestBuilders.post("/login").content(payload).contentType("application/json")).andReturn()
        def loginResponse = mockMvcFailureResponse.resolvedException.toString()
        def response = mockMvcFailureResponse.getResponse()

        then:
        response?.status == 500
        loginResponse.contains("authorization.failed")

    }

    def 'test bad login request'() {
        setup:
        String payload = "{\"userId\":\"\",\"password\":\"\"}"

        when:
        def mockMvcBadResponse = mockMvc.perform(MockMvcRequestBuilders.post("/login").content(payload).contentType("application/json")).andReturn()
        def badResponse = mockMvcBadResponse.resolvedException.toString()
        def response = mockMvcBadResponse.getResponse()
        then:
        response?.status == 400
        badResponse.contains("Validation failed")
    }
}
