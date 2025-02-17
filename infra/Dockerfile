FROM emqx/nanomq:0.22.8-full as nanomq

WORKDIR /etc

COPY config/nanomq.conf etc/
COPY config/certs etc/certs

EXPOSE 1883 
EXPOSE 9083 
EXPOSE 8883 