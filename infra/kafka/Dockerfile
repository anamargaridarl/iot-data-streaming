# # BUILD KC IMAGE
# FROM confluentinc/cp-kafka-connect:7.2.1

# COPY ./config/certs/ca.crt ./certs/ca.crt
# COPY ./config/certs/server.crt ./certs/server.crt

# RUN keytool -import -trustcacerts -cacerts -storepass changeit -noprompt -alias meias -file /certs/ca.crt
# # RUN keytool -keystore /etc/ssl/certs/truststore -alias CARoot -importcert -file /certs/ca.crt -storepass anamargarida
# # RUN keytool -keystore /etc/ssl/certs/truststore -alias serverCert -importcert -file /certs/server.crt -storepass anamargarida

FROM confluentinc/cp-kafka-connect:7.2.1

USER root


# keytool -importcert -trustcacerts -alias my-mqtt-ca -file /certs/ca.crt -keystore /etc/ssl/certs/java/cacerts -storepass changeit
#ls -l /usr/lib/jvm/zulu11-ca/lib/security/cacerts

#keytool -list -keystore /usr/lib/jvm/zulu11-ca/lib/security/cacerts -storepass changeit
