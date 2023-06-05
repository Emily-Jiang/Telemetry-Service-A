FROM icr.io/appcafe/open-liberty:full-java11-openj9-ubi


COPY --chown=1001:0 src/main/liberty/config/ /config/
COPY --chown=1001:0 target/liberty/wlp/usr/servers/gids/bootstrap.properties /config/
COPY --chown=1001:0 target/*.war /config/apps/

RUN configure.sh