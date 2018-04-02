import com.spring.login.dao.LoginDao
import com.spring.login.model.LoginResponse
import com.spring.login.model.Payload
import com.spring.login.service.LoginService
import spock.lang.Shared
import spock.lang.Specification

class ServiceSpec extends Specification {

    @Shared
    LoginService loginService

    @Shared
    LoginDao loginDaoMock

    @Shared
    Payload payload

    def setup() {
        loginDaoMock = Mock(LoginDao)
        loginService = new LoginService(loginDaoMock)
        payload = new Payload("A1234567", "password")
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


        when:
        def response = loginService.login(payload)

        then:
        response.toBlocking().single().getUserId() == expectedLoginResponse.getUserId()
        1 * loginDaoMock.login(_ as Payload) >> expectedLoginResponse
    }
}
