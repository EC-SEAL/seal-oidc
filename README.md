This is the implementation of the SEAL specific 
authenticators for Keycloak. It includes all things necessary 
to build a custom docker image for keycloak and the configuration
to setup a SEAL SP microservice supporting OIDC and SAML clients
---
# Build
This generates a base Keycloak image but including the 
module that implements the SEAL specific authenticators.

-- *The makefile contains all these steps, you can just run it* --

Go to the project directory.

Build the library:
```shell script
mvn clean install
```

The resulting archive will be in this path: 
```shell script
./esmocloak-ear/target/esmocloak-0.1-SNAPSHOT.ear
```

The resources directory is in this path:
```shell script
./esmocloak-module/src/test/resources
```

On your docker building environment (if not the same where you 
build), do the following:
* Copy the Dockerfile to the root of your working directory
* Copy the archive to some path in your working directory
* Edit the source path for the archive on the Dockerfile to the 
one you chose above
* Copy the resources directory to your working directory
* Edit the source path for the resources on the Dockerfile to 
the one you chose above

Build the Docker image
```shell script
docker build -t sealoidc
```



# Deploy
To deploy the keycloak, you just need to run the included 
docker-compose file.

But before that:

Copy the resources directory in the path below to your deployment 
path
```shell script
./esmocloak-module/src/test/resources
```

Copy the SSL certificate and private key to a directory in your
 deployment path. There are some conditions that you need to 
 verify:
* Both must be in PEM format
* The certificate file must be named `tls.crt`
* The private key file must be named `tls.key`
* Make sure both files are **readable by the internal 
jboss(uid:1000) user** (*if you don't have security concerns
 and cannot give the proper permissions, just give read
  permissions to others*)
* Also make sure the path to the files is **accessible by 
the internal jboss(uid:1000) user**  (*if you don't have security 
concerns and cannot give the proper permissions, just give exec 
permissions to others in all the directories up to it*)

Edit the two volume paths in the composer, if necessary, to match 
the directories established in the steps above (*the left side one 
is the guest path, the one you have to edit*):

```yaml
    volumes:
      - ./resources:/resources
      - ./certs:/etc/x509/https
```

Edit the guest port in the composer, if desired (*the left side one, 8180*):
```yaml
    ports:
      - 8180:8443
```

Edit the database parameters, both on the database container and the keycloak container:
```yaml
# database container:
      POSTGRES_DB: keycloak
      POSTGRES_USER: keycloak
      POSTGRES_PASSWORD: passcloak

# keycloak container:
      DB_DATABASE: keycloak
      DB_USER: keycloak
      DB_PASSWORD: passcloak
```

Set the admin interface username and password
```yaml
      KEYCLOAK_USER: admin
      KEYCLOAK_PASSWORD: adminpass
```


Edit the domain and port in this entry to the ones yu have deployed the container into:
```yaml
      ESMO_SP_OIDC_AUTH_ENDPOINT: http://domain:port/auth/realms/seal/protocol/openid-connect/auth
```

The keystore has the SEAL HTTPSig RSA key (the demo one). Import there the one you are using
```shell script
resources/testKeys/keystore.jks
```
Set the friendly name (alias) of the key in the keystore to use, and the key and keystore passwords
```yaml
      KEY_PASS: selfsignedpass
      STORE_PASS: keystorepass
      HTTPSIG_CERT_ALIAS: 1
```
Set the microservice ID for this microservice
```yaml
      ESMO_SP_NAME: SPms001
      ESMO_SP_REQUEST_ISSUER: SPms001
      ESMO_SP_REQUEST_SENDER: SPms001
```

Set the microservice ID, base URL and path of the destination endpoint for the requests
```yaml
      ESMO_SP_REQUEST_RECIPIENT: RMms001
      ESMO_SP_REQUEST_RECEIVER: RMms001
      ACM_URL: https://vm.project-seal.eu:9063
      ACM_URI: /rm/request
```
The return endpoint for responses in this microservice will be here:      
```
https://domain:port/auth/realms/seal/sp/response
```


Set the base URL of the Config Manager and Session Manager microservices
```yaml
      SESSION_MANAGER_URL: http://vm.project-seal.eu:9090
      CONFIGURATION_MANAGER_URL: https://vm.project-seal.eu:9083
```

The list of supported attributes is on the file `resources/attributes.json`. For each attribute (you can add or remove any) you can specify:
* The `name`, full name of the SAML attribute
* The `friendlyName`, short readable name of the SAML attribute
* The `claim`, OIDC claim name for the equivalent SAML attribute
Here you can define which of those attributes represent a user ID
```yaml
      ID_ATTRIBUTES: schacPersonalUniqueID,schacPersonalUniqueCode,eduPersonTargetedID,eduPersonPrincipalName
```

Now, run the docker-compose file
```shell script
docker-compose up -d 
```


# Configure
Once the service is running, access the admin user interface:
```
https://domain:port/auth/admin/
```

There, login with the admin name and password you set on the composer

Hoover over the `Master` realm name and click on the `Add realm` button

Click on the `Import` button and select the `realm-export.json` file included in this repo.

**Now, the keycloak is fully configured. You just need to register a client and map the attributes to claims**

For the sake of completeness, we'll explain the configurations that the imported real contains:

* Create new realm `seal`
* Enable the SEAL authenticator flow:
  - Go to `Authentication`
  - Copy the Browser flow:
    - Select `Browser` on the drop-down
    - Click `Copy` button
  - On the copy:
    - Add execution (`New` button)
    - On the list select `ESMO SPms Authenticator`
    - Make it required
    - Move it to the top
    - Add another execution (`New` button)
    - On the list select `ESMO SPms Post Response Authenticator`
    - Make it alternative
    - Move it to the top (above the other one)
  - Set the copy flow as the browser auth flow:
    - Click `Bindings` tab
    - On the `Browser Flow` drop-down, select the copy of browser. 
* Set the specific theme:
  - Go to `Realm Settings/Themes`
  - On the `Login Theme` drop-dowm, select `spms`






# Client registration (OIDC)
For every RP/SP we want to authorise to access SEAL, we need to do the following:
* Go to SEAL realm
* Go to `Client/Create`
* Fill the fields:
  - `Client ID`: The unique ID established on the RP
  - `Client Protocol`: select `openid-connect`
  - `Root URL`: Paste here the base URL of the RP (the url from which Keycloack can find the well-known path: `.well-known/openid-configuration`)
  - Click `Save`
* On the Client `Settings`, enable `Authorization Enabled` switch
* On the `Credentials` tab, you can find the secret you need to configure on the RP.

# Attribute mapping to claims
You will need to map the user attributes to claims for each RP/SP you enable.
* Go to `Clients/<clientID>/Mappers`
* For each claim the client might ask, `Create` a new Mapper:
  - `Name`: Any meaningful name
  - `Mapper Type`: `User Attribute`
  - `User Attribute`: The friendyName of the attribute
  - `Token Claim Name`: The claim name you want the attribute to translate to
  - Click `Save`
