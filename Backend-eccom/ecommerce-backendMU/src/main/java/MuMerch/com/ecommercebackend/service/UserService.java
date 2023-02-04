package MuMerch.com.ecommercebackend.service;

import MuMerch.com.ecommercebackend.api.model.LoginBody;
import MuMerch.com.ecommercebackend.api.model.RegistrationBody;
import MuMerch.com.ecommercebackend.exception.UserAlreadyExistsException;
import MuMerch.com.ecommercebackend.model.LocalUser;
import MuMerch.com.ecommercebackend.model.dao.LocalUserDAO;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {


    private LocalUserDAO localUserDAO;
    private EncryptionService encryptionService;
    private JWTService jwtService;

    public UserService(LocalUserDAO localUserDAO, EncryptionService encryptionService, JWTService jwtService) {
        this.localUserDAO = localUserDAO;
        this.encryptionService = encryptionService;
        this.jwtService = jwtService;
    }

    public LocalUser registerUser(RegistrationBody registrationBody) throws UserAlreadyExistsException {
        if(localUserDAO.findByEmailIgnoreCase(registrationBody.getEmail()).isPresent()
                || localUserDAO.findByUsernameIgnoreCase(registrationBody.getUsername()).isPresent()){
            throw new UserAlreadyExistsException();
        }

        LocalUser user= new LocalUser();
        user.setEmail(registrationBody.getEmail());
        user.setFirstName(registrationBody.getFirstName());
        user.setLastName(registrationBody.getLastName());
        user.setUsername(registrationBody.getUsername());
        //Encrypt the password!
        user.setPassword(encryptionService.encryptPassword(registrationBody.getPassword()));
        return localUserDAO.save(user);

    }
    public String loginUser(LoginBody loginBody){
        Optional<LocalUser> opUser=localUserDAO.findByUsernameIgnoreCase(loginBody.getUsername());

        if(opUser.isPresent()) {
            LocalUser user = opUser.get();
            if (encryptionService.verifyPassword(loginBody.getPassword(), user.getPassword())) {
                return jwtService.generateJWT(user);
            }
        }
        return null;
    }
}
