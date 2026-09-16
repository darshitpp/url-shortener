# Java URL Shortener (JUST)

## A self-hosted URL Shortener

Swagger API playground: https://just.darshit.dev/swagger-ui/index.html

Try it out at https://just.darshit.dev with Postman (ironically, I did not buy a new short domain)
### REST API

(Optional) 
Update Default Domain:

1. `PUT` :`/update/defaultDomain?value=<defaultDomainName>`

Sets the default domain name to be returned from the service. 
Skip this step if you want your application to be mapped to multiple domains.

Note: requires basic authentication based on the `USER_NAME` and `USER_PASS`. See [Environment Variables](#environment-variables)

------

2. `POST`: `/shorten`

Request Body:
```json
{
  "options": {
    "customPath": "string",
    "domain": "string",
    "liberalHash": true,
    "urlSize": 0,
    "ttl": 0
  },
  "strategy": "custom",
  "url": "string"
}
```

##### Mandatory:

`url` : the URL to shorten

##### Optional:

`strategy`: Can either be `custom`, `hash`, `word`, or `wordHashCombo`. If no strategy is provided, `word` is taken as default.

>`custom`: can add a custom URL identifier.
> The custom URL identifier has to be defined in `options.customPath`

>`hash`: generates a Hash value as an identifier
> The hash generated is of size 8 by default. If you require a custom size of hash, you must define it in `options.pathSize`

>`word`: generate a word based identifer, e.g. `upper-dipper-dapper`. 
> The default generated is a combination of 3 words. 
> Configurable only from `application.properties` file at the moment

>`wordHashCombo`: Combination of a word and a hash, e.g. `hillbilly-ae7f`

###### options

`domain`: setting a domain will allow you to get the shortened URL in the form of `domain/<identifier>`.
However this only works when a default domain is not set for the application. If the default domain is set,
and no `domain` is specified in the request, the service will just return the identifier. This helps when you want to map
multiple domains to your URL shortening service.

`customPath`: set this if you select the `custom` strategy.

`urlSize`: set this if you select the `hash` strategy.

`liberalHash`: adds support for additional characters like `$`, `-`, `_`, `.`, `:`, `=` which are unsafe special characters but can be used in a URL resolver to increase the number of hash values that can be generated from a number. See https://tools.ietf.org/html/rfc1738

`ttl` (in days): adds a ttl to a URL. Default value considered is 7 days

Response:
```json
{
    "shortUrl": "https://just.darshit.dev/homeotransplantation-subcommissioners-kopeks",
    "ttl": 7,
    "error": ""
}
```

------

3. `PUT`:`/delete/defaultDomain`

Removes the default domain

Note: requires basic authentication based on the `USER_NAME` and `PASSWORD`. See [Environment Variables](#environment-variables)

------

### Deployment options:

1. Run as a JAR file
2. Run as Docker Image
3. Compile from source


#### Environment Variables:
Required environment variables (no defaults; the service fails to start if any are blank):
```
REDIS_HOST=localhost // Redis Host Name
REDIS_PORT=6379      // Redis Port
REDIS_PASS=admin     // Redis Password (Redis AUTH)
USER_NAME=...        // Required. Username for Basic auth to protected features. No default.
PASSWORD=...         // Required. Password for Basic auth to protected features. No default.
CORS_ENABLED=https://app.example.com,https://web.example.com // Required. Comma-separated
                                                            // origin allowlist for POST /shorten.
                                                            // Wildcard (*) origins are rejected.
```

> `USER_NAME`, `PASSWORD`, and `CORS_ENABLED` are required at startup and must not be blank. `CORS_ENABLED` is a comma-separated list of allowed origins; wildcard (`*`) origins are rejected. There are no default credentials.

> **Destination disclosure (interstitial):** every short link, including legacy
> aliases, renders a safety page at `GET /{shortPath}` that shows the destination
> scheme, host, and full URL before redirecting. The browser is never given a
> redirect destination from the server until the user confirms via
> `POST /resolve/{shortPath}`.
>
> **Request and field limits:** `POST /shorten` bodies are capped at 32,768 bytes
> (returns `413` above that). Destination URLs are capped at 2,048 characters,
> custom paths at 64, domains at 253, and strategies at 32. Anonymous shortening
> is rate limited to 30 requests per minute per client IP (returns `429` with
> `Retry-After: 60`). Custom paths may not collide with reserved routes
> (`ui`, `shorten`, `resolve`, `update`, `delete`, `links`, and Swagger paths).

### Run as JAR

Requirements: Java 11 (Recommended using [SDKMAN!](https://sdkman.io/))

1. Download the JAR from the releases page
2. Run with Java 11 using the command

```bash
java -jar url-shortener-<release>.jar -Dspring.profiles.active=prod
```

### Run as Docker Image

Requirements: Docker

You can pull the latest docker image by using
```bash
docker pull darshit12/url-shortener:latest
```
and then run using
```bash
docker run -d --env-file ./env.list --publish=8080:8080 -it darshit12/url-shortener
```

### Compile from source

Requirements:
1. Java 11
2. Maven
3. Redis


For installing Redis, use [Homebrew](https://formulae.brew.sh/formula/redis)

For Java 11 and Maven, I recommend [SDKMAN!](https://sdkman.io/) for easy installs for the above (only works the best if you're on *NIX environments)

Once you have them set up, you can easily run the following commands depending on what you want (JAR or a Docker image)

Ensure that you have Redis running on port `6379` when building from source as these will run integration tests as well as unit tests.

#### JAR
```bash
cd url-shortener
mvn clean install
```

The JAR should be present in the `target` folder inside `url-shortener` project.

#### Docker Image

```bash
cd url-shortener
mvn compile jib:dockerBuild
```

It should build the image directly to your local Docker daemon.


### Statistics

```
127.0.0.1:6379> SET abcdefghijkl https://blogs.oracle.com/javamagazine/loop-unrolling
OK
127.0.0.1:6379> memory usage abcdefghijkl
(integer) 120
```

A free Redis instance from RedisLabs provides 30 MB of Storage, which after calculations

```
120 bytes stored for a key of length 12

30 MB = 31457280 Bytes

31457280/120 = 262144 URLs
```

means you can store up to 262144 URLs
