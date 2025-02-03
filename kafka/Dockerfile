FROM ghcr.io/kafbat/kafka-ui:latest

# Remove commons-logging jar from the classpath
RUN find / -name "commons-logging*.jar" -exec rm -f {} \;

# Continue with the base image functionality
CMD ["./start-kafbat-ui.sh"]